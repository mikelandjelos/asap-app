package rs.ac.ni.elfak.asap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import rs.ac.ni.elfak.asap.history.HistoryStore;
import rs.ac.ni.elfak.asap.network.ScanQueryClient;
import rs.ac.ni.elfak.asap.network.V2ApiModels;

public class ScanSessionTest {

    private static final int EAN_13 = com.google.mlkit.vision.barcode.common.Barcode.FORMAT_EAN_13;
    private static final int QR = com.google.mlkit.vision.barcode.common.Barcode.FORMAT_QR_CODE;

    private final List<Pending> calls = new ArrayList<>();
    private final List<ScanSession.Phase> phases = new ArrayList<>();
    private HistoryStore history;
    private ScanSession session;

    private static final class Pending {
        String value;
        List<V2ApiModels.HistoryEvent> history;
        ScanSession.Callback callback;
        boolean cancelled;
    }

    @Before
    public void setUp() throws Exception {
        File dir = Files.createTempDirectory("session").toFile();
        int[] id = {0};
        history = new HistoryStore(new File(dir, "h.json"), () -> 1_790_000_000_000L, () -> "e" + id[0]++);
        session = new ScanSession((value, format, h, callback) -> {
            Pending p = new Pending();
            p.value = value;
            p.history = h;
            p.callback = callback;
            calls.add(p);
            return () -> p.cancelled = true;
        }, history, Runnable::run);
        session.addListener(s -> phases.add(s.phase));
    }

    static V2ApiModels.ScanQueryResponse known(String id) {
        V2ApiModels.ScanQueryResponse r = new V2ApiModels.ScanQueryResponse();
        r.product = new V2ApiModels.ProductOutcome();
        r.product.status = "KNOWN";
        r.product.data = rs.ac.ni.elfak.asap.history.HistoryStoreTest.product(id);
        return r;
    }

    static V2ApiModels.ScanQueryResponse unknown() {
        V2ApiModels.ScanQueryResponse r = new V2ApiModels.ScanQueryResponse();
        r.product = new V2ApiModels.ProductOutcome();
        r.product.status = "UNKNOWN";
        return r;
    }

    @Test
    public void emptyAndUnsupportedScansMakeNoRequest() {
        session.onScanned(null, " ", EAN_13);
        session.onScanned("123", "123", QR);
        assertTrue(calls.isEmpty());
        assertEquals(ScanSession.Phase.UNSUPPORTED_BARCODE, session.state().phase);
        assertTrue(phases.contains(ScanSession.Phase.EMPTY_BARCODE));
    }

    @Test
    public void knownProductIsDeliveredAndRecordedOnce() {
        session.onScanned("3017620422003", "3017620422003", EAN_13);
        assertEquals(ScanSession.Phase.LOADING, session.state().phase);
        calls.get(0).callback.onSuccess(known("off:1"));
        assertEquals(ScanSession.Phase.LOADED, session.state().phase);
        assertEquals(1, history.entries().size());
        session.open("5000112519945", "EAN_13");
        assertEquals(1, calls.get(1).history.size()); // second request carries the recorded view
    }

    @Test
    public void unknownProductAndFailuresAreNotRecorded() {
        session.open("1", "EAN_13");
        calls.get(0).callback.onSuccess(unknown());
        session.open("2", "EAN_13");
        calls.get(1).callback.onFailure(ScanQueryClient.Failure.transport());
        assertEquals(ScanSession.Phase.FAILED, session.state().phase);
        assertTrue(history.entries().isEmpty());
    }

    @Test
    public void newerRequestCancelsOlderAndStaleCallbacksAreDropped() {
        session.open("1", "EAN_13");
        session.open("2", "EAN_13");
        assertTrue(calls.get(0).cancelled);
        calls.get(0).callback.onSuccess(known("off:old"));
        assertEquals(ScanSession.Phase.LOADING, session.state().phase);
        calls.get(1).callback.onSuccess(known("off:new"));
        assertEquals("off:new", session.state().response.product.data.id);
        assertEquals(1, history.entries().size());
    }

    @Test
    public void retryRepeatsTheLastRequestAndCloseCancels() {
        session.open("1", "EAN_13");
        calls.get(0).callback.onFailure(ScanQueryClient.Failure.http(500));
        session.retry();
        assertEquals(2, calls.size());
        assertEquals("1", calls.get(1).value);
        session.close();
        assertTrue(calls.get(1).cancelled);
        calls.get(1).callback.onSuccess(known("off:late"));
        assertNull(session.state().response);
    }
}
