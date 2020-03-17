package martian.mystery.controller;

import martian.mystery.view.QuestionActivity;

import static martian.mystery.controller.QuestionAnswerController.DATA_CURRENT_RIDDLE;
import static martian.mystery.controller.QuestionAnswerController.DATA_NEXT_RIDDLE;
import static martian.mystery.controller.QuestionAnswerController.EMPTY_RIDDLE;
import static martian.mystery.controller.QuestionAnswerController.ERROR_LOAD_RIDDLE;

public class UpdateDataController {
    private static final UpdateDataController ourInstance = new UpdateDataController();

    private boolean isConnection = false;

    public static UpdateDataController getInstance() {
        return ourInstance;
    }

    private UpdateDataController() {
        isConnection = checkConnection();
    }

    private boolean checkConnection() {
        boolean isConnection = false;
        isConnection = RequestController
                .getInstance()
                .hasConnection(GetContextClass.getContext()); // check connection
        if(!isConnection) isConnection = RequestController
                .getInstance()
                .hasConnection(GetContextClass.getContext()); // if there is no connection, try again
        return isConnection;
    }
    public boolean winnerIsChecked() {
        return StoredData.getDataBool(StoredData.DATA_WINNER_IS_CHECKED);
    }
    public void setWinnerChecked(boolean check) {
        StoredData.saveData(StoredData.DATA_WINNER_IS_CHECKED,check);
    }
    public boolean nextRiddleIsLoaded() {
        String riddle = StoredData.getDataString(DATA_NEXT_RIDDLE,ERROR_LOAD_RIDDLE);
        if(riddle.equals(ERROR_LOAD_RIDDLE) || riddle.equals(EMPTY_RIDDLE)) {
            return false;
        } else return true;
    }
    public boolean riddleIsLoaded() {
        String riddle = StoredData.getDataString(DATA_CURRENT_RIDDLE,ERROR_LOAD_RIDDLE);
        if(riddle.equals(ERROR_LOAD_RIDDLE) || riddle.equals(EMPTY_RIDDLE)) {
            return false;
        } else return true;
    }
}
