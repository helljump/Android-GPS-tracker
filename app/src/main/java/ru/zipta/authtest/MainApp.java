package ru.zipta.authtest;

import android.app.Application;
import android.content.Intent;

/**
 * Created by User on 06.08.2015.
 */
public class MainApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        //test
        startService(new Intent(this, NetService.class));
        startService(new Intent(this, GPSService.class));
    }
}
