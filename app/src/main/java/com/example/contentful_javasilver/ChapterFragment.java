package com.example.contentful_javasilver;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.contentful_javasilver.databinding.FragmentChapterBinding;
import com.google.android.material.card.MaterialCardView;

public class ChapterFragment extends Fragment {
    private FragmentChapterBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentChapterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupClickListeners();
    }

    private void setupClickListeners() {
        // Removed backButton.setOnClickListener as it's handled by MainActivity's Toolbar

        // 各章カードのクリックリスナー
        binding.chapter1Card.setOnClickListener(v ->
            navigateToCategoryFragment(v, 1, "Java の概要と簡単なJavaプログラムの作成"));
        binding.chapter2Card.setOnClickListener(v -> 
            navigateToCategoryFragment(v, 2, "Javaの基本データ型と文字列の操作"));
        binding.chapter3Card.setOnClickListener(v -> 
            navigateToCategoryFragment(v, 3, "演算子と制御構造"));
        binding.chapter4Card.setOnClickListener(v -> 
            navigateToCategoryFragment(v, 4, "クラスの定義とインスタンスの使用"));
        binding.chapter5Card.setOnClickListener(v -> 
            navigateToCategoryFragment(v, 5, "継承とインタフェースの使用"));
        binding.chapter6Card.setOnClickListener(v -> 
            navigateToCategoryFragment(v, 6, "例外処理"));
    }

    private void navigateToCategoryFragment(View view, int chapterNumber, String chapterTitle) {
        // Bundleでデータを渡す
        Bundle args = new Bundle();
        args.putInt("chapterNumber", chapterNumber);
        args.putString("chapterTitle", chapterTitle);
        
        // CategoryFragmentに遷移
        Navigation.findNavController(view).navigate(R.id.action_chapterFragment_to_categoryFragment, args);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
