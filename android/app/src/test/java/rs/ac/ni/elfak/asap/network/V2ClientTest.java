package rs.ac.ni.elfak.asap.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.squareup.moshi.Moshi;
import java.util.Collections;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.Timeout;
import org.junit.Test;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

public class V2ClientTest {

    private static final MediaType JSON = MediaType.get("application/json");

    static final String KNOWN = "{\"product\":{\"status\":\"KNOWN\",\"data\":{\"id\":\"off:1\","
            + "\"barcode\":{\"value\":\"3017620422003\",\"format\":\"EAN_13\"},\"name\":\"Nutella\",\"brand\":\"Ferrero\","
            + "\"category\":\"Spreads\",\"tags\":[],\"provenance\":{\"type\":\"FALLBACK_DATASET\",\"source\":\"open_food_facts\"},"
            + "\"theme\":{\"id\":21,\"label\":\"Chocolate & hazelnut spreads\"},\"mapPosition\":{\"x\":0.1,\"y\":-0.2}}},"
            + "\"recommendations\":{\"status\":\"RESULTS\",\"mode\":\"PERSONALIZED_HISTORY\",\"historyState\":\"APPLIED\","
            + "\"pipelineVersion\":\"20261006-07904a7f\",\"diversification\":{\"method\":\"MMR\",\"lambda\":0.7},\"items\":["
            + "{\"rank\":1,\"product\":{\"id\":\"off:2\",\"name\":\"Cream\"},\"theme\":{\"id\":21,\"label\":\"Spreads\"},"
            + "\"evidence\":{\"score\":0.91,\"scoreType\":\"PERSONALIZED_HYBRID_RELEVANCE\",\"modelVersion\":\"e5@614241f\"}}]},"
            + "\"you\":{\"historyUsed\":5,\"mapPosition\":{\"x\":0.0,\"y\":0.1}},"
            + "\"attribution\":\"Contains data from Open Food Facts\"}";

    private static V2ApiModels.ScanQueryResponse parse(String json) throws Exception {
        return new Moshi.Builder().build().adapter(V2ApiModels.ScanQueryResponse.class).fromJson(json);
    }

    @Test
    public void serializesRequestWithHistoryAndPath() throws Exception {
        V2ApiService service = new Retrofit.Builder().baseUrl("https://example.invalid/")
                .addConverterFactory(MoshiConverterFactory.create(new Moshi.Builder().build())).build()
                .create(V2ApiService.class);
        Request request = service.query(new V2ApiModels.ScanQueryRequest("3017620422003", "EAN_13",
                Collections.singletonList(new V2ApiModels.HistoryEvent("e1", "off:9", "2026-10-06T12:00:00.000Z")))).request();
        Buffer body = new Buffer();
        request.body().writeTo(body);
        assertEquals("/api/v2/scan-queries", request.url().encodedPath());
        String json = body.readUtf8();
        assertTrue(json.contains("\"history\":[{\"id\":\"e1\",\"kind\":\"PRODUCT_VIEWED\""));
        assertEquals("/api/v2/catalog-map", service.catalogMap().request().url().encodedPath());
    }

    @Test
    public void acceptsValidKnownResponseAndRejectsMisleadingOnes() throws Exception {
        assertTrue(V2ResponseValidator.isUsable(parse(KNOWN)));
        assertFalse(V2ResponseValidator.isUsable(parse(KNOWN.replace("\"APPLIED\"", "\"COLD_START\"")))); // mode/state mismatch
        assertFalse(V2ResponseValidator.isUsable(parse(KNOWN.replace("\"rank\":1", "\"rank\":2"))));
        assertFalse(V2ResponseValidator.isUsable(parse(KNOWN.replace("\"id\":\"off:2\"", "\"id\":\"off:1\"")))); // self
        assertFalse(V2ResponseValidator.isUsable(parse(KNOWN.replace(",\"attribution\":\"Contains data from Open Food Facts\"", ""))));
        assertFalse(V2ResponseValidator.isUsable(parse(KNOWN.replace("\"historyUsed\":5", "\"historyUsed\":0"))));
    }

    @Test
    public void acceptsUnknownAndUnavailableOnlyWithoutRecommendations() throws Exception {
        String unknown = "{\"product\":{\"status\":\"UNAVAILABLE\"},\"recommendations\":{\"status\":\"NOT_APPLICABLE\","
                + "\"historyState\":\"NOT_USED\",\"pipelineVersion\":\"v\"},\"attribution\":\"a\"}";
        assertTrue(V2ResponseValidator.isUsable(parse(unknown)));
        assertFalse(V2ResponseValidator.isUsable(parse(unknown.replace("NOT_APPLICABLE", "RESULTS"))));
    }

    @Test
    public void clientDeliversValidatedSuccessAndClassifiesFailures() {
        Capture ok = run(Response.success(ResponseBody.create(KNOWN, JSON)));
        assertNotNull(ok.value);
        assertEquals("off:2", ok.value.recommendations.items.get(0).product.id);
        assertEquals(ScanQueryClient.Failure.Kind.INVALID_RESPONSE,
                run(Response.success(ResponseBody.create("{\"product\":{}}", JSON))).failure.kind());
        assertEquals(ScanQueryClient.Failure.Kind.HTTP,
                run(Response.error(400, ResponseBody.create("{}", JSON))).failure.kind());
    }

    private static final class Capture {
        V2ApiModels.ScanQueryResponse value;
        ScanQueryClient.Failure failure;
    }

    private static Capture run(Response<ResponseBody> response) {
        V2Client client = new V2Client(new V2ApiService() {
            @Override
            public Call<ResponseBody> query(V2ApiModels.ScanQueryRequest request) {
                return new StubCall(response);
            }

            @Override
            public Call<ResponseBody> catalogMap() {
                return new StubCall(response);
            }
        });
        Capture capture = new Capture();
        client.query("3017620422003", "EAN_13", null, new V2Client.Callback<V2ApiModels.ScanQueryResponse>() {
            @Override
            public void onSuccess(V2ApiModels.ScanQueryResponse value) {
                capture.value = value;
            }

            @Override
            public void onFailure(ScanQueryClient.Failure failure) {
                capture.failure = failure;
            }
        });
        return capture;
    }

    private static final class StubCall implements Call<ResponseBody> {
        private final Response<ResponseBody> response;

        StubCall(Response<ResponseBody> response) {
            this.response = response;
        }

        @Override
        public Response<ResponseBody> execute() {
            return response;
        }

        @Override
        public void enqueue(Callback<ResponseBody> callback) {
            callback.onResponse(this, response);
        }

        @Override
        public boolean isExecuted() {
            return true;
        }

        @Override
        public void cancel() {
        }

        @Override
        public boolean isCanceled() {
            return false;
        }

        @Override
        public Call<ResponseBody> clone() {
            return this;
        }

        @Override
        public Request request() {
            return new Request.Builder().url("https://example.invalid/").build();
        }

        @Override
        public Timeout timeout() {
            return Timeout.NONE;
        }
    }
}
