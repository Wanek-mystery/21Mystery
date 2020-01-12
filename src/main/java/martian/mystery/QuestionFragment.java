package martian.mystery;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.fragment.app.Fragment;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuestionFragment extends Fragment implements RewardedVideoAdListener {

    public RewardedVideoAd mRewardedVideoAd;

    private TextView tvQuestion;
    private TextView tvTopLvl;
    private TextView tvBottomLvl;
    private EditText etAnswer;
    private Button btnNext;
    private Button btnCheckAnswer;
    private ImageView imgLeft;
    private ImageView imgRight;
    private ImageView imgBottom;
    private ImageView imgBackToMain;
    private MotionLayout mlLevel;

    private QuestionAnswerController questionAnswerController = new QuestionAnswerController();
    private StatisticsController statisticsController;
    private Handler handler;
    private ShowAdThread showAdThread;

    private final int ALPHA_DOWN = 1;
    private final int ALPHA_UP = 2;
    private final int LOAD_AD = 3;
    private final int SET_RED_ET = 4;
    private final int SET_GREEN_ET = 5;
    private final int SET_NORMAL = 6;
    private final int ALPHA_DOWN_BTNNEXT = 7;
    private final int SET_INVISIBLE_BTNNEXT = 8;
    private final int TIME_YES = 9;
    private final int CHECK_LOAD_AD = 11;
    private final int SHOW_AD = 12;

    private int countErrorLoadAd = 0;

    private boolean adLoaded = false;
    private boolean adShow = false;
    private boolean adFailed = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.question_fragment,container,false);

        tvQuestion = view.findViewById(R.id.tvQuestion);
        tvTopLvl = view.findViewById(R.id.tvTop);
        tvBottomLvl = view.findViewById(R.id.tvBottom);
        etAnswer = view.findViewById(R.id.etAnswer);
        btnNext = view.findViewById(R.id.btnNextQuestion);
        btnCheckAnswer = view.findViewById(R.id.btnCheckAnswer);
        imgLeft = view.findViewById(R.id.imgLeft);
        imgRight = view.findViewById(R.id.imgRight);
        imgBottom = view.findViewById(R.id.imgWithLine);
        imgBackToMain = view.findViewById(R.id.imgBackToMain);
        mlLevel = view.findViewById(R.id.mlLevel);

        btnNext.setOnClickListener(onClickListener);
        btnCheckAnswer.setOnClickListener(onClickListener);
        imgBackToMain.setOnClickListener(onClickListener);
        etAnswer.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) {
                    editTextAnimation();
                }
            }
        });
        animationBtnNext(false);

        // если юзер разгадал все, но не проверил является ли он победителем
        if(!StoredData.getDataBool(StoredData.DATA_WINNER_IS_CHECKED) && (Progress.getInstance().getLevel() < 22)) {
            tvQuestion.setText(questionAnswerController.getQuestion());
            tvTopLvl.setText(String.valueOf(Progress.getInstance().getLevel()+1));
            tvBottomLvl.setText(String.valueOf(Progress.getInstance().getLevel()));
        } else if(!StoredData.getDataBool(StoredData.DATA_WINNER_IS_CHECKED) && (Progress.getInstance().getLevel() == 22)) {
            tvQuestion.setText("");
            tvBottomLvl.setText(String.valueOf(Progress.getInstance().getLevel()-1));
            // просьба подкоючиться к интернету
        }
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MobileAds.initialize(getActivity(), "ca-app-pub-3637770884242866~3613287665");
        // Use an activity context to get the rewarded video instance.
        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(getActivity());
        mRewardedVideoAd.setRewardedVideoAdListener(this);

        handler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);

                switch (msg.what) {
                    case ALPHA_DOWN: tvQuestion.animate().alpha(0).setDuration(1000); break;
                    case ALPHA_UP: tvQuestion.animate().alpha(1).setDuration(1000); break;
                    case LOAD_AD: {
                        loadRewardedVideoAd();
                        break;
                    }
                    case CHECK_LOAD_AD: {
                        adLoaded = mRewardedVideoAd.isLoaded();
                        break;
                    }
                    case SHOW_AD: {
                        mRewardedVideoAd.show();
                        break;
                    }
                    case TIME_YES: {
                        tvQuestion.setText(questionAnswerController.getQuestion());
                        break;
                    }
                    case SET_RED_ET: {
                        imgBottom.setImageResource(R.drawable.bottom_img_wrong);
                        break;
                    }
                    case SET_GREEN_ET: {
                        imgBottom.setImageResource(R.drawable.bottom_img_right);
                        break;
                    }
                    case SET_NORMAL: {
                        imgBottom.setImageResource(R.drawable.bottom_img);
                        break;
                    }
                    case ALPHA_DOWN_BTNNEXT: {
                        btnNext.animate().alpha(0).setDuration(400);
                        break;
                    }
                    case SET_INVISIBLE_BTNNEXT: {
                        btnNext.setVisibility(View.INVISIBLE);
                        break;
                    }
                }
            }
        };

        statisticsController = new StatisticsController();

    }
    @Override
    public void onResume() {
        super.onResume();
        //mRewardedVideoAd.resume(getActivity());
        if(!mRewardedVideoAd.isLoaded()) loadRewardedVideoAd();
        int countAttempts = StoredData.getCountAttempts();
        if(countAttempts == 0) {
            btnCheckAnswer.setMaxLines(2);
            btnCheckAnswer.setText(R.string.look_ad);
        }
        else if(countAttempts <= 3) {
            btnCheckAnswer.setMaxLines(1);
            btnCheckAnswer.setText(R.string.check_answer);
        }
        etAnswer.setHint(getResources().getString(R.string.attempts) + " " + countAttempts);
        if(Progress.getInstance().getLevel() == 22) {
            etAnswer.setText(StoredData.getDataString(StoredData.DATA_LAST_ANSWER,""));
        }
        SecurityController security = new SecurityController();
        adFailed = security.getQuestion(21);
    }
    @Override
    public void onPause() {
        super.onPause();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void changeQuestion() {
        // анимация вопроса
        new Thread(new Runnable() {
            @Override
            public void run() {
                handler.sendEmptyMessage(ALPHA_DOWN);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                handler.sendEmptyMessage(SET_NORMAL);
                handler.sendEmptyMessage(TIME_YES);
                handler.sendEmptyMessage(ALPHA_UP);
            }
        }).start();

        etAnswer.setText("");
        changeLevelTop();
        if(Progress.getInstance().getLevel() == 21) {
            AssistentDialog assistentDialog = new AssistentDialog(AssistentDialog.DIALOG_ALERT_LAST_LVL);
            assistentDialog.show(getActivity().getSupportFragmentManager(),"ALERT_LAST_LVL");
        }
    }
    private void changeLevelTop() {
        mlLevel.setTransitionListener(new MotionLayout.TransitionListener() {
            @Override
            public void onTransitionStarted(MotionLayout motionLayout, int i, int i1) {

            }

            @Override
            public void onTransitionChange(MotionLayout motionLayout, int i, int i1, float v) {

            }

            @Override
            public void onTransitionCompleted(MotionLayout motionLayout, int i) {
                if(i == R.id.end) {
                    tvBottomLvl.setText(String.valueOf(Progress.getInstance().getLevel()));
                    motionLayout.setProgress(0f);
                    motionLayout.setTransition(R.id.start, R.id.end);
                }
            }

            @Override
            public void onTransitionTrigger(MotionLayout motionLayout, int i, boolean b, float v) {

            }

        });
        tvTopLvl.setText(String.valueOf(Progress.getInstance().getLevel()));
        mlLevel.transitionToEnd();
    }
    private void animationBtnNext(boolean appear) { // анимация появлеия кнопки "дальше"
        ObjectAnimator animatorBtnNextX;
        ObjectAnimator animatorBtnNextY;
        if(appear) {
            btnNext.setVisibility(View.VISIBLE);
            btnNext.setClickable(true);
            btnNext.setAlpha(1.0f);
            animatorBtnNextX = ObjectAnimator.ofFloat(btnNext,"scaleX",1.0f,1.1f,1.0f);
            animatorBtnNextY = ObjectAnimator.ofFloat(btnNext,"scaleY",1.0f,1.1f,1.0f);
            animatorBtnNextX.setDuration(300);
            animatorBtnNextY.setDuration(300);
            animatorBtnNextX.start();
        } else {
            btnNext.setClickable(false);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    handler.sendEmptyMessage(ALPHA_DOWN_BTNNEXT);
                    try {
                        TimeUnit.MILLISECONDS.sleep(400);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    handler.sendEmptyMessage(SET_INVISIBLE_BTNNEXT);
                }
            }).start();
        }
    }
    private float getWidth() {
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.x; // ширина экрана
    }
    private float dpToPx(float dp){
        return dp * ((float) GetContextClass.getContext().getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    private void editTextAnimation() {
        imgRight.animate().translationX((getWidth()-dpToPx(48))/2).setDuration(3000);
        imgLeft.animate().translationX(-(getWidth()-dpToPx(48))/2).setDuration(3000);
    }
    private void getAttemptByAd() { // показать рекламу, чтобы добавить попытку
        if(mRewardedVideoAd.isLoaded()) {
            mRewardedVideoAd.show();
        } else {
            if(showAdThread == null || !showAdThread.isAlive()) {
                showAdThread = new ShowAdThread();
                showAdThread.start();
            }
        }
    }
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btnNextQuestion: {
                    if(!(Progress.getInstance().getLevel() >= 22)) {
                        animationBtnNext(false);
                        changeQuestion();
                    }
                    break;
                }
                case R.id.btnCheckAnswer: {
                    if(adFailed) {
                        if(StoredData.getCountAttempts() == 0) {
                            getAttemptByAd();
                        } else {
                            CheckTask checkTask = new CheckTask();
                            checkTask.execute();
                        }
                    }
                    break;
                }
                case R.id.imgBackToMain:
                    // при возвращении на главную активити отправляем разницу между уровнем, когда юзер был на главном экране, и уровнем на данный момент
                    // это нужно для анимации изменения уровня на главной активити
                    int pastLevel = 1;//getActivity().getIntent().getIntExtra("level",1);
                    Intent intentMain = new Intent();
                    //intentMain.putExtra("difflevel",Progress.getInstance().getLevel() - pastLevel);
                    try {
                        getActivity().setResult(Activity.RESULT_OK,intentMain);
                        getActivity().finish();
                    } catch (NullPointerException ex) {
                    }
                    break;
            }
        }
    };

    private void loadRewardedVideoAd() {
        mRewardedVideoAd.loadAd(getResources().getString(R.string.ad_block),
                new AdRequest.Builder().build());
    }
    @Override
    public void onRewarded(RewardItem reward) {
        Toast.makeText(getActivity(), R.string.attempt_is_added, Toast.LENGTH_SHORT).show();
        StoredData.incrementCountAtempts();
    }

    @Override
    public void onRewardedVideoAdLeftApplication() {
    }

    @Override
    public void onRewardedVideoAdClosed() {
        loadRewardedVideoAd();
        //Toast.makeText(GetContextClass.getContext(), "Closed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int errorCode) {
        if(errorCode == 2 && (countErrorLoadAd %8 == 0)) {
            Toast.makeText(GetContextClass.getContext(), R.string.internet_error, Toast.LENGTH_SHORT).show();
        } else if(countErrorLoadAd %8 == 0) {
            Toast.makeText(GetContextClass.getContext(), R.string.error_download_ad, Toast.LENGTH_SHORT).show();
        }
        countErrorLoadAd++;
        LoadAdAfterFail loadAdAfterFail = new LoadAdAfterFail();
        loadAdAfterFail.execute();
    }

    @Override
    public void onRewardedVideoAdLoaded() {
        adLoaded = true;
        countErrorLoadAd = 0;
        //Toast.makeText(GetContextClass.getContext(), "Реклама загружена", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardedVideoAdOpened() {
    }

    @Override
    public void onRewardedVideoStarted() {
        //Toast.makeText(GetContextClass.getContext(),"Start ad",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardedVideoCompleted() {
        adShow = false;
    }

    private class LoadAdAfterFail extends AsyncTask<Void,Void,Void> { // Task для загрузки рекламы в случае ошибки

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                TimeUnit.SECONDS.sleep(2);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            loadRewardedVideoAd();
        }
    }
    private class ShowAdThread extends Thread {

        @Override
        public void run() {
            for(int i = 0; i < 29; i++) {
                handler.sendEmptyMessage(CHECK_LOAD_AD);
                try {
                    TimeUnit.MILLISECONDS.sleep(78);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(adLoaded) {
                    handler.sendEmptyMessage(SHOW_AD);
                    break;
                }
                else {
                    try {
                        TimeUnit.MILLISECONDS.sleep(250);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    private class CheckTask extends AsyncTask<Void, Void, Boolean> {

        boolean nextLvlIsLast = false;
        boolean answerIsRight = false;
        @Override
        protected void onPreExecute() {
            String answerOfUser = etAnswer.getText().toString();
            if(!(answerOfUser.equals(""))) {
                if(questionAnswerController.checkAnswer(answerOfUser)) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() { // поток для изменения цвета обводки ответа на неправильный
                            handler.sendEmptyMessage(SET_GREEN_ET);
                            try {
                                TimeUnit.SECONDS.sleep(3);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            handler.sendEmptyMessage(SET_NORMAL);
                        }
                    }).start();
                    answerIsRight = true;
                    if(Progress.getInstance().getLevel() <= 20) {
                        animationBtnNext(true);
                        if(Progress.getInstance().getLevel() == 20) nextLvlIsLast = true;
                        Progress.getInstance().levelUp(); // повышвем уровень
                        statisticsController.setStartTime();
                    } else if(Progress.getInstance().getLevel() == 21) {
                        animationBtnNext(false);
                    }
                    sendStatistic();
                    StoredData.saveData(StoredData.DATA_LAST_ANSWER,answerOfUser);
                    StoredData.saveData(StoredData.DATA_COUNT_ATTEMPTS,3);

                } else { // если ответ неверный, увеличваем попытки
                    answerIsRight = false;
                    new Thread(new Runnable() {
                        @Override
                        public void run() { // поток для изменения цвета обводки ответа на неправильный
                            handler.sendEmptyMessage(SET_RED_ET);
                            try {
                                TimeUnit.SECONDS.sleep(3);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            handler.sendEmptyMessage(SET_NORMAL);
                        }
                    }).start();
                    int countAttempts = StoredData.getCountAttempts();
                    if(countAttempts > 0) {
                        StoredData.decrementCountAtempts();
                    } else if(countAttempts > 3) {
                        StoredData.saveData(StoredData.DATA_COUNT_ATTEMPTS,0);
                    }
                }
            }
        }

        @Override
        protected Boolean doInBackground(Void... voids) {

            if(Progress.getInstance().getLevel() == 21 && !nextLvlIsLast && answerIsRight) {
                if(RequestController.hasConnection(GetContextClass.getContext())) {
                    try {
                        ResponseFromServer response = RequestController.getInstance()
                                .getJsonApi()
                                .getMainData("true3")
                                .execute().body();
                        if(response.getExistWinner() == 0) {
                            StoredData.saveData(StoredData.DATA_IS_WINNER,true);
                        } else {
                            StoredData.saveData(StoredData.DATA_IS_WINNER,false);
                        }
                        response = RequestController.getInstance()
                                .getJsonApi()
                                .sendWinner("isa3") // отпрвляем данные о том, что победитель есть
                                .execute().body();
                        if(response.getResult() == 1) {
                            UpdateDataController.getInstance().setWinnerChecked(true);
                            Progress.getInstance().levelUp();
                            Progress.getInstance().done(true);
                            StoredData.saveData(StoredData.DATA_PLACE,response.getPlace());
                            return true;
                        } else throw new IOException();
                    } catch (IOException e) {
                        AssistentDialog assistentDialog = new AssistentDialog(AssistentDialog.DIALOG_SERVER_ERROR);
                        assistentDialog.show(getActivity().getSupportFragmentManager(),"ALERT_SERVER");
                        return false;
                    }
                } else {
                    AssistentDialog assistentDialog = new AssistentDialog(AssistentDialog.DIALOG_ALERT_INTERNET);
                    assistentDialog.show(getActivity().getSupportFragmentManager(),"ALERT_INTERNET");
                    UpdateDataController.getInstance().setWinnerChecked(false);
                    return false;
                }
            } else return false;
        }

        @Override
        protected void onPostExecute(Boolean isChecked) {
            int countAttempts = StoredData.getCountAttempts();
            if(countAttempts > 0 && countAttempts <= 3) {
                etAnswer.setHint(getResources().getString(R.string.attempts) + " " + countAttempts);
                btnCheckAnswer.setMaxLines(1);
                btnCheckAnswer.setText(R.string.check_answer);
            } else if(countAttempts == 0) {
                etAnswer.setHint(getResources().getString(R.string.attempts) + " " + countAttempts);
                btnCheckAnswer.setMaxLines(2);
                btnCheckAnswer.setText(R.string.look_ad);
            }
            if(isChecked) { // если наличие победителя проверно
                if(StoredData.getDataBool(StoredData.DATA_IS_WINNER)) {
                    ((QuestionActivity) getActivity()).replaceFragment(DoneFirstFragment.class); // замена текущего фрагмента на фрагмент с концом игры для побеителя
                } else {
                    ((QuestionActivity) getActivity()).replaceFragment(DoneFragment.class); // замена текущего фрагмента на фрагмент с концом игры
                }
            } else if(!UpdateDataController.getInstance().winnerIsChecked()){
                // выводим сообщение подключиться к интернету и попробовать снова
            }
        }

        private void sendStatistic() {
            DataOfUser dataOfUser = new DataOfUser();
            dataOfUser.setLevel(Progress.getInstance().getLevel());
            dataOfUser.setLonglevel(statisticsController.getDifference());
            RequestController.getInstance()
                    .getJsonApi()
                    .sendStatistics(String.valueOf(Progress.getInstance().getLevel()))
                    .enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                        }
                    });
        }
    }
}

