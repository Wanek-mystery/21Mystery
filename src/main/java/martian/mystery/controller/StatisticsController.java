package martian.mystery.controller;


import android.provider.ContactsContract;
import android.util.Log;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import martian.mystery.data.DataOfUser;
import martian.mystery.data.Player;
import martian.mystery.data.ResponseFromServer;
import martian.mystery.exceptions.ErrorOnServerException;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class StatisticsController {

    private final String DATA_UPDATE_LEVEL = "update_level";
    private final int ERROR_ON_SERVER = -1;
    private static final String TAG = "StatisticsController";

    public StatisticsController() { }

    private int getLongOfLevel() { // получить время прохождения уровня
        SimpleDateFormat format = new SimpleDateFormat("hh dd MM yyyy");
        Date nowDate = new Date();
        String nowDateString = format.format(nowDate);
        String lastDate = StoredData.getDataString(StoredData.DATA_LASTDATE,"1");
        try {
            Date oldDate = format.parse(lastDate);
            Date newDate = format.parse(nowDateString);
            int diffInDays = (int)( (newDate.getTime() - oldDate.getTime())
                    / (1000 * 60 * 60));
            return diffInDays;
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }
    public void setStartTimeLevel() { // установить время начала прохождения уровня
        SimpleDateFormat format = new SimpleDateFormat("hh dd MM yyyy");
        Date nowDate = new Date();
        String nowDateString = format.format(nowDate);
        StoredData.saveData(StoredData.DATA_LASTDATE,nowDateString);
    }
    public int sendNewLevel() throws IOException, ErrorOnServerException { // отправка статистики на сервер
        DataOfUser data = new DataOfUser();
        data.setNameOfUser(encryptLogin(Player.getInstance().getName()));
        Log.d(TAG, "sendNewLevel: name = " + data.getNameOfUser() + " other: " + Player.getInstance().getName());
        data.setLevel(Player.getInstance().getLevel());
        if(Progress.getInstance().getLevel() <= 21) {
            RequestController.getInstance()
                    .getJsonApi()
                    .newLevel(data)
                    .enqueue(new Callback<ResponseFromServer>() {
                        @Override
                        public void onResponse(Call<ResponseFromServer> call, Response<ResponseFromServer> response) {
                            ResponseFromServer responseFromServer = response.body();
                            Log.d(TAG, "onResponse: result = " + responseFromServer.getResult());
                            if(responseFromServer.getResult() == ERROR_ON_SERVER) {
                                StoredData.saveData(DATA_UPDATE_LEVEL,"no");
                            } else {
                                StoredData.saveData(DATA_UPDATE_LEVEL,"yes");
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseFromServer> call, Throwable t) {
                            StoredData.saveData(DATA_UPDATE_LEVEL,"no");
                            Log.d(TAG, "onFailure: error newlevel sending = " + t.toString());
                        }
                    });
        } else if(Progress.getInstance().isDone()) {
            Log.d(TAG, "sendNewLevel: isDone");
            ResponseFromServer response = RequestController.getInstance()
                    .getJsonApi()
                    .newLevel(data)
                    .execute().body();
            if(response.getResult() == -1) throw new ErrorOnServerException();
            else {
                Log.d(TAG, "sendNewLevel: place = " + response.getPlace());
                return response.getPlace();
            }
        }
        return 0;
    }
    private String encryptLogin(String login) { // шифруем логин перед отправкой
        return "hik;" + login + ";9gl";
    }
}
