package martian.mystery.view;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

import martian.mystery.R;
import martian.mystery.controller.Progress;
import martian.mystery.controller.RequestController;
import martian.mystery.controller.StoredData;
import martian.mystery.data.DataOfUser;
import martian.mystery.data.Player;
import martian.mystery.data.ResponseFromServer;

public class LogupActivity extends AppCompatActivity {

    private EditText etLogin;
    private Button btnLogup;

    private final int LOGIN_WRONG = -1;
    private final int LOGIN_EXIST = 0;
    private final int LOGIN_NOT_EXIST = 1;
    private final int BAD_WORDS = 2;
    private final int WRONG_SYMBOLS = 3;
    private final int SHORT_LOGIN = 4;
    private final int LONG_LOGIN = 5;
    private final int MANY_SPACE = 6;
    private final int LOGIN_IS_ACCESS = 7;

    private static final String TAG = "LogupActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.logup_activity);

        etLogin = findViewById(R.id.etLogin);
        btnLogup = findViewById(R.id.btnLogup);

        btnLogup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogupTask logupTask = new LogupTask();
                logupTask.execute(etLogin.getText().toString());
            }
        });
    }

    private class LogupTask extends AsyncTask<String,Void,Integer> {

        private String resultLogin;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            btnLogup.setClickable(false);
        }

        @Override
        protected Integer doInBackground(String... voids) {
            String login = voids[0].trim();
            int validateLogin = isValidLogin(login);

            if(validateLogin != LOGIN_IS_ACCESS) return validateLogin;
            if(loginIsExist(login)) return LOGIN_EXIST;
            else {
                resultLogin = login;
                return LOGIN_NOT_EXIST;
            }
        }

        @Override
        protected void onPostExecute(Integer res) {
            super.onPostExecute(res);
            if(res == LOGIN_EXIST) {
                Log.d(TAG, "onPostExecute: login exist");
            } else if(res == LOGIN_NOT_EXIST) {
                Log.d(TAG, "onPostExecute: login not exist");
                StoredData.saveData(Player.DATA_NAME_PLAYER,resultLogin);
            } else if(res == WRONG_SYMBOLS) {
                Log.d(TAG, "onPostExecute: wrong symbols");
            } else if(res == BAD_WORDS) {
                Log.d(TAG, "onPostExecute: fuck");
            } else if (res == SHORT_LOGIN) {
                Log.d(TAG, "onPostExecute: login is short");
            } else if(res == LONG_LOGIN) {

            } else if(res == MANY_SPACE) {

            }
            btnLogup.setClickable(true);
        }

        private int isValidLogin(String login) { // проверка логина на валидность
            if(login.length() < 4) return SHORT_LOGIN;
            if(login.length() > 15) return LONG_LOGIN;
            if(login.contains("хуй") ||
                    login.contains("пизда") ||
                    login.contains("fuck") ||
                    login.contains("член") ||
                    login.contains("пидор") ||
                    login.contains("пидр") ||
                    login.contains("pidor") ||
                    login.equals("соси") ||
                    login.equals("sosi") ||
                    login.contains("pizda") ||
                    login.contains("hui") ||
                    login.contains("pizdec") ||
                    login.contains("pidr")) {
                Log.d(TAG, "isValidLogin: bad words");
                return BAD_WORDS;
            }
            if(login.indexOf(' ') != login.lastIndexOf(' ')) return MANY_SPACE; // если больше одного пробела
            if(login.matches("[A-Za-z_0-9а-яА-Я?\\s]+")) return LOGIN_IS_ACCESS;
            else return WRONG_SYMBOLS;
        }
        private String encryptLogin(String login) { // шифруем логин перед отправкой
            return "hik;" + login + ";9gl";
        }
        private boolean loginIsExist(String login) { // проверка логина на занятость
            DataOfUser dataOfUser = new DataOfUser();
            dataOfUser.setNameOfUser(encryptLogin(login));
            dataOfUser.setLevel(Progress.getInstance().getLevel());
            try {
                ResponseFromServer response = RequestController.getInstance()
                        .getJsonApi()
                        .logup(dataOfUser)
                        .execute().body();
                if(response.getResult() == LOGIN_NOT_EXIST) return false;
                else if(response.getResult() == LOGIN_EXIST) return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }
    }
}
