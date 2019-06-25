package com.example.zlt;

import android.app.Application;

public class MyApp extends Application {
    public static MyApp instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }
}
