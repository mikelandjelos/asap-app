package rs.ac.ni.elfak.asap.network;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

interface V2ApiService {

    @POST("api/v2/scan-queries")
    Call<ResponseBody> query(@Body V2ApiModels.ScanQueryRequest request);

    @GET("api/v2/catalog-map")
    Call<ResponseBody> catalogMap();
}
