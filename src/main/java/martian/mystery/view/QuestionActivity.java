package martian.mystery.view;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.motion.widget.MotionLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import martian.mystery.controller.GetContextClass;
import martian.mystery.controller.Progress;
import martian.mystery.R;
import martian.mystery.controller.QuestionAnswerController;
import martian.mystery.controller.RequestController;
import martian.mystery.controller.SecurityController;
import martian.mystery.controller.StatisticsController;
import martian.mystery.controller.StoredData;
import martian.mystery.controller.UpdateDataController;
import martian.mystery.data.ResponseFromServer;
import martian.mystery.exceptions.ErrorOnServerException;
import martian.mystery.exceptions.NoInternetException;

import static martian.mystery.controller.StoredData.DATA_COUNT_ATTEMPTS;
import static martian.mystery.controller.StoredData.DATA_IS_WINNER;


public class QuestionActivity extends AppCompatActivity implements RewardedVideoAdListener { // активити, где отображаются загадки


    public RewardedVideoAd mRewardedVideoAd;

    private TextView tvQuestion;
    private TextView tvTopLvl;
    private TextView tvBottomLvl;
    private TextView tvPartingWord;
    private EditText etAnswer;
    private Button btnNext;
    private Button btnCheckAnswer;
    private ProgressBar progressAdLoad;
    private ImageView imgLeft;
    private ImageView imgRight;
    private ImageView imgBottom;
    private ImageView imgGreenMark;
    private ImageView imgBackToMain;
    private MotionLayout mlMain;
    private MotionLayout mlLevel;

    private QuestionAnswerController questionAnswerController = new QuestionAnswerController();
    private StatisticsController statisticsController;
    private Handler handler;
    private ShowAdThread showAdThread;
    private AnimationController animationController;
    private AttemptsController attemptsController;
    private ProgressBarAdController progressBarAdController;
    private PartingWords partingWords;

    private final int ALPHA_DOWN = 1;
    private final int ALPHA_UP = 2;
    private final int LOAD_AD = 3;
    private final int SET_RED_ET = 4;
    private final int RIGHT_ANSWER_ANIMATION = 5;
    private final int SET_NORMAL = 6;
    private final int ALPHA_DOWN_BTNNEXT = 7;
    private final int SET_INVISIBLE_BTNNEXT = 8;
    private final int CHANGE_ANSWER = 9;
    private final int CHECK_LOAD_AD = 11;
    private final int SHOW_AD = 12;
    private final int TRANSITION_RESET = 13;
    private final int SHOW_PROGRESS = 14;
    private final int HIDE_PROGRESS = 15;
    private final int WRONG_ANSWER_ANIMATION = 16;
    private final int CHANGE_HINT_ANSWER = 17;
    private final int SHOW_PARTING_WORD = 18;


    private String adBlock;
    private boolean adLoaded = false;
    private boolean adFailed = false;

    private static final String TAG = "QuestionActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);

        MobileAds.initialize(this, "ca-app-pub-3637770884242866~3613287665");
        // Use an activity context to get the rewarded video instance.
        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this);
        mRewardedVideoAd.setRewardedVideoAdListener(this);
        adBlock = this.getResources().getString(R.string.ad_block);

        handler = new Handler() {

            ObjectAnimator animPartingWordShow = ObjectAnimator.ofFloat(tvPartingWord, "alpha", 0.0f,1.0f);
            ObjectAnimator animPartingWordHide = ObjectAnimator.ofFloat(tvPartingWord, "alpha", 1.0f,0.0f);
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);

                switch (msg.what) {
                    case ALPHA_DOWN:
                        tvQuestion.animate().alpha(0).setDuration(1000);
                        break;
                    case ALPHA_UP:
                        tvQuestion.animate().alpha(1).setDuration(1000);
                        break;
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
                    case CHANGE_ANSWER: {
                        tvQuestion.setText(questionAnswerController.getQuestion());
                        break;
                    }
                    case SET_RED_ET: {
                        imgBottom.setImageResource(R.drawable.bottom_img_wrong);
                        animationController.INPUT_STATE = 2;
                        break;
                    }
                    case TRANSITION_RESET: {
                        animationController.transitionInputReset();
                        break;
                    }
                    case SET_NORMAL: {
                        imgBottom.setImageDrawable(getDrawable(R.drawable.norm_to_right));
                        animationController.INPUT_STATE = 0;
                        break;
                    }
                    case RIGHT_ANSWER_ANIMATION: {
                        animationController.editTextRightAnswer();
                        animationController.animationBtnNext();
                        animationController.markAnimate();
                        btnCheckAnswer.setClickable(false);
                        break;
                    }
                    case WRONG_ANSWER_ANIMATION: {
                        animationController.editTextWrongAnswer();
                        etAnswer.setText("");
                        break;
                    }
                    case CHANGE_HINT_ANSWER: {
                        int countAttempts = attemptsController.getCountAttempts();
                        if (countAttempts > 0 && countAttempts <= 3) {
                            etAnswer.setHint(getResources().getString(R.string.attempts) + " " + countAttempts);
                            btnCheckAnswer.setMaxLines(1);
                            btnCheckAnswer.setText(R.string.check_answer);
                        } else if (countAttempts == 0) {
                            etAnswer.setHint(getResources().getString(R.string.attempts) + " " + countAttempts);
                            btnCheckAnswer.setMaxLines(2);
                            btnCheckAnswer.setText(R.string.look_ad);
                        }
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
                    case SHOW_PROGRESS: {
                        progressBarAdController.showProgress();
                        break;
                    }
                    case HIDE_PROGRESS: {
                        progressBarAdController.hideProgress();
                        break;
                    }
                    case SHOW_PARTING_WORD: {
                        String partingWord = partingWords.getRandomWord();
                        if(!partingWord.equals("")) {
                            if(!animPartingWordHide.isStarted()) {
                                tvPartingWord.setText(partingWord);
                                animPartingWordShow = ObjectAnimator.ofFloat(tvPartingWord, "alpha", 0.0f,1.0f);
                                animPartingWordHide = ObjectAnimator.ofFloat(tvPartingWord, "alpha", 1.0f,0.0f);
                                animPartingWordShow.setDuration(1000);
                                animPartingWordHide.setDuration(1000);
                                animPartingWordHide.setStartDelay(9000);
                                animPartingWordHide.start();
                                animPartingWordShow.start();
                            }
                        }
                        break;
                    }
                }
            }
        };

        tvQuestion = findViewById(R.id.tvQuestion);
        tvTopLvl = findViewById(R.id.tvTop);
        tvBottomLvl = findViewById(R.id.tvBottom);
        tvPartingWord = findViewById(R.id.tvPartingWords);
        imgGreenMark = findViewById(R.id.imgGreenMark);
        etAnswer = findViewById(R.id.etAnswer);
        btnNext = findViewById(R.id.btnNextQuestion);
        progressAdLoad = findViewById(R.id.progressLoadAd);
        btnCheckAnswer = findViewById(R.id.btnCheckAnswer);
        imgLeft = findViewById(R.id.imgLeft);
        imgRight = findViewById(R.id.imgRight);
        imgBottom = findViewById(R.id.imgWithLine);
        imgBackToMain = findViewById(R.id.imgBackToMain);
        mlMain = findViewById(R.id.mlMain);
        mlLevel = findViewById(R.id.mlLevel);

        btnNext.setOnClickListener(onClickListener);
        btnCheckAnswer.setOnClickListener(onClickListener);
        imgBackToMain.setOnClickListener(onClickListener);
        etAnswer.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    animationController.focusEditText();
                }
            }
        });

        progressBarAdController = new ProgressBarAdController();
        statisticsController = new StatisticsController();
        animationController = new AnimationController();
        attemptsController = new AttemptsController();
        partingWords = new PartingWords();

        // если юзер разгадал все, но не проверил является ли он победителем
        if (!StoredData.getDataBool(StoredData.DATA_WINNER_IS_CHECKED) && (Progress.getInstance().getLevel() < 22)) {
            tvQuestion.setText(questionAnswerController.getQuestion());
            tvTopLvl.setText(String.valueOf(Progress.getInstance().getLevel() + 1));
            tvBottomLvl.setText(String.valueOf(Progress.getInstance().getLevel()));
        } else if (!StoredData.getDataBool(StoredData.DATA_WINNER_IS_CHECKED) && (Progress.getInstance().getLevel() == 22)) {
            tvQuestion.setText("");
            tvBottomLvl.setText(String.valueOf(Progress.getInstance().getLevel() - 1));
            // просьба подкоючиться к интернету
        }
        setInputMode(); // если экран маленький, то макет поднимается при фокусе клавиатуры

    }

    private int getWidthSreeen() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.x;
    }

    public static float convertPixelsToDp(float px) {
        return px / ((float) GetContextClass.getContext().getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    private void setInputMode() {
        int widthScreen = (int) convertPixelsToDp(getWidthSreeen());
        if (widthScreen < 360) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!mRewardedVideoAd.isLoaded()) loadRewardedVideoAd();
        attemptsController.setAttemptsOnScreen();
        if (Progress.getInstance().getLevel() == 22) {
            etAnswer.setText(StoredData.getDataString(StoredData.DATA_LAST_ANSWER, ""));
        }
        SecurityController security = new SecurityController();
        adFailed = security.getQuestion(21); // проверка на взлом
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
                handler.sendEmptyMessage(CHANGE_ANSWER);
                handler.sendEmptyMessage(TRANSITION_RESET); // сбрасываем transition, чтобы запустить потом снова
                handler.sendEmptyMessage(ALPHA_UP);
            }
        }).start();

        animationController.changeQuestion();
        animationController.changeLevelTop();        btnCheckAnswer.setClickable(true);
       etAnswer.setText("");
        if(Progress.getInstance().getLevel() == 15) {
            AssistentDialog assistentDialog = new AssistentDialog(AssistentDialog.DIALOG_CHECK_ON_SERRVER_ALERT);
            assistentDialog.show(this.getSupportFragmentManager(), "ALERT_SERVERCHECK_LVL");
        } else if (Progress.getInstance().getLevel() == 21) {
            AssistentDialog assistentDialog = new AssistentDialog(AssistentDialog.DIALOG_ALERT_LAST_LVL);
            assistentDialog.show(this.getSupportFragmentManager(), "ALERT_LAST_LVL");
        }
    }

    private void loadRewardedVideoAd() {
        mRewardedVideoAd.loadAd(adBlock,
                new AdRequest.Builder().build());
        progressBarAdController.setCurrentState(1);
    }

    @Override
    public void onRewarded(RewardItem reward) {
        Toast.makeText(this, R.string.attempt_is_added, Toast.LENGTH_SHORT).show();
        attemptsController.incrementCountAtempts();
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
        if (errorCode == 2) {
            Toast.makeText(GetContextClass.getContext(), R.string.internet_error, Toast.LENGTH_SHORT).show();
        } else if (errorCode == 0) {
            Toast.makeText(GetContextClass.getContext(), R.string.error_download_ad, Toast.LENGTH_SHORT).show();
        }
        progressBarAdController.setCurrentState(2);
    }

    @Override
    public void onRewardedVideoAdLoaded() {
        adLoaded = true;
        progressBarAdController.setCurrentState(0);
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
    }


    // внутренние контроллеры и потоки -----------------------------------------------------------------------------
    private class ProgressBarAdController {

        public int currentState;

        public int getCurrentState() {
            return currentState;
        }

        public void setCurrentState(int currentState) {
            this.currentState = currentState;
        }

        public void showProgress() {
            progressAdLoad.setVisibility(View.VISIBLE);
        }

        public void hideProgress() {
            progressAdLoad.setVisibility(View.INVISIBLE);
        }
    }

    private class AttemptsController {

        private String DATA_WRONG_ANSWERS = "count_wrong_answers";
        private int countWrongAnswers;

        public AttemptsController() {
            countWrongAnswers = StoredData.getDataInt(DATA_WRONG_ANSWERS,0);
        }
        public void getAttemptByAd() { // показать рекламу, чтобы добавить попытку
            if (mRewardedVideoAd.isLoaded()) {
                mRewardedVideoAd.show();
            } else {
                if (showAdThread == null || !showAdThread.isAlive()) {
                    showAdThread = new ShowAdThread();
                    showAdThread.start();
                }
            }
        }

        public int getCountAttempts() {
            return StoredData.getDataInt(DATA_COUNT_ATTEMPTS, 3);
        }

        public void decrementCountAtempts() { // уменьшает кол-во попыток на 1 и сохраняет
            int countAttempts = StoredData.getDataInt(DATA_COUNT_ATTEMPTS, 3);
            if (countAttempts > 0) StoredData.saveData(DATA_COUNT_ATTEMPTS, countAttempts - 1);
        }

        public void incrementCountAtempts() { // уменьшает кол-во попыток на 1 и сохраняет
            int countAttempts = StoredData.getDataInt(DATA_COUNT_ATTEMPTS, 3);
            if (countAttempts < 9) StoredData.saveData(DATA_COUNT_ATTEMPTS, countAttempts + 1);
        }

        public void setAttemptsOnScreen() {
            int countAttempts = StoredData.getDataInt(DATA_COUNT_ATTEMPTS, 3);
            if (countAttempts == 0) {
                btnCheckAnswer.setMaxLines(2);
                btnCheckAnswer.setText(R.string.look_ad);
            } else if (countAttempts <= 3) {
                btnCheckAnswer.setMaxLines(1);
                btnCheckAnswer.setText(R.string.check_answer);
            }
            etAnswer.setHint(getResources().getString(R.string.attempts) + " " + countAttempts);
        }



        public int getCountWrongAnswers() { return countWrongAnswers; }

        public void increaseCountWrongAnswers() {
            StoredData.saveData(DATA_WRONG_ANSWERS,++countWrongAnswers);
            Log.d(TAG, "increaseCountWrongAnswers: count = " + countWrongAnswers);
        }

        public void resetCountWrongAnswers() {
            StoredData.saveData(DATA_WRONG_ANSWERS,0);
            countWrongAnswers = 0;
        }
    }

    private class AnimationController {

        /*переменная хранит состояние поля ввода ответа
        0 - обычное
        1 - зеленое
        2 - красное
        */
        public int INPUT_STATE = 0;

        private TransitionDrawable imgBottomDrawable;

        public AnimationController() {
            animationBtnNext(false); // делаем кнопку "дальше" невидимой при старте
            imgBottomDrawable = (TransitionDrawable) imgBottom.getDrawable();
            imgBottomDrawable.setCrossFadeEnabled(true);
        }

        private float getWidth() {
            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            return size.x; // ширина экрана
        }

        private float dpToPx(float dp) {
            return dp * ((float) GetContextClass.getContext().getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        }

        public void focusEditText() {
            imgRight.animate().translationX((getWidth() - dpToPx(48)) / 2).setDuration(3000);
            imgLeft.animate().translationX(-(getWidth() - dpToPx(48)) / 2).setDuration(3000);
        }

        private void changeQuestion() {
            imgBottomDrawable.reverseTransition(500);
            mlMain.transitionToStart();
        }

        private void markAnimate() {
            mlMain.transitionToEnd();
            Drawable drawable = imgGreenMark.getDrawable();
            if (drawable instanceof Animatable) {
                ((Animatable) drawable).start();
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
                    if (i == R.id.end) {
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
            if (appear) {
                btnNext.setVisibility(View.VISIBLE);
                btnNext.setClickable(true);
                btnNext.setAlpha(1.0f);
                animatorBtnNextX = ObjectAnimator.ofFloat(btnNext, "scaleX", 1.0f, 1.1f, 1.0f);
                animatorBtnNextY = ObjectAnimator.ofFloat(btnNext, "scaleY", 1.0f, 1.1f, 1.0f);
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

        private void animationBtnNext() { // анимация появлеия кнопки "дальше"
            ObjectAnimator animatorBtnNextX;
            ObjectAnimator animatorBtnNextY;
            if (Progress.getInstance().getLevel() <= 20) {
                btnNext.setVisibility(View.VISIBLE);
                btnNext.setClickable(true);
                btnNext.setAlpha(1.0f);
                animatorBtnNextX = ObjectAnimator.ofFloat(btnNext, "scaleX", 1.0f, 1.1f, 1.0f);
                animatorBtnNextY = ObjectAnimator.ofFloat(btnNext, "scaleY", 1.0f, 1.1f, 1.0f);
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

        private void transitionInputReset() {
            imgBottomDrawable.resetTransition();
        }

        private void editTextRightAnswer() {
            if (INPUT_STATE == 2) { // если сейчас красный ободок, то заменяем на другой
                imgBottom.setImageDrawable(getDrawable(R.drawable.norm_to_right));
            }
            imgBottomDrawable = (TransitionDrawable) imgBottom.getDrawable();
            imgBottomDrawable.startTransition(280);
            INPUT_STATE = 1;
        }

        private void editTextWrongAnswer() {
            new Thread(new Runnable() {
                @Override
                public void run() { // поток для изменения цвета обводки ответа на неправильный
                    handler.sendEmptyMessage(SET_RED_ET);
                    try {
                        TimeUnit.SECONDS.sleep(3);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (INPUT_STATE != 1) {
                        handler.sendEmptyMessage(SET_NORMAL);
                    }
                }
            }).start();
        }
    }

    private class PartingWords {

        private ArrayList<String> partingWords;
        private String DATA_LAST_PARTING_WORD = "last_parting_word"; // последнее напутсвеннное слово (для StoredData)

        PartingWords () {
            partingWords = new ArrayList<>();
            partingWords.add(getResources().getString(R.string.parting_word1));
            partingWords.add(getResources().getString(R.string.parting_word2));
            partingWords.add(getResources().getString(R.string.parting_word3));
            partingWords.add(getResources().getString(R.string.parting_word4));
            partingWords.add(getResources().getString(R.string.parting_word4));
            partingWords.add(getResources().getString(R.string.parting_word5));
            partingWords.add(getResources().getString(R.string.parting_word6));
            partingWords.add(getResources().getString(R.string.parting_word7));
            partingWords.add(getResources().getString(R.string.parting_word8));
            partingWords.add(getResources().getString(R.string.parting_word9));
            partingWords.add(getResources().getString(R.string.parting_word10));
            partingWords.add(getResources().getString(R.string.parting_word11));
            partingWords.add(getResources().getString(R.string.parting_word12));
            partingWords.add(getResources().getString(R.string.parting_word13));
            partingWords.add(getResources().getString(R.string.parting_word14));
            partingWords.add(getResources().getString(R.string.parting_word15));
            partingWords.add(getResources().getString(R.string.parting_word16));
            partingWords.add(getResources().getString(R.string.parting_word17));
            partingWords.add(getResources().getString(R.string.parting_word18));
        }

        String getRandomWord() {
            if(attemptsController.getCountWrongAnswers() > 7 && (getRandomInt(1,3) == 3)) {
                String lastWord = StoredData.getDataString(DATA_LAST_PARTING_WORD,"");
                int indexWord = getRandomInt(0,17);
                if(!partingWords.get(indexWord).equals(lastWord)) {
                    StoredData.saveData(DATA_LAST_PARTING_WORD,partingWords.get(indexWord));
                    return partingWords.get(indexWord);
                }
            }
            return "";
        }
        private int getRandomInt(int min, int max) {
            Random rnd = new Random(System.currentTimeMillis());
            return (min + rnd.nextInt(max - min + 1));
        }
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btnNextQuestion: {
                    if (!(Progress.getInstance().getLevel() >= 22)) {
                        animationController.animationBtnNext(false);
                        changeQuestion();
                    }
                    break;
                }
                case R.id.btnCheckAnswer: {
                    handler.sendEmptyMessage(SHOW_PARTING_WORD);
                    if (adFailed) { // если взлома ответов нет(adFailed == true), то предоставляем функции
                        if (attemptsController.getCountAttempts() == 0) {
                            attemptsController.getAttemptByAd();
                        } else {
                            CheckAnswerTask checkAnswerTask = new CheckAnswerTask();
                            checkAnswerTask.execute(etAnswer.getText().toString());
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
                        setResult(Activity.RESULT_OK, intentMain);
                        finish();
                    } catch (NullPointerException ex) {
                    }
                    break;
            }
        }
    };

    private class ShowAdThread extends Thread {

        @Override
        public void run() {
            handler.sendEmptyMessage(SHOW_PROGRESS);
            if (progressBarAdController.getCurrentState() != 1) {
                handler.sendEmptyMessage(LOAD_AD);
            }
            while (progressBarAdController.getCurrentState() != 2) {
                handler.sendEmptyMessage(CHECK_LOAD_AD);
                try {
                    TimeUnit.MILLISECONDS.sleep(78);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (adLoaded) {
                    handler.sendEmptyMessage(HIDE_PROGRESS);
                    handler.sendEmptyMessage(SHOW_AD);
                    break;
                } else {
                    try {
                        TimeUnit.MILLISECONDS.sleep(250);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            progressBarAdController.setCurrentState(0);
            handler.sendEmptyMessage(HIDE_PROGRESS);
        }
    }

    private class CheckAnswerTask extends AsyncTask<String, Void, Boolean> { // проверка ответа

        private boolean winnerIsChecked;

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Boolean doInBackground(String... answer) {
            String answerOfUser = answer[0];
            if (!(answerOfUser.equals(""))) {
                try {
                    if (questionAnswerController.checkAnswer(answerOfUser)) { // если ответ правильный
                        handler.sendEmptyMessage(RIGHT_ANSWER_ANIMATION);
                        statisticsController.sendStatistics(); // отправляем статистику на сервер
                        StoredData.saveData(DATA_COUNT_ATTEMPTS, 3);
                        attemptsController.resetCountWrongAnswers();
                        if (Progress.getInstance().getLevel() <= 20) {
                            Progress.getInstance().levelUp(); // повышвем уровень
                            statisticsController.setStartTimeLevel(); // устанавливаем время начала прохождения нового уровня
                        } else if (Progress.getInstance().getLevel() == 21) {
                            Progress.getInstance().done(true);
                            if (isWinner()) {
                                StoredData.saveData(DATA_IS_WINNER, true);
                                return true;
                            } else {
                                StoredData.saveData(DATA_IS_WINNER, false);
                                return false;
                            }
                        }
                    } else { // если ответ неверный, уменьшаем попытки
                        handler.sendEmptyMessage(WRONG_ANSWER_ANIMATION);
                        int countAttempts = attemptsController.getCountAttempts();
                        if (countAttempts > 0) {
                            attemptsController.decrementCountAtempts();
                        }
                        attemptsController.increaseCountWrongAnswers();
                    }
                } catch (NoInternetException ex) {
                    AssistentDialog assistentDialog = new AssistentDialog(AssistentDialog.DIALOG_ALERT_INTERNET);
                    assistentDialog.show(QuestionActivity.this.getSupportFragmentManager(),"ALERT_INTERNET");
                    UpdateDataController.getInstance().setWinnerChecked(false);
                } catch (ErrorOnServerException ex) {
                    Log.d(TAG, "doInBackground: error = " + ex.toString());
                    AssistentDialog assistentDialog = new AssistentDialog(AssistentDialog.DIALOG_SERVER_ERROR);
                    assistentDialog.show(QuestionActivity.this.getSupportFragmentManager(),"ALERT_SERVER");
                } catch (IOException e) {
                    Log.d(TAG, "doInBackground: error = " + e.toString());
                    AssistentDialog assistentDialog = new AssistentDialog(AssistentDialog.DIALOG_SERVER_ERROR);
                    assistentDialog.show(QuestionActivity.this.getSupportFragmentManager(),"ALERT_SERVER");
                }
                handler.sendEmptyMessage(CHANGE_HINT_ANSWER);
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean isWinner) {
            if (Progress.getInstance().isDone()) {
                if (winnerIsChecked) {
                    if (isWinner) {
                        finish();
                        startActivity(new Intent(QuestionActivity.this, DoneFirstActivity.class)); // замена текущего фрагмента на фрагмент с концом игры для побеителя
                    } else {
                        finish();
                        startActivity(new Intent(QuestionActivity.this, DoneActivity.class)); // замена текущего фрагмента на фрагмент с концом игры
                    }
                }
            }
        }

        private boolean isWinner() throws NoInternetException, ErrorOnServerException, IOException {
            boolean isWinner = false;
            if (RequestController.hasConnection(GetContextClass.getContext())) {
                ResponseFromServer response = RequestController.getInstance()
                        .getJsonApi()
                        .getMainData("money")
                        .execute().body();
                isWinner = (response.getExistWinner() == 0);
                response = RequestController.getInstance()
                        .getJsonApi()
                        .sendWinner("acdc") // отпрвляем данные о том, что победитель есть
                        .execute().body();
                if (response.getResult() == 1) {
                    UpdateDataController.getInstance().setWinnerChecked(true);
                    Progress.getInstance().levelUp();
                    StoredData.saveData(StoredData.DATA_PLACE, response.getPlace());
                    winnerIsChecked = true;
                } else throw new ErrorOnServerException();
            } else {
                throw new NoInternetException();
            }
            return isWinner;
        }

    }
}
