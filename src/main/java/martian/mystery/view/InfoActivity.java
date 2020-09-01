package martian.mystery.view;

import android.animation.ObjectAnimator;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;

import martian.mystery.BuildConfig;
import martian.mystery.R;
import martian.mystery.controllers.RequestController;
import martian.mystery.data.ResponseFromServer;

public class InfoActivity extends AppCompatActivity {

    private Button btnTelegram;
    private TextView tvInfoUpdate;
    private Button btnUpdate;
    private Button btnReview;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        btnTelegram = findViewById(R.id.btnTelegram);
        tvInfoUpdate = findViewById(R.id.tvInfoUpdate);
        btnUpdate = findViewById(R.id.btnUpdateApp);
        btnReview = findViewById(R.id.btnReview);

        btnReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String appPackageName = getPackageName();
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                }
            }
        });
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String appPackageName = getPackageName();
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                }
            }
        });
        if(btnTelegram != null) { // если кнопка телеграма есть в этой локализации
            btnTelegram.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("tg://resolve?domain=twenty_one_mystery"));
                        startActivity(intent);
                    } catch (ActivityNotFoundException ex) {
                        AssistentDialog assistentDialog = new AssistentDialog(AssistentDialog.DIALOG_NO_TELEGRAM);
                        assistentDialog.show(getSupportFragmentManager(),null);
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("", "twenty_one_mystery");
                        clipboard.setPrimaryClip(clip);
                    }
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        new CheckUpdateTask().execute();
    }

    private class CheckUpdateTask extends AsyncTask<Void,Void,Boolean> { // проверяет принудительные обновления

        int typeUpdate; // тип актуальности версии

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                ResponseFromServer responseFromServer = RequestController.getInstance()
                        .getJsonApi()
                        .checkSoftUpdate(BuildConfig.VERSION_CODE)
                        .execute().body();
                if(responseFromServer != null) {
                    typeUpdate = responseFromServer.getUpdating();
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
                    case 0: { // версия актуальна
                        tvInfoUpdate.setText(getString(R.string.current_version));
                        btnUpdate.setClickable(false);
                        ObjectAnimator tvInfo = ObjectAnimator.ofFloat(tvInfoUpdate,"alpha",0f,1f);
                        tvInfo.setDuration(800);
                        tvInfo.start();
                        break;
                    }
                    case 1: { // версия не актуальна
                        tvInfoUpdate.setText(R.string.older_version);
                        tvInfoUpdate.setTextColor(getResources().getColor(R.color.warning));
                        btnUpdate.setAlpha(1f);
                        btnUpdate.setClickable(true);
                        ObjectAnimator tvInfo = ObjectAnimator.ofFloat(tvInfoUpdate,"alpha",0f,1f);
                        tvInfo.setDuration(800);
                        tvInfo.start();
                        break;
                    }
                }
            }
        }
    }
}
