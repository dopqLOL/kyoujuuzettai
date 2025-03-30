package com.example.contentful_javasilver;

import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.contentful_javasilver.data.QuizEntity;
import com.example.contentful_javasilver.databinding.FragmentQuizBinding;
import com.example.contentful_javasilver.viewmodels.QuizViewModel;

import java.util.List;

public class QuizFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "QuizFragment"; // Log用TAG
    private FragmentQuizBinding binding; // ViewBinding
    private QuizViewModel viewModel;
    private List<Integer> rightAnswers;
    private String initialQid; // 初期表示用のqid
    private String category; // カテゴリ選択からの遷移用

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentQuizBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ViewModelの取得 (Activityスコープ)
        viewModel = new ViewModelProvider(requireActivity()).get(QuizViewModel.class);

        // 引数からデータを取得
        if (getArguments() != null) {
            initialQid = getArguments().getString("qid");
            category = getArguments().getString("category"); // カテゴリも受け取る
            Log.d(TAG, "Arguments received - qid: " + initialQid + ", category: " + category);
        }

        // UIの初期設定
        setupObservers();
        setupClickListeners();

        // クイズをロード
        if (initialQid != null && !initialQid.isEmpty()) {
            Log.d(TAG, "Loading quiz by initial QID: " + initialQid);
            viewModel.loadQuizByQid(initialQid); // 単一qid読み込みメソッドを呼ぶ
        } else if (category != null && !category.isEmpty()) {
            Log.d(TAG, "Loading quizzes by category: " + category);
            viewModel.loadQuizzesByCategory(category); // カテゴリ指定読み込み
        } else {
            Log.d(TAG, "Loading random quizzes (default)");
            viewModel.loadRandomQuizzes(5); // デフォルトはランダム5件
        }
    }

    private void setupObservers() {
        // 現在のクイズの監視
        viewModel.getCurrentQuiz().observe(getViewLifecycleOwner(), quiz -> {
            if (quiz != null) {
                Log.d(TAG, "Observer received new quiz: " + quiz.getQid());
                updateQuizUI(quiz);
            } else {
                Log.w(TAG, "Observer received null quiz");
                // クイズがない場合の処理（例：ローディング表示、エラー表示など）
            }
        });

        // エラーメッセージの監視
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Log.e(TAG, "Error message observed: " + error);
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show();
                // エラー発生時は前の画面に戻るなどの処理を追加しても良い
                // requireActivity().getSupportFragmentManager().popBackStack();
            }
        });

        // ローディング状態の監視
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            Log.d(TAG, "Loading state changed: " + isLoading);
            // ローディングインジケーターの表示/非表示
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            // ローディング中は操作不可にするなど
            binding.quizContentGroup.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        });

        // クイズ終了通知の監視は不要になったため削除
        // viewModel.getQuizFinished().observe(getViewLifecycleOwner(), finished -> {
        //     if (finished != null && finished) {
        //         Log.d(TAG, "Quiz finished observed");
        //         // showResult(); // ダイアログは表示しない
        //         // 代わりに最後の問題で「次へ」を押したときに前の画面に戻るなどの処理が必要か検討
        //         // 現状は「次へ」ボタンのonClick内で処理
        //     }
        // });
    }

    private void setupClickListeners() {
        binding.answerBtn1.setOnClickListener(this);
        binding.answerBtn2.setOnClickListener(this);
        binding.answerBtn3.setOnClickListener(this);
        binding.answerBtn4.setOnClickListener(this);
        binding.nextButton.setOnClickListener(this);
        binding.backButton.setOnClickListener(this); // Add listener for back button
    }

    private void updateQuizUI(QuizEntity quiz) {
        Log.d(TAG, "Updating UI for quiz: " + quiz.getQid());
        // UI要素をリセット
        binding.explanationCard.setVisibility(View.GONE); // explanation_card を非表示にする
        binding.nextButton.setVisibility(View.GONE);
        setAnswerButtonsEnabled(true); // 回答ボタンを有効化
        resetButtonStyles(); // ボタンのスタイルをリセット

        // qid表示 (フォーマット変更)
        binding.countLabel.setText(quiz.getQid()); // "(qid)" から"qid"形式で表示に変更した

        // 質問文
        binding.questionLabel.setText(quiz.getQuestionText());

        // コードブロック (CardViewの表示/非表示を制御)
        String code = quiz.getCode();
        Log.d(TAG, "Quiz code: '" + code + "'"); // Log the code content
        if (code != null && !code.isEmpty()) {
            Log.d(TAG, "Setting code block text and making card VISIBLE");
            binding.codeBlock.setText(code); // TextViewにテキストを設定
            binding.codeBlockCard.setVisibility(View.VISIBLE); // CardViewを表示
        } else {
            Log.d(TAG, "Code is null or empty, making card GONE");
            binding.codeBlockCard.setVisibility(View.GONE); // CardViewを非表示
        }

        // 選択肢と正解
        List<String> choices = quiz.getChoices();
        rightAnswers = quiz.getAnswer(); // 正解インデックスリストを保持

        // ボタンのテキストと表示/非表示
        Button[] buttons = {binding.answerBtn1, binding.answerBtn2, binding.answerBtn3, binding.answerBtn4};
        for (int i = 0; i < buttons.length; i++) {
            if (choices != null && i < choices.size()) {
                buttons[i].setText(choices.get(i));
                buttons[i].setVisibility(View.VISIBLE);
            } else {
                buttons[i].setVisibility(View.GONE); // 選択肢が足りないボタンは非表示
            }
        }
    }

    // showResultメソッドは不要になったため削除
    // private void showResult() { ... }

    // 回答ボタンの有効/無効を切り替えるヘルパーメソッド
    private void setAnswerButtonsEnabled(boolean enabled) {
        Log.d(TAG, "Setting answer buttons enabled: " + enabled);
        binding.answerBtn1.setEnabled(enabled);
        binding.answerBtn2.setEnabled(enabled);
        binding.answerBtn3.setEnabled(enabled);
        binding.answerBtn4.setEnabled(enabled);
    }

    // ボタンのスタイルをデフォルトに戻す
    private void resetButtonStyles() {
        // MaterialButtonのデフォルトスタイルに戻す処理（必要なら）
        // 例: binding.answerBtn1.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.design_default_color_primary));
        // Material Design 3 のスタイルによっては不要な場合もある
    }

    // 正解・不正解のスタイルをボタンに適用
    private void highlightAnswers(int selectedIndex) {
        Button[] buttons = {binding.answerBtn1, binding.answerBtn2, binding.answerBtn3, binding.answerBtn4};
        QuizEntity currentQuiz = viewModel.getCurrentQuiz().getValue();
        if (currentQuiz == null || currentQuiz.getChoices() == null) return;

        List<String> choices = currentQuiz.getChoices();

        for (int i = 0; i < buttons.length; i++) {
            if (i >= choices.size()) continue; // 存在しない選択肢はスキップ

            if (rightAnswers != null && rightAnswers.contains(i)) {
                // 正解のボタン
                // buttons[i].setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.correct_color)); // 正解色
            } else if (i == selectedIndex) {
                // 選択したが不正解のボタン
                // buttons[i].setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.incorrect_color)); // 不正解色
            } else {
                // 選択されなかった不正解のボタン（スタイル変更なし or グレーアウトなど）
            }
        }
    }


    @Override
    public void onClick(View view) {
        int id = view.getId();
        Log.d(TAG, "onClick triggered for view ID: " + id);

        if (id == R.id.next_button) {
            Log.d(TAG, "Next button clicked");
            // 次へボタンが押された場合
            viewModel.moveToNextQuiz(); // ViewModelに次のシーケンシャルなクイズへの遷移を指示
            // 次の問題がない場合のエラーメッセージはViewModel内のerrorMessageで処理され、ObserverでToast表示される想定
        } else if (id == R.id.backButton) {
            // 戻るボタンが押された場合
            Log.d(TAG, "Back button clicked");
            // Jetpack Navigationを使用して前の画面に戻る
            try {
                Navigation.findNavController(requireView()).popBackStack();
            } catch (Exception e) {
                Log.e(TAG, "Failed to navigate back", e);
                // Fallback or error handling if needed
            }
        } else if (id == R.id.answerBtn1 || id == R.id.answerBtn2 || id == R.id.answerBtn3 || id == R.id.answerBtn4) {
            // 回答ボタンが押された場合
            Button answerBtn = (Button) view;
            String btnText = answerBtn.getText().toString();
            Log.d(TAG, "Answer button clicked: " + btnText);

            QuizEntity currentQuiz = viewModel.getCurrentQuiz().getValue();
            if (currentQuiz == null) {
                Log.w(TAG, "Current quiz is null, cannot process answer.");
                return;
            }

            List<String> choices = currentQuiz.getChoices();
            int selectedIndex = -1;
            if (choices != null) {
                 selectedIndex = choices.indexOf(btnText);
            }
            if (selectedIndex == -1) {
                Log.w(TAG, "Selected choice text not found in current quiz choices.");
                return; // 選択肢が見つからない
            }

            Log.d(TAG, "Selected index: " + selectedIndex);

            String explanation = currentQuiz.getExplanation();

            // 正誤判定
            boolean isCorrect = rightAnswers != null && rightAnswers.contains(selectedIndex);
            Log.d(TAG, "Answer is correct: " + isCorrect);
            if (isCorrect) {
                viewModel.incrementCorrectAnswerCount();
            }

            // 正解・不正解をハイライト（任意）
            highlightAnswers(selectedIndex);

            // 正解/不正解テキストを作成
            String resultText;
            int resultColor;
            if (isCorrect) {
                resultText = "正解！";
                resultColor = ContextCompat.getColor(requireContext(), R.color.correct_color);
            } else {
                resultText = "不正解";
                resultColor = ContextCompat.getColor(requireContext(), R.color.incorrect_color);
            }
            SpannableString spannableResult = new SpannableString(resultText);
            spannableResult.setSpan(new ForegroundColorSpan(resultColor), 0, resultText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            // 解説文と結合して表示
            String originalExplanation = (explanation != null && !explanation.isEmpty()) ? explanation : "解説はありません。";
            binding.explanationTextView.setText(spannableResult);
            binding.explanationTextView.append("\n\n"); // 改行を追加
            binding.explanationTextView.append(originalExplanation);
            binding.explanationCard.setVisibility(View.VISIBLE); // explanation_card を表示する

            // 回答ボタンを無効化
            setAnswerButtonsEnabled(false);

            // 次へボタンを表示
            binding.nextButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView called");
        binding = null; // ViewBindingの参照を解放
    }
}
