package com.example.contentful_javasilver;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.contentful_javasilver.data.QuizDao;
import com.example.contentful_javasilver.data.QuizDatabase;
import com.example.contentful_javasilver.databinding.FragmentQuestionCategoryBinding;
import com.example.contentful_javasilver.databinding.ItemQuestionCategoryBinding;

import java.util.ArrayList;
import java.util.List;

import kotlin.Unit;

import com.example.contentful_javasilver.DatabaseHelperCoroutines.QuestionCategoryItem;
import androidx.navigation.Navigation;

public class QuestionCategoryFragment extends Fragment {
    private FragmentQuestionCategoryBinding binding;
    private QuestionCategoryAdapter adapter;
    private QuizDao quizDao;
    private DatabaseHelperCoroutines databaseHelper;
    private String selectedCategory;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentQuestionCategoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 引数からデータを取得
        if (getArguments() != null) {
            selectedCategory = getArguments().getString("category");
        }

        // データベースの初期化
        QuizDatabase db = QuizDatabase.getDatabase(requireContext());
        quizDao = db.quizDao();
        databaseHelper = new DatabaseHelperCoroutines();

        setupViews();
        setupRecyclerView();
        loadQuestionCategories();
    }

    private void setupViews() {
        // Category title is now handled by the Toolbar via nav_graph label
        // Removed backButton.setOnClickListener as it's handled by MainActivity's Toolbar
    }

    private void setupRecyclerView() {
        binding.questionCategoryRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
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
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
                return Unit.INSTANCE;
            }
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        databaseHelper.cleanup();
        binding = null;
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
                // 引数を設定してQuizFragmentに遷移
                Bundle args = new Bundle();
                args.putString("qid", item.getQid());
                
                Navigation.findNavController(v).navigate(
                    R.id.action_questionCategoryFragment_to_quizFragment, 
                    args
                );
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
