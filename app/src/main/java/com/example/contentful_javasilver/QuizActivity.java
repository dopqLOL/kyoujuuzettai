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
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.contentful.java.cda.CDAEntry;
import com.example.contentful_javasilver.data.QuizDatabase;
import com.example.contentful_javasilver.data.QuizEntity;
import com.example.contentful_javasilver.databinding.ActivityMainBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
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
    private QuizDatabase db;
    private List<QuizEntity> quizEntities = new ArrayList<>();
    private int currentQuizIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // TextViewの参照を取得
        codeBlock1 = binding.codeBlock;

        // Roomデータベースの初期化
        db = QuizDatabase.getDatabase(this);

        // データベースにデータがあるかチェック
        db.quizDao().getQuizCount().observe(this, count -> {
            if (count == 0) {
                // 初回起動時はContentfulからデータを取得
                fetchFromContentful();
            } else {
                // データベースからクイズを取得
                loadQuizzesFromDatabase();
            }
        });

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

    private void fetchFromContentful() {
        ContentfulGetApi contentfulgetapi = new ContentfulGetApi(SPACE_ID, ACCESS_TOKEN);
        AsyncHelperCoroutines asyncHelper = new AsyncHelperCoroutines(contentfulgetapi);

        asyncHelper.fetchEntriesAsync("javaSilverQ",
            entries -> {
                runOnUiThread(() -> {
                    List<QuizEntity> entities = new ArrayList<>();
                    for (CDAEntry entry : entries) {
                        // Double型のanswerをInteger型に変換
                        List<Double> rawAnswers = entry.getField("answer");
                        List<Integer> intAnswers = new ArrayList<>();
                        for (Double answer : rawAnswers) {
                            intAnswers.add(answer.intValue());
                        }

                        QuizEntity entity = new QuizEntity(
                            entry.getField("qid"),
                            entry.getField("chapter"),
                            entry.getField("category"),
                            entry.getField("questioncategory"),
                            entry.getField("difficulty"),
                            entry.getField("code"),
                            entry.getField("questionText"),
                            entry.getField("choices"),
                            intAnswers,
                            entry.getField("explanation")
                        );
                        entities.add(entity);
                    }
                    
                    // データベースに保存
                    new Thread(() -> {
                        db.quizDao().insertAll(entities);
                        loadQuizzesFromDatabase();
                    }).start();
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
    }

    private void loadQuizzesFromDatabase() {
        db.quizDao().getRandomQuizzes(QUIZ_COUNT).observe(this, quizzes -> {
            quizEntities = quizzes;
            if (!quizEntities.isEmpty()) {
                showNextQuiz();
            }
        });
    }

    private void showNextQuiz() {
        if (currentQuizIndex >= quizEntities.size()) {
            showResult();
            return;
        }

        binding.countLabel.setText(getString(R.string.count_label, quizCount));

        QuizEntity currentQuiz = quizEntities.get(currentQuizIndex);
        
        binding.questionLabel.setText(currentQuiz.getQuestionText());
        
        String code = currentQuiz.getCode();
        if (code != null && !code.isEmpty()) {
            codeBlock1.setText(code);
            codeBlock1.setVisibility(View.VISIBLE);
        } else {
            codeBlock1.setVisibility(View.GONE);
        }

        List<String> choices = currentQuiz.getChoices();
        rightAnswers = currentQuiz.getAnswer();

        android.util.Log.d("QuizActivity", "正解のインデックス: " + rightAnswers.toString());

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
        
        QuizEntity currentQuiz = quizEntities.get(currentQuizIndex);
        List<String> choices = currentQuiz.getChoices();
        int selectedIndex = choices.indexOf(btnText);
        
        String alertTitle;
        String explanation = currentQuiz.getExplanation();
        
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

