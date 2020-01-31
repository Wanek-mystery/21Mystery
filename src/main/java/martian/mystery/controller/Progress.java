package martian.mystery.controller;


import android.util.Log;

import static martian.mystery.controller.StoredData.DATA_COUNT_LAUNCH_APP;
import static martian.mystery.controller.StoredData.DATA_LEVEL;

public class Progress { // класс синглтон для управления уровнем(прогрессом)
    private int level;
    private boolean isDone = false;

    private static final Progress instanceProgress = new Progress();

    public static Progress getInstance() {
        return instanceProgress;
    }

    private Progress() { this.level = getLevelFromStorage(); }
    private static int getLevelFromStorage() {
        // получение уровня игрока из какой-нибудь базы данных
        int countLaunches = StoredData.getDataInt(DATA_COUNT_LAUNCH_APP,0);
        int level = StoredData.getDataInt(DATA_LEVEL,1);
        if(countLaunches == 1 && level > 1) level = 1; // для защиты от взлома
        return level;
    }
    public void levelUp() {
        level++;
        if(level == 22) isDone = true;
        if(level <= 22) {
            incrementSaveLevel();
        }
    }
    public int getLevel() {
        return level;
    }
    private void incrementSaveLevel() { // увеличивает уровень на 1 и сохраняет на устройстве
        StoredData.saveData(DATA_LEVEL,StoredData.getDataInt(DATA_LEVEL,1) + 1);
    }
    public boolean isDone() {
        return isDone;
    }
    public void done(boolean done) {
        isDone = done;
    }
}
