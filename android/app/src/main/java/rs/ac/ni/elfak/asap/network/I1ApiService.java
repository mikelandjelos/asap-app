package rs.ac.ni.elfak.asap.network;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

interface I1ApiService {

    @POST("api/v1/scan-queries")
    Call<ResponseBody> query(@Body I1ApiModels.ScanQueryRequest request);
}
