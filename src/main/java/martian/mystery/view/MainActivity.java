package martian.mystery.view;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.ObjectAnimator;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.florent37.viewtooltip.ViewTooltip;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import martian.mystery.BuildConfig;
import martian.mystery.controller.GetContextClass;
import martian.mystery.controller.LeadersAdapter;
import martian.mystery.controller.Progress;
import martian.mystery.R;
import martian.mystery.controller.RequestController;
import martian.mystery.controller.UpdateDataController;
import martian.mystery.data.Player;
import martian.mystery.data.ResponseFromServer;
import martian.mystery.controller.StoredData;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import yanzhikai.textpath.AsyncTextPathView;
import yanzhikai.textpath.calculator.AroundCalculator;

import static martian.mystery.controller.StoredData.DATA_COUNT_LAUNCH_APP;


public class MainActivity extends AppCompatActivity {

    private RecyclerView leadersList;
    private Button btnNext;
    private AsyncTextPathView tvPrize;
    private TextView tvProgressPick1;
    private TextView tvProgressPick2;
    private TextView tvProgressPick3;
    private TextView tvProgressPick4;
    private ImageView imgLvlTop1;
    private ImageView imgLvlTop2;
    private ImageView imgLvlTop3;
    private ImageView imgLvlTop4;
    private ImageView imgLvl2;
    private ImageView imgLvl3;
    private ImageView imgLvl4;
    private ImageView imgFirst;
    private ImageView imgLast;
    private ImageView btnHelp;
    private ConstraintLayout clProgressPick1;
    private ConstraintLayout clProgressPick2;
    private ConstraintLayout clProgressPick3;
    private ConstraintLayout clProgressPick4;
    ConstraintLayout.LayoutParams imgTop1Params;
    ConstraintLayout.LayoutParams imgTop2Params;
    ConstraintLayout.LayoutParams imgTop3Params;
    ConstraintLayout.LayoutParams imgTop4Params;

    private UpdateDataThread updateDataThread;
    private CheckForceUpdateTask checkForceUpdateTask;
    private ProgressViewController progressViewController;
    private AnimationController animController;
    private ObjectAnimator animBtnHelp;
    private AssistentDialog assistentDialogRules;

    private String locale;
    private ArrayList<String> playersNames = new ArrayList<>();
    private ArrayList<String> playersLevels = new ArrayList<>();
    private ArrayList<Integer> playersCount = new ArrayList<>();

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(StoredData.getDataString(Player.DATA_NAME_PLAYER,Player.getInstance().getName()).equals("")) {
            startActivity(new Intent(this,LogupActivity.class));
            finish();
        }
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //
        StoredData.saveData(DATA_COUNT_LAUNCH_APP,StoredData.getDataInt(DATA_COUNT_LAUNCH_APP,0)+1); // увеличиваем кол-во звапусков игры на один
        locale = Locale.getDefault().getLanguage();
        Log.d("my", "onCreate: " + locale);

        leadersList = findViewById(R.id.leadersList);
        leadersList.setHasFixedSize(true);
        leadersList.setAdapter(new LeadersAdapter(this,playersNames,playersLevels,playersCount));
        leadersList.setLayoutManager(new LinearLayoutManager(this));


        btnNext = findViewById(R.id.btnNext);
        btnHelp = findViewById(R.id.btnHelp);
        imgLast = findViewById(R.id.imgLastCircleLvl);
        imgFirst = findViewById(R.id.imgFirstCircleLvl);

        // все что между первым и последним уровнем
        imgLvl2 = findViewById(R.id.imgLvl2);
        imgLvl3 = findViewById(R.id.imgLvl3);
        imgLvl4 = findViewById(R.id.imgLvl4);
        imgLvlTop1 = findViewById(R.id.imgLvlTop1);
        imgLvlTop2 = findViewById(R.id.imgLvlTop2);
        imgLvlTop3 = findViewById(R.id.imgLvlTop3);
        imgLvlTop4 = findViewById(R.id.imgLvlTop4);
        tvProgressPick1 = findViewById(R.id.tvProgressPick1);
        tvProgressPick2 = findViewById(R.id.tvProgressPick2);
        tvProgressPick3 = findViewById(R.id.tvProgressPick3);
        tvProgressPick4 = findViewById(R.id.tvProgressPick4);
        clProgressPick1 = findViewById(R.id.clProgressPick1);
        clProgressPick2 = findViewById(R.id.clProgressPick2);
        clProgressPick3 = findViewById(R.id.clProgressPick3);
        clProgressPick4 = findViewById(R.id.clProgressPick4);
        imgTop1Params = (ConstraintLayout.LayoutParams) imgLvlTop1.getLayoutParams();
        imgTop2Params = (ConstraintLayout.LayoutParams) imgLvlTop2.getLayoutParams();
        imgTop3Params = (ConstraintLayout.LayoutParams) imgLvlTop3.getLayoutParams();
        imgTop4Params = (ConstraintLayout.LayoutParams) imgLvlTop4.getLayoutParams();

        tvPrize = findViewById(R.id.tvPrize);
        tvPrize.setText(
                String.valueOf(
                StoredData.getDataString(StoredData.DATA_PRIZE,
                        getResources().getString(R.string.prize))));
        tvPrize.setOnClickListener(onClickListener);
        tvPrize.setCalculator(new AroundCalculator());

        btnNext.setOnClickListener(onClickListener);
        btnHelp.setOnClickListener(onClickListener);

        progressViewController = new ProgressViewController(); // контроллер для анимации прогресса
        animController = new AnimationController(); // контроллер для остальных анимаций в данной активити

        if(StoredData.getDataInt(DATA_COUNT_LAUNCH_APP,0) == 1) { // доп. анимации и подсказки, если запуск первый
            ViewTooltip
                    .on(this, btnHelp)
                    .autoHide(true, 5000)
                    .corner(30)
                    .position(ViewTooltip.Position.BOTTOM)
                    .withShadow(false)
                    .text(getResources().getString(R.string.read_rules))
                    .show();
            animController.helpBtnAnimation();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();

        progressViewController.increaseProgressAnimation(0);
        animController.setTextForMainButton();

        // запускаем потоки для обновления данных и проверки принудительных обновлений
        updateDataThread = new UpdateDataThread();
        updateDataThread.start();
        checkForceUpdateTask = new CheckForceUpdateTask();
        checkForceUpdateTask.execute();
    }

    @Override
    protected void onPause() {
        super.onPause();
        updateDataThread.toStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(updateDataThread != null) updateDataThread.toStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        progressViewController.increaseProgressAnimation(0);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btnNext: {
                    Intent intent;
                    if(Progress.getInstance().getLevel() < 22) {
                        intent = new Intent(MainActivity.this,QuestionActivity.class);
                    } else if(Progress.getInstance().getLevel() == 22) {
                        if(!UpdateDataController.getInstance().winnerIsChecked()) {
                            intent = new Intent(MainActivity.this,QuestionActivity.class);
                        } else if(!StoredData.getDataBool(StoredData.DATA_IS_WINNER)) {
                            intent = new Intent(MainActivity.this,DoneActivity.class);
                        } else {
                            intent = new Intent(MainActivity.this,DoneFirstActivity.class);
                        }
                    } else intent = null;
                    startActivityForResult(intent, 1);
                    break;
                }
                case R.id.btnHelp: {
                    assistentDialogRules.show(getSupportFragmentManager(),"HELP");
                    animController.clickRules();
                    break;
                }
                case R.id.tvPrize: {
                    tvPrize.startAnimation(0,1);
                    break;
                }
            }
        }
    };




    // внутренние контроллеры и потоки -----------------------------------------------------------------------------
    private class AnimationController {

        public AnimationController() {
            assistentDialogRules = new AssistentDialog(AssistentDialog.DIALOG_RULES);
        }

        public void helpBtnAnimation() {
            animBtnHelp = ObjectAnimator.ofFloat(btnHelp, "rotationY", 0.0f, 360f);
            animBtnHelp.setDuration(2400);
            animBtnHelp.setRepeatCount(ObjectAnimator.INFINITE);
            animBtnHelp.setInterpolator(new AccelerateDecelerateInterpolator());
            animBtnHelp.start();
        }
        public void setTextForMainButton() {
            if(Progress.getInstance().getLevel() == 1) btnNext.setText(MainActivity.this.getResources().getString(R.string.start_game));
            else if(Progress.getInstance().getLevel() < 22) btnNext.setText(MainActivity.this.getResources().getString(R.string.continue_game));
        }

        public void clickRules() { // анимация нажатия кнопки с правилами
            ObjectAnimator animBtnHelpClickX = ObjectAnimator.ofFloat(btnHelp, "scaleX", 1.0f,0.8f,1.0f);
            ObjectAnimator animBtnHelpClickY = ObjectAnimator.ofFloat(btnHelp, "scaleY", 1.0f,0.8f,1.0f);
            animBtnHelpClickX.setRepeatCount(0);
            animBtnHelpClickY.setRepeatCount(0);
            animBtnHelpClickX.setDuration(200);
            animBtnHelpClickY.setDuration(200);
            animBtnHelpClickX.start();
            animBtnHelpClickY.start();
            if(animBtnHelp != null) {
                animBtnHelp.cancel();
                animBtnHelp = ObjectAnimator.ofFloat(btnHelp, "rotationY", 0.0f);
                animBtnHelp.setRepeatCount(0);
                animBtnHelp.setDuration(500);
                animBtnHelp.start();
            }

        }
    }
    private class ProgressViewController { // класс для управления состоянием (вида) прогресса

        private float widthBetweenLvl; // ширина между первым и последним уровнем
        private float widthOneBlockLvl; // ширина одной оранжевой полоски
        private float ONE_LVL_WIDTH_LEFTRIGHT; // шаг перемещения указателя уровня для крайних полосок
        private float ONE_LVL_WIDTH_CENTER; // шаг перемещения указателя уровня для средних полосок

        public ProgressViewController() {
            int widthScreen = getWidthSreeen();
            widthBetweenLvl = widthScreen - widthScreen*0.2f - widthScreen*0.2f;
            widthOneBlockLvl = (widthBetweenLvl - (imgLvl2.getLayoutParams().width*3))/4 + 1;
            ONE_LVL_WIDTH_LEFTRIGHT = widthOneBlockLvl/5;
            ONE_LVL_WIDTH_CENTER = widthOneBlockLvl/4;
        }
        public int getWidthSreeen() {
            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            return size.x;
        }

        public void increaseProgressAnimation(int differenceLvl) {
            int currentLevel = Progress.getInstance().getLevel() - 1;
            if(currentLevel == 0) {
                imgFirst.setAlpha(0.5f);
                imgLvlTop1.setVisibility(View.INVISIBLE);
                clProgressPick1.setVisibility(View.INVISIBLE);
            }
            if(currentLevel == 1) {
                imgFirst.setAlpha(1.0f);
                imgLvlTop1.setVisibility(View.INVISIBLE);
                clProgressPick1.setVisibility(View.INVISIBLE);
            } else if(currentLevel > 1 && currentLevel < 7) {
                imgFirst.setAlpha(1.0f);
                imgLvlTop1.setVisibility(View.VISIBLE);
                clProgressPick1.setVisibility(View.VISIBLE);
                tvProgressPick1.setText(String.valueOf(currentLevel));

                if((currentLevel - 2) == 4) imgTop1Params.width = (int)ONE_LVL_WIDTH_LEFTRIGHT*(currentLevel-2)+(int)ONE_LVL_WIDTH_LEFTRIGHT/2;
                else imgTop1Params.width = (int)ONE_LVL_WIDTH_LEFTRIGHT*(currentLevel-1);

                imgLvlTop1.setLayoutParams(imgTop1Params);
            } else if(currentLevel == 7) {
                imgFirst.setAlpha(1.0f);
                imgLvl2.setAlpha(1.0f);
                imgLvlTop1.setVisibility(View.VISIBLE);
                imgTop1Params.width = (int)ONE_LVL_WIDTH_LEFTRIGHT*5;
                clProgressPick1.setVisibility(View.INVISIBLE);

                imgLvlTop1.setLayoutParams(imgTop1Params);
            } else if(currentLevel > 7 && currentLevel < 11) {
                imgFirst.setAlpha(1.0f);
                imgLvlTop1.setVisibility(View.VISIBLE);
                imgLvlTop2.setVisibility(View.VISIBLE);
                clProgressPick1.setVisibility(View.INVISIBLE);
                imgLvl2.setAlpha(1.0f);
                imgTop1Params.width = (int)ONE_LVL_WIDTH_LEFTRIGHT*5;

                clProgressPick2.setVisibility(View.VISIBLE);
                tvProgressPick2.setText(String.valueOf(currentLevel));

                if((currentLevel - 8) == 3) imgTop2Params.width = (int)ONE_LVL_WIDTH_CENTER*(currentLevel-8)+(int)ONE_LVL_WIDTH_CENTER/2;
                else imgTop2Params.width = (int)ONE_LVL_WIDTH_CENTER*(currentLevel-7);

                imgLvlTop1.setLayoutParams(imgTop1Params);
                imgLvlTop2.setLayoutParams(imgTop2Params);
            } else if(currentLevel == 11) {
                imgFirst.setAlpha(1.0f);
                imgLvlTop1.setVisibility(View.VISIBLE);
                imgLvlTop2.setVisibility(View.VISIBLE);
                imgLvl2.setAlpha(1.0f);
                imgLvl3.setAlpha(1.0f);
                imgTop1Params.width = (int)ONE_LVL_WIDTH_LEFTRIGHT*5;
                imgTop2Params.width = (int)ONE_LVL_WIDTH_CENTER*4;
                clProgressPick1.setVisibility(View.INVISIBLE);
                clProgressPick2.setVisibility(View.INVISIBLE);

                imgLvlTop1.setLayoutParams(imgTop1Params);
                imgLvlTop2.setLayoutParams(imgTop2Params);
            } else if(currentLevel > 11 && currentLevel < 15) {
                imgFirst.setAlpha(1.0f);
                imgLvlTop1.setVisibility(View.VISIBLE);
                imgLvlTop2.setVisibility(View.VISIBLE);
                imgLvlTop3.setVisibility(View.VISIBLE);
                imgLvl2.setAlpha(1.0f);
                imgLvl3.setAlpha(1.0f);
                imgTop1Params.width = (int)ONE_LVL_WIDTH_LEFTRIGHT*5;
                imgTop2Params.width = (int)ONE_LVL_WIDTH_CENTER*4;
                clProgressPick1.setVisibility(View.INVISIBLE);
                clProgressPick2.setVisibility(View.INVISIBLE);

                clProgressPick3.setVisibility(View.VISIBLE);
                tvProgressPick3.setText(String.valueOf(currentLevel));

                if((currentLevel - 12) == 3) imgTop3Params.width = (int)ONE_LVL_WIDTH_CENTER*(currentLevel-12)+(int)ONE_LVL_WIDTH_CENTER/2;
                else imgTop3Params.width = (int)ONE_LVL_WIDTH_CENTER*(currentLevel-11);

                imgLvlTop1.setLayoutParams(imgTop1Params);
                imgLvlTop2.setLayoutParams(imgTop2Params);
                imgLvlTop3.setLayoutParams(imgTop3Params);
            } else if(currentLevel == 15) {
                imgFirst.setAlpha(1.0f);
                imgLvlTop1.setVisibility(View.VISIBLE);
                imgLvlTop2.setVisibility(View.VISIBLE);
                imgLvlTop3.setVisibility(View.VISIBLE);
                imgLvl2.setAlpha(1.0f);
                imgLvl3.setAlpha(1.0f);
                imgLvl4.setAlpha(1.0f);
                imgTop1Params.width = (int)ONE_LVL_WIDTH_LEFTRIGHT*5;
                imgTop2Params.width = (int)ONE_LVL_WIDTH_CENTER*4;
                imgTop3Params.width = (int)ONE_LVL_WIDTH_CENTER*4;
                clProgressPick1.setVisibility(View.INVISIBLE);
                clProgressPick2.setVisibility(View.INVISIBLE);
                clProgressPick3.setVisibility(View.INVISIBLE);

                imgLvlTop1.setLayoutParams(imgTop1Params);
                imgLvlTop2.setLayoutParams(imgTop2Params);
                imgLvlTop3.setLayoutParams(imgTop3Params);
            } else if(currentLevel > 15 && currentLevel < 21) {
                imgFirst.setAlpha(1.0f);
                imgLvlTop1.setVisibility(View.VISIBLE);
                imgLvlTop2.setVisibility(View.VISIBLE);
                imgLvlTop3.setVisibility(View.VISIBLE);
                imgLvlTop4.setVisibility(View.VISIBLE);
                imgLvl2.setAlpha(1.0f);
                imgLvl3.setAlpha(1.0f);
                imgLvl4.setAlpha(1.0f);
                imgTop1Params.width = (int)ONE_LVL_WIDTH_LEFTRIGHT*5;
                imgTop2Params.width = (int)ONE_LVL_WIDTH_CENTER*4;
                imgTop3Params.width = (int)ONE_LVL_WIDTH_CENTER*4;
                clProgressPick1.setVisibility(View.INVISIBLE);
                clProgressPick2.setVisibility(View.INVISIBLE);
                clProgressPick3.setVisibility(View.INVISIBLE);

                clProgressPick4.setVisibility(View.VISIBLE);
                tvProgressPick4.setText(String.valueOf(currentLevel));
                if((currentLevel - 16) == 4) imgTop4Params.width = (int)ONE_LVL_WIDTH_LEFTRIGHT*(currentLevel-16)+(int)ONE_LVL_WIDTH_LEFTRIGHT/2;
                else imgTop4Params.width = (int)ONE_LVL_WIDTH_LEFTRIGHT*(currentLevel-15);

                imgLvlTop1.setLayoutParams(imgTop1Params);
                imgLvlTop2.setLayoutParams(imgTop2Params);
                imgLvlTop3.setLayoutParams(imgTop3Params);
                imgLvlTop4.setLayoutParams(imgTop4Params);
            } else if(currentLevel >= 21) {
                imgFirst.setAlpha(1.0f);
                imgLvlTop1.setVisibility(View.VISIBLE);
                imgLvlTop2.setVisibility(View.VISIBLE);
                imgLvlTop3.setVisibility(View.VISIBLE);
                imgLvlTop4.setVisibility(View.VISIBLE);
                imgLvl2.setAlpha(1.0f);
                imgLvl3.setAlpha(1.0f);
                imgLvl4.setAlpha(1.0f);
                imgTop1Params.width = (int)ONE_LVL_WIDTH_LEFTRIGHT*5;
                imgTop2Params.width = (int)ONE_LVL_WIDTH_CENTER*4;
                imgTop3Params.width = (int)ONE_LVL_WIDTH_CENTER*4;
                imgTop4Params.width = (int)ONE_LVL_WIDTH_CENTER*4;
                clProgressPick1.setVisibility(View.INVISIBLE);
                clProgressPick2.setVisibility(View.INVISIBLE);
                clProgressPick3.setVisibility(View.INVISIBLE);
                clProgressPick4.setVisibility(View.INVISIBLE);
                imgLast.setAlpha(1.0f);

                imgLvlTop1.setLayoutParams(imgTop1Params);
                imgLvlTop2.setLayoutParams(imgTop2Params);
                imgLvlTop3.setLayoutParams(imgTop3Params);
                imgLvlTop4.setLayoutParams(imgTop4Params);
            }
        }
    }
    private class CheckForceUpdateTask extends AsyncTask<Void,Void,Boolean> { // проверяет принудительные обновления

        int typeUpdate; // тип обновления

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                ResponseFromServer responseFromServer = RequestController.getInstance()
                        .getJsonApi()
                        .checkUpdate(BuildConfig.VERSION_CODE)
                        .execute().body();
                if(responseFromServer.getForceUpdate() > 0) {
                    typeUpdate = responseFromServer.getForceUpdate();
                    return true;
                }
            } catch (IOException e) {
                return false;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            if(success) {
                switch (typeUpdate) {
                    case 1: { // Обновления ради нового сезона
                        AssistentDialog updateDialog = new AssistentDialog(AssistentDialog.DIALOG_UPDATE_APP);
                        updateDialog.show(getSupportFragmentManager(),"UPDATE");
                        break;
                    }
                    case 2: { // Техническое обновление
                        AssistentDialog updateDialog = new AssistentDialog(AssistentDialog.DIALOG_UPDATE_APP_TECH);
                        updateDialog.show(getSupportFragmentManager(),"UPDATE");
                        break;
                    }
                }
            }
        }
    }
    private class UpdateDataThread extends Thread { // поток, обновляющий основыне данные на главной активити

        private boolean isStop = false;
        private final String DATA_LEADERS = "leaders_list";
        private String leaders;
        private ArrayList<String> newPlayersNames = new ArrayList<>(5);
        private ArrayList<String> newPlayersLevels = new ArrayList<>(5);
        private ArrayList<Integer> newPlayersCount = new ArrayList<>(5);

        public UpdateDataThread() {
            leaders = StoredData.getDataString(DATA_LEADERS,"0-0-...;0-0-...;0-0-...;0-0-...;0-0-...;");
            if(playersNames.size() == 0) { // если поток запустился первый раз в сессии, то инициализируем
                initLeaders();
            }
        }

        private void initLeaders() {
            String[] oneLevelLeadersTemp = leaders.split(";");
            List<String> oneLevelLeaders = Arrays.asList("0-0-...;","0-0-...;","0-0-...;","0-0-...;","0-0-...;");
            for(int i = 0; i < oneLevelLeadersTemp.length; i++) {
                 oneLevelLeaders.set(i,oneLevelLeadersTemp[i]);
            }
            for(int i = 0; i < oneLevelLeaders.size(); i++) {
                playersNames.add(i, oneLevelLeaders.get(i).split("-")[2]);
                playersCount.add(i, Integer.valueOf(oneLevelLeaders.get(i).split("-")[1]));
                playersLevels.add(i, oneLevelLeaders.get(i).split("-")[0]);
            }
        }

        @Override
        public void run() {
            while (true) {
                if(!isStop) {
                    RequestController.getInstance() // получем приз
                            .getJsonApi()
                            .getPrize("prize")
                            .enqueue(new Callback<ResponseFromServer>() {
                                @Override
                                public void onResponse(Call<ResponseFromServer> call, Response<ResponseFromServer> response) {
                                    ResponseFromServer responseFromServer = response.body();
                                    String prize; // переменная хранит приз в зависимости от языка устройства
                                    if(locale.equals("ru") || locale.equals("be") || locale.equals("uk")) {
                                        prize = responseFromServer.getPrize().split(",")[0];
                                    } else prize = responseFromServer.getPrize().split(",")[1];
                                    if(!prize.equals(StoredData.getDataString(StoredData.DATA_PRIZE,GetContextClass.getContext().getResources().getString(R.string.prize)))) {
                                        StoredData.saveData(StoredData.DATA_PRIZE,prize);
                                        tvPrize.setText(prize);
                                        tvPrize.startAnimation(0,1);
                                    }

                                    /*// проверяем есть ли ссылка на ооцсеть победителя
                                    if(existWinner) {
                                        if(!responseFromServer.getLinktowinner().equals("none")) {
                                            linkToWinner = responseFromServer.getLinktowinner();
                                        } else linkToWinner = "none";
                                    }*/
                                }

                                @Override
                                public void onFailure(Call<ResponseFromServer> call, Throwable t) {
                                    Log.d(TAG, "onFailure: error =" + t.toString());
                                }
                            });

                    RequestController.getInstance() // получаем список лидеров
                            .getJsonApi()
                            .getLeaders("please")
                            .enqueue(new Callback<ResponseFromServer>() {
                                @Override
                                public void onResponse(Call<ResponseFromServer> call, Response<ResponseFromServer> response) {
                                    ResponseFromServer responseFromServer = response.body();
                                    if(!responseFromServer.getLeaders().equals(leaders)) {
                                        leaders = responseFromServer.getLeaders();
                                        Log.d(TAG, "onResponse: leadersUpdate = " + leaders);
                                        StoredData.saveData(DATA_LEADERS,leaders);
                                        String oneLevelLeaders[] = leaders.split(";");
                                        for(int i = 0; i < oneLevelLeaders.length; i++) {
                                            newPlayersNames.add(i, oneLevelLeaders[i].split("-")[2]);
                                            newPlayersCount.add(i, Integer.valueOf(oneLevelLeaders[i].split("-")[1]));
                                            newPlayersLevels.add(i, oneLevelLeaders[i].split("-")[0]);
                                            Log.d(TAG, "onResponse: new Leader = " + newPlayersNames.get(i));
                                            Log.d(TAG, "onResponse: new Count = " + newPlayersCount.get(i));
                                            Log.d(TAG, "onResponse: new Level = " + newPlayersLevels.get(i));

                                            if(!newPlayersNames.get(i).equals(playersNames.get(i)) ||
                                                    newPlayersCount.get(i) != (playersCount.get(i)) ||
                                                    !newPlayersLevels.get(i).equals(playersLevels.get(i))) {
                                                playersNames.set(i,newPlayersNames.get(i));
                                                playersCount.set(i,newPlayersCount.get(i));
                                                playersLevels.set(i,newPlayersLevels.get(i));
                                                leadersList.getAdapter().notifyItemChanged(i);
                                            }
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<ResponseFromServer> call, Throwable t) {
                                    Log.d(TAG, "onFailure: leaderserror = " + t.toString());
                                }
                            });

                    try {
                        sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else return;
            }
        }
        public void toStop() { // остановить поток
            isStop = true;
        }
    }
}
