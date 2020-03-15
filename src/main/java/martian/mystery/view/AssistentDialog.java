package martian.mystery.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import martian.mystery.R;

public class AssistentDialog extends DialogFragment {


    public static final int DIALOG_RULES = 1;
    public static final int DIALOG_ALERT_INTERNET = 2;
    public static final int DIALOG_ALERT_LAST_LVL = 3;
    public static final int DIALOG_REVIEW = 4;
    public static final int DIALOG_SERVER_ERROR = 5;
    public static final int DIALOG_UPDATE_APP = 6;
    public static final int DIALOG_UPDATE_APP_TECH = 7;
    public static final int DIALOG_CHECK_ON_SERRVER_ALERT = 8;
    public static final int DIALOG_NO_TELEGRAM = 9;
    int typeDialog;

    public AssistentDialog(int typeDialog) {
        this.typeDialog = typeDialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        switch (typeDialog) {
            case DIALOG_RULES: {
                builder = new AlertDialog.Builder(getActivity(), R.style.CustomAlertDialog);
                builder.setView(inflater.inflate(R.layout.dialog_rules, null))
                        .setPositiveButton(R.string.dialog_review_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String appPackageName = getActivity().getPackageName();
                                try {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                                } catch (android.content.ActivityNotFoundException anfe) {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                                }
                            }
                        })
                        .setNeutralButton(R.string.read_rules_ok_btn, null);
                break;
            }
            case DIALOG_ALERT_INTERNET: {
                builder.setTitle(R.string.no_internet)
                        .setPositiveButton(R.string.read_rules_ok_btn, null);
                break;
            }
            case DIALOG_SERVER_ERROR: {
                builder.setTitle(R.string.no_server)
                        .setPositiveButton(R.string.read_rules_ok_btn, null);
                break;
            }
            case DIALOG_CHECK_ON_SERRVER_ALERT: {
                builder.setTitle(R.string.alert_check_on_server_title)
                        .setMessage(R.string.alert_check_on_server)
                        .setPositiveButton(R.string.read_rules_ok_btn, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                break;
            }
            case DIALOG_ALERT_LAST_LVL: {
                builder.setTitle(R.string.alert_last_lvl_title)
                        .setMessage(R.string.alert_last_lvl)
                        .setPositiveButton(R.string.read_rules_ok_btn, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                break;
            }
            case DIALOG_NO_TELEGRAM: {
                builder = new AlertDialog.Builder(getActivity(), R.style.CustomAlertDialog);
                builder.setMessage(R.string.there_is_no_telegram)
                        .setPositiveButton(R.string.install_telegram_btn, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=org.telegram.messenger")));
                                } catch (android.content.ActivityNotFoundException anfe) {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=org.telegram.messenger")));
                                }
                            }
                        })
                        .setNeutralButton(R.string.dialog_review_no, null);
                break;
            }
            case DIALOG_REVIEW: {
                builder.setMessage(R.string.dialog_review_mes)
                        .setPositiveButton(R.string.dialog_review_yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                String appPackageName = getActivity().getPackageName();
                                try {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                                } catch (android.content.ActivityNotFoundException anfe) {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                                }
                            }
                        })
                        .setNegativeButton(R.string.dialog_review_no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                break;
            }
            case DIALOG_UPDATE_APP: {
                builder.setMessage(R.string.force_update_season)
                        .setPositiveButton(R.string.update_btn, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                String appPackageName = getActivity().getPackageName();
                                try {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                                } catch (android.content.ActivityNotFoundException anfe) {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                                }
                            }
                        });
                break;
            }
            case DIALOG_UPDATE_APP_TECH: {
                builder.setMessage(R.string.force_update_tech)
                        .setPositiveButton(R.string.update_btn, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                String appPackageName = getActivity().getPackageName();
                                try {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                                } catch (android.content.ActivityNotFoundException anfe) {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                                }
                            }
                        });
                break;
            }
        }

        return builder.create();
    }
}
