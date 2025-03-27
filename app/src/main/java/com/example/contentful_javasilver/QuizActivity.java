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

import kotlin.Unit;

public class QuizActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivityMainBinding binding;
    private String rightAnswer;
    private int rightAnswerCount;
    private int quizCount = 1;
    private static final int QUIZ_COUNT = 5;
    private static final String ACCESS_TOKEN = BuildConfig.CONTENTFUL_API_KEY;
    private static final String SPACE_ID = BuildConfig.CONTENTFUL_SPACE_ID;
    private TextView codeBlock1;
    String code1;

    private ArrayList<ArrayList<String>> quizArray = new ArrayList<>();
    private String[][] quizData = {
            // {"都道府県名", "正解", "選択肢１", "選択肢２", "選択肢３"}
            {"北海道", "札幌市", "長崎市", "福島市", "前橋市"},
            {"青森県", "青森市", "広島市", "甲府市", "岡山市"},
            {"岩手県", "盛岡市","大分市", "秋田市", "福岡市"},
            {"宮城県", "仙台市", "水戸市", "岐阜市", "福井市"},
            {"秋田県", "秋田市","横浜市", "鳥取市", "仙台市"},
            {"山形県", "山形市","青森市", "山口市", "奈良市"},
            {"福島県", "福島市", "盛岡市", "新宿区", "京都市"},
            {"茨城県", "水戸市", "金沢市", "名古屋市", "奈良市"},
            {"栃木県", "宇都宮市", "札幌市", "岡山市", "奈良市"},
            {"群馬県", "前橋市", "福岡市", "松江市", "福井市"},
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // TextViewの参照を取得
        codeBlock1 = findViewById(R.id.codeBlock);

        // Contentful APIクライアントを初期化
        ContentfulGetApi contentfulgetapi = new ContentfulGetApi(SPACE_ID, ACCESS_TOKEN);
        AsyncHelperCoroutines asyncHelper = new AsyncHelperCoroutines(contentfulgetapi);

        // Contentfulからデータを取得（エラーハンドリング付き）
        asyncHelper.fetchEntriesAsync("javaSilverQ", 
            entries -> {
                runOnUiThread(() -> {
                    for (CDAEntry entry : entries) {
                        String qid = entry.getField("qid");
                        // qidが"1-50"のエントリーを探す
                        if ("1-50".equals(qid)) {
                            // codeフィールドを取得
                            code1 = entry.getField("code");
                            // TextViewに表示
                            codeBlock1.setText(code1);
                            break;
                        }
                    }
                });
                return Unit.INSTANCE;
            },
            errorMessage -> {
                // エラー時の処理
                runOnUiThread(() -> {
                    // エラーメッセージを表示
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                    // エラー時は空のテキストを表示
                    codeBlock1.setText("");
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

        // quizDataからクイズ出題用のquizArrayを作成する
        for (String[] quizDatum : quizData) {

            // 新しいArrayListを準備
            ArrayList<String> tmpArray = new ArrayList<>();

            // クイズデータを追加
            tmpArray.add(quizDatum[0]);  // 都道府県名
            tmpArray.add(quizDatum[1]);  // 正解
            tmpArray.add(quizDatum[2]);  // 選択肢１
            tmpArray.add(quizDatum[3]);  // 選択肢２
            tmpArray.add(quizDatum[4]);  // 選択肢３

            // tmpArrayをquizArrayに追加する
            quizArray.add(tmpArray);
        }

        Collections.shuffle(quizArray);

        showNextQuiz();
    }

    private void showNextQuiz() {
        // クイズカウントラベルを更新
        binding.countLabel.setText(getString(R.string.count_label, quizCount));

        // quizArrayからクイズを１つ取り出す
        ArrayList<String> quiz = quizArray.get(0);

        // 問題文（都道府県名）を表示
        binding.questionLabel.setText(quiz.get(0));

        // 正解をrightAnswerにセット
        rightAnswer = quiz.get(1);

        // クイズ配列から問題文（都道府県名）を削除
        quiz.remove(0);

        // 正解と選択肢３つをシャッフル
        Collections.shuffle(quiz);

        // 解答ボタンに正解と選択肢３つを表示
        binding.answerBtn1.setText(quiz.get(0));
        binding.answerBtn2.setText(quiz.get(1));
        binding.answerBtn3.setText(quiz.get(2));
        binding.answerBtn4.setText(quiz.get(3));

        // このクイズをquizArrayから削除
        quizArray.remove(0);
    }

    @Override
    public void onClick(View view) {
        // どの解答ボタンが押されたか
        Button answerBtn = findViewById(view.getId());
        String btnText = answerBtn.getText().toString();

        String alertTitle;
        if (btnText.equals(rightAnswer)) {
            alertTitle = "正解!";
            rightAnswerCount++;
        } else {
            alertTitle = "不正解...";
        }

        // ダイアログを作成
        new MaterialAlertDialogBuilder(this)
                .setTitle(alertTitle)
                .setMessage("答え : " + rightAnswer)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (quizCount == QUIZ_COUNT) {
                            // 結果画面へ移動
                        } else {
                            quizCount++;
                            showNextQuiz();
                        }
                    }
                })
                .setCancelable(false)
                .show();
    }

}

