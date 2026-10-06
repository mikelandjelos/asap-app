package rs.ac.ni.elfak.asap.backend.sources;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rs.ac.ni.elfak.asap.backend.barcode.Barcode;
import rs.ac.ni.elfak.asap.backend.sources.SourceTypes.Found;
import rs.ac.ni.elfak.asap.backend.sources.SourceTypes.Lookup;
import rs.ac.ni.elfak.asap.backend.sources.SourceTypes.NotFound;
import rs.ac.ni.elfak.asap.backend.sources.SourceTypes.ProductSource;
import rs.ac.ni.elfak.asap.backend.sources.SourceTypes.SourceRecord;
import rs.ac.ni.elfak.asap.backend.sources.SourceTypes.Unavailable;

/**
 * Resolves a barcode outside the local catalog by querying sources in priority order within a total budget,
 * stopping once name, brand and category are known, and merging fields with per-field provenance (D-029).
 */
public final class ProductRouter {

    public enum Status { KNOWN, UNKNOWN, UNAVAILABLE }

    /** Merged product; {@code fieldSources} names the source of every present field. */
    public record Resolution(Status status, SourceRecord merged, Map<String, String> fieldSources, List<String> sources) {
    }

    private record CacheEntry(Lookup lookup, Instant expires) {
    }

    private static final Logger LOG = LoggerFactory.getLogger(ProductRouter.class);
    private static final int BREAKER_THRESHOLD = 3;
    private static final Duration BREAKER_OPEN = Duration.ofSeconds(60);
    private static final Duration FOUND_TTL = Duration.ofHours(24);
    private static final Duration NOT_FOUND_TTL = Duration.ofHours(1);
    private static final int CACHE_LIMIT = 2_000;

    private final List<ProductSource> sources;
    private final Duration totalBudget;
    private final Duration perSource;
    private final Clock clock;
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private final Map<String, Integer> failures = new ConcurrentHashMap<>();
    private final Map<String, Instant> openUntil = new ConcurrentHashMap<>();

    public ProductRouter(List<ProductSource> sources, Duration totalBudget, Duration perSource, Clock clock) {
        this.sources = List.copyOf(sources);
        this.totalBudget = totalBudget;
        this.perSource = perSource;
        this.clock = clock;
    }

    public Resolution resolve(Barcode barcode) {
        if (SourceTypes.restricted(barcode)) {
            return new Resolution(Status.UNKNOWN, null, Map.of(), List.of());
        }
        Instant deadline = clock.instant().plus(totalBudget);
        List<SourceRecord> found = new ArrayList<>();
        boolean anyUnavailable = false;
        for (ProductSource source : sources) {
            Duration left = Duration.between(clock.instant(), deadline);
            if (left.isNegative() || left.isZero()) {
                anyUnavailable = true;
                break;
            }
            Lookup lookup = lookup(source, barcode, left.compareTo(perSource) < 0 ? left : perSource);
            if (lookup instanceof Found f) {
                found.add(f.record());
                if (complete(merge(found).merged())) {
                    break;
                }
            } else if (lookup instanceof Unavailable) {
                anyUnavailable = true;
            }
        }
        if (!found.isEmpty()) {
            return merge(found);
        }
        return new Resolution(anyUnavailable ? Status.UNAVAILABLE : Status.UNKNOWN, null, Map.of(), List.of());
    }

    private Lookup lookup(ProductSource source, Barcode barcode, Duration timeout) {
        String key = source.id() + ":" + barcode.format() + ":" + barcode.value();
        Instant now = clock.instant();
        if (source.cacheable()) {
            CacheEntry hit = cache.get(key);
            if (hit != null && hit.expires().isAfter(now)) {
                return hit.lookup();
            }
        }
        Instant open = openUntil.get(source.id());
        if (open != null && open.isAfter(now)) {
            LOG.info("source={} barcode={} outcome=Unavailable reason=circuit_open", source.id(), barcode.value());
            return new Unavailable("circuit_open");
        }
        long started = System.nanoTime();
        Lookup lookup = source.lookup(barcode, timeout);
        LOG.info("source={} barcode={} outcome={}{} ms={}", source.id(), barcode.value(), lookup.getClass().getSimpleName(),
                lookup instanceof Unavailable u ? " reason=" + u.reason() : "", (System.nanoTime() - started) / 1_000_000);
        if (lookup instanceof Unavailable u && !"local_quota".equals(u.reason())) {
            if (failures.merge(source.id(), 1, Integer::sum) >= BREAKER_THRESHOLD) {
                openUntil.put(source.id(), now.plus(BREAKER_OPEN));
                failures.put(source.id(), 0);
            }
        } else if (!(lookup instanceof Unavailable)) {
            failures.put(source.id(), 0);
            if (source.cacheable()) {
                if (cache.size() >= CACHE_LIMIT) {
                    cache.clear();
                }
                cache.put(key, new CacheEntry(lookup, now.plus(lookup instanceof NotFound ? NOT_FOUND_TTL : FOUND_TTL)));
            }
        }
        return lookup;
    }

    private static boolean complete(SourceRecord r) {
        return r.name() != null && r.brand() != null && r.category() != null;
    }

    /** First non-blank value in source priority order; the longest description wins. */
    static Resolution merge(List<SourceRecord> records) {
        Map<String, String> fieldSources = new LinkedHashMap<>();
        String name = pick(records, "name", SourceRecord::name, fieldSources);
        String brand = pick(records, "brand", SourceRecord::brand, fieldSources);
        String category = pick(records, "category", SourceRecord::category, fieldSources);
        String labels = pick(records, "labels", SourceRecord::labels, fieldSources);
        String imageUrl = pick(records, "imageUrl", SourceRecord::imageUrl, fieldSources);
        List<String> tags = List.of();
        for (SourceRecord r : records) {
            if (!r.categoryTags().isEmpty()) {
                tags = r.categoryTags();
                fieldSources.put("categoryTags", r.source());
                break;
            }
        }
        String description = null;
        for (SourceRecord r : records) {
            if (r.description() != null && (description == null || r.description().length() > description.length())) {
                description = r.description();
                fieldSources.put("description", r.source());
            }
        }
        List<String> used = records.stream().map(SourceRecord::source).filter(fieldSources::containsValue).distinct().toList();
        return new Resolution(Status.KNOWN, new SourceRecord(String.join("+", used), name, brand, category, tags, labels, description, imageUrl),
                Map.copyOf(fieldSources), used);
    }

    private static String pick(List<SourceRecord> records, String field,
            java.util.function.Function<SourceRecord, String> getter, Map<String, String> fieldSources) {
        for (SourceRecord r : records) {
            String v = getter.apply(r);
            if (v != null) {
                fieldSources.put(field, r.source());
                return v;
            }
        }
        return null;
    }
}
