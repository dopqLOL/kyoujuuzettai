package com.example.contentful_javasilver;

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
import com.example.contentful_javasilver.data.QuizDatabase;
import com.example.contentful_javasilver.data.QuizEntity;
import com.example.contentful_javasilver.databinding.ActivityQuizBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import kotlin.Unit;

public class QuizActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivityQuizBinding binding;
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
    private AsyncHelperCoroutines asyncHelper;
    private List<QuizEntity> allQuizzes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityQuizBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // TextViewの参照を取得
        codeBlock1 = binding.codeBlock;

        // Roomデータベースの初期化
        db = QuizDatabase.getDatabase(this);
        asyncHelper = new AsyncHelperCoroutines(new ContentfulGetApi(SPACE_ID, ACCESS_TOKEN));

        // データベースにデータがあるかチェック
        loadQuizzesFromDatabase();

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
        asyncHelper.fetchEntriesAsync("javaSilverQ",
            entries -> {
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
                asyncHelper.insertQuizEntitiesAsync(db, entities,
                    () -> {
                        loadQuizzesFromDatabase();
                        return Unit.INSTANCE;
                    },
                    error -> {
                        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                        return Unit.INSTANCE;
                    }
                );
                return Unit.INSTANCE;
            },
            error -> {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                return Unit.INSTANCE;
            }
        );
    }

    private void loadQuizzesFromDatabase() {
        new Thread(() -> {
            try {
                allQuizzes = db.quizDao().getAllQuizzes();
                if (allQuizzes.isEmpty()) {
                    runOnUiThread(() -> fetchFromContentful());
                } else {
                    getRandomQuizzes();
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "データの読み込みに失敗しました", Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private void getRandomQuizzes() {
        List<QuizEntity> shuffled = new ArrayList<>(allQuizzes);
        Collections.shuffle(shuffled);
        quizEntities = shuffled.subList(0, Math.min(QUIZ_COUNT, shuffled.size()));
        runOnUiThread(this::showNextQuiz);
    }

    private void showNextQuiz() {
        QuizEntity currentQuiz = quizEntities.get(currentQuizIndex);
        
        // 変更があった場合のみ更新
        if (!currentQuiz.getQid().equals(binding.countLabel.getText())) {
            binding.countLabel.setText(currentQuiz.getQid());
        }
        
        if (!currentQuiz.getQuestionText().equals(binding.questionLabel.getText())) {
            binding.questionLabel.setText(currentQuiz.getQuestionText());
        }
        
        // コードブロックの表示/非表示の最適化
        String code = currentQuiz.getCode();
        if (code != null && !code.isEmpty()) {
            if (codeBlock1.getVisibility() != View.VISIBLE || !code.equals(codeBlock1.getText())) {
                codeBlock1.setText(code);
                codeBlock1.setVisibility(View.VISIBLE);
            }
        } else if (codeBlock1.getVisibility() != View.GONE) {
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (asyncHelper != null) {
            asyncHelper.cleanup();
        }
    }
}

