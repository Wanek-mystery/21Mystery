package martian.mystery.controller;

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
    public boolean nameIsSended() {
        return StoredData.getDataBool(StoredData.DATA_WINNER_IS_SENDED);
    }
    public void setNameIsSended(boolean isSend) {
        StoredData.saveData(StoredData.DATA_WINNER_IS_SENDED,isSend);
    }
}
