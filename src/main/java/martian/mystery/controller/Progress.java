package martian.mystery.controller;


import static martian.mystery.controller.StoredData.DATA_LEVEL;

public class Progress { // класс синглтон для управления уровнем(прогрессом)
    private int level;
    private boolean isDone = false;

    private static final Progress instanceProgress = new Progress();

    public static Progress getInstance() {
        return instanceProgress;
    }

    private Progress() { this.level = getLevelFromStorage(); }
    private static int getLevelFromStorage() { // получение уровня игрока из какой-нибудь базы данных
        return StoredData.getDataInt(DATA_LEVEL,1
        );
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
