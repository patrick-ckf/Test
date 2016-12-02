package com.example.patrick.tumblrloader.other;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

import com.example.patrick.tumblrloader.R;

/**
 * Created by patrick on 2/12/2016.
 */

public class AppUtility extends Activity {
    public void show_alert_message(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

}
