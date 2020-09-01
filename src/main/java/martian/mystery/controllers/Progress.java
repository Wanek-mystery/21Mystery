package martian.mystery.controllers;


import static martian.mystery.controllers.StoredData.DATA_COUNT_LAUNCH_APP;
import static martian.mystery.controllers.StoredData.DATA_LEVEL;

public class Progress { // класс синглтон для управления уровнем(прогрессом)
    private int level;
    private boolean isDone = false;
    public static final int DEFAULT_LEVEL = 1;

    private static final Progress instanceProgress = new Progress();

    public static Progress getInstance() {
        return instanceProgress;
    }

    private Progress() { this.level = getLevelFromStorage(); }
    private static int getLevelFromStorage() {
        // получение уровня игрока из какой-нибудь базы данных
        int countLaunches = StoredData.getDataInt(DATA_COUNT_LAUNCH_APP,0);
        int level = ciphering(StoredData.getDataInt(DATA_LEVEL,DEFAULT_LEVEL));
        if(level != -1) {
            if(countLaunches == 1 && level > 1) level = 1; // для защиты от взлома
            return level;
        } else level = 1;
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
        int currentLevel = ciphering(StoredData.getDataInt(DATA_LEVEL,DEFAULT_LEVEL));
        int incLevel = currentLevel+1;
        StoredData.saveData(DATA_LEVEL,ciphering(incLevel));
    }
    private static int ciphering(int lvl) {
        if(lvl < 8) return lvl;
        else {
            if(lvl <= 22) {
                switch (lvl) {
                    case 8: return 29035;
                    case 9: return 55861;
                    case 10: return 42700;
                    case 11: return 49763;
                    case 12: return 68929;
                    case 13: return 44069;
                    case 14: return 35325;
                    case 15: return 89922;
                    case 16: return 83228;
                    case 17: return 19406;
                    case 18: return 93124;
                    case 19: return 52191;
                    case 20: return 95281;
                    case 21: return 83956;
                    case 22: return 74629;
                }
            } else if(lvl > 9999) {
                switch (lvl) {
                    case 29035: return 8;
                    case 55861: return 9;
                    case 42700: return 10;
                    case 49763: return 11;
                    case 68929: return 12;
                    case 44069: return 13;
                    case 35325: return 14;
                    case 89922: return 15;
                    case 83228: return 16;
                    case 19406: return 17;
                    case 93124: return 18;
                    case 52191: return 19;
                    case 95281: return 20;
                    case 83956: return 21;
                    case 74629: return 22;
                }
            }
        }
        return -1;
    }
    public boolean isDone() {
        return isDone;
    }
    public void done(boolean done) {
        isDone = done;
    }
}
