package com.example.contentful_javasilver;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.contentful_javasilver.data.QuizDao;
import com.example.contentful_javasilver.data.QuizDatabase;
import com.example.contentful_javasilver.databinding.ActivityQuestionCategorySelectBinding;
import com.example.contentful_javasilver.databinding.ItemQuestionCategoryBinding;

import java.util.ArrayList;
import java.util.List;

import kotlin.Unit;

import com.example.contentful_javasilver.DatabaseHelperCoroutines.QuestionCategoryItem;

public class QuestionCategorySelectActivity extends AppCompatActivity {
    private ActivityQuestionCategorySelectBinding binding;
    private QuestionCategoryAdapter adapter;
    private QuizDao quizDao;
    private DatabaseHelperCoroutines databaseHelper;
    private String selectedCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQuestionCategorySelectBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // インテントからデータを取得
        selectedCategory = getIntent().getStringExtra("category");

        // データベースの初期化
        QuizDatabase db = QuizDatabase.getDatabase(this);
        quizDao = db.quizDao();
        databaseHelper = new DatabaseHelperCoroutines();

        setupViews();
        setupRecyclerView();
        loadQuestionCategories();
    }

    private void setupViews() {
        binding.categoryTitleText.setText(selectedCategory);
        binding.backButton.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        binding.questionCategoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.questionCategoryRecyclerView.setHasFixedSize(true);
        adapter = new QuestionCategoryAdapter(new ArrayList<>());
        binding.questionCategoryRecyclerView.setAdapter(adapter);
    }

    private void loadQuestionCategories() {
        databaseHelper.loadQuestionCategoriesAsync(
            selectedCategory,
            quizDao,
            questionCategories -> {
                adapter.updateQuestionCategories(questionCategories);
                return Unit.INSTANCE;
            },
            error -> {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
                return Unit.INSTANCE;
            }
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        databaseHelper.cleanup();
    }

    // RecyclerViewのアダプター
    private class QuestionCategoryAdapter extends RecyclerView.Adapter<QuestionCategoryAdapter.ViewHolder> {
        private List<QuestionCategoryItem> items;

        QuestionCategoryAdapter(List<QuestionCategoryItem> items) {
            this.items = new ArrayList<>(items);
        }

        public void updateQuestionCategories(List<QuestionCategoryItem> newItems) {
            this.items.clear();
            this.items.addAll(newItems);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemQuestionCategoryBinding itemBinding = ItemQuestionCategoryBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new ViewHolder(itemBinding);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            QuestionCategoryItem item = items.get(position);
            holder.binding.qidText.setText(item.getQid());
            holder.binding.questionCategoryText.setText(item.getQuestionCategory());

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(QuestionCategorySelectActivity.this, QuizActivity.class);
                intent.putExtra("qid", item.getQid());
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ItemQuestionCategoryBinding binding;

            ViewHolder(ItemQuestionCategoryBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }
} 