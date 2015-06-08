package com.clancy.cleartoken2;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;

import android.app.Activity;
import android.app.ProgressDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.view.View;

import android.widget.ImageButton;
import android.widget.ProgressBar;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;

import java.nio.channels.Selector;


/**
 * Created by jrb on 5/14/2015.
 */
public class BackgroundTask extends AsyncTask<Void, Void, Void> {
    private ProgressDialog dialog;
    public String ProgMsg = "Doing something...";
    public Button thisButton;

    public BackgroundTask(DeviceControlActivity activity) {
        dialog = new ProgressDialog(activity);
    }

    @Override
    protected void onPreExecute() {
        // dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);  // Alternate ProgressBar Style

        // --- Do NOT Show Activity ProgressBar ---
        //dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);  // Default ProgressBar Style
        //dialog.setIndeterminate(true);
        //dialog.setMessage(ProgMsg);
        //dialog.show();

        // --- JRB ---
/*        if (ProgMsg.contains("Talking to Unit") || (ProgMsg.contains("Exiting")) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
            View dialogView = inflater.inflate(R.drawable.roundedbutton, null);
            if (ProgMsg.contains("Talking to Unit")) {
                thisButton = dialogView.findViewById(R.id.pay_button);
            }

            if (ProgMsg.contains("Exiting")) {
                thisButton = dialogView.findViewById(R.id.exit_button);
            }
            thisButton.setBackgroundColor(R.color.payButtonLight);
        }*/
    }

    @Override
    protected void onPostExecute(Void result) {
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            Thread.sleep(3000);   // 3 Seconds
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
}
