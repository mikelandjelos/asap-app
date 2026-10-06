package rs.ac.ni.elfak.asap;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import rs.ac.ni.elfak.asap.history.HistoryStore;
import rs.ac.ni.elfak.asap.network.BarcodeFormatMapper;
import rs.ac.ni.elfak.asap.network.ScanQueryClient;
import rs.ac.ni.elfak.asap.network.V2ApiModels;

/**
 * Holds the current scan query state for all screens. Only the newest request may deliver (stale callbacks are
 * dropped, the previous call is cancelled); a {@code PRODUCT_VIEWED} event is recorded only after a KNOWN product
 * is delivered (DOMAIN_MODEL.md).
 */
public final class ScanSession {

    public enum Phase { IDLE, EMPTY_BARCODE, UNSUPPORTED_BARCODE, LOADING, LOADED, FAILED }

    /** Immutable snapshot rendered by the screens. */
    public static final class State {
        public final Phase phase;
        public final String barcode;
        public final V2ApiModels.ScanQueryResponse response;
        public final ScanQueryClient.Failure failure;

        State(Phase phase, String barcode, V2ApiModels.ScanQueryResponse response, ScanQueryClient.Failure failure) {
            this.phase = phase;
            this.barcode = barcode;
            this.response = response;
            this.failure = failure;
        }
    }

    public interface Listener {
        void onState(State state);
    }

    /** Port of {@code V2Client.query}, so tests can substitute it. */
    public interface Api {
        ScanQueryClient.CallHandle query(String value, String format, List<V2ApiModels.HistoryEvent> history,
                Callback callback);
    }

    public interface Callback {
        void onSuccess(V2ApiModels.ScanQueryResponse response);

        void onFailure(ScanQueryClient.Failure failure);
    }

    private final Api api;
    private final HistoryStore history;
    private final Executor main;
    private final List<Listener> listeners = new ArrayList<>();
    private State state = new State(Phase.IDLE, null, null, null);
    private Object active;
    private ScanQueryClient.CallHandle activeHandle;
    private String lastFormat;

    public ScanSession(Api api, HistoryStore history, Executor main) {
        this.api = api;
        this.history = history;
        this.main = main;
    }

    public State state() {
        return state;
    }

    public HistoryStore history() {
        return history;
    }

    public void addListener(Listener l) {
        listeners.add(l);
        l.onState(state);
    }

    public void removeListener(Listener l) {
        listeners.remove(l);
    }

    /** Raw scanner result; maps value/format with the tested v1 helpers. */
    public void onScanned(String rawValue, String displayValue, int scannerFormat) {
        String value = BarcodeResultFormatter.selectValue(rawValue, displayValue);
        if (value == null) {
            cancel();
            publish(new State(Phase.EMPTY_BARCODE, null, null, null));
            return;
        }
        String format = BarcodeFormatMapper.toContractFormat(scannerFormat);
        if (format == null) {
            cancel();
            publish(new State(Phase.UNSUPPORTED_BARCODE, value, null, null));
            return;
        }
        open(value, format);
    }

    /** Opens a product by barcode (scan result, recent item or a tapped recommendation). */
    public void open(String value, String format) {
        cancel();
        Object token = new Object();
        active = token;
        lastFormat = format;
        publish(new State(Phase.LOADING, value, null, null));
        ScanQueryClient.CallHandle handle = api.query(value, format, history.requestContext(), new Callback() {
            @Override
            public void onSuccess(V2ApiModels.ScanQueryResponse response) {
                main.execute(() -> {
                    if (active != token) {
                        return;
                    }
                    active = null;
                    if ("KNOWN".equals(response.product.status)) {
                        history.recordView(response.product.data);
                    }
                    publish(new State(Phase.LOADED, value, response, null));
                });
            }

            @Override
            public void onFailure(ScanQueryClient.Failure failure) {
                main.execute(() -> {
                    if (active != token) {
                        return;
                    }
                    active = null;
                    publish(new State(Phase.FAILED, value, null, failure));
                });
            }
        });
        if (active == token) {
            activeHandle = handle;
        }
    }

    /** Repeats the last request (error state "Try again"). */
    public void retry() {
        if (state.barcode != null && lastFormat != null) {
            open(state.barcode, lastFormat);
        }
    }

    public void close() {
        cancel();
        listeners.clear();
    }

    private void cancel() {
        active = null;
        if (activeHandle != null) {
            activeHandle.cancel();
            activeHandle = null;
        }
    }

    private void publish(State next) {
        state = next;
        for (Listener l : new ArrayList<>(listeners)) {
            l.onState(next);
        }
    }
}
