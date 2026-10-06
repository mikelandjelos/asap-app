package rs.ac.ni.elfak.asap.network;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import java.io.IOException;
import java.util.List;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;
import rs.ac.ni.elfak.asap.network.ScanQueryClient.CallHandle;
import rs.ac.ni.elfak.asap.network.ScanQueryClient.Failure;

/** Strict client of the AI v2 API; failures use the same transport/HTTP/invalid-response classes as v1. */
public final class V2Client {

    public interface Callback<T> {
        void onSuccess(T value);

        void onFailure(Failure failure);
    }

    private interface Check<T> {
        boolean usable(T value);
    }

    private final V2ApiService service;
    private final JsonAdapter<V2ApiModels.ScanQueryResponse> queryAdapter;
    private final JsonAdapter<V2ApiModels.CatalogMap> mapAdapter;

    V2Client(V2ApiService service) {
        Moshi moshi = new Moshi.Builder().build();
        this.service = service;
        this.queryAdapter = moshi.adapter(V2ApiModels.ScanQueryResponse.class);
        this.mapAdapter = moshi.adapter(V2ApiModels.CatalogMap.class);
    }

    public static V2Client create(String baseUrl) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(MoshiConverterFactory.create(new Moshi.Builder().build()))
                .callbackExecutor(Runnable::run)
                .build();
        return new V2Client(retrofit.create(V2ApiService.class));
    }

    public CallHandle query(String value, String format, List<V2ApiModels.HistoryEvent> history,
            Callback<V2ApiModels.ScanQueryResponse> callback) {
        return enqueue(service.query(new V2ApiModels.ScanQueryRequest(value, format, history)), queryAdapter,
                V2ResponseValidator::isUsable, callback);
    }

    public CallHandle catalogMap(Callback<V2ApiModels.CatalogMap> callback) {
        return enqueue(service.catalogMap(), mapAdapter, V2ResponseValidator::isUsable, callback);
    }

    private static <T> CallHandle enqueue(Call<ResponseBody> call, JsonAdapter<T> adapter, Check<T> usable,
            Callback<T> callback) {
        call.enqueue(new retrofit2.Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> c, Response<ResponseBody> response) {
                if (!response.isSuccessful()) {
                    callback.onFailure(Failure.http(response.code()));
                    return;
                }
                ResponseBody body = response.body();
                if (body == null) {
                    callback.onFailure(Failure.invalidResponse());
                    return;
                }
                try (ResponseBody closeable = body) {
                    T parsed = adapter.fromJson(closeable.source());
                    if (!usable.usable(parsed)) {
                        callback.onFailure(Failure.invalidResponse());
                        return;
                    }
                    callback.onSuccess(parsed);
                } catch (IOException | RuntimeException e) {
                    callback.onFailure(Failure.invalidResponse());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> c, Throwable throwable) {
                callback.onFailure(Failure.transport());
            }
        });
        return call::cancel;
    }
}
