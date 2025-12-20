package com.example.qrscanner;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

public class SplashPage extends AppCompatActivity {

    private static final int SPLASH_DELAY = 3000; // 3 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_page);

        ProgressBar progressBar = findViewById(R.id.loading_line);
        progressBar.setIndeterminate(true); // for simple loading line

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(SplashPage.this, MainActivity.class));
            finish();
        }, SPLASH_DELAY);
    }
}
