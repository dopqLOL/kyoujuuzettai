package com.example.contentful_javasilver;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.contentful_javasilver.databinding.FragmentHomeBinding;

/**
 * ホーム画面のフラグメント
 * - メインアクティビティからナビゲーションされる画面
 * - ランダム問題とチャプター選択への選択肢を提供
 */
public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ランダム出題ボタンのクリックリスナー
        binding.randomButton.setOnClickListener(v -> {
            // QuizFragmentへナビゲーション
            Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_quizFragment);
        });

        // 分野別に出題ボタンのクリックリスナー
        binding.categoryButton.setOnClickListener(v -> {
            // ChapterFragmentへナビゲーション
            Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_chapterFragment);
        });
        
        // 問題一覧ボタンのクリックリスナー
        binding.allQuestionsButton.setOnClickListener(v -> {
            // QuizFragmentへナビゲーション（すべての問題）
            Bundle bundle = new Bundle();
            bundle.putString("qid", "all"); // すべての問題を表示するためのフラグ
            Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_quizFragment, bundle);
        });
        
        // 設定ボタンのクリックリスナー
        binding.settingsButton.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.settingsFragment);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 