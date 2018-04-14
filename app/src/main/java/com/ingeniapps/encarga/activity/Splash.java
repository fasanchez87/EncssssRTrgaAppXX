package com.ingeniapps.encarga.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;

import com.crashlytics.android.Crashlytics;
import com.ingeniapps.encarga.R;

import java.util.Timer;
import java.util.TimerTask;

import io.fabric.sdk.android.Fabric;

public class Splash extends AppCompatActivity
{
    private static final long SPLASH_SCREEN_DELAY = 3000;
    Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        //getActionBar().hide();
        setContentView(R.layout.activity_splash);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        TimerTask task = new TimerTask()
        {
            @Override
            public void run()
            {
                //Intent intent = new Intent(Splash.this, Principal.class);
                Intent intent = new Intent(Splash.this, Login.class);
                startActivity(intent);
                finish();
            }
        };

        // Simulate a long loading process on application startup.
        timer = new Timer();
        timer.schedule(task, SPLASH_SCREEN_DELAY);

    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        timer.cancel();

    }
}
