package martian.mystery.controller;

import martian.mystery.data.DataOfUser;
import martian.mystery.data.ResponseFromServer;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ServerApi {

    @GET("/update/")
    Call<ResponseFromServer> checkUpdate(@Query("update") int versionCode);

    @GET("/pubdata/")
    Call<ResponseFromServer> getMainData(@Query("green") String queryTrue);

    @GET("/winner/")
    Call<ResponseFromServer> sendWinner(@Query("black") String winner);

    @GET("/winner/")
    Call<ResponseFromServer> getEmail(@Query("mouse") String way);

    @GET("/statistics/")
    Call<Void> sendStatistics(@Query("newlevel") int level, @Query("time") int time);

    @POST("/winner/")
    Call<Void> sendNameWinner(@Body DataOfUser data);

}

