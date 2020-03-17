package martian.mystery.view;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.TransitionDrawable;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.florent37.viewtooltip.ViewTooltip;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import martian.mystery.BuildConfig;
import martian.mystery.controller.AttemptsController;
import martian.mystery.controller.GetContextClass;
import martian.mystery.controller.Progress;
import martian.mystery.R;
import martian.mystery.controller.RequestController;
import martian.mystery.controller.StatisticsController;
import martian.mystery.controller.UpdateDataController;
import martian.mystery.data.Player;
import martian.mystery.data.ResponseFromServer;
import martian.mystery.controller.StoredData;
import martian.mystery.exceptions.ErrorOnServerException;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import yanzhikai.textpath.AsyncTextPathView;
import yanzhikai.textpath.calculator.AroundCalculator;

import static martian.mystery.controller.StoredData.DATA_COUNT_LAUNCH_APP;


public class MainActivity extends AppCompatActivity {

    private Button btnNext;
    private AsyncTextPathView tvPrize;
    private ImageView btnHelp;
    private ImageView imgSeason;
    private TextView tvName;
    private ImageView imgBackLevel;
    private TextView tvLevel;

    private UpdateDataThread updateDataThread;
    private CheckForceUpdateTask checkForceUpdateTask;
    private ProgressViewController progressViewController;
    private AnimationController animController;
    private ObjectAnimator animBtnHelp;
    private AssistentDialog assistentDialogRules;

    private String locale;
    private ArrayList<String> playersNames = new ArrayList<>();
    private ArrayList<Integer> playersLevels = new ArrayList<>();
    private ArrayList<Integer> playersCount = new ArrayList<>();

    private static final String TAG = "MainActivity";
    private final String DATA_LEADERS = "leaders_list";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(StoredData.getDataString(Player.DATA_NAME_PLAYER,Player.getInstance().getName()).equals("")) {
            startActivity(new Intent(this,LogupActivity.class));
            finish();
        }
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StoredData.saveData(DATA_COUNT_LAUNCH_APP,StoredData.getDataInt(DATA_COUNT_LAUNCH_APP,0)+1); // увеличиваем кол-во звапусков игры на один
        locale = Locale.getDefault().getLanguage();


        tvName = findViewById(R.id.tvName);
        imgBackLevel = findViewById(R.id.imgBackLevel);
        tvLevel = findViewById(R.id.tvLevel);
        btnNext = findViewById(R.id.btnNext);
        btnHelp = findViewById(R.id.btnHelp);

        tvPrize = findViewById(R.id.tvPrize);
        tvPrize.setText(
                String.valueOf(
                StoredData.getDataString(StoredData.DATA_PRIZE,
                        getResources().getString(R.string.prize))));
        tvPrize.setOnClickListener(onClickListener);
        tvPrize.setCalculator(new AroundCalculator());
        imgSeason = findViewById(R.id.imgSeason);
        imgSeason.setOnClickListener(onClickListener);

        btnNext.setOnClickListener(onClickListener);
        btnHelp.setOnClickListener(onClickListener);

        progressViewController = new ProgressViewController(); // контроллер для анимации прогресса
        animController = new AnimationController(); // контроллер для остальных анимаций в данной активити

        if(StoredData.getDataInt(DATA_COUNT_LAUNCH_APP,0) == 2) { // доп. анимации и подсказки, если запуск первый
            ViewTooltip
                    .on(this, btnHelp)
                    .autoHide(true, 5000)
                    .corner(30)
                    .position(ViewTooltip.Position.BOTTOM)
                    .withShadow(false)
                    .text(getResources().getString(R.string.read_rules))
                    .show();
        } else {
            if(StoredData.getDataString(StatisticsController.DATA_UPDATE_LEVEL,"no").equals("no")) {
                new LoadNewLevelTask().execute();
            }
        }
    }
    @Override
    protected void onResume() {
        super.onResume();

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

        if(data != null) {
            animController.increaseLevel(data.getIntExtra("differ_level",0));
        } else animController.initLevel(true);
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
                        intent.putExtra("past_level",Player.getInstance().getLevel());
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
                    startActivity(new Intent(MainActivity.this,InfoActivity.class));
                    animController.clickRules();
                    break;
                }
                case R.id.imgSeason: {
                    ViewTooltip
                            .on(MainActivity.this, imgSeason)
                            .autoHide(true, 4000)
                            .corner(30)
                            .position(ViewTooltip.Position.BOTTOM)
                            .withShadow(false)
                            .text(getResources().getString(R.string.date_start_season))
                            .show();
                    break;
                }
                case R.id.tvPrize: {
                    tvPrize.startAnimation(0,1);
                    break;
                }
            }
        }
    };

    View.OnClickListener playerClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(v.getId() != R.id.tvNameLeader1) {
                ViewTooltip
                        .on(MainActivity.this, v)
                        .autoHide(true, 5000)
                        .corner(30)
                        .position(ViewTooltip.Position.BOTTOM)
                        .withShadow(false)
                        .text(getResources().getString(R.string.info_player))
                        .show();
            } else {
                ViewTooltip
                        .on(MainActivity.this, v)
                        .autoHide(true, 5000)
                        .corner(30)
                        .position(ViewTooltip.Position.BOTTOM)
                        .withShadow(false)
                        .text(getResources().getString(R.string.info_first_player))
                        .show();
            }
        }
    };

    View.OnClickListener levelClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ViewTooltip
                    .on(MainActivity.this, v)
                    .autoHide(true, 5000)
                    .corner(30)
                    .position(ViewTooltip.Position.BOTTOM)
                    .withShadow(false)
                    .text(getResources().getString(R.string.current_level_info))
                    .show();
        }
    };


    // внутренние контроллеры и потоки -----------------------------------------------------------------------------
    private class AnimationController {

        // view`шки лидеров
        private ArrayList<TextView> namesLeaders = new ArrayList<>();
        private ArrayList<TextView> levelsLeaders = new ArrayList<>();
        private ArrayList<ImageView> lines = new ArrayList<>();

        // блоки одного уровня
        private ArrayList<ImageView> levelBlocks = new ArrayList<>();

        // анимация для уровня
        ObjectAnimator blockShow;
        ObjectAnimator blockScaleX;
        ObjectAnimator blockScaleY;

        // анимация для лидеров
        ObjectAnimator nameHide;
        ObjectAnimator nameShow;
        ObjectAnimator levelHide;
        ObjectAnimator levelShow;
        ObjectAnimator lineShow;
        ObjectAnimator lineScale;

        private float alphaCurrentLevel = 0.5f;

        public AnimationController() {
            assistentDialogRules = new AssistentDialog(AssistentDialog.DIALOG_RULES);

            tvName.setText(Player.getInstance().getName());

            levelBlocks.add((ImageView) findViewById(R.id.imgLvl1));
            levelBlocks.add((ImageView) findViewById(R.id.imgLvl2));
            levelBlocks.add((ImageView) findViewById(R.id.imgLvl3));
            levelBlocks.add((ImageView) findViewById(R.id.imgLvl4));
            levelBlocks.add((ImageView) findViewById(R.id.imgLvl5));
            levelBlocks.add((ImageView) findViewById(R.id.imgLvl6));
            levelBlocks.add((ImageView) findViewById(R.id.imgLvl7));
            levelBlocks.add((ImageView) findViewById(R.id.imgLvl8));
            levelBlocks.add((ImageView) findViewById(R.id.imgLvl9));
            levelBlocks.add((ImageView) findViewById(R.id.imgLvl10));
            levelBlocks.add((ImageView) findViewById(R.id.imgLvl11));
            levelBlocks.add((ImageView) findViewById(R.id.imgLvl12));
            levelBlocks.add((ImageView) findViewById(R.id.imgLvl13));
            levelBlocks.add((ImageView) findViewById(R.id.imgLvl14));
            levelBlocks.add((ImageView) findViewById(R.id.imgLvl15));
            levelBlocks.add((ImageView) findViewById(R.id.imgLvl16));
            levelBlocks.add((ImageView) findViewById(R.id.imgLvl17));
            levelBlocks.add((ImageView) findViewById(R.id.imgLvl18));
            levelBlocks.add((ImageView) findViewById(R.id.imgLvl19));
            levelBlocks.add((ImageView) findViewById(R.id.imgLvl20));
            levelBlocks.add((ImageView) findViewById(R.id.imgLvl21));

            namesLeaders.add((TextView)findViewById(R.id.tvNameLeader1));
            namesLeaders.add((TextView)findViewById(R.id.tvNameLeader2));
            namesLeaders.add((TextView)findViewById(R.id.tvNameLeader3));
            namesLeaders.add((TextView)findViewById(R.id.tvNameLeader4));
            namesLeaders.add((TextView)findViewById(R.id.tvNameLeader5));
            levelsLeaders.add((TextView)findViewById(R.id.tvLevel1));
            levelsLeaders.add((TextView)findViewById(R.id.tvLevel2));
            levelsLeaders.add((TextView)findViewById(R.id.tvLevel3));
            levelsLeaders.add((TextView)findViewById(R.id.tvLevel4));
            levelsLeaders.add((TextView)findViewById(R.id.tvLevel5));
            lines.add((ImageView)findViewById(R.id.underLine1));
            lines.add((ImageView)findViewById(R.id.underLine2));
            lines.add((ImageView)findViewById(R.id.underLine3));
            lines.add((ImageView)findViewById(R.id.underLine4));
            lines.add((ImageView)findViewById(R.id.underLine5));

            initLevel(false);
            initLeaders();
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
            ObjectAnimator btnHide = ObjectAnimator.ofFloat(btnHelp, "alpha", 1.0f,0.8f);
            ObjectAnimator btnShow = ObjectAnimator.ofFloat(btnHelp, "alpha", 0.8f,1.0f);
            btnHide.setDuration(300);
            btnHide.start();
            btnShow.setStartDelay(300);
            btnShow.setDuration(300);
            btnShow.start();
            if(animBtnHelp != null) {
                animBtnHelp.cancel();
                animBtnHelp = ObjectAnimator.ofFloat(btnHelp, "rotationY", 0.0f);
                animBtnHelp.setRepeatCount(0);
                animBtnHelp.setDuration(500);
                animBtnHelp.start();
            }

        }
        public void initLevel(boolean isFirstTime) {
            boolean isComplete = false;
            int currentLevel = Player.getInstance().getLevel();
            if(currentLevel == 22) {
                currentLevel = 21;
                isComplete = true;
            }
            if(isFirstTime) { // если игрок только что прошел все уровни, то готовим особую анимацию
                tvLevel.setAlpha(0);
                for(int i = 0; i < 21; i++) {
                    blockShow = ObjectAnimator.ofFloat(levelBlocks.get(i),"alpha",1f,0f);
                    blockScaleX = ObjectAnimator.ofFloat(levelBlocks.get(i),"scaleX",1f,0f);
                    blockScaleY = ObjectAnimator.ofFloat(levelBlocks.get(i),"scaleY",1f,0f);

                    blockShow.setDuration(0);
                    blockShow.start();

                    blockScaleX.setDuration(0);
                    blockScaleX.start();
                    blockScaleY.setDuration(0);
                    blockScaleY.start();
                }
            }
            for(int i = 0, delay = 0; i < currentLevel; i++, delay += 100) {
                if(i != (currentLevel - 1)) {
                    blockShow = ObjectAnimator.ofFloat(levelBlocks.get(i),"alpha",0f,1f);
                } else if(!isComplete) blockShow = ObjectAnimator.ofFloat(levelBlocks.get(i),"alpha",0f,alphaCurrentLevel);
                else blockShow = ObjectAnimator.ofFloat(levelBlocks.get(i),"alpha",0f,1f);
                blockScaleX = ObjectAnimator.ofFloat(levelBlocks.get(i),"scaleX",0f,1f);
                blockScaleY = ObjectAnimator.ofFloat(levelBlocks.get(i),"scaleY",0f,1f);

                blockShow.setStartDelay(delay);
                blockShow.setDuration(250);
                blockShow.start();

                blockScaleX.setStartDelay(delay);
                blockScaleX.setDuration(250);
                blockScaleX.start();
                blockScaleY.setStartDelay(delay);
                blockScaleY.setDuration(250);
                blockScaleY.start();
            }
            if(Progress.getInstance().getLevel() < 22) {
                String level = Player.getInstance().getLevel() + " " + getResources().getString(R.string.level);
                tvLevel.setText(level);
            } else {
                tvLevel.setText(R.string.complete_level);
                tvLevel.setTextColor(getResources().getColor(R.color.rightAnswer));
                TransitionDrawable transitionDrawable = (TransitionDrawable) imgBackLevel.getDrawable();
                transitionDrawable.startTransition(0);
            }
            if(isFirstTime) {
                ObjectAnimator levelShow = ObjectAnimator.ofFloat(tvLevel,"alpha",0f,1f);
                levelShow.setStartDelay(2100);
                levelShow.setDuration(1000);
                levelShow.start();
                TransitionDrawable transitionDrawable = (TransitionDrawable) imgBackLevel.getDrawable();
                transitionDrawable.startTransition(1000);

                // делаем аниамцию оконтовки
            }
        }
        public void initLeaders() {
            String leaders = StoredData.getDataString(DATA_LEADERS,"0-0-...;0-0-...;0-0-...;0-0-...;0-0-...;"); //0-0-...;0-0-...;0-0-...;0-0-...;0-0-...;
            String[] oneLevelLeadersTemp = leaders.split(";");
            List<String> oneLevelLeaders = Arrays.asList("0-0-...","0-0-...","0-0-...","0-0-...","0-0-...");
            for(int i = 0; i < oneLevelLeadersTemp.length; i++) {
                oneLevelLeaders.set(i,oneLevelLeadersTemp[i]);
            }
            for(int i = 0; i < oneLevelLeaders.size(); i++) {
                playersNames.add(oneLevelLeaders.get(i).split("-")[2]);
                playersCount.add(Integer.valueOf(oneLevelLeaders.get(i).split("-")[1]));
                playersLevels.add(Integer.valueOf(oneLevelLeaders.get(i).split("-")[0]));
                levelsLeaders.get(i).setOnClickListener(levelClickListener);
                namesLeaders.get(i).setOnClickListener(playerClickListener);

                // установка имени первого игрока и кол-ва других игроков на этом уровне
                if(playersCount.get(i) > 2) {
                    namesLeaders.get(i).setText(spanText(playersNames.get(i) + " " + getString(R.string.and) + " " + (playersCount.get(i)-1) + " " + getString(R.string.people)));
                } else if(playersCount.get(i) == 2) {
                    if(locale.equals("en")) {
                        namesLeaders.get(i).setText(spanText(playersNames.get(i) + " " + getString(R.string.and) +
                                " " + getString(R.string.one_person)));
                    } else {
                        namesLeaders.get(i).setText(spanText(playersNames.get(i) + " " + getString(R.string.and) + " " + (playersCount.get(i)-1) + " " + getString(R.string.people)));
                    }
                 } else {
                     namesLeaders.get(i).setText(playersNames.get(i));
                 }

                // установка уровня
                if(playersLevels.get(i) > 0 && playersLevels.get(i) < 22) {
                    levelsLeaders.get(i).setText(playersLevels.get(i) + " " + getString(R.string.lvl));
                } else if (playersLevels.get(i) == 22) {
                    levelsLeaders.get(i).setText(getString(R.string.complete_game_level));
                    lines.get(i).setImageDrawable(getDrawable(R.drawable.winner_line));
                } else {
                    levelsLeaders.get(i).setText(playersLevels.get(i) + " " + getString(R.string.lvl));
                }
            }
            initLeadersAnimation();
        }
        private void initLeadersAnimation() {
            for(int i = 0, delay = 0; i < namesLeaders.size(); i++, delay += 300) {
                lineShow = ObjectAnimator.ofFloat(lines.get(i),"alpha",0f,1f);
                lineScale = ObjectAnimator.ofFloat(lines.get(i),"scaleX",0f,1f);
                nameShow = ObjectAnimator.ofFloat(namesLeaders.get(i),"alpha",0f,1f);
                levelShow = ObjectAnimator.ofFloat(levelsLeaders.get(i),"alpha",0f,1f);
                lines.get(i).setPivotX(0);
                lineScale.setDuration(1000);
                lineScale.setStartDelay(delay);
                lineShow.setStartDelay(delay);
                nameShow.setStartDelay(delay);
                nameShow.setDuration(1500);
                levelShow.setDuration(1500);
                levelShow.setStartDelay(delay+500);
                lineShow.start();
                lineScale.start();
                nameShow.start();
                levelShow.start();
            }
        }
        private void increaseLevel(int differLevel) {
            tvLevel.setText(Player.getInstance().getLevel() + " " + getString(R.string.level));
            if(differLevel > 0) {
                int currentLevel = Player.getInstance().getLevel();
                int pastLevel = currentLevel - differLevel;
                boolean isComplete = (currentLevel == 22);
                for(int i = pastLevel-1, delay = 0; i < currentLevel; i++, delay += 200) {
                    if(i != (currentLevel-1)) blockShow = ObjectAnimator.ofFloat(levelBlocks.get(i),"alpha",0f,1f);
                    else if(!isComplete) blockShow = ObjectAnimator.ofFloat(levelBlocks.get(i),"alpha",0f,alphaCurrentLevel);
                    else blockShow = ObjectAnimator.ofFloat(levelBlocks.get(i),"alpha",0f,1f);
                    blockScaleX = ObjectAnimator.ofFloat(levelBlocks.get(i),"scaleX",0f,1f);
                    blockScaleY = ObjectAnimator.ofFloat(levelBlocks.get(i),"scaleY",0f,1f);

                    blockShow.setStartDelay(delay);
                    blockShow.setDuration(400);
                    blockShow.start();

                    blockScaleX.setStartDelay(delay);
                    blockScaleX.setDuration(400);
                    blockScaleX.start();
                    blockScaleY.setStartDelay(delay);
                    blockScaleY.setDuration(400);
                    blockScaleY.start();
                }
            }
            if(Progress.getInstance().getLevel() < 22) {
                tvLevel.setText(Progress.getInstance().getLevel() + " " + getResources().getString(R.string.level));
            } else {
                tvLevel.setText(R.string.complete_level);
                tvLevel.setTextColor(getResources().getColor(R.color.rightAnswer));
            }

        }
        private void changeInfoLeader(int position, String nameLeader, int countOtherPLayers, int level) {
            // установка имени первого игрока и кол-ва других игроков на этом уровне
            if(playersCount.get(position) > 2) {
                namesLeaders.get(position).setText(spanText(playersNames.get(position) + " " + getString(R.string.and) + " " + (playersCount.get(position)-1) + " " + getString(R.string.people)));
            } else if(playersCount.get(position) == 2) {
                if(locale.equals("en")) {
                    namesLeaders.get(position).setText(spanText(playersNames.get(position) + " " + getString(R.string.and) +
                            " " + getString(R.string.one_person)));
                } else {
                    namesLeaders.get(position).setText(spanText(playersNames.get(position) + " " + getString(R.string.and) + " " + (playersCount.get(position)-1) + " " + getString(R.string.people)));
                }
            } else {
                namesLeaders.get(position).setText(playersNames.get(position));
            }

            // установка уровня
            if(playersLevels.get(position) > 0 && playersLevels.get(position) < 22) {
                levelsLeaders.get(position).setText(playersLevels.get(position) + " " + getString(R.string.lvl));
            } else if (playersLevels.get(position) == 22) {
                levelsLeaders.get(position).setText(getString(R.string.complete_game_level));
                lines.get(position).setImageDrawable(getDrawable(R.drawable.winner_line));
            } else {
                levelsLeaders.get(position).setText(playersLevels.get(position) + " " + getString(R.string.lvl));
            }
        }
        public void animateChangeLeaders(final int position, final String nameLeader, final int countOtherPLayers, final int level) {
            nameHide = ObjectAnimator.ofFloat(namesLeaders.get(position),"alpha",1f,0f);
            nameShow = ObjectAnimator.ofFloat(namesLeaders.get(position),"alpha",0f,1f);
            levelHide = ObjectAnimator.ofFloat(levelsLeaders.get(position),"alpha",1f,0f);
            levelShow = ObjectAnimator.ofFloat(levelsLeaders.get(position),"alpha",0f,1f);
            nameHide.setDuration(800);
            nameShow.setDuration(800);
            levelHide.setDuration(800);
            levelShow.setDuration(800);
            nameShow.setStartDelay(800);
            levelShow.setStartDelay(800);
            nameHide.start();
            nameShow.start();
            levelHide.start();
            levelShow.start();
            nameHide.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    changeInfoLeader(position,nameLeader,countOtherPLayers,level);
                }
            });
        }
        private Spannable spanText(String str) {
            Spannable spans = new SpannableString(str);
            spans.setSpan(new ForegroundColorSpan(MainActivity.this.getResources().getColor(R.color.count_players)), str.lastIndexOf(getString(R.string.letter_from_gray)), str.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            //spans.setSpan(new ForegroundColorSpan(MainActivity.this.getResources().getColor(R.color.colorAccent)), 0, 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            return spans;
        }
    }
    private class ProgressViewController { // класс для управления состоянием (вида) прогресса

        private float widthBetweenLvl; // ширина между первым и последним уровнем
        private float widthOneBlockLvl; // ширина одной оранжевой полоски
        private float ONE_LVL_WIDTH_LEFTRIGHT; // шаг перемещения указателя уровня для крайних полосок
        private float ONE_LVL_WIDTH_CENTER; // шаг перемещения указателя уровня для средних полосок

        public ProgressViewController() {
            int currentLevel = Player.getInstance().getLevel();
            /*ObjectAnimator levelBar = ObjectAnimator.ofFloat(imgLevelBar,"scaleX",0f,1f);
            levelBar.setDuration(1500);
            levelBar.start();*/

            /*int widthScreen = getWidthSreeen();
            widthBetweenLvl = widthScreen - widthScreen*0.2f - widthScreen*0.2f;
            widthOneBlockLvl = (widthBetweenLvl - (imgLvl2.getLayoutParams().width*3))/4 + 1;
            ONE_LVL_WIDTH_LEFTRIGHT = widthOneBlockLvl/5;
            ONE_LVL_WIDTH_CENTER = widthOneBlockLvl/4;*/
        }
        public int getWidthSreeen() {
            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            return size.x;
        }
    }
    private class LoadNewLevelTask extends AsyncTask<Void,Void,Void> { // отправляем данные о новым уровне, если в прошлый раз не было инета
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            StatisticsController statisticsController = new StatisticsController(MainActivity.this);
            try {
                statisticsController.sendNewLevel();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ErrorOnServerException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
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
                if(responseFromServer.getUpdateforce() > 0) {
                    typeUpdate = responseFromServer.getUpdateforce();
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
        private boolean isFirstLaunch = true;
        private String leaders;
        private ArrayList<String> newPlayersNames = new ArrayList<>(5);
        private ArrayList<Integer> newPlayersLevels = new ArrayList<>(5);
        private ArrayList<Integer> newPlayersCount = new ArrayList<>(5);

        public UpdateDataThread() { }

        @Override
        public void run() {
            while (true) {
                if(!isStop) {
                    if(!isFirstLaunch) {
                        try {
                            sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            sleep(3500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        isFirstLaunch = false;
                    }
                    RequestController.getInstance() // получем приз
                            .getJsonApi()
                            .getPrize("prize")
                            .enqueue(new Callback<ResponseFromServer>() {
                                @Override
                                public void onResponse(Call<ResponseFromServer> call, Response<ResponseFromServer> response) {
                                    try {
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
                                    } catch (NullPointerException ex) {
                                        Toast.makeText(MainActivity.this,"Prize error",Toast.LENGTH_SHORT).show();
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
                                        String oldLeaders = StoredData.getDataString(DATA_LEADERS,"0-0-...;0-0-...;0-0-...;0-0-...;0-0-...;");
                                        StoredData.saveData(DATA_LEADERS,leaders);
                                        String oneLevelLeaders[] = leaders.split(";");
                                        int oldLength = oldLeaders.split(";").length;
                                        for(int i = 0; i < oneLevelLeaders.length; i++) {
                                            newPlayersNames.add(i, oneLevelLeaders[i].split("-")[2]);
                                            newPlayersCount.add(i, Integer.valueOf(oneLevelLeaders[i].split("-")[1]));
                                            newPlayersLevels.add(i, Integer.valueOf(oneLevelLeaders[i].split("-")[0]));

                                            if(!newPlayersNames.get(i).equals(playersNames.get(i)) ||
                                                    newPlayersCount.get(i) != (playersCount.get(i)) ||
                                                    !newPlayersLevels.get(i).equals(playersLevels.get(i))) {
                                                playersNames.set(i,newPlayersNames.get(i));
                                                playersCount.set(i,newPlayersCount.get(i));
                                                playersLevels.set(i,newPlayersLevels.get(i));
                                                animController.animateChangeLeaders(i,playersNames.get(i),playersCount.get(i),playersLevels.get(i));
                                            }
                                        }
                                        for(int i = oneLevelLeaders.length, j = 0; j < (oldLength - oneLevelLeaders.length); j++) {
                                            playersNames.set(i,"...");
                                            playersCount.set(i,0);
                                            playersLevels.set(i,0);
                                            animController.animateChangeLeaders(i,"...",0,0);
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<ResponseFromServer> call, Throwable t) {
                                }
                            });
                } else return;
            }
        }
        public void toStop() { // остановить поток
            isStop = true;
        }
    }
}
