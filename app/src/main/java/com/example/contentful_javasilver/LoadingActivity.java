package com.example.contentful_javasilver;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import com.contentful.java.cda.CDAEntry;
import com.example.contentful_javasilver.data.QuizDatabase;
import com.example.contentful_javasilver.data.QuizEntity;

import java.util.ArrayList;
import java.util.List;

import kotlin.Unit;

public class LoadingActivity extends AppCompatActivity {
    private static final String TAG = "LoadingActivity";
    private static final String ACCESS_TOKEN = BuildConfig.CONTENTFUL_ACCESS_TOKEN;
    private static final String SPACE_ID = BuildConfig.CONTENTFUL_SPACE_ID;
    private QuizDatabase database;
    private TextView loadingText;
    private int loadedCount = 0;
    private int totalEntries = 0;

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

        database = QuizDatabase.getDatabase(this);
        loadingText = findViewById(R.id.loadingText);
        
        // ローディングテキストのフェードインアニメーション
        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(1000);
        fadeIn.setFillAfter(true);
        loadingText.startAnimation(fadeIn);

        // データベースにデータがあるかチェック
        database.quizDao().getQuizCount().observe(this, count -> {
            Log.d(TAG, String.format("Current database entries: %d", count));
            if (count == 0) {
                // データがない場合はContentfulから取得
                fetchFromContentful();
            } else {
                // データがある場合は直接ホーム画面に遷移
                Log.d(TAG, "Using existing database data");
                new Handler(Looper.getMainLooper()).post(() -> {
                    Intent intent = new Intent(LoadingActivity.this, HomeActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    finish();
                });
            }
        });
    }

    private void fetchFromContentful() {
        loadingText.setText("データを読み込み中...\n0/0");

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

                totalEntries = entries.size();
                Log.d(TAG, String.format("Total entries from Contentful: %d", totalEntries));
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
                        List<Double> rawAnswers = entry.getField("answer");
                        String explanation = entry.getField("explanation");

                        // Double型のanswerをInteger型に変換
                        List<Integer> answers = new ArrayList<>();
                        if (rawAnswers != null) {
                            for (Double answer : rawAnswers) {
                                answers.add(answer.intValue());
                            }
                        }

                        if (qid != null && code != null) {
                            quizEntities.add(new QuizEntity(
                                qid, chapter, category, questionCategory,
                                difficulty, code, questionText,
                                choices, answers, explanation
                            ));
                            loadedCount++;
                            updateLoadingText();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing entry: " + entry.id(), e);
                    }
                }

                Log.d(TAG, String.format("Successfully processed %d entries", loadedCount));
                Log.d(TAG, String.format("Success rate: %.2f%%", (loadedCount * 100.0 / totalEntries)));

                // データベースに保存
                new Thread(() -> {
                    try {
                        database.quizDao().insertAll(quizEntities);
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
                Log.e(TAG, "Error fetching entries: " + errorMessage);
                runOnUiThread(() -> {
                    loadingText.setText("データの読み込みに失敗しました。\nアプリを再起動してください。");
                });
                return Unit.INSTANCE;
            }
        );
    }

    private void updateLoadingText() {
        runOnUiThread(() -> {
            loadingText.setText(String.format("データを読み込み中...\n%d/%d", loadedCount, totalEntries));
        });
    }
} 