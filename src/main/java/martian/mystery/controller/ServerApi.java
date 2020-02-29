package martian.mystery.controller;

import martian.mystery.data.DataOfUser;
import martian.mystery.data.ResponseFromServer;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ServerApi {

    @GET("/update/") // проверка обновлений
    Call<ResponseFromServer> checkUpdate(@Query("update") int versionCode);

    @Headers("dont touch")
    @GET("/prize/") // получение приза
    Call<ResponseFromServer> getPrize(@Query("prize") String queryTrue);

    @Headers("dont touch")
    @GET("/login/") // регистрация логина
    Call<ResponseFromServer> logup(@Body DataOfUser dataOfUser);

    @GET("/leaders/") // получение списка лидеров
    Call<ResponseFromServer> getEmail(@Query("lead") String lead);

    @Headers("dont touch")
    @GET("/changelevel/") // отправка информации о переходе на новый уровень
    Call<ResponseFromServer> newLevel(@Body DataOfUser dataOfUser);

    @Headers("dont touch")
    @POST("/winner/") // получение email
    Call<Void> getEmail(@Body DataOfUser data);

    @POST("/checkanswer/") // проверка ответа
    Call<ResponseFromServer> checkAnswer(@Body DataOfUser data);

}

