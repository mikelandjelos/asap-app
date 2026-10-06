package rs.ac.ni.elfak.asap.ui;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import rs.ac.ni.elfak.asap.ScanSession;
import rs.ac.ni.elfak.asap.history.HistoryStore;
import rs.ac.ni.elfak.asap.network.ApiClientFactory;
import rs.ac.ni.elfak.asap.network.ScanQueryClient;
import rs.ac.ni.elfak.asap.network.V2ApiModels;
import rs.ac.ni.elfak.asap.network.V2Client;

/** Keeps the scan session and v2 client across configuration changes. */
public final class SessionViewModel extends AndroidViewModel {

    private static final String TAG = "AsapTiming";

    private final ScanSession session;
    private final V2Client client;

    public SessionViewModel(@NonNull Application application) {
        super(application);
        client = ApiClientFactory.createV2();
        Handler main = new Handler(Looper.getMainLooper());
        session = new ScanSession(
                (value, format, history, callback) -> {
                    long started = SystemClock.elapsedRealtime();
                    return client.query(value, format, history, new V2Client.Callback<V2ApiModels.ScanQueryResponse>() {
                        @Override
                        public void onSuccess(V2ApiModels.ScanQueryResponse response) {
                            // S8 phone latency evidence: request start to validated response, per product outcome
                            Log.i(TAG, "query barcode=" + value + " outcome=" + response.product.status
                                    + " source=" + (response.product.data == null ? "-" : response.product.data.provenance.type)
                                    + " ms=" + (SystemClock.elapsedRealtime() - started));
                            callback.onSuccess(response);
                        }

                        @Override
                        public void onFailure(ScanQueryClient.Failure failure) {
                            Log.i(TAG, "query barcode=" + value + " failure=" + failure.kind()
                                    + " ms=" + (SystemClock.elapsedRealtime() - started));
                            callback.onFailure(failure);
                        }
                    });
                },
                HistoryStore.create(application.getFilesDir()),
                main::post);
    }

    public ScanSession session() {
        return session;
    }

    public V2Client client() {
        return client;
    }

    @Override
    protected void onCleared() {
        session.close();
    }
}
