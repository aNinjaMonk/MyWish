package com.valentinewish.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.aspiration.photoviewer.R;
import com.valentinewish.MyApplication;

public class LaunchActivity extends Activity {
    int SPLASH_TIME = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_launch);
        new BackgroundTask().execute();
    }

    private class BackgroundTask extends AsyncTask {
        Intent intent;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            intent = new Intent(MyApplication.mContext,MainActivity.class);
        }

        @Override
        protected Object doInBackground(Object[] params) {
            try{
                Thread.sleep(SPLASH_TIME);
            }
            catch (InterruptedException e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
            finish();
        }
    }
}
