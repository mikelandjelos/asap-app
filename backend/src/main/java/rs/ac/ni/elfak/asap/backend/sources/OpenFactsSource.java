package rs.ac.ni.elfak.asap.backend.sources;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import rs.ac.ni.elfak.asap.backend.ai.ProductText;
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

/** Open Food Facts family, API v3 with {@code product_type=all}; results are ODbL-cacheable (D-029). */
public final class OpenFactsSource implements ProductSource {

    static final String FIELDS = "code,product_name,product_name_en,generic_name,generic_name_en,brands,categories_tags,labels_tags,image_front_small_url";
    private static final ObjectMapper JSON = JsonMapper.builder().build();

    private final HttpFetcher http;
    private final String baseUrl;

    public OpenFactsSource(HttpFetcher http, String baseUrl) {
        this.http = http;
        this.baseUrl = baseUrl;
    }

    @Override
    public String id() {
        return "open_food_facts";
    }

    @Override
    public boolean cacheable() {
        return true;
    }

    @Override
    public Lookup lookup(Barcode barcode, Duration timeout) {
        Response r;
        try {
            r = http.get(baseUrl + "/api/v3/product/" + barcode.value() + "?product_type=all&fields=" + FIELDS, timeout);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new Unavailable("interrupted");
        } catch (Exception e) {
            return new Unavailable("transport");
        }
        if (r.status() == 404) {
            return new NotFound();
        }
        if (r.status() == 429) {
            return new Unavailable("rate_limited");
        }
        if (r.status() != 200 || r.contentType() == null || !r.contentType().contains("json")) {
            return new Unavailable("http_" + r.status());
        }
        try {
            JsonNode product = JSON.readTree(r.body()).path("product");
            if (product.isMissingNode() || product.isNull()) {
                return new NotFound();
            }
            String name = SourceTypes.blankToNull(first(text(product, "product_name"), text(product, "product_name_en")));
            if (name == null) {
                return new NotFound();
            }
            List<String> tags = new ArrayList<>();
            for (JsonNode t : product.path("categories_tags")) {
                if (t.asString().startsWith("en:")) {
                    tags.add(t.asString());
                }
            }
            List<String> labels = new ArrayList<>();
            for (JsonNode t : product.path("labels_tags")) {
                if (t.asString().startsWith("en:")) {
                    labels.add(ProductText.tagLabel(t.asString()));
                }
            }
            String brands = text(product, "brands");
            String category = tags.isEmpty() ? null : capitalize(ProductText.tagLabel(tags.get(tags.size() - 1)));
            List<String> deepest = tags.subList(Math.max(0, tags.size() - 3), tags.size());
            return new Found(new SourceRecord(id(), name,
                    SourceTypes.blankToNull(brands == null ? null : brands.split(",")[0]), category, deepest,
                    SourceTypes.blankToNull(String.join(", ", labels)),
                    SourceTypes.blankToNull(first(text(product, "generic_name"), text(product, "generic_name_en"))),
                    SourceTypes.allowedImage(text(product, "image_front_small_url"))));
        } catch (RuntimeException e) {
            return new Unavailable("malformed");
        }
    }

    private static String text(JsonNode node, String field) {
        JsonNode v = node.path(field);
        return v.isString() ? v.asString() : null;
    }

    private static String first(String a, String b) {
        return a != null && !a.isBlank() ? a : b;
    }

    private static String capitalize(String s) {
        return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
