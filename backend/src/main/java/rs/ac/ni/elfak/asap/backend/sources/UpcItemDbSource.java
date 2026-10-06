package rs.ac.ni.elfak.asap.backend.sources;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import rs.ac.ni.elfak.asap.backend.barcode.Barcode;
import rs.ac.ni.elfak.asap.backend.sources.SourceTypes.Found;
import rs.ac.ni.elfak.asap.backend.sources.SourceTypes.HttpFetcher;
import rs.ac.ni.elfak.asap.backend.sources.SourceTypes.Lookup;
import rs.ac.ni.elfak.asap.backend.sources.SourceTypes.NotFound;
import rs.ac.ni.elfak.asap.backend.sources.SourceTypes.ProductSource;
import rs.ac.ni.elfak.asap.backend.sources.SourceTypes.Response;
import rs.ac.ni.elfak.asap.backend.sources.SourceTypes.SourceRecord;
import rs.ac.ni.elfak.asap.backend.sources.SourceTypes.Unavailable;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/**
 * UPCitemdb free trial lookup: transient only (never cached), local daily quota and minimum spacing so the
 * provider limit is never hit; UPC-E is expanded to UPC-A because the provider rejects UPC-E (S2 evidence).
 */
public final class UpcItemDbSource implements ProductSource {

    private static final ObjectMapper JSON = JsonMapper.builder().build();

    private final HttpFetcher http;
    private final String baseUrl;
    private final int dailyQuota;
    private final Duration minSpacing;
    private final Clock clock;
    private LocalDate day;
    private int used;
    private Instant lastCall = Instant.EPOCH;

    public UpcItemDbSource(HttpFetcher http, String baseUrl, int dailyQuota, Duration minSpacing, Clock clock) {
        this.http = http;
        this.baseUrl = baseUrl;
        this.dailyQuota = dailyQuota;
        this.minSpacing = minSpacing;
        this.clock = clock;
    }

    @Override
    public String id() {
        return "upcitemdb";
    }

    @Override
    public boolean cacheable() {
        return false;
    }

    private synchronized boolean acquire() {
        Instant now = clock.instant();
        LocalDate today = LocalDate.ofInstant(now, ZoneOffset.UTC);
        if (!today.equals(day)) {
            day = today;
            used = 0;
        }
        if (used >= dailyQuota || now.isBefore(lastCall.plus(minSpacing))) {
            return false;
        }
        used++;
        lastCall = now;
        return true;
    }

    @Override
    public Lookup lookup(Barcode barcode, Duration timeout) {
        String code = barcode.format() == Barcode.Format.UPC_E ? SourceTypes.upcEToUpcA(barcode.value()) : barcode.value();
        if (!acquire()) {
            return new Unavailable("local_quota");
        }
        Response r;
        try {
            r = http.get(baseUrl + "/prod/trial/lookup?upc=" + code, timeout);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new Unavailable("interrupted");
        } catch (Exception e) {
            return new Unavailable("transport");
        }
        if (r.status() == 429) {
            return new Unavailable("rate_limited");
        }
        if (r.contentType() == null || !r.contentType().contains("json")) {
            return new Unavailable("http_" + r.status());
        }
        try {
            JsonNode body = JSON.readTree(r.body());
            String providerCode = body.path("code").asString("");
            if (r.status() == 400 && "INVALID_UPC".equals(providerCode)) {
                return new NotFound();
            }
            if (r.status() != 200 || !"OK".equals(providerCode)) {
                return new Unavailable("http_" + r.status());
            }
            JsonNode items = body.path("items");
            if (!items.isArray() || items.isEmpty()) {
                return new NotFound();
            }
            JsonNode item = items.get(0);
            String title = SourceTypes.blankToNull(item.path("title").asString(null));
            if (title == null) {
                return new NotFound();
            }
            String category = SourceTypes.blankToNull(item.path("category").asString(null));
            if (category != null && category.contains(">")) {
                category = SourceTypes.blankToNull(category.substring(category.lastIndexOf('>') + 1));
            }
            return new Found(new SourceRecord(id(), title, SourceTypes.blankToNull(item.path("brand").asString(null)),
                    category, null, null, SourceTypes.blankToNull(item.path("description").asString(null))));
        } catch (RuntimeException e) {
            return new Unavailable("malformed");
        }
    }
}
