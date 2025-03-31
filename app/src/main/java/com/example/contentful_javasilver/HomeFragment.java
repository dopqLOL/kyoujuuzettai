package com.example.contentful_javasilver;

import android.os.Bundle;
import android.util.Log; // Import Log
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast; // Import Toast
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider; // Import ViewModelProvider
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.example.contentful_javasilver.databinding.FragmentHomeBinding;
import com.example.contentful_javasilver.viewmodels.QuizViewModel; // Import QuizViewModel

/**
 * ホーム画面のフラグメント
 * - メインアクティビティからナビゲーションされる画面
 * - ランダム問題とチャプター選択への選択肢を提供
 */
public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment"; // TAG for logging
    private FragmentHomeBinding binding;
    private QuizViewModel quizViewModel; // Add ViewModel instance

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        // Initialize ViewModel
        quizViewModel = new ViewModelProvider(requireActivity()).get(QuizViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated");
        final NavController navController = Navigation.findNavController(view);

        // ランダム出題ボタンのクリックリスナー
        binding.randomButton.setOnClickListener(v -> {
            // 1. ViewModelにランダムなqidのロードを要求
            quizViewModel.loadRandomQuizId();

            // 2. LiveDataを監視し、qidが取得できたらナビゲーション
            quizViewModel.getRandomQuizId().observe(getViewLifecycleOwner(), qid -> {
                // Observeを一度だけ実行するように変更 (Navigation後に再度呼ばれるのを防ぐ)
                // ただし、エラーなどでqidがnullの場合もあるため、nullチェックは必要
                if (qid != null) {
                    // LiveDataの監視を解除 (ナビゲーション後に再度トリガーされるのを防ぐため)
                    quizViewModel.getRandomQuizId().removeObservers(getViewLifecycleOwner());

                    // Safe Args を使って QuizFragment へナビゲーション (isRandomModeをtrueに設定)
                    HomeFragmentDirections.ActionHomeFragmentToQuizFragment action =
                            HomeFragmentDirections.actionHomeFragmentToQuizFragment(qid);
                    action.setIsRandomMode(true); // Set random mode flag
                    try {
                        navController.navigate(action);
                    } catch (IllegalStateException e) {
                        // Navigation中に再度クリックされた場合などのエラーハンドリング
                        android.util.Log.e("HomeFragment", "Navigation failed: " + e.getMessage());
                    } catch (IllegalArgumentException e) {
                        // NavController が見つからない場合など
                         android.util.Log.e("HomeFragment", "Navigation failed (IllegalArgument): " + e.getMessage());
                    }

                } else {
                    // qidがnullの場合（DBが空、または取得エラー）
                    // エラーメッセージはViewModel側でToast表示されるはずなので、ここでは何もしないか、
                    // 必要であれば追加のUIフィードバックを行う
                    // 例: Toast.makeText(getContext(), "ランダム問題の取得に失敗しました", Toast.LENGTH_SHORT).show();
                    // ViewModelのエラーメッセージを監視して表示する方がより良い
                }
            });
        });

        // ViewModelのエラーメッセージを監視してToast表示
        quizViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                // エラーメッセージをクリアする処理をViewModelに追加しても良い
            }
        });


        // 分野別に出題ボタンのクリックリスナー
        binding.categoryButton.setOnClickListener(v ->
            navController.navigate(R.id.action_homeFragment_to_chapterFragment)
        );

        // 問題一覧ボタンのクリックリスナー
        binding.problemListButton.setOnClickListener(v ->
            navController.navigate(R.id.action_homeFragment_to_problemListFragment)
        );

        // 設定ボタンのクリックリスナー
        binding.settingsButton.setOnClickListener(v ->
            navController.navigate(R.id.settingsFragment)
        );
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView");
        binding = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }
}
