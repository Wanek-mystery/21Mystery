package martian.mystery.controllers;

import android.content.Context;
import android.content.SharedPreferences;


public class StoredData { // класс, реализующий доступ к сохраненным на устройстве данным

    public static final String APP_PREFERENCES = "app_data2";
    public static final String DATA_LEVEL = "game_level4";
    public static final String DATA_WINS = ";wins4";
    public static final String DATA_PRIZE = "prize4";
    public static final String DATA_COUNT_ATTEMPTS = "count_attempts4";
    public static final String DATA_COUNT_LAUNCH_APP = "count_launch_app2"; // счетчик запусков лучше не менять (серьезно)
    public static final String DATA_WINNER_IS_CHECKED = ";winner_is_checked4";
    public static final String DATA_IS_WINNER = "is_winner4";
    public static final String DATA_PLACE = ";place_gamer4";
    public static final String DATA_START_TIME = ";last_date4";

    public StoredData() { }

    // сохранение данных на устройстве
    public static void saveData(String typeData, int data) {
        SharedPreferences sharedPreferences = GetContextClass.getContext().getSharedPreferences(APP_PREFERENCES,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(typeData,data);
        editor.apply();
    }
    public static void saveData(String typeData, String data) {
        SharedPreferences sharedPreferences = GetContextClass.getContext().getSharedPreferences(APP_PREFERENCES,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(typeData,data);
        editor.apply();
    }
    public static void saveData(String typeData, boolean data) {
        SharedPreferences sharedPreferences = GetContextClass.getContext().getSharedPreferences(APP_PREFERENCES,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(typeData,data);
        editor.commit();
    }

    // получение данных с устройства
    public static int getDataInt(String typeData, int defValue) {
        SharedPreferences sharedPreferences = GetContextClass.getContext().getSharedPreferences(APP_PREFERENCES,Context.MODE_PRIVATE);
        return sharedPreferences.getInt(typeData,defValue);
    }
    public static String getDataString(String typeData, String defValue) {
        SharedPreferences sharedPreferences = GetContextClass.getContext().getSharedPreferences(APP_PREFERENCES,Context.MODE_PRIVATE);
        return sharedPreferences.getString(typeData,defValue);
    }
    public static boolean getDataBool(String typeData) {
        SharedPreferences sharedPreferences = GetContextClass.getContext().getSharedPreferences(APP_PREFERENCES,Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(typeData,false);
    }

}
