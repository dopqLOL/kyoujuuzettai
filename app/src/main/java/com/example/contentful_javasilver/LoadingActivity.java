package com.example.contentful_javasilver;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.contentful.java.cda.CDAEntry;
import com.example.contentful_javasilver.data.AppDatabase;
import com.example.contentful_javasilver.data.QuizEntity;

import java.util.ArrayList;
import java.util.List;

import kotlin.Unit;

public class LoadingActivity extends AppCompatActivity {
    private static final String TAG = "LoadingActivity";
    private static final String ACCESS_TOKEN = BuildConfig.CONTENTFUL_ACCESS_TOKEN;
    private static final String SPACE_ID = BuildConfig.CONTENTFUL_SPACE_ID;
    private AppDatabase database;
    private TextView loadingText;
    private int loadedCount = 0;
    private static final int TOTAL_ENTRIES = 50;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        // Contentfulの認証情報をチェック
        if (SPACE_ID == null || SPACE_ID.isEmpty() || ACCESS_TOKEN == null || ACCESS_TOKEN.isEmpty()) {
            Log.e(TAG, "Contentful credentials are not properly configured");
            Log.e(TAG, "SPACE_ID: " + (SPACE_ID == null ? "null" : SPACE_ID));
            Log.e(TAG, "ACCESS_TOKEN: " + (ACCESS_TOKEN == null ? "null" : ACCESS_TOKEN));
            loadingText = findViewById(R.id.loadingText);
            loadingText.setText("Contentfulの設定が正しくありません。\nlocal.propertiesを確認してください。");
            return;
        }

        database = AppDatabase.getDatabase(this);
        loadingText = findViewById(R.id.loadingText);
        
        // ローディングテキストのフェードインアニメーション
        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(1000);
        fadeIn.setFillAfter(true);
        loadingText.startAnimation(fadeIn);

        // Contentfulからデータを取得して保存
        ContentfulGetApi contentfulgetapi = new ContentfulGetApi(SPACE_ID, ACCESS_TOKEN);
        AsyncHelperCoroutines asyncHelper = new AsyncHelperCoroutines(contentfulgetapi);

        asyncHelper.fetchEntriesAsync("javaSilverQ", 
            entries -> {
                if (entries == null || entries.isEmpty()) {
                    Log.e(TAG, "No entries found");
                    Log.e(TAG, "Contentful API Response: entries is " + (entries == null ? "null" : "empty"));
                    runOnUiThread(() -> {
                        loadingText.setText("データが見つかりませんでした。\nアプリを再起動してください。");
                    });
                    return Unit.INSTANCE;
                }

                List<QuizEntity> quizEntities = new ArrayList<>();
                
                for (CDAEntry entry : entries) {
                    try {
                        String qid = entry.getField("qid");
                        String chapter = entry.getField("chapter");
                        String category = entry.getField("category");
                        String questionCategory = entry.getField("questionCategory");
                        String difficulty = entry.getField("difficulty");
                        String code = entry.getField("code");
                        String questionText = entry.getField("questionText");
                        List<String> choices = entry.getField("choices");
                        List<Integer> answer = entry.getField("answer");
                        String explanation = entry.getField("explanation");

                        // 各フィールドの値をログ出力
                        Log.d(TAG, String.format("Entry %s: qid=%s, chapter=%s, category=%s", 
                            entry.id(), qid, chapter, category));

                        if (qid != null && code != null) {
                            quizEntities.add(new QuizEntity(
                                qid, chapter, category, questionCategory,
                                difficulty, code, questionText,
                                choices, answer, explanation
                            ));
                            loadedCount++;
                            updateLoadingText();
                        } else {
                            Log.w(TAG, String.format("Skipping entry %s: qid=%s, code=%s", 
                                entry.id(), qid, code));
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing entry: " + entry.id(), e);
                    }
                }

                Log.d(TAG, String.format("Successfully processed %d entries", loadedCount));

                // データベースに保存
                new Thread(() -> {
                    try {
                        database.quizDao().deleteAll(); // 既存データを削除
                        database.quizDao().insertAll(quizEntities); // 新しいデータを挿入
                        Log.d(TAG, "Successfully saved entries to database");
                        
                        // メインスレッドでホーム画面に遷移
                        new Handler(Looper.getMainLooper()).post(() -> {
                            Intent intent = new Intent(LoadingActivity.this, HomeActivity.class);
                            startActivity(intent);
                            overridePendingTransition(0, 0);
                            finish();
                        });
                    } catch (Exception e) {
                        Log.e(TAG, "Error saving to database", e);
                        runOnUiThread(() -> {
                            loadingText.setText("データの保存に失敗しました。\nアプリを再起動してください。");
                        });
                    }
                }).start();

                return Unit.INSTANCE;
            },
            errorMessage -> {
                // エラー時の処理
                Log.e(TAG, "Error fetching entries: " + errorMessage);
                Log.e(TAG, "Contentful API Error Details: " + errorMessage);
                runOnUiThread(() -> {
                    loadingText.setText("データの読み込みに失敗しました。\nアプリを再起動してください。");
                });
                return Unit.INSTANCE;
            }
        );
    }

    private void updateLoadingText() {
        runOnUiThread(() -> {
            loadingText.setText(String.format("データを読み込み中...\n%d/%d", loadedCount, TOTAL_ENTRIES));
        });
    }
} 