package martian.mystery.view;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import martian.mystery.R;
import martian.mystery.controller.RequestController;
import martian.mystery.controller.UpdateDataController;
import martian.mystery.data.DataOfUser;
import martian.mystery.data.ResponseFromServer;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DoneFirstActivity extends AppCompatActivity {

    private EditText etNameWin;
    private ImageButton btnSendName;
    private TextView tvEmail;
    private Button btnSendReview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new GetEmailTask().execute(); // загружаем контакт для связи
        if(UpdateDataController.getInstance().nameIsSended()) {
            setContentView(R.layout.donefirst_activity);
            tvEmail = findViewById(R.id.tvEmailFinish);
            btnSendReview = findViewById(R.id.btnSendReview);
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
            tvEmail.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if(event.getAction() == MotionEvent.ACTION_DOWN) {
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("", tvEmail.getText().toString());
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(DoneFirstActivity.this, R.string.email_copy, Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
            });
        } else {
            setContentView(R.layout.donefirst_sendname_activity);
            tvEmail = findViewById(R.id.tvEmail);
            etNameWin = findViewById(R.id.etNameWin);
            btnSendName = findViewById(R.id.btnSendWin);
            btnSendReview = findViewById(R.id.btnSendReview);
            tvEmail.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if(event.getAction() == MotionEvent.ACTION_DOWN) {
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("", tvEmail.getText().toString());
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(DoneFirstActivity.this, R.string.email_copy, Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
            });
            btnSendName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String name = etNameWin.getText().toString().trim();
                    DataOfUser dataOfUser = new DataOfUser();
                    dataOfUser.setNameOfUser(name);
                    if(!name.equals("") && !name.equals("none")) {
                        RequestController.getInstance()
                                .getJsonApi()
                                .sendNameWinner(dataOfUser)
                                .enqueue(new Callback<Void>() {
                                    @Override
                                    public void onResponse(Call<Void> call, Response<Void> response) {
                                        UpdateDataController.getInstance().setNameIsSended(true);
                                        finish();
                                    }

                                    @Override
                                    public void onFailure(Call<Void> call, Throwable t) {
                                        UpdateDataController.getInstance().setNameIsSended(false);
                                        Toast.makeText(DoneFirstActivity.this,R.string.no_internet,Toast.LENGTH_SHORT);
                                    }
                                });
                    } else {
                        Toast.makeText(DoneFirstActivity.this,R.string.error_name,Toast.LENGTH_SHORT);
                        UpdateDataController.getInstance().setNameIsSended(false);
                    }

                }
            });
        }
    }

    private class GetEmailTask extends AsyncTask<Void,Void,String> {

        @Override
        protected String doInBackground(Void... voids) {
            while(true) {
                try {
                    ResponseFromServer response = RequestController
                            .getInstance()
                            .getJsonApi()
                            .getEmail("keyb")
                            .execute().body();
                    return response.getEmail();

                } catch (IOException e) {
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                    continue;
                }
            }
        }

        @Override
        protected void onPostExecute(String s) {
            tvEmail.setText(s);
        }
    }
}
