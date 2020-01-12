package martian.mystery;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StatisticsController {

    public StatisticsController() {
    }

    public int getDifference() {
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
    public void setStartTime() {
        SimpleDateFormat format = new SimpleDateFormat("hh dd MM yyyy");
        Date nowDate = new Date();
        String nowDateString = format.format(nowDate);
        StoredData.saveData(StoredData.DATA_LASTDATE,nowDateString);
    }
}
