package com.example.contentful_javasilver;

import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox; // CheckBoxをインポート
import android.widget.CompoundButton; // CompoundButtonをインポート
import android.widget.Toast;

import androidx.annotation.ColorInt; // ColorIntをインポート
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.contentful_javasilver.data.QuizEntity;
import com.example.contentful_javasilver.data.QuizEntity;
import com.example.contentful_javasilver.databinding.FragmentQuizBinding;
import com.example.contentful_javasilver.viewmodels.QuizViewModel;

import java.util.ArrayList; // ArrayListをインポート
import java.util.Collections; // Collectionsをインポート
import java.util.HashSet; // HashSetをインポート
import java.util.List;
import java.util.Set; // Setをインポート

public class QuizFragment extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private static final String TAG = "QuizFragment";
    private FragmentQuizBinding binding;
    private QuizViewModel viewModel;
    private List<Integer> rightAnswers;
    private List<CheckBox> answerCheckBoxes;
    private List<Button> answerButtons; // Buttonのリストを追加
    private String initialQid;
    private String category;
    private boolean isMultipleChoice = false; // 回答形式フラグ
    private boolean isRandomMode = false; // ランダムモードフラグ

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentQuizBinding.inflate(inflater, container, false);
        // リストの初期化は onViewCreated で行う (bindingが利用可能なため)
        return binding.getRoot();
    }

     @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ViewModelの取得 (Activityスコープ)
        viewModel = new ViewModelProvider(requireActivity()).get(QuizViewModel.class);

        // 引数からデータを取得 (Safe Argsを使用)
        if (getArguments() != null) {
            QuizFragmentArgs args = QuizFragmentArgs.fromBundle(getArguments());
            initialQid = args.getQid(); // Safe Argsからqidを取得
            isRandomMode = args.getIsRandomMode(); // Safe ArgsからisRandomModeを取得
            // category は ProblemListFragment からは渡されない想定
            // category = args.getCategory(); // 必要であればnav_graphと遷移元で定義・設定
            Log.d(TAG, "Arguments received - qid: " + initialQid + ", isRandomMode: " + isRandomMode);
        }

        // リストの初期化
        initializeAnswerControlsLists();

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

    // 回答コントロール（ButtonとCheckBox）のリストを初期化
    private void initializeAnswerControlsLists() {
        answerButtons = new ArrayList<>();
        answerButtons.add(binding.answerBtn1);
        answerButtons.add(binding.answerBtn2);
        answerButtons.add(binding.answerBtn3);
        answerButtons.add(binding.answerBtn4);

        answerCheckBoxes = new ArrayList<>();
        answerCheckBoxes.add(binding.answerCheck1);
        answerCheckBoxes.add(binding.answerCheck2);
        answerCheckBoxes.add(binding.answerCheck3);
        answerCheckBoxes.add(binding.answerCheck4);
    }


    private void setupClickListeners() {
        // 単一回答ボタン
        for (Button btn : answerButtons) {
            btn.setOnClickListener(this);
        }
        // 複数回答チェックボックス
        for (CheckBox cb : answerCheckBoxes) {
            cb.setOnCheckedChangeListener(this);
        }
        // その他のボタン
        binding.submitAnswerButton.setOnClickListener(this);
        binding.nextButton.setOnClickListener(this);
        binding.backButton.setOnClickListener(this);
    }

    private void updateQuizUI(QuizEntity quiz) {
        Log.d(TAG, "Updating UI for quiz: " + quiz.getQid());
        rightAnswers = quiz.getAnswer();
        isMultipleChoice = rightAnswers != null && rightAnswers.size() > 1;
        Log.d(TAG, "Is multiple choice: " + isMultipleChoice + " (Answer count: " + (rightAnswers != null ? rightAnswers.size() : "null") + ")");

        // UI要素をリセット
        binding.explanationCard.setVisibility(View.GONE);
        binding.nextButton.setVisibility(View.GONE);
        resetAnswerControlStyles(); // ボタンとチェックボックスのスタイルをリセット
        setAnswerControlsEnabled(true); // 回答コントロールを有効化

        // qid表示
        binding.countLabel.setText(quiz.getQid());

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

        // 選択肢
        List<String> choices = quiz.getChoices();

        // 回答形式に応じてUIを切り替え
        if (isMultipleChoice) {
            // 複数回答
            binding.answerButtonsLayout.setVisibility(View.GONE);
            binding.answerChoicesLayout.setVisibility(View.VISIBLE);
            binding.submitAnswerButton.setVisibility(View.VISIBLE);
            binding.submitAnswerButton.setEnabled(false); // 初期状態は無効

            for (int i = 0; i < answerCheckBoxes.size(); i++) {
                CheckBox checkBox = answerCheckBoxes.get(i);
                if (choices != null && i < choices.size()) {
                    checkBox.setText(choices.get(i));
                    checkBox.setVisibility(View.VISIBLE);
                    checkBox.setChecked(false);
                } else {
                    checkBox.setVisibility(View.GONE);
                }
            }
        } else {
            // 単一回答
            binding.answerButtonsLayout.setVisibility(View.VISIBLE);
            binding.answerChoicesLayout.setVisibility(View.GONE);
            binding.submitAnswerButton.setVisibility(View.GONE);

            for (int i = 0; i < answerButtons.size(); i++) {
                Button button = answerButtons.get(i);
                if (choices != null && i < choices.size()) {
                    button.setText(choices.get(i));
                    button.setVisibility(View.VISIBLE);
                } else {
                    button.setVisibility(View.GONE);
                }
            }
        }
    }


    // 回答コントロール（ボタン、チェックボックス、回答ボタン）の有効/無効を切り替える
    private void setAnswerControlsEnabled(boolean enabled) {
        Log.d(TAG, "Setting answer controls enabled: " + enabled + ", isMultipleChoice: " + isMultipleChoice);
        if (isMultipleChoice) {
            for (CheckBox checkBox : answerCheckBoxes) {
                checkBox.setEnabled(enabled);
            }
            // submitAnswerButtonの有効状態はチェック状態にも依存するため、ここでは制御しない
            // onCheckedChanged と updateQuizUI で制御
             binding.submitAnswerButton.setEnabled(enabled && isAnyCheckboxChecked());
        } else {
            for (Button button : answerButtons) {
                button.setEnabled(enabled);
            }
        }
    }

    // ボタンとチェックボックスのスタイルをデフォルトに戻す
    private void resetAnswerControlStyles() {
        @ColorInt int defaultTextColor = ContextCompat.getColor(requireContext(), R.color.text_primary);

        // ボタンのリセット (MaterialButtonOutlinedStyleのデフォルトに戻すのは難しいので、色をリセット)
        for (Button button : answerButtons) {
             button.setTextColor(defaultTextColor); // 必要に応じて色を設定
             button.setBackgroundColor(Color.TRANSPARENT); // 背景を透明に (OutlinedButtonの場合)
             // button.setBackgroundTintList(null); // Tintをリセット
             // button.setStrokeColor(null); // 枠線の色をリセット
        }

        // チェックボックスのリセット
        for (CheckBox checkBox : answerCheckBoxes) {
            checkBox.setTextColor(defaultTextColor);
            checkBox.setBackgroundColor(Color.TRANSPARENT);
            checkBox.setButtonTintList(null); // Tintをデフォルトに戻す
        }
    }

    // 正解・不正解のスタイルを適用 (単一/複数対応)
    private void highlightAnswers(int selectedIndex) { // selectedIndexは単一選択の場合のみ意味を持つ
        QuizEntity currentQuiz = viewModel.getCurrentQuiz().getValue();
        if (currentQuiz == null || currentQuiz.getChoices() == null || rightAnswers == null) return;

        @ColorInt int correctColor = ContextCompat.getColor(requireContext(), R.color.correct_color);
        @ColorInt int incorrectColor = ContextCompat.getColor(requireContext(), R.color.incorrect_color);
        @ColorInt int missedColor = ContextCompat.getColor(requireContext(), R.color.missed_color);
        @ColorInt int defaultColor = ContextCompat.getColor(requireContext(), R.color.text_primary);

        Set<Integer> answerSet = new HashSet<>(rightAnswers);

        if (isMultipleChoice) {
            // 複数回答 (チェックボックス)
            List<Integer> selectedIndices = getSelectedIndices();
            Set<Integer> selectedSet = new HashSet<>(selectedIndices);

            for (int i = 0; i < answerCheckBoxes.size(); i++) {
                CheckBox checkBox = answerCheckBoxes.get(i);
                if (checkBox.getVisibility() != View.VISIBLE) continue;

                boolean isCorrectAnswer = answerSet.contains(i);
                boolean isSelected = selectedSet.contains(i);

                if (isCorrectAnswer && isSelected) checkBox.setTextColor(correctColor);
                else if (!isCorrectAnswer && isSelected) checkBox.setTextColor(incorrectColor);
                else if (isCorrectAnswer && !isSelected) checkBox.setTextColor(missedColor);
                else checkBox.setTextColor(defaultColor);
            }
        } else {
            // 単一回答 (ボタン)
            for (int i = 0; i < answerButtons.size(); i++) {
                Button button = answerButtons.get(i);
                if (button.getVisibility() != View.VISIBLE) continue;

                boolean isCorrectAnswer = answerSet.contains(i); // 単一でもSetで判定可能

                if (isCorrectAnswer) {
                    button.setTextColor(correctColor); // 正解ボタン
                    // button.setBackgroundTintList(ColorStateList.valueOf(correctColor)); // 背景色変更など
                } else if (i == selectedIndex) {
                    button.setTextColor(incorrectColor); // 選択した不正解ボタン
                    // button.setBackgroundTintList(ColorStateList.valueOf(incorrectColor));
                } else {
                    button.setTextColor(defaultColor); // その他のボタン
                    // button.setBackgroundTintList(null);
                }
            }
        }
    }

    // 選択されたチェックボックスのインデックスリストを取得 (複数回答用)
    private List<Integer> getSelectedIndices() {
        List<Integer> selectedIndices = new ArrayList<>();
        for (int i = 0; i < answerCheckBoxes.size(); i++) {
            if (answerCheckBoxes.get(i).isChecked() && answerCheckBoxes.get(i).getVisibility() == View.VISIBLE) {
                selectedIndices.add(i);
            }
        }
        return selectedIndices;
    }

     // いずれかのチェックボックスがチェックされているか (複数回答用)
     private boolean isAnyCheckboxChecked() {
         if (!isMultipleChoice) return false; // 単一回答時は常にfalse
         for (CheckBox checkBox : answerCheckBoxes) {
             if (checkBox.isChecked() && checkBox.getVisibility() == View.VISIBLE) {
                 return true;
             }
         }
         return false;
     }

    // 正誤判定ロジック
    private boolean checkAnswer(int selectedIndex) { // 単一回答用
        if (rightAnswers == null || rightAnswers.isEmpty()) return false;
        // 単一回答の場合、rightAnswersには要素が1つだけのはず
        return rightAnswers.contains(selectedIndex);
    }
    private boolean checkAnswer(List<Integer> selectedIndices) { // 複数回答用
        if (rightAnswers == null || selectedIndices == null) return false;
        return new HashSet<>(selectedIndices).equals(new HashSet<>(rightAnswers));
    }


    @Override
    public void onClick(View view) {
        int id = view.getId();
        Log.d(TAG, "onClick triggered for view ID: " + id + ", isMultipleChoice: " + isMultipleChoice);

        QuizEntity currentQuiz = viewModel.getCurrentQuiz().getValue();
        if (currentQuiz == null) {
            Log.w(TAG, "Current quiz is null, cannot process click.");
            return;
        }
        String explanation = currentQuiz.getExplanation();

        if (id == R.id.next_button) {
            Log.d(TAG, "Next button clicked, isRandomMode: " + isRandomMode);
            // isRandomMode の値に応じて次のクイズをロード
            viewModel.loadNextQuiz(isRandomMode);
        } else if (id == R.id.backButton) {
            Log.d(TAG, "Back button clicked");
            try {
            // Navigate directly to HomeFragment instead of just popping the back stack
            Navigation.findNavController(requireView()).navigate(R.id.homeFragment);
        } catch (Exception e) {
            Log.e(TAG, "Failed to navigate to home", e);
        }
    } else if (id == R.id.submitAnswerButton && isMultipleChoice) {
            // 複数回答の「回答する」ボタン
            Log.d(TAG, "Submit Answer button clicked (Multiple Choice)");
            List<Integer> selectedIndices = getSelectedIndices();
            boolean isCorrect = checkAnswer(selectedIndices);
            Log.d(TAG, "Answer is correct: " + isCorrect + ", Selected: " + selectedIndices);
            // Record the history
            if (currentQuiz != null && currentQuiz.getQid() != null) {
                viewModel.recordAnswerHistory(currentQuiz.getQid(), isCorrect); // Use new method name
            }
            // Increment correct count (if needed for immediate UI feedback, though stats are now separate)
            if (isCorrect) viewModel.incrementCorrectAnswerCount();

            highlightAnswers(-1); // ハイライト表示 (selectedIndexは不要)
            showExplanation(isCorrect, explanation);
            setAnswerControlsEnabled(false);
            binding.submitAnswerButton.setVisibility(View.GONE);
            binding.nextButton.setVisibility(View.VISIBLE);

        } else if (!isMultipleChoice && (id == R.id.answerBtn1 || id == R.id.answerBtn2 || id == R.id.answerBtn3 || id == R.id.answerBtn4)) {
            // 単一回答の回答ボタン
            Button answerBtn = (Button) view;
            int selectedIndex = answerButtons.indexOf(answerBtn); // リストからインデックス取得
            Log.d(TAG, "Answer button clicked (Single Choice): index " + selectedIndex);

            if (selectedIndex == -1) {
                 Log.w(TAG, "Clicked button not found in list.");
                 return;
            }

            boolean isCorrect = checkAnswer(selectedIndex);
            Log.d(TAG, "Answer is correct: " + isCorrect);
            // Record the history
            if (currentQuiz != null && currentQuiz.getQid() != null) {
                viewModel.recordAnswerHistory(currentQuiz.getQid(), isCorrect); // Use new method name
            }
            // Increment correct count (if needed for immediate UI feedback)
            if (isCorrect) viewModel.incrementCorrectAnswerCount();

            highlightAnswers(selectedIndex); // ハイライト表示
            showExplanation(isCorrect, explanation);
            setAnswerControlsEnabled(false);
            binding.nextButton.setVisibility(View.VISIBLE);
        }
    }

    // 解説表示ロジックを共通化
    private void showExplanation(boolean isCorrect, String explanation) {
        String resultText;
        @ColorInt int resultColor;
        if (isCorrect) {
            resultText = "正解！";
            resultColor = ContextCompat.getColor(requireContext(), R.color.correct_color);
        } else {
            resultText = "不正解";
            resultColor = ContextCompat.getColor(requireContext(), R.color.incorrect_color);
        }
        SpannableString spannableResult = new SpannableString(resultText);
        spannableResult.setSpan(new ForegroundColorSpan(resultColor), 0, resultText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        String originalExplanation = (explanation != null && !explanation.isEmpty()) ? explanation : "解説はありません。";
        binding.explanationTextView.setText(spannableResult);
        binding.explanationTextView.append("\n\n");
        binding.explanationTextView.append(originalExplanation);
        binding.explanationCard.setVisibility(View.VISIBLE);
    }

     @Override
     public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
         // 複数選択モードで、回答前の場合のみ「回答する」ボタンの有効状態を更新
         if (isMultipleChoice && binding.submitAnswerButton.getVisibility() == View.VISIBLE && binding.submitAnswerButton.isEnabled() != isAnyCheckboxChecked()) {
              Log.d(TAG, "onCheckedChanged: Updating submit button enabled state");
             binding.submitAnswerButton.setEnabled(isAnyCheckboxChecked());
         }
     }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView called");
        binding = null;
        answerCheckBoxes = null;
        answerButtons = null;
    }
}
