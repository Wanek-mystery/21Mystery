package martian.mystery.view;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.motion.widget.MotionLayout;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import martian.mystery.controller.AttemptsController;
import martian.mystery.controller.GetContextClass;
import martian.mystery.controller.Progress;
import martian.mystery.R;
import martian.mystery.controller.QuestionAnswerController;
import martian.mystery.controller.SecurityController;
import martian.mystery.controller.StatisticsController;
import martian.mystery.controller.StoredData;
import martian.mystery.controller.UpdateDataController;
import martian.mystery.data.Player;
import martian.mystery.exceptions.ErrorOnServerException;
import martian.mystery.exceptions.NoInternetException;

import static martian.mystery.controller.StoredData.DATA_COUNT_ATTEMPTS;
import static martian.mystery.controller.StoredData.DATA_IS_WINNER;
import static martian.mystery.controller.StoredData.DATA_PLACE;


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
    private MotionLayout mlBottom;
    private ImageView imgShowBuy;
    private Button btnBuy;

    private QuestionAnswerController questionAnswerController = new QuestionAnswerController();
    private StatisticsController statisticsController;
    private Handler handler;
    private ShowAdThread showAdThread;
    private AnimationController animationController;
    private AttemptsController attemptsController;
    private ProgressBarAdController progressBarAdController;
    private PurchaseController purchaseController;
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
    private final int SHOW_PURCHASE = 19;
    private final int HIDE_PURCHASE = 20;


    private String adBlock;
    private boolean adLoaded = false;
    private boolean adFailed = false;
    private boolean adShowed = false; // если реклама показалась, то можно показывать предложение о покупке

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

            ObjectAnimator animPartingWordShow = ObjectAnimator.ofFloat(tvPartingWord, "alpha", 0.0f, 1.0f);
            ObjectAnimator animPartingWordHide = ObjectAnimator.ofFloat(tvPartingWord, "alpha", 1.0f, 0.0f);

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
                        if (!attemptsController.isEndlessAttempts()) {
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
                        } else {
                            etAnswer.setHint("");
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
                    case SHOW_PURCHASE: {
                        animationController.showPurchase();
                        break;
                    }
                    case HIDE_PURCHASE: {
                        animationController.hidePurchase();
                        break;
                    }
                    case SHOW_PARTING_WORD: {
                        String partingWord = partingWords.getRandomWord();
                        if (!partingWord.equals("")) {
                            if (!animPartingWordHide.isStarted()) {
                                tvPartingWord.setText(partingWord);
                                animPartingWordShow = ObjectAnimator.ofFloat(tvPartingWord, "alpha", 0.0f, 1.0f);
                                animPartingWordHide = ObjectAnimator.ofFloat(tvPartingWord, "alpha", 1.0f, 0.0f);
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
        mlBottom = findViewById(R.id.mlCheckAndNext);
        imgShowBuy = findViewById(R.id.imgShowBuy);
        btnBuy = findViewById(R.id.btnBuy);
        btnBuy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                purchaseController.buy();
            }
        });

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
        statisticsController = new StatisticsController(this);
        purchaseController = new PurchaseController(this);
        animationController = new AnimationController();
        attemptsController = new AttemptsController(statisticsController);
        partingWords = new PartingWords();

        // если юзер разгадал все, но не проверил является ли он победителем
        if (!StoredData.getDataBool(StoredData.DATA_WINNER_IS_CHECKED) && (Progress.getInstance().getLevel() < 22)) {
            tvQuestion.setText(questionAnswerController.getQuestion());
            tvTopLvl.setText(String.valueOf(Progress.getInstance().getLevel() + 1));
            tvBottomLvl.setText(String.valueOf(Progress.getInstance().getLevel()));
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
        animationController.setAttemptsOnScreen();
        SecurityController security = new SecurityController();
        adFailed = security.getQuestion(21); // проверка на взлом
        if (Player.getInstance().getLevel() > 13) {
            new LoadRiddle().execute();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        int pastLevel = getIntent().getIntExtra("past_level", 1);
        Intent intentMain = new Intent();
        intentMain.putExtra("differ_level", Progress.getInstance().getLevel() - pastLevel);
        try {
            setResult(Activity.RESULT_OK, intentMain);
            finish();
        } catch (NullPointerException ex) {
        }
        finish();
    }

    public void getAttemptByAd() { // показать рекламу, чтобы добавить попытку
        if (mRewardedVideoAd.isLoaded()) {
            mRewardedVideoAd.show();
        } else {
            if (showAdThread == null || !showAdThread.isAlive()) {
                showAdThread = new QuestionActivity.ShowAdThread();
                showAdThread.start();
            }
        }
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
        animationController.changeLevelTop();
        btnCheckAnswer.setClickable(true);
        etAnswer.setText("");
        if (Progress.getInstance().getLevel() == 15) {
            AssistentDialog assistentDialog = new AssistentDialog(AssistentDialog.DIALOG_CHECK_ON_SERRVER_ALERT);
            assistentDialog.show(this.getSupportFragmentManager(), "ALERT_SERVERCHECK_LVL");
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
        adShowed = true;
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
        statisticsController.sendErrorAd(errorCode);
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

    public class PurchaseController {

        BillingClient billingClient;
        private Map<String, SkuDetails> mSkuDetailsMap = new HashMap<>();
        private Context context;
        private int countPurchaseOffer;
        private boolean isPayComplete = false;

        private String mSkuId = "endless_attempts";
        public final static String DATA_SHOW_PURCHASE = "show_purchase";
        private static final String TAG = "PurchaseController";

        public PurchaseController(Context context) {
            this.context = context;
            countPurchaseOffer = StoredData.getDataInt(DATA_SHOW_PURCHASE, 0);
            billingClient = BillingClient.newBuilder(context)
                    .enablePendingPurchases()
                    .setListener(new PurchasesUpdatedListener() {
                        @Override
                        public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> list) {
                            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && list != null) {
                                //сюда мы попадем когда будет осуществлена покупка
                                payComplete();
                                statisticsController.sendPurchase(attemptsController.getCountWrongAnswers());
                                handler.sendEmptyMessage(HIDE_PURCHASE);
                            }
                        }

                    }).build();
            billingClient.startConnection(new BillingClientStateListener() {

                @Override
                public void onBillingSetupFinished(BillingResult billingResult) {
                    try {
                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                            querySkuDetails(); //запрос о товарах
                            List<Purchase> purchasesList = queryPurchases(); //запрос о покупках

                            //если товар уже куплен, предоставить его пользователю
                            for (int i = 0; i < purchasesList.size(); i++) {
                                String purchaseId = purchasesList.get(i).getSku();
                                if (TextUtils.equals(mSkuId, purchaseId)) {
                                    payComplete();
                                }
                            }
                        }
                    } catch (NullPointerException ex) {
                    }
                }

                private List<Purchase> queryPurchases() {
                    Purchase.PurchasesResult purchasesResult = billingClient.queryPurchases(BillingClient.SkuType.INAPP);
                    return purchasesResult.getPurchasesList();
                }

                @Override
                public void onBillingServiceDisconnected() {
                    //сюда мы попадем если что-то пойдет не так
                }
            });
        }

        public int getCountPurchaseOffer() {
            return countPurchaseOffer;
        }

        public void increaseCountPurchaseOffer() {
            StoredData.saveData(DATA_SHOW_PURCHASE, ++countPurchaseOffer);
        }

        public void buy() {
            BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                    .setSkuDetails(mSkuDetailsMap.get(mSkuId))
                    .build();
            billingClient.launchBillingFlow((Activity) context, billingFlowParams);
        }

        private void payComplete() {
            attemptsController.setEndlessAttempts(true);
            animationController.setAttemptsOnScreen();
            isPayComplete = true;
        }

        public boolean isPayComplete() {
            return isPayComplete;
        }

        private void querySkuDetails() {
            SkuDetailsParams.Builder skuDetailsParamsBuilder = SkuDetailsParams.newBuilder();
            List<String> skuList = new ArrayList<>();
            skuList.add(mSkuId);
            skuDetailsParamsBuilder.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);
            billingClient.querySkuDetailsAsync(skuDetailsParamsBuilder.build(), new SkuDetailsResponseListener() {

                @Override
                public void onSkuDetailsResponse(BillingResult billingResult, List<SkuDetails> list) {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && list != null) {
                        for (SkuDetails skuDetails : list) {
                            mSkuDetailsMap.put(skuDetails.getSku(), skuDetails);
                        }
                    }
                }
            });
        }
    }

    private class AnimationController {

        /*переменная хранит состояние поля ввода ответа
        0 - обычное
        1 - зеленое
        2 - красное
        */
        public int INPUT_STATE = 0;
        private boolean isFirstLaunch = true;

        private TransitionDrawable imgBottomDrawable;

        public AnimationController() {
            animationBtnNext(false); // делаем кнопку "дальше" невидимой при старте
            imgBottomDrawable = (TransitionDrawable) imgBottom.getDrawable();
            imgBottomDrawable.setCrossFadeEnabled(true);
            mlBottom.setTransitionListener(new MotionLayout.TransitionListener() {
                @Override
                public void onTransitionStarted(MotionLayout motionLayout, int i, int i1) {
                }

                @Override
                public void onTransitionChange(MotionLayout motionLayout, int i, int i1, float v) {

                }

                @Override
                public void onTransitionCompleted(MotionLayout motionLayout, int i) {
                    if (i == R.id.end) {
                        imgShowBuy.animate().rotation(180);
                    } else if (i == R.id.start) {
                        imgShowBuy.animate().rotation(0);
                    }
                }

                @Override
                public void onTransitionTrigger(MotionLayout motionLayout, int i, boolean b, float v) {

                }
            });
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

        public void setAttemptsOnScreen() {
            int countAttempts = attemptsController.getCountAttempts();
            if (attemptsController.isEndlessAttempts()) {
                btnCheckAnswer.setMaxLines(1);
                btnCheckAnswer.setText(R.string.check_answer);
                imgShowBuy.setClickable(false);
                imgShowBuy.setAlpha(0f);
                etAnswer.setHint("");
            } else if (countAttempts == 0) {
                btnCheckAnswer.setMaxLines(2);
                btnCheckAnswer.setText(R.string.look_ad);
                etAnswer.setHint(getResources().getString(R.string.attempts) + " " + countAttempts);
            } else if (countAttempts <= 3) {
                btnCheckAnswer.setMaxLines(1);
                btnCheckAnswer.setText(R.string.check_answer);
                etAnswer.setHint(getResources().getString(R.string.attempts) + " " + countAttempts);
            }

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

        private void showPurchase() {
            mlBottom.transitionToEnd();
        }

        private void hidePurchase() {
            mlBottom.transitionToStart();
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
                ObjectAnimator btnNextAnimator = ObjectAnimator.ofFloat(btnNext, "alpha", 1.0f, 0.0f);
                btnNextAnimator.setDuration(400);
                btnNextAnimator.start();
            }
        }

        private void animationBtnNext() { // анимация появлеия кнопки "дальше"
            ObjectAnimator animatorBtnNextX;
            ObjectAnimator animatorBtnNextY;
            if (Progress.getInstance().getLevel() <= 21) {
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
                ObjectAnimator btnNextAnimator = ObjectAnimator.ofFloat(btnNext, "alpha", 1.0f, 0.0f);
                if (!isFirstLaunch) {
                    btnNextAnimator.setDuration(400);
                } else {
                    isFirstLaunch = false;
                    btnNext.setVisibility(View.INVISIBLE);
                    btnNextAnimator.setDuration(0);
                }
                btnNextAnimator.start();
                /*btnNextAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                    }
                });*/
                /*new Thread(new Runnable() {
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
                }).start();*/
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

        PartingWords() {
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
            if (attemptsController.getCountWrongAnswers() > 7 && (getRandomInt(1, 3) == 3)) {
                String lastWord = StoredData.getDataString(DATA_LAST_PARTING_WORD, "");
                int indexWord = getRandomInt(0, 17);
                if (!partingWords.get(indexWord).equals(lastWord) && !partingWords.get(indexWord).equals("")) {
                    StoredData.saveData(DATA_LAST_PARTING_WORD, partingWords.get(indexWord));
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
                        if (attemptsController.isEndlessAttempts()) {
                            CheckAnswerTask checkAnswerTask = new CheckAnswerTask();
                            checkAnswerTask.execute(etAnswer.getText().toString());
                        } else if (attemptsController.getCountAttempts() == 0) {
                            getAttemptByAd();
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
                    int pastLevel = getIntent().getIntExtra("past_level", 1);
                    Intent intentMain = new Intent();
                    intentMain.putExtra("differ_level", Progress.getInstance().getLevel() - pastLevel);
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

    private class LoadRiddle extends AsyncTask<Void, Void, Boolean> {

        boolean loadError = false;

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                if (!UpdateDataController.getInstance().nextRiddleIsLoaded() && Player.getInstance().getLevel() < 21) {
                    questionAnswerController.loadNextRiddle();
                    Log.d(TAG, "doInBackground: загружаем следующую");
                }
                if (!UpdateDataController.getInstance().riddleIsLoaded() && Player.getInstance().getLevel() > 14) {
                    questionAnswerController.loadRiddle();
                    Log.d(TAG, "doInBackground: загружаем текущую");
                    return true;
                }
            } catch (NoInternetException ex) {
                loadError = true;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Boolean isCurrentRiddle) {
            super.onPostExecute(isCurrentRiddle);
            if (isCurrentRiddle != null) {
                if (!loadError) {
                    if (isCurrentRiddle) {
                        tvQuestion.setText(questionAnswerController.getQuestion());
                        Log.d(TAG, "onPostExecute: получаем загадку getQuestion");
                    }
                } else
                    Toast.makeText(QuestionActivity.this, R.string.load_riddle_error, Toast.LENGTH_LONG).show();
            }
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
                        attemptsController.resetCountAttempts();
                        attemptsController.resetCountWrongAnswers();
                        handler.sendEmptyMessage(HIDE_PURCHASE);
                        if (Progress.getInstance().getLevel() <= 20) {
                            Progress.getInstance().levelUp(); // повышвем уровень
                            statisticsController.sendNewLevel(); // отправляем статистику на сервер
                            statisticsController.setStartTimeLevel(); // устанавливаем время начала прохождения нового уровня
                            handler.sendEmptyMessage(RIGHT_ANSWER_ANIMATION);
                        } else if (Progress.getInstance().getLevel() == 21) { // если пройденнй уровень был последним
                            Progress.getInstance().done(true);
                            Progress.getInstance().levelUp();
                            handler.sendEmptyMessage(RIGHT_ANSWER_ANIMATION);
                            int place = statisticsController.sendNewLevel(); // отправляем статистику на сервер и получем место игрока
                            StoredData.saveData(DATA_PLACE, place);
                            winnerIsChecked = true;
                            if (place == 1) {
                                StoredData.saveData(DATA_IS_WINNER, true);
                                UpdateDataController.getInstance().setWinnerChecked(true);
                                return true;
                            } else {
                                StoredData.saveData(DATA_IS_WINNER, false);
                                UpdateDataController.getInstance().setWinnerChecked(true);
                                return false;
                            }
                        }
                    } else { // если ответ неверный, уменьшаем попытки
                        handler.sendEmptyMessage(WRONG_ANSWER_ANIMATION);

                        // показываем предложение о покупке
                        if (adShowed && !purchaseController.isPayComplete()) {
                            if (purchaseController.getCountPurchaseOffer() == 0) {
                                handler.sendEmptyMessage(SHOW_PURCHASE);
                                purchaseController.increaseCountPurchaseOffer();
                            } else if (purchaseController.getCountPurchaseOffer() == 1) {
                                if (attemptsController.getCountWrongAnswers() == 18) {
                                    handler.sendEmptyMessage(SHOW_PURCHASE);
                                    purchaseController.increaseCountPurchaseOffer();
                                }
                            }
                        }

                        int countAttempts = attemptsController.getCountAttempts();
                        if (countAttempts > 0 && !attemptsController.isEndlessAttempts()) {
                            attemptsController.decrementCountAtempts();
                        }
                        attemptsController.increaseCountWrongAnswers();
                    }
                } catch (NoInternetException ex) {
                    AssistentDialog assistentDialog = new AssistentDialog(AssistentDialog.DIALOG_ALERT_INTERNET);
                    assistentDialog.show(QuestionActivity.this.getSupportFragmentManager(), "ALERT_INTERNET");
                    UpdateDataController.getInstance().setWinnerChecked(false);
                    winnerIsChecked = false;
                } catch (ErrorOnServerException ex) {
                    AssistentDialog assistentDialog = new AssistentDialog(AssistentDialog.DIALOG_SERVER_ERROR);
                    assistentDialog.show(QuestionActivity.this.getSupportFragmentManager(), "ALERT_SERVER");
                    winnerIsChecked = false;
                } catch (IOException e) {
                    AssistentDialog assistentDialog = new AssistentDialog(AssistentDialog.DIALOG_SERVER_ERROR);
                    assistentDialog.show(QuestionActivity.this.getSupportFragmentManager(), "ALERT_SERVER");
                    winnerIsChecked = false;
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
                        Intent intent = new Intent(QuestionActivity.this, DoneFirstActivity.class);
                        intent.putExtra("past_level", getIntent().getIntExtra("past_level", 1));
                        finish();
                        startActivity(intent); // замена текущего фрагмента на фрагмент с концом игры для побеителя
                    } else {
                        finish();
                        Intent intent = new Intent(QuestionActivity.this, DoneActivity.class);
                        intent.putExtra("past_level", getIntent().getIntExtra("past_level", 1));
                        startActivity(new Intent(QuestionActivity.this, DoneActivity.class)); // замена текущего фрагмента на фрагмент с концом игры
                    }
                }
            }
        }

    }
}
