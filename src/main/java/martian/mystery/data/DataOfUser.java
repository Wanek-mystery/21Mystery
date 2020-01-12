package martian.mystery.data;

public class DataOfUser {

    private String name; // имя победителя
    private int longlevel;
    private int newlevel;

    public int getLevel() {
        return newlevel;
    }

    public void setLevel(int level) {
        this.newlevel = level;
    }

    public int getLonglevel() {
        return longlevel;
    }

    public void setLonglevel(int longlevel) {
        this.longlevel = longlevel;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
