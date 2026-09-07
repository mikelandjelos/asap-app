package rs.ac.ni.elfak.asap.network;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import java.io.IOException;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

public final class RetrofitScanQueryClient implements ScanQueryClient {

    private final I1ApiService service;
    private final JsonAdapter<I1ApiModels.ScanQueryResponse> responseAdapter;

    private RetrofitScanQueryClient(
            I1ApiService service,
            JsonAdapter<I1ApiModels.ScanQueryResponse> responseAdapter) {
        this.service = service;
        this.responseAdapter = responseAdapter;
    }

    public static RetrofitScanQueryClient create(String baseUrl) {
        Moshi moshi = new Moshi.Builder().build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .callbackExecutor(Runnable::run)
                .build();
        return new RetrofitScanQueryClient(
                retrofit.create(I1ApiService.class),
                moshi.adapter(I1ApiModels.ScanQueryResponse.class));
    }

    static RetrofitScanQueryClient createForTesting(I1ApiService service) {
        Moshi moshi = new Moshi.Builder().build();
        return new RetrofitScanQueryClient(
                service, moshi.adapter(I1ApiModels.ScanQueryResponse.class));
    }

    @Override
    public CallHandle query(String value, String format, Callback callback) {
        Call<ResponseBody> call = service.query(new I1ApiModels.ScanQueryRequest(value, format));
        call.enqueue(new retrofit2.Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (!response.isSuccessful()) {
                            callback.onFailure(Failure.http(response.code()));
                            return;
                        }
                        ResponseBody body = response.body();
                        if (body == null) {
                            callback.onFailure(Failure.invalidResponse());
                            return;
                        }
                        try (ResponseBody closeableBody = body) {
                            I1ApiModels.ScanQueryResponse parsed =
                                    responseAdapter.fromJson(closeableBody.source());
                            if (!I1ResponseValidator.isUsable(parsed)) {
                                callback.onFailure(Failure.invalidResponse());
                                return;
                            }
                            callback.onSuccess(parsed);
                        } catch (IOException | RuntimeException exception) {
                            callback.onFailure(Failure.invalidResponse());
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable throwable) {
                        callback.onFailure(Failure.transport());
                    }
                });
        return call::cancel;
    }
}
