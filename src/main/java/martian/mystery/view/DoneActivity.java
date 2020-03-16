package martian.mystery.view;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import martian.mystery.R;
import martian.mystery.controller.Progress;
import martian.mystery.controller.StoredData;

public class DoneActivity extends AppCompatActivity {

    private TextView tvPlace;
    private TextView tvFinalPhrase;
    private Button btnSendReview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.done_activity);

        int place = StoredData.getDataInt(StoredData.DATA_PLACE,2);
        tvPlace = findViewById(R.id.tvPlace);
        btnSendReview = findViewById(R.id.btnSendReview);
        tvPlace.setText(String.valueOf(place));
        tvFinalPhrase = findViewById(R.id.tvCongrut);
        tvFinalPhrase.setText(getPhrase(place));

        btnSendReview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        v.animate().scaleXBy(1).scaleX(0.9f).scaleYBy(1).scaleY(0.9f).setDuration(30).start();
                        v.animate().alphaBy(1.0f).alpha(0.9f).setDuration(80).start();
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        v.animate().scaleXBy(0.9f).scaleX(1).scaleYBy(0.9f).scaleY(1).setDuration(80).start();
                        v.animate().alphaBy(0.9f).alpha(1.0f).setDuration(80).start();
                        String appPackageName = getPackageName();
                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                        } catch (android.content.ActivityNotFoundException anfe) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                        }
                        break;
                    }
                }
                return true;
            }
        });
    }

    @Override
    public void onBackPressed() {
        int pastLevel = getIntent().getIntExtra("past_level",22);
        Intent intentMain = new Intent();
        intentMain.putExtra("differ_level", Progress.getInstance().getLevel() - pastLevel);
        try {
            setResult(Activity.RESULT_OK, intentMain);
            finish();
        } catch (NullPointerException ex) {
        }
        finish();
    }

    private String getPhrase(int place) {
        switch (place) {
            case 2: return getResources().getString(R.string.place2_congratulations);
            case 3: return getResources().getString(R.string.place3_congratulations);
            case 4: return getResources().getString(R.string.place4_congratulations);
            case 5: return getResources().getString(R.string.place5_congratulations);
            case 6: return getResources().getString(R.string.place6_congratulations);
            case 7: return getResources().getString(R.string.place7_congratulations);
            case 8: return getResources().getString(R.string.place8_congratulations);
            case 9: return getResources().getString(R.string.place9_congratulations);
            case 10: return getResources().getString(R.string.place10_congratulations);
            case 11: return getResources().getString(R.string.place11_congratulations);
            case 12: return getResources().getString(R.string.place12_congratulations);
            case 13: return getResources().getString(R.string.place13_congratulations);
            case 14: return getResources().getString(R.string.place14_congratulations);
            default: return getResources().getString(R.string.place_congratulations_default);
        }
    }
}
