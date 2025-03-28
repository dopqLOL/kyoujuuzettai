package com.example.contentful_javasilver;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.contentful_javasilver.databinding.ActivityChapterSelectBinding;

public class ChapterSelectActivity extends AppCompatActivity {
    private ActivityChapterSelectBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChapterSelectBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupClickListeners();
    }

    private void setupClickListeners() {
        // 戻るボタンのクリックリスナー
        binding.backButton.setOnClickListener(v -> finish());

        // 各章カードのクリックリスナー
        binding.chapter1Card.setOnClickListener(v -> 
            startCategoryActivity(1, "Java の概要と簡単なJavaプログラムの作成"));
        binding.chapter2Card.setOnClickListener(v -> 
            startCategoryActivity(2, "Javaの基本データ型と文字列の操作"));
        binding.chapter3Card.setOnClickListener(v -> 
            startCategoryActivity(3, "演算子と制御構造"));
        binding.chapter4Card.setOnClickListener(v -> 
            startCategoryActivity(4, "クラスの定義とインスタンスの使用"));
        binding.chapter5Card.setOnClickListener(v -> 
            startCategoryActivity(5, "継承とインタフェースの使用"));
        binding.chapter6Card.setOnClickListener(v -> 
            startCategoryActivity(6, "例外処理"));
    }

    private void startCategoryActivity(int chapterNumber, String chapterTitle) {
        Intent intent = new Intent(this, CategorySelectActivity.class);
        intent.putExtra("chapterNumber", chapterNumber);
        intent.putExtra("chapterTitle", chapterTitle);
        startActivity(intent);
    }
} 