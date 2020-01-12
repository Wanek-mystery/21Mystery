package martian.mystery;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


public class QuestionActivity extends AppCompatActivity { // активити, где отображаются загадки


    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);

        setInputMode(); // если экран маленький, то макет поднимается при фокусе клавиатуры
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        if(Progress.getInstance().getLevel() < 22) {
            fragmentTransaction.add(R.id.clQuestion, new QuestionFragment());
        } else if(Progress.getInstance().getLevel() == 22) {
            if(!UpdateDataController.getInstance().winnerIsChecked()) {
                fragmentTransaction.add(R.id.clQuestion, new QuestionFragment());
            } else if(!StoredData.getDataBool(StoredData.DATA_IS_WINNER)) {
                fragmentTransaction.add(R.id.clQuestion, new DoneFragment());
            } else {
                fragmentTransaction.add(R.id.clQuestion, new DoneFirstFragment());
            }
        }
        fragmentTransaction.commit();
    }

    private int getWidthSreeen() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.x;
    }
    public static float convertPixelsToDp(float px){
        return px / ((float) GetContextClass.getContext().getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }
    private void setInputMode() {
        int widthScreen = (int) convertPixelsToDp(getWidthSreeen());
        if(widthScreen < 360) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
    }
    @Override
    public void onBackPressed() {
        // при возвращении на главную активити отправляем разницу между уровнем, когда юзер был на главном экране, и уровнем на данный момент
        // это нужно для анимации изменения уровня на главной активити
        int pastLevel = getIntent().getIntExtra("level",1);
        Intent intentMain = new Intent();
        intentMain.putExtra("difflevel",Progress.getInstance().getLevel() - pastLevel);
        try {
            setResult(Activity.RESULT_OK,intentMain);
            finish();
        } catch (NullPointerException ex) {
        }
    }

    public void replaceFragment(Class fragmentClass) {
        Fragment fragment = null;
        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.clQuestion, fragment)
                .commit();
    }
}
