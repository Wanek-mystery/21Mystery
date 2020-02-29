package martian.mystery.data;

import martian.mystery.controller.Progress;
import martian.mystery.controller.StoredData;

public class Player {

    private String name;
    private int level;
    public static final String DATA_NAME_PLAYER = "name_player";
    private static final Player instancePlayer = new Player();


    public static Player getInstance() {
        return instancePlayer;
    }

    private Player() {
        name = StoredData.getDataString(DATA_NAME_PLAYER,"");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        StoredData.saveData(DATA_NAME_PLAYER,name);
        this.name = name;
    }

    public int getLevel() {
        return Progress.getInstance().getLevel();
    }

}
