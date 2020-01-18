package martian.mystery.controller;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import martian.mystery.data.DataOfUser;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StatisticsController {

    String TAG = "my";

    public StatisticsController() { }

    public int getLongOfLevel() { // получить время прохождения уровня
        SimpleDateFormat format = new SimpleDateFormat("hh dd MM yyyy");
        Date nowDate = new Date();
        String nowDateString = format.format(nowDate);
        String lastDate = StoredData.getDataString(StoredData.DATA_LASTDATE,"1");
        Log.d(TAG, "lastData: " + lastDate);
        try {
            Date oldDate = format.parse(lastDate);
            Date newDate = format.parse(nowDateString);
            int diffInDays = (int)( (newDate.getTime() - oldDate.getTime())
                    / (1000 * 60 * 60));
            Log.d(TAG, "getLongOfLevel: " + diffInDays);
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
        Log.d(TAG, "setStartTimeLevel: " + nowDateString);
        StoredData.saveData(StoredData.DATA_LASTDATE,nowDateString);
    }
    public void sendStatistics() {
        RequestController.getInstance()
                .getJsonApi()
                .sendStatistics(Progress.getInstance().getLevel(),getLongOfLevel())
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        Log.d(TAG, "onResponse: стаститка успешно отправилась");
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Log.d(TAG, "onFailure: ошибка при отправке статистики " + t.toString());
                    }
                });
    }
}
