package martian.mystery.controllers;


import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import martian.mystery.data.DataOfUser;
import martian.mystery.data.Player;
import martian.mystery.data.ResponseFromServer;
import martian.mystery.exceptions.ErrorOnServerException;
import martian.mystery.view.AssistentDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class StatisticsController {

    private FirebaseAnalytics mFirebaseAnalytics;
    private Context context;

    public static final String DATA_UPDATE_LEVEL = "update_level";
    private final int ERROR_ON_SERVER = -1;

    public StatisticsController(Context context) {
        this.context = context;
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
    }

    private int getLongOfLevel() { // получить время прохождения уровня
        SimpleDateFormat format = new SimpleDateFormat("hh dd MM yyyy");
        Date nowDate = new Date();
        String nowDateString = format.format(nowDate);
        String startTime = StoredData.getDataString(StoredData.DATA_START_TIME,"1");
        try {
            Date oldDate = format.parse(startTime);
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
        StoredData.saveData(StoredData.DATA_START_TIME,nowDateString);
    }

    // отправка статистики на сервер
    public void sendAttempt() {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Level: " + Player.getInstance().getLevel());
        bundle.putDouble(FirebaseAnalytics.Param.VALUE, 1);
        bundle.putString(FirebaseAnalytics.Param.VIRTUAL_CURRENCY_NAME, "attempt");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SPEND_VIRTUAL_CURRENCY, bundle);
    }
    public void sendPurchase(int countWrongAttempts) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Count wrong attempts");
        bundle.putInt(FirebaseAnalytics.Param.VALUE,countWrongAttempts);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.ECOMMERCE_PURCHASE,bundle);
    }
    public void sendErrorAd(int errorCode) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Error showing ad");
        bundle.putInt(FirebaseAnalytics.Param.VALUE,errorCode);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.CAMPAIGN_DETAILS,bundle);
    }
    public int sendNewLevel() throws IOException, ErrorOnServerException {
        DataOfUser data = new DataOfUser();
        data.setNameOfUser(AssistentDialog.assist.concat(Player.getInstance().getName()));
        data.setLevel(Player.getInstance().getLevel());
        data.setTimeOfLevel(getLongOfLevel());
        if(Progress.getInstance().getLevel() <= 21) {
            RequestController.getInstance()
                    .getJsonApi()
                    .newLevel(data)
                    .enqueue(new Callback<ResponseFromServer>() {
                        @Override
                        public void onResponse(Call<ResponseFromServer> call, Response<ResponseFromServer> response) {
                            ResponseFromServer responseFromServer = response.body();
                            if(responseFromServer.getResult() == ERROR_ON_SERVER) {
                                StoredData.saveData(DATA_UPDATE_LEVEL,"no");
                            } else {
                                StoredData.saveData(DATA_UPDATE_LEVEL,"yes");
                            }
                            // отправляем данные в Firebase
                            Bundle bundle = new Bundle();
                            bundle.putString(FirebaseAnalytics.Param.CHARACTER, "Some riddle");
                            bundle.putLong(FirebaseAnalytics.Param.LEVEL, Player.getInstance().getLevel());
                            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.LEVEL_UP, bundle);
                        }

                        @Override
                        public void onFailure(Call<ResponseFromServer> call, Throwable t) {
                            StoredData.saveData(DATA_UPDATE_LEVEL,"no");
                        }
                    });
        } else if(Progress.getInstance().isDone()) {
            ResponseFromServer response = RequestController.getInstance()
                    .getJsonApi()
                    .newLevel(data)
                    .execute().body();
            if(response.getResult() == -1) throw new ErrorOnServerException();
            else {
                return response.getPlace();
            }
        }
        return 0;
    }
}
