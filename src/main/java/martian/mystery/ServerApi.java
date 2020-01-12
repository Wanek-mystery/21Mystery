package martian.mystery;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ServerApi {

    @GET("/")
    Call<ResponseFromServer> checkUpdate(@Query("update") int versionCode);

    @GET("/")
    Call<ResponseFromServer> getMainData(@Query("maindata") String queryTrue);

    @GET("/")
    Call<ResponseFromServer> sendWinner(@Query("notbad") String winner);

    @GET("/")
    Call<ResponseFromServer> getEmail(@Query("way") String way);

    @GET("/")
    Call<Void> sendStatistics(@Query("newlevel") String level);

    @POST("/")
    Call<Void> sendNameWinner(@Body DataOfUser data);

}

