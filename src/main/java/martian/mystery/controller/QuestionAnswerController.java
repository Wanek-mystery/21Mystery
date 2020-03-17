package martian.mystery.controller;


import java.io.IOException;
import java.util.Locale;

import martian.mystery.R;
import martian.mystery.data.DataOfUser;
import martian.mystery.data.Player;
import martian.mystery.data.ResponseFromServer;
import martian.mystery.exceptions.ErrorOnServerException;
import martian.mystery.exceptions.NoInternetException;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuestionAnswerController {

    public static final String DATA_CURRENT_RIDDLE = "current_riddle";
    public static final String DATA_NEXT_RIDDLE = "next_riddle";
    public static final String EMPTY_RIDDLE = "empty_riddle";
    public static final String ERROR_LOAD_RIDDLE = "error_riddle";

    public boolean checkAnswer(String answer) throws NoInternetException, ErrorOnServerException, IOException {
        answer = answer.trim().toLowerCase();
        int currentLevel = Progress.getInstance().getLevel();
        if (currentLevel < 15) {
            String[] keysAnswers = getAnswers();
            for (int i = 0; i < keysAnswers.length; i++) {
                if (answer.equals(keysAnswers[i])) {
                    if(currentLevel == 14) {
                        // меняем местами текущую загадку и следующую
                        StoredData.saveData(DATA_CURRENT_RIDDLE,StoredData.getDataString(DATA_NEXT_RIDDLE,EMPTY_RIDDLE));
                        StoredData.saveData(DATA_NEXT_RIDDLE,EMPTY_RIDDLE);
                    }
                    return true;
                }
            }
        } else if (currentLevel <= 21) {
            if (RequestController.hasConnection(GetContextClass.getContext())) {
                boolean isRight;
                DataOfUser dataOfUser = new DataOfUser();
                dataOfUser.setAnswer(answer);
                dataOfUser.setLevel(currentLevel);
                ResponseFromServer response = RequestController
                        .getInstance()
                        .getJsonApi()
                        .checkAnswer(dataOfUser)
                        .execute().body();
                isRight = (response.getResult() == 1);
                if (response.getResult() == 2) throw new ErrorOnServerException();
                if(isRight) {
                    if(currentLevel < 21) {
                        // меняем местами текущую загадку и следующую
                        StoredData.saveData(DATA_CURRENT_RIDDLE,StoredData.getDataString(DATA_NEXT_RIDDLE,EMPTY_RIDDLE));
                        StoredData.saveData(DATA_NEXT_RIDDLE,EMPTY_RIDDLE);
                    }
                }
                return isRight;
            } else throw new NoInternetException();
        }
        return false;
    }

    public String getQuestion() {

        int level = Player.getInstance().getLevel();
        String riddle = "Riddle";
        switch (level) {
            case 1:
                riddle = GetContextClass.getContext().getResources().getString(R.string.qst1);
                break;
            case 2:
                riddle = GetContextClass.getContext().getResources().getString(R.string.qst2);
                break;
            case 3:
                riddle = GetContextClass.getContext().getResources().getString(R.string.qst3);
                break;
            case 4:
                riddle = GetContextClass.getContext().getResources().getString(R.string.qst4);
                break;
            case 5:
                riddle = GetContextClass.getContext().getResources().getString(R.string.qst5);
                break;
            case 6:
                riddle = GetContextClass.getContext().getResources().getString(R.string.qst6);
                break;
            case 7:
                riddle = GetContextClass.getContext().getResources().getString(R.string.qst7);
                break;
            case 8:
                riddle = GetContextClass.getContext().getResources().getString(R.string.qst8);
                break;
            case 9:
                riddle = GetContextClass.getContext().getResources().getString(R.string.qst9);
                break;
            case 10:
                riddle = GetContextClass.getContext().getResources().getString(R.string.qst10);
                break;
            case 11:
                riddle = GetContextClass.getContext().getResources().getString(R.string.qst11);
                break;
            case 12:
                riddle = GetContextClass.getContext().getResources().getString(R.string.qst12);
                break;
            case 13:
                riddle = GetContextClass.getContext().getResources().getString(R.string.qst13);
                break;
            case 14: {
                riddle = GetContextClass.getContext().getResources().getString(R.string.qst14);
                break;
            }
            default: {
                riddle = StoredData.getDataString(DATA_CURRENT_RIDDLE,EMPTY_RIDDLE);
                if(riddle.equals(EMPTY_RIDDLE) || riddle.equals(ERROR_LOAD_RIDDLE)) {
                    riddle = GetContextClass.getContext().getString(R.string.load_riddle_error);
                    try {
                        loadRiddle();
                    } catch (NoInternetException ex) {}
                }
                break;
            }
        }

        // подгружаем следующую загадку
        if(level > 13 && level != 21) {
            if(StoredData.getDataString(DATA_NEXT_RIDDLE,EMPTY_RIDDLE).equals(EMPTY_RIDDLE)) { // если следующая загадка не загружена
                try {
                    loadNextRiddle();
                } catch (NoInternetException ex) {

                }
            }
        }

        return riddle;
    }

    public void loadRiddle() throws NoInternetException{
        int level = Player.getInstance().getLevel();
        downloadRiddle(level,false);
    }
    public void loadNextRiddle() throws NoInternetException{
        // загружаем следующуй загадку заранее
        int level = Player.getInstance().getLevel();
        downloadRiddle(++level,true);
    }

    private void downloadRiddle(int level, final boolean nextRiddle) throws NoInternetException{
        if(RequestController.hasConnection(GetContextClass.getContext())) {
            RequestController.getInstance()
                    .getJsonApi()
                    .getRiddle("ok", level)
                    .enqueue(new Callback<ResponseFromServer>() {
                        @Override
                        public void onResponse(Call<ResponseFromServer> call, Response<ResponseFromServer> response) {
                            String riddle = response.body().getRiddle();
                            String locale = Locale.getDefault().getLanguage();
                            if(locale.equals("ru") ||
                                    locale.equals("be") ||
                                    locale.equals("uk") ||
                                    locale.equals("kk")) {
                                riddle = riddle.split(";")[0];
                            } else riddle = riddle.split(";")[1];
                            if(nextRiddle) StoredData.saveData(DATA_NEXT_RIDDLE, riddle);
                            else StoredData.saveData(DATA_CURRENT_RIDDLE, riddle);
                        }

                        @Override
                        public void onFailure(Call<ResponseFromServer> call, Throwable t) {
                            if(nextRiddle) StoredData.saveData(DATA_NEXT_RIDDLE, ERROR_LOAD_RIDDLE);
                            else StoredData.saveData(DATA_CURRENT_RIDDLE, ERROR_LOAD_RIDDLE);
                        }
                    });
        } else throw new NoInternetException();
    }

    private String[] getAnswers() {
        SecurityController securityController = new SecurityController();
        String[] answers = null;
        answers = securityController.getAnswer(Progress.getInstance().getLevel());
        return answers;
    }
}
