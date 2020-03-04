package martian.mystery.view;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.drawable.TransitionDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

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

    private TextView tvInfo;
    private ImageView imgAnimation;
    private EditText etLogin;
    private Button btnLogup;
    private TextView tvError;

    private ObjectAnimator errorAnimatorShow = ObjectAnimator.ofFloat(tvError, "alpha", 1.0f,0.0f);
    private ObjectAnimator errorAnimatorHide = ObjectAnimator.ofFloat(tvError, "alpha", 0.0f,1.0f);

    private final int MANY_LOGUP_IP = -2;
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

        tvInfo = findViewById(R.id.tvTopWord);
        imgAnimation = findViewById(R.id.imgLogupAnimation);
        etLogin = findViewById(R.id.etLogin);
        btnLogup = findViewById(R.id.btnLogup);
        tvError = findViewById(R.id.tvInvalidName);

        btnLogup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!etLogin.getText().toString().equals("")) {
                    LogupTask logupTask = new LogupTask();
                    logupTask.execute(etLogin.getText().toString());
                }
            }
        });
        if(StoredData.getDataInt(StoredData.DATA_COUNT_LAUNCH_APP,0) > 1) {
            tvInfo.setText(R.string.logup_info);
            btnLogup.setText(R.string.continue_game);
        }
        etLogin.setOnFocusChangeListener(onFocusChangeListener);
    }

    private void animateError(int errorString, int duration) {
        if(errorAnimatorHide.isStarted()) {
            errorAnimatorShow.cancel();
            errorAnimatorHide.cancel();

            tvError.setText(errorString);
            errorAnimatorHide = ObjectAnimator.ofFloat(tvError, "alpha", 1.0f,0.0f);
            errorAnimatorShow = ObjectAnimator.ofFloat(tvError, "alpha", 0.0f,1.0f);
            errorAnimatorHide.setDuration(800);
            errorAnimatorHide.setStartDelay(duration);
            errorAnimatorShow.setDuration(800);
            errorAnimatorShow.start();
            errorAnimatorHide.start();
            return;
        } else {
            tvError.setText(errorString);
            errorAnimatorHide = ObjectAnimator.ofFloat(tvError, "alpha", 1.0f,0.0f);
            errorAnimatorShow = ObjectAnimator.ofFloat(tvError, "alpha", 0.0f,1.0f);
            errorAnimatorHide.setDuration(800);
            errorAnimatorHide.setStartDelay(duration);
            errorAnimatorShow.setDuration(800);
            errorAnimatorShow.start();
            errorAnimatorHide.start();
            return;
        }
    }

    View.OnFocusChangeListener onFocusChangeListener = new View.OnFocusChangeListener() {

        private boolean wasShow = false;
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if(hasFocus) {
                if(!wasShow) { // если warning еще не был показан
                    animateError(R.string.logup_warning,7000);
                }
                TransitionDrawable transitionDrawable = (TransitionDrawable) imgAnimation.getDrawable();
                transitionDrawable.startTransition(1200);
            }
        }
    };

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
            else {
                resultLogin = login;
                return loginIsExist(login);
            }

        }

        @Override
        protected void onPostExecute(Integer res) {
            super.onPostExecute(res);
            if(res == LOGIN_EXIST) {
                animateError(R.string.login_exist,6000);
            } else if(res == LOGIN_NOT_EXIST) {
                StoredData.saveData(Player.DATA_NAME_PLAYER,resultLogin);
                Player.getInstance().setName(resultLogin);
                startActivity(new Intent(LogupActivity.this,MainActivity.class));
                finish();
            } else if(res == WRONG_SYMBOLS) {
                animateError(R.string.wrong_symbols,5000);
            } else if(res == BAD_WORDS) {
                animateError(R.string.bad_words,6000);
            } else if (res == SHORT_LOGIN) {
                animateError(R.string.invalid_long_name,5000);
            } else if(res == LONG_LOGIN) {
                animateError(R.string.invalid_long_name,5000);
            } else if(res == MANY_SPACE) {
                animateError(R.string.many_scapes,5000);
            } else if(res == MANY_LOGUP_IP) {
                animateError(R.string.many_ip_logup,4000);
            }
            btnLogup.setClickable(true);
        }

        private int isValidLogin(String login) { // проверка логина на валидность
            if(login.length() < 4) return SHORT_LOGIN;
            if(login.length() > 15) return LONG_LOGIN;
            if(login.toLowerCase().contains("хуй") ||
                    login.toLowerCase().contains("пизда") ||
                    login.toLowerCase().contains("fuck") ||
                    login.toLowerCase().contains("член") ||
                    login.toLowerCase().contains("пидор") ||
                    login.toLowerCase().contains("пидр") ||
                    login.toLowerCase().contains("pidor") ||
                    login.toLowerCase().equals("соси") ||
                    login.toLowerCase().equals("sosi") ||
                    login.toLowerCase().contains("pizda") ||
                    login.toLowerCase().contains("hui") ||
                    login.toLowerCase().contains("pizdec") ||
                    login.toLowerCase().contains("pidr")) {
                return BAD_WORDS;
            }
            if(login.indexOf(' ') != login.lastIndexOf(' ')) return MANY_SPACE; // если больше одного пробела
            if(login.matches("[A-Za-z_0-9а-яА-Я?\\s]+")) return LOGIN_IS_ACCESS;
            else return WRONG_SYMBOLS;
        }
        private String encryptLogin(String login) { // шифруем логин перед отправкой
            return "hik;" + login + ";9gl";
        }
        private int loginIsExist(String login) { // проверка логина на занятость
            DataOfUser dataOfUser = new DataOfUser();
            dataOfUser.setNameOfUser(encryptLogin(login));
            dataOfUser.setLevel(Progress.getInstance().getLevel());
            if(Build.VERSION.SDK_INT <= 28) {
                dataOfUser.setDeviceId(Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));
            }
            try {
                ResponseFromServer response = RequestController.getInstance()
                        .getJsonApi()
                        .logup(dataOfUser)
                        .execute().body();
                if(response.getResult() == LOGIN_NOT_EXIST) return LOGIN_NOT_EXIST;
                else if(response.getResult() == LOGIN_EXIST) return LOGIN_EXIST;
                else if(response.getResult() == MANY_LOGUP_IP) return MANY_LOGUP_IP;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return 0;
        }
    }
}
