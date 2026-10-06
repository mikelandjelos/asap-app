package rs.ac.ni.elfak.asap.history;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;
import okio.BufferedSource;
import okio.Okio;
import rs.ac.ni.elfak.asap.network.V2ApiModels;

/**
 * Device-owned bounded history of {@code PRODUCT_VIEWED} events (DOMAIN_MODEL.md, D-038).
 * Stored only in app-private storage; newest first; at most {@value #MAX_ENTRIES} entries and
 * {@value #MAX_AGE_DAYS} days. Display fields (name, theme, map position) never leave the device; requests carry
 * only event id, product id, kind and time.
 */
public final class HistoryStore {

    public static final int MAX_ENTRIES = 50;
    public static final int MAX_AGE_DAYS = 90;
    public static final int REQUEST_WINDOW = 20;
    private static final long DAY_MS = 24L * 60 * 60 * 1000;

    /** One viewed known product with local display fields. */
    public static final class Entry {
        public String id;
        public String productId;
        public long occurredAtMs;
        public String barcodeValue;
        public String barcodeFormat;
        public String name;
        public String brand;
        public String themeLabel;
        public int themeId;
        public double x;
        public double y;
        /** "catalog", "open_food_facts", "upcitemdb", ... (display only). */
        public String source;
        public String imageUrl;
    }

    public interface Clock {
        long nowMs();
    }

    public interface IdSource {
        String next();
    }

    private final File file;
    private final Clock clock;
    private final IdSource ids;
    private final JsonAdapter<List<Entry>> adapter;
    private List<Entry> entries;

    public HistoryStore(File file, Clock clock, IdSource ids) {
        this.file = file;
        this.clock = clock;
        this.ids = ids;
        this.adapter = new Moshi.Builder().build().adapter(Types.newParameterizedType(List.class, Entry.class));
        this.entries = load();
    }

    public static HistoryStore create(File dir) {
        return new HistoryStore(new File(dir, "history.json"), System::currentTimeMillis, () -> UUID.randomUUID().toString());
    }

    private List<Entry> load() {
        if (!file.exists()) {
            return new ArrayList<>();
        }
        try (BufferedSource source = Okio.buffer(Okio.source(file))) {
            List<Entry> loaded = adapter.fromJson(source);
            return prune(loaded == null ? new ArrayList<Entry>() : new ArrayList<>(loaded));
        } catch (IOException | RuntimeException e) {
            return new ArrayList<>(); // unreadable history is discarded, never guessed
        }
    }

    private List<Entry> prune(List<Entry> list) {
        long cutoff = clock.nowMs() - MAX_AGE_DAYS * DAY_MS;
        List<Entry> kept = new ArrayList<>();
        for (Entry e : list) {
            if (e != null && e.productId != null && e.occurredAtMs >= cutoff && kept.size() < MAX_ENTRIES) {
                kept.add(e);
            }
        }
        return kept;
    }

    /** Records that a known product was displayed; returns the stored entry. */
    public synchronized Entry recordView(V2ApiModels.ProductData product) {
        Entry e = new Entry();
        e.id = ids.next();
        e.productId = product.id;
        long now = clock.nowMs();
        e.occurredAtMs = entries.isEmpty() ? now : Math.max(now, entries.get(0).occurredAtMs + 1);
        e.barcodeValue = product.barcode.value;
        e.barcodeFormat = product.barcode.format;
        e.name = product.name;
        e.brand = product.brand;
        e.themeLabel = product.theme.label;
        e.themeId = product.theme.id;
        e.x = product.mapPosition.x;
        e.y = product.mapPosition.y;
        e.source = product.provenance == null ? null
                : "EXTERNAL_PROVIDER".equals(product.provenance.type) ? product.provenance.source : "catalog";
        e.imageUrl = product.imageUrl;
        List<Entry> next = new ArrayList<>();
        next.add(e);
        next.addAll(entries);
        entries = prune(next);
        save();
        return e;
    }

    /** Newest-first entries for display. */
    public synchronized List<Entry> entries() {
        entries = prune(entries);
        return Collections.unmodifiableList(new ArrayList<>(entries));
    }

    /** Newest {@value #REQUEST_WINDOW} events in the v2 request shape (strictly descending times). */
    public synchronized List<V2ApiModels.HistoryEvent> requestContext() {
        List<V2ApiModels.HistoryEvent> out = new ArrayList<>();
        for (Entry e : entries()) {
            if (out.size() == REQUEST_WINDOW) {
                break;
            }
            out.add(new V2ApiModels.HistoryEvent(e.id, e.productId, iso(e.occurredAtMs)));
        }
        return out;
    }

    public synchronized void clear() {
        entries = new ArrayList<>();
        if (file.exists() && !file.delete()) {
            save();
        }
    }

    private void save() {
        File tmp = new File(file.getPath() + ".tmp");
        try (FileOutputStream out = new FileOutputStream(tmp)) {
            out.write(adapter.toJson(entries).getBytes(Charset.forName("UTF-8")));
        } catch (IOException e) {
            return;
        }
        if (!tmp.renameTo(file)) {
            tmp.delete();
        }
    }

    static String iso(long ms) {
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ROOT);
        f.setTimeZone(TimeZone.getTimeZone("UTC"));
        return f.format(new Date(ms));
    }
}
