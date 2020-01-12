package martian.mystery;

public class Progress { // класс синглтон для управления уровнем(прогрессом)
    private int level = 1;
    private boolean isDone = false;

    private static final Progress instanceProgress = new Progress();

    public static Progress getInstance() {
        return instanceProgress;
    }

    private Progress() { this.level = getLevelFromSystem(); }
    private static int getLevelFromSystem() { // получение уровня игрока из какой-нибудь базы данных
        return StoredData.getLevel();
    }
    public void levelUp() {
        level++;
        if(level == 22) isDone = true;
        if(level <= 22) {
            if(StoredData.incrementSaveLevel()) {}
        }
    }
    public int getLevel() {
        return level;
    }
    public boolean isDone() {
        return isDone;
    }
    public void done(boolean done) {
        isDone = done;
    }
}
