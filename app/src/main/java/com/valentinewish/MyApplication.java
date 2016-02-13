package com.valentinewish;

import android.app.Application;
import android.content.Context;

import com.shamanland.fonticon.FontIconTypefaceHolder;

/**
 * Created by abhijeet on 10/02/16.
 */
public class MyApplication extends Application{
    public static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();

        mContext = getApplicationContext();

        FontIconTypefaceHolder.init(getAssets(), "icons.ttf");
    }
}
