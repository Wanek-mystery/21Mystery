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

    @GET("/updateapp/") // проверка обновлений
    Call<ResponseFromServer> checkUpdate(@Query("update") int versionCode);

    @Headers("User-Agent: dont touch")
    @GET("/prize/") // получение приза
    Call<ResponseFromServer> getPrize(@Query("prize") String queryTrue);

    @Headers("User-Agent: dont touch")
    @POST("/login/") // регистрация логина
    Call<ResponseFromServer> logup(@Body DataOfUser dataOfUser);

    @GET("/leaders/") // получение списка лидеров
    Call<ResponseFromServer> getLeaders(@Query("lead") String lead);

    @Headers("User-Agent: dont touch")
    @POST("/changelevel/") // отправка информации о переходе на новый уровень
    Call<ResponseFromServer> newLevel(@Body DataOfUser dataOfUser);

    @Headers("User-Agent: dont touch")
    @POST("/conn/") // получение email
    Call<ResponseFromServer> getEmail(@Body DataOfUser data);


    @POST("/checkanswer/") // проверка ответа
    Call<ResponseFromServer> checkAnswer(@Body DataOfUser data);

}

