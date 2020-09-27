package com.eschava.firenotify;

import android.app.Application;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Log.init(this);
    }
}
