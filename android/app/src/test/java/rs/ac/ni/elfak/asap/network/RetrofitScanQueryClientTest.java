package rs.ac.ni.elfak.asap.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.squareup.moshi.Moshi;
import java.io.IOException;
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

public class RetrofitScanQueryClientTest {

    private static final MediaType JSON = MediaType.get("application/json");

    @Test
    public void serializesFrozenRequestAndPath() throws Exception {
        Moshi moshi = new Moshi.Builder().build();
        I1ApiService service = new Retrofit.Builder()
                .baseUrl("https://example.invalid/")
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(I1ApiService.class);

        Request request = service.query(
                new I1ApiModels.ScanQueryRequest("2000000000015", "EAN_13"))
                .request();
        Buffer body = new Buffer();
        request.body().writeTo(body);

        assertEquals("POST", request.method());
        assertEquals("/api/v1/scan-queries", request.url().encodedPath());
        assertEquals("application/json; charset=UTF-8", request.body().contentType().toString());
        assertEquals(
                "{\"barcode\":{\"format\":\"EAN_13\",\"value\":\"2000000000015\"}}",
                body.readUtf8());
    }

    @Test
    public void parsesKnownProductAndResults() {
        Capture capture = query(Response.success(jsonBody(knownResponse("RESULTS", resultItem()))));

        assertNotNull(capture.success);
        assertNull(capture.failure);
        assertEquals("KNOWN", capture.success.product.status);
        assertEquals("fixture:oat-drink", capture.success.product.data.id);
        assertEquals(1, capture.success.recommendations.items.size());
    }

    @Test
    public void parsesKnownProductWithEmptyRecommendations() {
        assertSuccessStatus(knownResponse("EMPTY", ""), "KNOWN", "EMPTY");
    }

    @Test
    public void parsesKnownProductWithUnavailableRecommendations() {
        assertSuccessStatus(knownResponse("UNAVAILABLE", ""), "KNOWN", "UNAVAILABLE");
    }

    @Test
    public void parsesUnknownProductWithNotApplicableRecommendations() {
        assertSuccessStatus(productWithoutData("UNKNOWN"), "UNKNOWN", "NOT_APPLICABLE");
    }

    @Test
    public void parsesUnavailableProductWithNotApplicableRecommendations() {
        assertSuccessStatus(productWithoutData("UNAVAILABLE"), "UNAVAILABLE", "NOT_APPLICABLE");
    }

    @Test
    public void classifiesMalformedJsonAsInvalidResponse() {
        assertFailure(Response.success(jsonBody("{")), ScanQueryClient.Failure.Kind.INVALID_RESPONSE);
    }

    @Test
    public void rejectsResponseThatBreaksOutcomeInvariant() {
        assertFailure(
                Response.success(jsonBody(knownResponse("RESULTS", ""))),
                ScanQueryClient.Failure.Kind.INVALID_RESPONSE);
    }

    @Test
    public void classifiesNonSuccessfulHttpResponse() {
        Response<ResponseBody> response = Response.error(500, jsonBody("{}"));
        Capture capture = query(response);

        assertNull(capture.success);
        assertEquals(ScanQueryClient.Failure.Kind.HTTP, capture.failure.kind());
        assertEquals(Integer.valueOf(500), capture.failure.httpStatus());
    }

    @Test
    public void classifiesCallFailureAsTransportFailure() {
        Capture capture = query(new IOException("offline"));

        assertNull(capture.success);
        assertEquals(ScanQueryClient.Failure.Kind.TRANSPORT, capture.failure.kind());
        assertNull(capture.failure.httpStatus());
    }

    @Test
    public void returnedHandleCancelsUnderlyingCall() {
        FakeCall call = new FakeCall(Response.success(jsonBody(productWithoutData("UNKNOWN"))), null);
        I1ApiService service = request -> call;
        RetrofitScanQueryClient client = RetrofitScanQueryClient.createForTesting(service);

        ScanQueryClient.CallHandle handle =
                client.query("2000000000992", "EAN_13", new Capture());
        handle.cancel();

        assertEquals(true, call.isCanceled());
    }

    private static void assertSuccessStatus(
            String json, String productStatus, String recommendationStatus) {
        Capture capture = query(Response.success(jsonBody(json)));

        assertNotNull(capture.success);
        assertNull(capture.failure);
        assertEquals(productStatus, capture.success.product.status);
        assertEquals(recommendationStatus, capture.success.recommendations.status);
    }

    private static void assertFailure(
            Response<ResponseBody> response, ScanQueryClient.Failure.Kind expected) {
        Capture capture = query(response);

        assertNull(capture.success);
        assertEquals(expected, capture.failure.kind());
    }

    private static Capture query(Response<ResponseBody> response) {
        return query(new FakeCall(response, null));
    }

    private static Capture query(Throwable failure) {
        return query(new FakeCall(null, failure));
    }

    private static Capture query(FakeCall call) {
        I1ApiService service = request -> call;
        RetrofitScanQueryClient client = RetrofitScanQueryClient.createForTesting(service);
        Capture capture = new Capture();
        client.query("2000000000015", "EAN_13", capture);
        return capture;
    }

    private static ResponseBody jsonBody(String json) {
        return ResponseBody.create(json, JSON);
    }

    private static String knownResponse(String recommendationStatus, String items) {
        return "{"
                + "\"product\":{\"status\":\"KNOWN\",\"data\":{"
                + "\"id\":\"fixture:oat-drink\","
                + "\"barcode\":{\"value\":\"2000000000015\",\"format\":\"EAN_13\"},"
                + "\"name\":\"ASAP oat drink\",\"brand\":\"ASAP Demo\","
                + "\"category\":\"Plant-based drinks\",\"description\":\"Demo\","
                + "\"tags\":[\"demo\"],"
                + "\"provenance\":{\"type\":\"CONTROLLED_FIXTURE\","
                + "\"source\":\"asap-i1-fixtures-v1\"}}},"
                + "\"recommendations\":{\"status\":\"" + recommendationStatus + "\","
                + "\"mode\":\"DETERMINISTIC_FIXTURE\",\"placeholder\":true,"
                + "\"items\":[" + items + "]}}";
    }

    private static String resultItem() {
        return "{\"rank\":1,\"product\":{\"id\":\"fixture:almond-drink\","
                + "\"name\":\"ASAP almond drink\",\"brand\":\"ASAP Demo\","
                + "\"category\":\"Plant-based drinks\"}}";
    }

    private static String productWithoutData(String status) {
        return "{\"product\":{\"status\":\"" + status + "\"},"
                + "\"recommendations\":{\"status\":\"NOT_APPLICABLE\","
                + "\"placeholder\":false,\"items\":[]}}";
    }

    private static final class Capture implements ScanQueryClient.Callback {
        private I1ApiModels.ScanQueryResponse success;
        private ScanQueryClient.Failure failure;

        @Override
        public void onSuccess(I1ApiModels.ScanQueryResponse response) {
            this.success = response;
        }

        @Override
        public void onFailure(ScanQueryClient.Failure failure) {
            this.failure = failure;
        }
    }

    private static final class FakeCall implements Call<ResponseBody> {
        private final Response<ResponseBody> response;
        private final Throwable failure;
        private boolean executed;
        private boolean canceled;

        private FakeCall(Response<ResponseBody> response, Throwable failure) {
            this.response = response;
            this.failure = failure;
        }

        @Override
        public Response<ResponseBody> execute() throws IOException {
            executed = true;
            if (failure instanceof IOException) {
                throw (IOException) failure;
            }
            return response;
        }

        @Override
        public void enqueue(Callback<ResponseBody> callback) {
            executed = true;
            if (failure != null) {
                callback.onFailure(this, failure);
            } else {
                callback.onResponse(this, response);
            }
        }

        @Override
        public boolean isExecuted() {
            return executed;
        }

        @Override
        public void cancel() {
            canceled = true;
        }

        @Override
        public boolean isCanceled() {
            return canceled;
        }

        @Override
        public Call<ResponseBody> clone() {
            return new FakeCall(response, failure);
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
