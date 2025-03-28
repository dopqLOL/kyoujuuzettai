package com.example.contentful_javasilver;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.contentful.java.cda.CDAEntry;
import com.example.contentful_javasilver.databinding.ActivityMainBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import kotlin.Unit;

public class QuizActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivityMainBinding binding;
    private List<Integer> rightAnswers;
    private int rightAnswerCount;
    private int quizCount = 1;
    private static final int QUIZ_COUNT = 5;
    private static final String ACCESS_TOKEN = BuildConfig.CONTENTFUL_ACCESS_TOKEN;
    private static final String SPACE_ID = BuildConfig.CONTENTFUL_SPACE_ID;
    private TextView codeBlock1;
    private ArrayList<CDAEntry> quizEntries = new ArrayList<>();
    private int currentQuizIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // TextViewの参照を取得
        codeBlock1 = binding.codeBlock;

        // Contentful APIクライアントを初期化
        ContentfulGetApi contentfulgetapi = new ContentfulGetApi(SPACE_ID, ACCESS_TOKEN);
        AsyncHelperCoroutines asyncHelper = new AsyncHelperCoroutines(contentfulgetapi);

        // Contentfulからデータを取得（エラーハンドリング付き）
        asyncHelper.fetchEntriesAsync("javaSilverQ",
            entries -> {
                runOnUiThread(() -> {
                    quizEntries.addAll(entries);
                    Collections.shuffle(quizEntries);
                    showNextQuiz();
                });
                return Unit.INSTANCE;
            },
            errorMessage -> {
                runOnUiThread(() -> {
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                });
                return Unit.INSTANCE;
            }
        );

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.answerBtn1.setOnClickListener(this);
        binding.answerBtn2.setOnClickListener(this);
        binding.answerBtn3.setOnClickListener(this);
        binding.answerBtn4.setOnClickListener(this);
    }

    private void showNextQuiz() {
        if (currentQuizIndex >= quizEntries.size()) {
            // クイズが終了した場合の処理
            showResult();
            return;
        }

        // クイズカウントラベルを更新
        binding.countLabel.setText(getString(R.string.count_label, quizCount));

        CDAEntry currentQuiz = quizEntries.get(currentQuizIndex);
        
        // 問題文を表示
        binding.questionLabel.setText(currentQuiz.getField("questionText"));
        
        // コードブロックの表示
        String code = currentQuiz.getField("code");
        if (code != null && !code.isEmpty()) {
            codeBlock1.setText(code);
            codeBlock1.setVisibility(View.VISIBLE);
        } else {
            codeBlock1.setVisibility(View.GONE);
        }

        // 選択肢を取得
        List<String> choices = currentQuiz.getField("choices");
        List<Double> rawAnswers = currentQuiz.getField("answer");
        rightAnswers = new ArrayList<>();
        for (Double answer : rawAnswers) {
            rightAnswers.add(answer.intValue());
        }

        // 正解のインデックスをログ出力
        android.util.Log.d("QuizActivity", "正解のインデックス: " + rightAnswers.toString());

        // 解答ボタンに選択肢を表示
        binding.answerBtn1.setText(choices.get(0));
        binding.answerBtn2.setText(choices.get(1));
        binding.answerBtn3.setText(choices.get(2));
        binding.answerBtn4.setText(choices.get(3));
    }

    private void showResult() {
        String resultMessage = String.format("クイズ終了！\n正解数: %d/%d", rightAnswerCount, QUIZ_COUNT);
        new MaterialAlertDialogBuilder(this)
                .setTitle("結果")
                .setMessage(resultMessage)
                .setPositiveButton("OK", (dialogInterface, i) -> finish())
                .setCancelable(false)
                .show();
    }

    @Override
    public void onClick(View view) {
        Button answerBtn = findViewById(view.getId());
        String btnText = answerBtn.getText().toString();
        
        CDAEntry currentQuiz = quizEntries.get(currentQuizIndex);
        List<String> choices = currentQuiz.getField("choices");
        
        // 選択されたボタンのインデックスを取得
        int selectedIndex = choices.indexOf(btnText);
        
        String alertTitle;
        String explanation = currentQuiz.getField("explanation");
        
        if (rightAnswers.contains(selectedIndex)) {
            alertTitle = "正解!";
            rightAnswerCount++;
        } else {
            alertTitle = "不正解...";
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle(alertTitle)
                .setMessage("解説: " + explanation)
                .setPositiveButton("OK", (dialogInterface, i) -> {
                    if (quizCount == QUIZ_COUNT) {
                        showResult();
                    } else {
                        quizCount++;
                        currentQuizIndex++;
                        showNextQuiz();
                    }
                })
                .setCancelable(false)
                .show();
    }
}

