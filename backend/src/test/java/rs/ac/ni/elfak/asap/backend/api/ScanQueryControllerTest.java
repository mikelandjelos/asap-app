package rs.ac.ni.elfak.asap.backend.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class ScanQueryControllerTest {

    private static final String PATH = "/api/v1/scan-queries";

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Test
    void returnsKnownProductAndOrderedPlaceholderResults() throws Exception {
        HttpResponse<String> response = postJson(request("2000000000015"));
        JsonNode body = json(response);

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(contentType(response)).startsWith("application/json");
        assertThat(body.at("/product/status").asString()).isEqualTo("KNOWN");
        assertThat(body.at("/product/data/id").asString()).isEqualTo("fixture:oat-drink");
        assertThat(body.at("/product/data/provenance/type").asString())
                .isEqualTo("CONTROLLED_FIXTURE");
        assertThat(body.at("/product/data/provenance/source").asString())
                .isEqualTo("asap-i1-fixtures-v1");
        assertThat(body.at("/recommendations/status").asString()).isEqualTo("RESULTS");
        assertThat(body.at("/recommendations/mode").asString())
                .isEqualTo("DETERMINISTIC_FIXTURE");
        assertThat(body.at("/recommendations/placeholder").asBoolean()).isTrue();
        assertThat(body.at("/recommendations/items")).hasSize(2);
        assertThat(body.at("/recommendations/items/0/rank").asInt()).isEqualTo(1);
        assertThat(body.at("/recommendations/items/0/product/id").asString())
                .isEqualTo("fixture:almond-drink");
        assertThat(body.at("/recommendations/items/1/rank").asInt()).isEqualTo(2);
        assertThat(body.at("/recommendations/items/1/product/id").asString())
                .isEqualTo("fixture:soy-drink");
        assertThat(body.at("/recommendations/items/0/score").isMissingNode()).isTrue();
    }

    @Test
    void returnsUnknownWithRecommendationsNotApplicable() throws Exception {
        assertProductWithoutRecommendations("2000000000992", "UNKNOWN");
    }

    @Test
    void returnsControlledProductUnavailableOutcome() throws Exception {
        assertProductWithoutRecommendations("2000000000985", "UNAVAILABLE");
    }

    @Test
    void preservesKnownProductWhenRecommendationsAreEmpty() throws Exception {
        assertPartialSuccess("2000000000046", "EMPTY");
    }

    @Test
    void preservesKnownProductWhenRecommendationsAreUnavailable() throws Exception {
        assertPartialSuccess("2000000000053", "UNAVAILABLE");
    }

    @Test
    void returnsIdenticalResponseForRepeatedQuery() throws Exception {
        HttpResponse<String> first = postJson(request("2000000000015"));
        HttpResponse<String> second = postJson(request("2000000000015"));

        assertThat(first.statusCode()).isEqualTo(200);
        assertThat(second.statusCode()).isEqualTo(200);
        assertThat(second.body()).isEqualTo(first.body());
    }

    @Test
    void rejectsInvalidCheckDigit() throws Exception {
        HttpResponse<String> response = postJson(request("2000000000014"));
        JsonNode body = json(response);

        assertProblem(response, 400);
        assertThat(body.at("/errors/0/field").asString()).isEqualTo("barcode.value");
        assertThat(body.at("/errors/0/code").asString()).isEqualTo("INVALID_CHECK_DIGIT");
    }

    @Test
    void rejectsMissingBarcode() throws Exception {
        HttpResponse<String> response = postJson("{}");
        JsonNode body = json(response);

        assertProblem(response, 400);
        assertThat(body.at("/errors/0/field").asString()).isEqualTo("barcode");
        assertThat(body.at("/errors/0/code").asString()).isEqualTo("REQUIRED");
    }

    @Test
    void rejectsUnknownField() throws Exception {
        HttpResponse<String> response = postJson("""
                {"barcode":{"value":"2000000000015",\
                "format":"EAN_13"},"extra":true}""");

        assertProblem(response, 400);
        assertThat(json(response).at("/errors/0/code").asString()).isEqualTo("UNKNOWN_FIELD");
    }

    @Test
    void rejectsMalformedJson() throws Exception {
        HttpResponse<String> response = postJson("{not-json}");

        assertProblem(response, 400);
        assertThat(json(response).at("/errors/0/code").asString()).isEqualTo("MALFORMED_JSON");
    }

    @Test
    void rejectsNonJsonMediaType() throws Exception {
        HttpResponse<String> response = post(request("2000000000015"), "text/plain");

        assertProblem(response, 415);
    }

    @Test
    void rejectsBodyAboveTwoKibibytes() throws Exception {
        String body = "{\"padding\":\"" + "a".repeat(RequestSizeFilter.MAX_BODY_BYTES) + "\"}";
        HttpResponse<String> response = postJson(body);

        assertProblem(response, 413);
        assertThat(json(response).at("/errors/0/code").asString()).isEqualTo("BODY_TOO_LARGE");
    }

    private void assertProductWithoutRecommendations(String value, String productStatus)
            throws Exception {
        HttpResponse<String> response = postJson(request(value));
        JsonNode body = json(response);

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(body.at("/product/status").asString()).isEqualTo(productStatus);
        assertThat(body.at("/product/data").isMissingNode()).isTrue();
        assertThat(body.at("/recommendations/status").asString()).isEqualTo("NOT_APPLICABLE");
        assertThat(body.at("/recommendations/mode").isMissingNode()).isTrue();
        assertThat(body.at("/recommendations/placeholder").asBoolean()).isFalse();
        assertThat(body.at("/recommendations/items")).isEmpty();
    }

    private void assertPartialSuccess(String value, String recommendationStatus)
            throws Exception {
        HttpResponse<String> response = postJson(request(value));
        JsonNode body = json(response);

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(body.at("/product/status").asString()).isEqualTo("KNOWN");
        assertThat(body.at("/product/data").isMissingNode()).isFalse();
        assertThat(body.at("/recommendations/status").asString())
                .isEqualTo(recommendationStatus);
        assertThat(body.at("/recommendations/mode").asString())
                .isEqualTo("DETERMINISTIC_FIXTURE");
        assertThat(body.at("/recommendations/placeholder").asBoolean()).isTrue();
        assertThat(body.at("/recommendations/items")).isEmpty();
    }

    private void assertProblem(HttpResponse<String> response, int expectedStatus) throws Exception {
        assertThat(response.statusCode()).isEqualTo(expectedStatus);
        assertThat(contentType(response)).startsWith("application/problem+json");
        assertThat(json(response).at("/status").asInt()).isEqualTo(expectedStatus);
    }

    private JsonNode json(HttpResponse<String> response) throws Exception {
        return objectMapper.readTree(response.body());
    }

    private HttpResponse<String> postJson(String body) throws Exception {
        return post(body, "application/json");
    }

    private HttpResponse<String> post(String body, String contentType) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:" + port + PATH))
                .header("Content-Type", contentType)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static String contentType(HttpResponse<String> response) {
        return response.headers().firstValue("Content-Type").orElse("");
    }

    private static String request(String value) {
        return """
                {"barcode":{"value":"%s","format":"EAN_13"}}""".formatted(value);
    }
}
