package com.example.contentful_javasilver;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class LoadingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        TextView loadingText = findViewById(R.id.loadingText);
        
        // ローディングテキストのフェードイン/アウトアニメーション
        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(1000);
        fadeIn.setFillAfter(true);
        loadingText.startAnimation(fadeIn);

        // 3秒後にホーム画面に遷移
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(LoadingActivity.this, HomeActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        }, 3000);
    }
} 