package rs.ac.ni.elfak.asap.ui;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
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

    private final ScanSession session;
    private final V2Client client;

    public SessionViewModel(@NonNull Application application) {
        super(application);
        client = ApiClientFactory.createV2();
        Handler main = new Handler(Looper.getMainLooper());
        session = new ScanSession(
                (value, format, history, callback) -> client.query(value, format, history,
                        new V2Client.Callback<V2ApiModels.ScanQueryResponse>() {
                            @Override
                            public void onSuccess(V2ApiModels.ScanQueryResponse response) {
                                callback.onSuccess(response);
                            }

                            @Override
                            public void onFailure(ScanQueryClient.Failure failure) {
                                callback.onFailure(failure);
                            }
                        }),
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
