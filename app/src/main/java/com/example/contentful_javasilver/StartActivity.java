package com.example.contentful_javasilver;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import androidx.appcompat.app.AppCompatActivity;

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        View rootView = findViewById(R.id.rootLayout);
        rootView.setOnClickListener(v -> {
            // クリックイベントの重複を防ぐ
            rootView.setClickable(false);

            // フェードアウトアニメーション
            AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
            fadeOut.setDuration(300);
            fadeOut.setFillAfter(true);
            fadeOut.setFillEnabled(true);
            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                private boolean hasEnded = false;

                @Override
                public void onAnimationStart(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (!hasEnded) {
                        hasEnded = true;
                        // ロード画面に遷移
                        Intent intent = new Intent(StartActivity.this, LoadingActivity.class);
                        startActivity(intent);
                        overridePendingTransition(0, 0);
                        finish();
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
            rootView.startAnimation(fadeOut);
        });
    }
} 