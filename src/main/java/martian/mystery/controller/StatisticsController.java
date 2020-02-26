package martian.mystery.controller;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StatisticsController {


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
    public void sendStatistics() { // отправка статистики на сервер
        int level = 2;
        if(Progress.getInstance().getLevel() <= 21) {
            level = Progress.getInstance().getLevel();
        } else if(Progress.getInstance().isDone()) {
            level = 22;
        }
        /*RequestController.getInstance()
                .getJsonApi()
                .sendStatistics(level,getLongOfLevel())
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) { }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) { }
                });*/
    }
}
