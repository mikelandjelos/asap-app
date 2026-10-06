package rs.ac.ni.elfak.asap.backend.api.v2;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import rs.ac.ni.elfak.asap.backend.ai.BundleModel.CatalogEntry;
import rs.ac.ni.elfak.asap.backend.ai.RecommendationEngine;
import rs.ac.ni.elfak.asap.backend.sources.SourceTypes.HttpFetcher;
import rs.ac.ni.elfak.asap.backend.sources.SourceTypes.Response;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@EnabledIf("rs.ac.ni.elfak.asap.backend.ai.BundleParityTest#bundleAvailable")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT,
        properties = {"asap.bundle.dir=${asap.bundle.root:../data/processed/bundle}",
                "asap.sources.upcitemdb.min-spacing=0s"})
class V2ControllerTest {

    private static final String QUERY = "/api/v2/scan-queries";

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RecommendationEngine engine;

    private final HttpClient http = HttpClient.newHttpClient();

    /** External barcodes used with the scripted fetcher; checked to be outside the catalog. */
    static final String OFF_ONLY = "3859999999997";
    static final String DOWN = "3859999999980";

    /** Replaces the real HTTP fetcher: these tests never call external providers. */
    @TestConfiguration
    static class ScriptedSources {
        @Bean
        @Primary
        HttpFetcher scriptedFetcher() {
            return (url, timeout) -> {
                if (url.contains(DOWN)) {
                    throw new java.io.IOException("connection refused");
                }
                if (url.contains("/api/v3/product/" + OFF_ONLY)) {
                    return new Response(200, "application/json", """
                            {"product":{"product_name":"Test mlinci","brands":"Podravka",
                             "categories_tags":["en:cereals-and-potatoes","en:pastas"],"generic_name":"Baked pasta sheets"}}""");
                }
                return url.contains("upcitemdb") || url.contains("/prod/trial/")
                        ? new Response(200, "application/json", "{\"code\":\"OK\",\"items\":[]}")
                        : new Response(404, "application/json", "{\"status\":\"failure\"}");
            };
        }
    }

    @Test
    void productOutsideTheCatalogResolvesThroughTheRouterAndGetsRecommendations() throws Exception {
        assertThat(engine.findByBarcode(OFF_ONLY, "EAN_13")).isEmpty();
        JsonNode body = objectMapper.readTree(post(QUERY, """
                {"barcode":{"value":"%s","format":"EAN_13"}}""".formatted(OFF_ONLY)).body());
        assertThat(body.at("/product/status").asString()).isEqualTo("KNOWN");
        assertThat(body.at("/product/data/id").asString()).isEqualTo("gtin:" + OFF_ONLY);
        assertThat(body.at("/product/data/name").asString()).isEqualTo("Test mlinci");
        assertThat(body.at("/product/data/provenance/type").asString()).isEqualTo("EXTERNAL_PROVIDER");
        assertThat(body.at("/product/data/fieldSources/name").asString()).isEqualTo("open_food_facts");
        assertThat(body.at("/product/data/theme/label").asString()).isNotBlank();
        assertThat(body.at("/recommendations/status").asString()).isEqualTo("RESULTS");
        assertThat(body.at("/recommendations/items")).hasSize(10);
    }

    @Test
    void unreachableProvidersMakeTheProductUnavailableNotUnknown() throws Exception {
        assertThat(engine.findByBarcode(DOWN, "EAN_13")).isEmpty();
        JsonNode body = objectMapper.readTree(post(QUERY, """
                {"barcode":{"value":"%s","format":"EAN_13"}}""".formatted(DOWN)).body());
        assertThat(body.at("/product/status").asString()).isEqualTo("UNAVAILABLE");
        assertThat(body.at("/recommendations/status").asString()).isEqualTo("NOT_APPLICABLE");
    }

    private CatalogEntry product(int index) {
        return engine.bundle().catalog().get(index);
    }

    private String request(CatalogEntry p, String history) {
        return """
                {"barcode":{"value":"%s","format":"%s"}%s}""".formatted(
                p.barcode().value(), p.barcode().format(), history == null ? "" : ",\"history\":" + history);
    }

    private String history(int... indices) {
        List<String> events = new ArrayList<>();
        Instant t = Instant.parse("2026-10-06T12:00:00Z");
        for (int i = 0; i < indices.length; i++) {
            events.add("""
                    {"id":"e%02d","productId":"%s","kind":"PRODUCT_VIEWED","occurredAt":"%s"}""".formatted(
                    i, product(indices[i]).id(), t.minusSeconds(60L * i)));
        }
        return "[" + String.join(",", events) + "]";
    }

    @Test
    void knownProductWithoutHistoryGetsGenericSemanticResults() throws Exception {
        CatalogEntry p = product(123);
        HttpResponse<String> response = post(QUERY, request(p, null));
        JsonNode body = objectMapper.readTree(response.body());

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(body.at("/product/status").asString()).isEqualTo("KNOWN");
        assertThat(body.at("/product/data/id").asString()).isEqualTo(p.id());
        assertThat(body.at("/product/data/provenance/type").asString()).isEqualTo("FALLBACK_DATASET");
        assertThat(body.at("/product/data/theme/label").asString()).isNotBlank();
        assertThat(body.at("/product/data/mapPosition/x").isNumber()).isTrue();
        assertThat(body.at("/recommendations/status").asString()).isEqualTo("RESULTS");
        assertThat(body.at("/recommendations/mode").asString()).isEqualTo("GENERIC_SEMANTIC");
        assertThat(body.at("/recommendations/historyState").asString()).isEqualTo("COLD_START");
        assertThat(body.at("/recommendations/diversification/lambda").asDouble())
                .isEqualTo(engine.bundle().manifest().params().mmrLambda());
        JsonNode items = body.at("/recommendations/items");
        assertThat(items).hasSize(10);
        Set<String> ids = new HashSet<>();
        for (int i = 0; i < items.size(); i++) {
            assertThat(items.get(i).at("/rank").asInt()).isEqualTo(i + 1);
            assertThat(items.get(i).at("/evidence/scoreType").asString()).isEqualTo("HYBRID_RELEVANCE");
            assertThat(items.get(i).at("/evidence/modelVersion").asString()).startsWith("intfloat/multilingual-e5-small@");
            ids.add(items.get(i).at("/product/id").asString());
        }
        assertThat(ids).hasSize(10).doesNotContain(p.id());
        assertThat(body.at("/you").isMissingNode()).isTrue();
        assertThat(body.at("/attribution").asString()).contains("Open Food Facts");
    }

    @Test
    void sufficientHistoryPersonalizesAndPlacesYouOnTheMap() throws Exception {
        CatalogEntry p = product(123);
        JsonNode body = objectMapper.readTree(post(QUERY, request(p, history(10, 20, 30, 40, 50))).body());

        assertThat(body.at("/recommendations/mode").asString()).isEqualTo("PERSONALIZED_HISTORY");
        assertThat(body.at("/recommendations/historyState").asString()).isEqualTo("APPLIED");
        assertThat(body.at("/recommendations/items/0/evidence/scoreType").asString()).isEqualTo("PERSONALIZED_HYBRID_RELEVANCE");
        assertThat(body.at("/you/historyUsed").asInt()).isEqualTo(5);
        assertThat(body.at("/you/mapPosition/y").isNumber()).isTrue();
        for (int i : new int[] {10, 20, 30, 40, 50}) {
            assertThat(body.at("/recommendations/items").toString()).doesNotContain("\"" + product(i).id() + "\"");
        }
    }

    @Test
    void insufficientHistoryStaysColdStart() throws Exception {
        JsonNode body = objectMapper.readTree(post(QUERY, request(product(123), history(10, 10, 20))).body());
        assertThat(body.at("/recommendations/mode").asString()).isEqualTo("GENERIC_SEMANTIC");
        assertThat(body.at("/recommendations/historyState").asString()).isEqualTo("COLD_START");
        assertThat(body.at("/you/historyUsed").asInt()).isEqualTo(3);
    }

    @Test
    void productOutsideTheCatalogIsUnknownAndNotApplicable() throws Exception {
        assertThat(engine.findByBarcode("9999999999994", "EAN_13")).isEmpty();
        JsonNode body = objectMapper.readTree(post(QUERY, """
                {"barcode":{"value":"9999999999994","format":"EAN_13"}}""").body());
        assertThat(body.at("/product/status").asString()).isEqualTo("UNKNOWN");
        assertThat(body.at("/recommendations/status").asString()).isEqualTo("NOT_APPLICABLE");
        assertThat(body.at("/recommendations/historyState").asString()).isEqualTo("NOT_USED");
        assertThat(body.at("/recommendations/items").isMissingNode()).isTrue();
    }

    @Test
    void malformedHistoryIsRejectedNotReinterpreted() throws Exception {
        CatalogEntry p = product(1);
        String outOfOrder = """
                [{"id":"a","productId":"x","kind":"PRODUCT_VIEWED","occurredAt":"2026-10-06T10:00:00Z"},
                 {"id":"b","productId":"y","kind":"PRODUCT_VIEWED","occurredAt":"2026-10-06T11:00:00Z"}]""";
        assertProblem(post(QUERY, request(p, outOfOrder)), "history", "OUT_OF_ORDER");
        String duplicate = """
                [{"id":"a","productId":"x","kind":"PRODUCT_VIEWED","occurredAt":"2026-10-06T11:00:00Z"},
                 {"id":"a","productId":"y","kind":"PRODUCT_VIEWED","occurredAt":"2026-10-06T10:00:00Z"}]""";
        assertProblem(post(QUERY, request(p, duplicate)), "history[1].id", "DUPLICATE_EVENT_ID");
        String badKind = """
                [{"id":"a","productId":"x","kind":"PURCHASED","occurredAt":"2026-10-06T11:00:00Z"}]""";
        assertProblem(post(QUERY, request(p, badKind)), "history[0].kind", "UNSUPPORTED_KIND");
        String badTime = """
                [{"id":"a","productId":"x","kind":"PRODUCT_VIEWED","occurredAt":"yesterday"}]""";
        assertProblem(post(QUERY, request(p, badTime)), "history[0].occurredAt", "INVALID_TIMESTAMP");
        int[] many = new int[51];
        for (int i = 0; i < many.length; i++) {
            many[i] = i;
        }
        assertProblem(post(QUERY, request(p, history(many))), "history", "HISTORY_TOO_LONG");
        String unknownField = """
                [{"id":"a","productId":"x","kind":"PRODUCT_VIEWED","occurredAt":"2026-10-06T11:00:00Z","extra":1}]""";
        assertProblem(post(QUERY, request(p, unknownField)), "$", "UNKNOWN_FIELD");
    }

    @Test
    void invalidBarcodeUsesTheV1Rules() throws Exception {
        assertProblem(post(QUERY, """
                {"barcode":{"value":"1234567890123","format":"EAN_13"}}"""), "barcode.value", "INVALID_CHECK_DIGIT");
    }

    @Test
    void catalogMapListsThemeCentroidsAndSamplePoints() throws Exception {
        HttpResponse<String> response = http.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v2/catalog-map")).GET().build(),
                HttpResponse.BodyHandlers.ofString());
        JsonNode body = objectMapper.readTree(response.body());
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(body.at("/themes")).hasSize(60);
        assertThat(body.at("/points").size()).isGreaterThan(1000);
        assertThat(body.at("/themes/0/label").asString()).isNotBlank();
    }

    @Test
    void v1StillServesTheControlledFixture() throws Exception {
        JsonNode body = objectMapper.readTree(post("/api/v1/scan-queries", """
                {"barcode":{"value":"2000000000015","format":"EAN_13"}}""").body());
        assertThat(body.at("/recommendations/mode").asString()).isEqualTo("DETERMINISTIC_FIXTURE");
    }

    private void assertProblem(HttpResponse<String> response, String field, String code) throws Exception {
        JsonNode body = objectMapper.readTree(response.body());
        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.headers().firstValue("Content-Type").orElse("")).startsWith("application/problem+json");
        assertThat(body.at("/errors").toString()).contains("\"field\":\"" + field + "\"").contains("\"code\":\"" + code + "\"");
    }

    private HttpResponse<String> post(String path, String json) throws Exception {
        return http.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json)).build(), HttpResponse.BodyHandlers.ofString());
    }
}
