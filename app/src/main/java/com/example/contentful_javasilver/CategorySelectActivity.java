package com.example.contentful_javasilver;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.contentful_javasilver.data.QuizDao;
import com.example.contentful_javasilver.data.QuizDatabase;
import com.example.contentful_javasilver.databinding.ActivityCategorySelectBinding;
import com.example.contentful_javasilver.databinding.ItemCategoryBinding;

import java.util.ArrayList;
import java.util.List;

import kotlin.Unit;

public class CategorySelectActivity extends AppCompatActivity {
    private ActivityCategorySelectBinding binding;
    private CategoryAdapter categoryAdapter;
    private QuizDao quizDao;
    private DatabaseHelperCoroutines databaseHelper;
    private int chapterNumber;
    private String chapterTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCategorySelectBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // インテントからデータを取得
        chapterNumber = getIntent().getIntExtra("chapterNumber", 1);
        chapterTitle = getIntent().getStringExtra("chapterTitle");

        // データベースの初期化
        QuizDatabase db = QuizDatabase.getDatabase(this);
        quizDao = db.quizDao();
        databaseHelper = new DatabaseHelperCoroutines();

        setupViews();
        setupRecyclerView();
        loadCategories();
    }

    private void setupViews() {
        binding.chapterTitleText.setText("第" + chapterNumber + "章");
        binding.chapterDescriptionText.setText(chapterTitle);

        binding.backButton.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        binding.categoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.categoryRecyclerView.setHasFixedSize(true);
        categoryAdapter = new CategoryAdapter(new ArrayList<>());
        binding.categoryRecyclerView.setAdapter(categoryAdapter);
    }

    private void loadCategories() {
        // デバッグ用のログを追加
        new Thread(() -> {
            try {
                List<?> allQuizzes = quizDao.getAllQuizzes();
                android.util.Log.d("CategorySelectActivity", "Total quizzes in DB: " + allQuizzes.size());
                for (Object quiz : allQuizzes) {
                    android.util.Log.d("CategorySelectActivity", "Quiz: " + quiz.toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        databaseHelper.loadCategoriesAsync(
            chapterNumber,
            quizDao,
            categories -> {
                android.util.Log.d("CategorySelectActivity", "Loaded categories: " + categories.size());
                List<CategoryItem> categoryItems = new ArrayList<>();
                for (String category : categories) {
                    android.util.Log.d("CategorySelectActivity", "Category: " + category);
                    databaseHelper.getQuizCountForCategoryAsync(
                        category,
                        quizDao,
                        count -> {
                            android.util.Log.d("CategorySelectActivity", "Category: " + category + ", Count: " + count);
                            categoryItems.add(new CategoryItem(category, count));
                            if (categoryItems.size() == categories.size()) {
                                categoryAdapter.updateCategories(categoryItems);
                                binding.categoryRecyclerView.scrollToPosition(0);
                            }
                            return Unit.INSTANCE;
                        },
                        error -> {
                            Toast.makeText(CategorySelectActivity.this, error, Toast.LENGTH_SHORT).show();
                            return Unit.INSTANCE;
                        }
                    );
                }
                return Unit.INSTANCE;
            },
            error -> {
                Toast.makeText(CategorySelectActivity.this, error, Toast.LENGTH_SHORT).show();
                return Unit.INSTANCE;
            }
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        databaseHelper.cleanup();
    }

    // カテゴリー項目を表すデータクラス
    private static class CategoryItem {
        String title;
        int questionCount;

        CategoryItem(String title, int questionCount) {
            this.title = title;
            this.questionCount = questionCount;
        }
    }

    // RecyclerViewのアダプター
    private class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
        private List<CategoryItem> categories;

        CategoryAdapter(List<CategoryItem> categories) {
            this.categories = new ArrayList<>(categories);
        }

        public void updateCategories(List<CategoryItem> newCategories) {
            this.categories.clear();
            this.categories.addAll(newCategories);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemCategoryBinding itemBinding = ItemCategoryBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new CategoryViewHolder(itemBinding);
        }

        @Override
        public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
            CategoryItem item = categories.get(position);
            holder.binding.categoryTitleText.setText(item.title);
            holder.binding.questionCountText.setText(item.questionCount + "問");

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(CategorySelectActivity.this, QuizActivity.class);
                intent.putExtra("category", item.title);
                intent.putExtra("questionCount", item.questionCount);
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return categories.size();
        }

        class CategoryViewHolder extends RecyclerView.ViewHolder {
            ItemCategoryBinding binding;

            CategoryViewHolder(ItemCategoryBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }
} 