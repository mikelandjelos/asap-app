package rs.ac.ni.elfak.asap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.google.mlkit.vision.barcode.common.Barcode;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Executor;
import org.junit.Test;
import rs.ac.ni.elfak.asap.network.I1ApiModels;
import rs.ac.ni.elfak.asap.network.ScanQueryClient;

public final class ScanQueryCoordinatorTest {

    @Test
    public void supportedScansUseExactContractFormats() {
        FakeClient client = new FakeClient();
        CapturingView view = new CapturingView();
        ScanQueryCoordinator coordinator = coordinator(client, Runnable::run, view);

        submit(coordinator, "2000000000015", Barcode.FORMAT_EAN_13);
        submit(coordinator, "20000005", Barcode.FORMAT_EAN_8);
        submit(coordinator, "200000000015", Barcode.FORMAT_UPC_A);
        submit(coordinator, "20000005", Barcode.FORMAT_UPC_E);

        assertEquals(4, client.queries.size());
        assertEquals("EAN_13", client.queries.get(0).format);
        assertEquals("EAN_8", client.queries.get(1).format);
        assertEquals("UPC_A", client.queries.get(2).format);
        assertEquals("UPC_E", client.queries.get(3).format);
        assertEquals("LOADING", view.event);
        assertEquals("20000005", view.value);
    }

    @Test
    public void displayValueFallbackIsSubmittedUnchanged() {
        FakeClient client = new FakeClient();
        CapturingView view = new CapturingView();
        ScanQueryCoordinator coordinator = coordinator(client, Runnable::run, view);

        coordinator.onScanSucceeded(" ", "01234565", Barcode.FORMAT_EAN_8);

        assertEquals(1, client.queries.size());
        assertEquals("01234565", client.queries.get(0).value);
        assertEquals("EAN_8", client.queries.get(0).format);
    }

    @Test
    public void emptyAndUnsupportedScansNeverReachClient() {
        FakeClient client = new FakeClient();
        CapturingView view = new CapturingView();
        ScanQueryCoordinator coordinator = coordinator(client, Runnable::run, view);

        coordinator.onScanSucceeded(null, " ", Barcode.FORMAT_EAN_13);
        assertEquals("EMPTY", view.event);
        coordinator.onScanSucceeded("payload", null, Barcode.FORMAT_QR_CODE);

        assertEquals("UNSUPPORTED", view.event);
        assertEquals("payload", view.value);
        assertTrue(client.queries.isEmpty());
    }

    @Test
    public void newerScanCancelsRequestAndRejectsItsStaleResponse() {
        FakeClient client = new FakeClient();
        CapturingView view = new CapturingView();
        ScanQueryCoordinator coordinator = coordinator(client, Runnable::run, view);

        submit(coordinator, "2000000000015", Barcode.FORMAT_EAN_13);
        FakeQuery first = client.queries.get(0);
        coordinator.onScanStarted();
        assertTrue(first.cancelled);
        submit(coordinator, "2000000000022", Barcode.FORMAT_EAN_13);
        FakeQuery second = client.queries.get(1);

        first.succeed(response("OLD"));
        assertEquals("LOADING", view.event);
        assertEquals("2000000000022", view.value);
        second.succeed(response("CURRENT"));

        assertEquals("RESPONSE", view.event);
        assertEquals("2000000000022", view.value);
        assertEquals("CURRENT", view.response.product.status);
    }

    @Test
    public void closeCancelsRequestAndBlocksLaterCallbacksOrScans() {
        FakeClient client = new FakeClient();
        CapturingView view = new CapturingView();
        ScanQueryCoordinator coordinator = coordinator(client, Runnable::run, view);

        submit(coordinator, "2000000000015", Barcode.FORMAT_EAN_13);
        FakeQuery query = client.queries.get(0);
        coordinator.close();
        query.succeed(response("LATE"));
        submit(coordinator, "2000000000022", Barcode.FORMAT_EAN_13);

        assertTrue(query.cancelled);
        assertEquals(1, client.queries.size());
        assertEquals("LOADING", view.event);
        assertNull(view.response);
    }

    @Test
    public void transportHttpAndInvalidResponseFailuresStayDistinct() {
        FakeClient client = new FakeClient();
        CapturingView view = new CapturingView();
        ScanQueryCoordinator coordinator = coordinator(client, Runnable::run, view);

        submit(coordinator, "2000000000015", Barcode.FORMAT_EAN_13);
        client.latest().fail(ScanQueryClient.Failure.transport());
        assertEquals(ScanQueryClient.Failure.Kind.TRANSPORT, view.failure.kind());

        submit(coordinator, "2000000000022", Barcode.FORMAT_EAN_13);
        client.latest().fail(ScanQueryClient.Failure.http(503));
        assertEquals(ScanQueryClient.Failure.Kind.HTTP, view.failure.kind());
        assertEquals(Integer.valueOf(503), view.failure.httpStatus());

        submit(coordinator, "2000000000039", Barcode.FORMAT_EAN_13);
        client.latest().fail(ScanQueryClient.Failure.invalidResponse());
        assertEquals(ScanQueryClient.Failure.Kind.INVALID_RESPONSE, view.failure.kind());
    }

    @Test
    public void callbackStateIsDeliveredThroughMainThreadExecutor() {
        FakeClient client = new FakeClient();
        QueuedExecutor executor = new QueuedExecutor();
        CapturingView view = new CapturingView();
        ScanQueryCoordinator coordinator = coordinator(client, executor, view);
        I1ApiModels.ScanQueryResponse response = response("KNOWN");

        submit(coordinator, "2000000000015", Barcode.FORMAT_EAN_13);
        client.latest().succeed(response);

        assertEquals("LOADING", view.event);
        assertNull(view.response);
        executor.runNext();
        assertEquals("RESPONSE", view.event);
        assertSame(response, view.response);
        assertFalse(executor.hasWork());
    }

    private static ScanQueryCoordinator coordinator(
            FakeClient client, Executor executor, CapturingView view) {
        return new ScanQueryCoordinator(client, executor, view);
    }

    private static void submit(ScanQueryCoordinator coordinator, String value, int format) {
        coordinator.onScanSucceeded(value, null, format);
    }

    private static I1ApiModels.ScanQueryResponse response(String productStatus) {
        I1ApiModels.ScanQueryResponse response = new I1ApiModels.ScanQueryResponse();
        response.product = new I1ApiModels.ProductOutcome();
        response.product.status = productStatus;
        response.recommendations = new I1ApiModels.RecommendationOutcome();
        return response;
    }

    private static final class FakeClient implements ScanQueryClient {
        private final List<FakeQuery> queries = new ArrayList<>();

        @Override
        public CallHandle query(String value, String format, Callback callback) {
            FakeQuery query = new FakeQuery(value, format, callback);
            queries.add(query);
            return () -> query.cancelled = true;
        }

        private FakeQuery latest() {
            return queries.get(queries.size() - 1);
        }
    }

    private static final class FakeQuery {
        private final String value;
        private final String format;
        private final ScanQueryClient.Callback callback;
        private boolean cancelled;

        private FakeQuery(String value, String format, ScanQueryClient.Callback callback) {
            this.value = value;
            this.format = format;
            this.callback = callback;
        }

        private void succeed(I1ApiModels.ScanQueryResponse response) {
            callback.onSuccess(response);
        }

        private void fail(ScanQueryClient.Failure failure) {
            callback.onFailure(failure);
        }
    }

    private static final class CapturingView implements ScanQueryCoordinator.View {
        private String event;
        private String value;
        private I1ApiModels.ScanQueryResponse response;
        private ScanQueryClient.Failure failure;

        @Override
        public void showEmptyBarcode() {
            event = "EMPTY";
        }

        @Override
        public void showUnsupportedBarcode(String value) {
            event = "UNSUPPORTED";
            this.value = value;
        }

        @Override
        public void showLoading(String value) {
            event = "LOADING";
            this.value = value;
        }

        @Override
        public void showResponse(String value, I1ApiModels.ScanQueryResponse response) {
            event = "RESPONSE";
            this.value = value;
            this.response = response;
        }

        @Override
        public void showFailure(String value, ScanQueryClient.Failure failure) {
            event = "FAILURE";
            this.value = value;
            this.failure = failure;
        }
    }

    private static final class QueuedExecutor implements Executor {
        private final Queue<Runnable> work = new ArrayDeque<>();

        @Override
        public void execute(Runnable command) {
            work.add(command);
        }

        private void runNext() {
            work.remove().run();
        }

        private boolean hasWork() {
            return !work.isEmpty();
        }
    }
}
