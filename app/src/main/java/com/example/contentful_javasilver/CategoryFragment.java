package com.example.contentful_javasilver;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.contentful_javasilver.data.QuizDao;
import com.example.contentful_javasilver.data.QuizDatabase;
import com.example.contentful_javasilver.data.QuizEntity;
import com.example.contentful_javasilver.databinding.FragmentCategoryBinding;
import com.example.contentful_javasilver.databinding.ItemCategoryBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import kotlin.Unit;

public class CategoryFragment extends Fragment {
    private static final String TAG = "CategoryFragment";
    private FragmentCategoryBinding binding;
    private CategoryAdapter categoryAdapter;
    private QuizDao quizDao;
    private DatabaseHelperCoroutines databaseHelper;
    private int chapterNumber;
    private String chapterTitle;
    private final Executor executor = Executors.newSingleThreadExecutor();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCategoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bundleからデータを取得
        if (getArguments() != null) {
            chapterNumber = getArguments().getInt("chapterNumber", 1);
            chapterTitle = getArguments().getString("chapterTitle", "");
        }

        Log.d(TAG, "Chapter number: " + chapterNumber + ", title: " + chapterTitle);

        // データベースの初期化
        QuizDatabase db = QuizDatabase.getDatabase(requireContext());
        quizDao = db.quizDao();
        databaseHelper = new DatabaseHelperCoroutines();

        setupViews();
        setupRecyclerView();
        
        // データベース内のクイズ数を確認
        checkDatabaseStatus();
        
        // カテゴリをロード
        showLoading(true);
        loadCategories();
    }

    private void checkDatabaseStatus() {
        executor.execute(() -> {
            try {
                List<QuizEntity> allQuizzes = quizDao.getAllQuizzes();
                Log.d(TAG, "Total quizzes in database: " + allQuizzes.size());
                
                if (allQuizzes.isEmpty()) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "データベースにクイズがありません。データをダウンロードしてください。", Toast.LENGTH_LONG).show();
                    });
                    return;
                }
                
                // すべての問題のフィールドを詳細に表示
                Log.d(TAG, "=============== すべてのクイズデータ ===============");
                for (QuizEntity quiz : allQuizzes) {
                    Log.d(TAG, String.format("ID: %s, Chapter: '%s', Category: '%s', QuestionCategory: '%s'",
                        quiz.getQid(),
                        quiz.getChapter(),
                        quiz.getCategory(),
                        quiz.getQuestionCategory()
                    ));
                }
                Log.d(TAG, "=================================================");
                
                // チャプター別に問題数をカウント
                Map<String, Integer> chapterCounts = new HashMap<>();
                Map<String, Set<String>> chapterCategories = new HashMap<>();
                
                for (QuizEntity quiz : allQuizzes) {
                    String chapter = quiz.getChapter();
                    String category = quiz.getCategory();
                    
                    // チャプターカウント更新
                    chapterCounts.put(chapter, chapterCounts.getOrDefault(chapter, 0) + 1);
                    
                    // チャプターごとのカテゴリー追加
                    if (!chapterCategories.containsKey(chapter)) {
                        chapterCategories.put(chapter, new HashSet<>());
                    }
                    if (category != null) {
                        chapterCategories.get(chapter).add(category);
                    }
                }
                
                // 結果を表示
                Log.d(TAG, "=============== チャプター統計 ===============");
                for (Map.Entry<String, Integer> entry : chapterCounts.entrySet()) {
                    Log.d(TAG, String.format("Chapter: '%s' - %d quizzes, Categories: %s",
                        entry.getKey(),
                        entry.getValue(),
                        chapterCategories.get(entry.getKey())
                    ));
                }
                Log.d(TAG, "==============================================");
                
                // 現在選択中のチャプターに関する詳細情報
                Log.d(TAG, String.format("現在選択中のチャプター: %d", chapterNumber));
                
                // 現在のチャプターに一致する問題を確認
                List<QuizEntity> matchingQuizzes = new ArrayList<>();
                for (QuizEntity quiz : allQuizzes) {
                    String chapterStr = quiz.getChapter();
                    boolean matches = false;
                    
                    try {
                        // 様々な形式の章番号に対応
                        if (chapterStr != null) {
                            chapterStr = chapterStr.trim();
                            if (chapterStr.equals(String.valueOf(chapterNumber))) {
                                matches = true;
                            } else if (chapterStr.equals(chapterNumber + "章")) {
                                matches = true; 
                            } else if (chapterStr.matches("^\\d+$") && Integer.parseInt(chapterStr) == chapterNumber) {
                                matches = true;
                            } else if (chapterStr.contains("章")) {
                                int num = Integer.parseInt(chapterStr.replace("章", "").trim());
                                if (num == chapterNumber) matches = true;
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing chapter: " + chapterStr, e);
                    }
                    
                    if (matches) {
                        matchingQuizzes.add(quiz);
                    }
                }
                
                Log.d(TAG, String.format("チャプター %d と一致する問題数: %d", chapterNumber, matchingQuizzes.size()));
                if (!matchingQuizzes.isEmpty()) {
                    Set<String> categories = new HashSet<>();
                    for (QuizEntity quiz : matchingQuizzes) {
                        if (quiz.getCategory() != null) {
                            categories.add(quiz.getCategory());
                        }
                    }
                    Log.d(TAG, String.format("チャプター %d のカテゴリー: %s", chapterNumber, categories));
                    
                    // いくつかのサンプル問題を表示
                    int samplesToShow = Math.min(5, matchingQuizzes.size());
                    Log.d(TAG, String.format("チャプター %d のサンプル問題:", chapterNumber));
                    for (int i = 0; i < samplesToShow; i++) {
                        QuizEntity quiz = matchingQuizzes.get(i);
                        Log.d(TAG, String.format("  - ID: %s, Chapter: '%s', Category: '%s'",
                            quiz.getQid(), quiz.getChapter(), quiz.getCategory()));
                    }
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error checking database status", e);
            }
        });
    }

    private void setupViews() {
        binding.chapterTitleText.setText("第" + chapterNumber + "章");
        binding.chapterDescriptionText.setText(chapterTitle);

        binding.backButton.setOnClickListener(v -> {
            // ナビゲーションコントローラーでポップバックスタック（前の画面に戻る）
            Navigation.findNavController(v).popBackStack();
        });
    }

    private void setupRecyclerView() {
        binding.categoryRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.categoryRecyclerView.setHasFixedSize(true);
        categoryAdapter = new CategoryAdapter(new ArrayList<>());
        binding.categoryRecyclerView.setAdapter(categoryAdapter);
    }
    
    private void showLoading(boolean isLoading) {
        if (binding != null) {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.categoryRecyclerView.setVisibility(isLoading ? View.GONE : View.VISIBLE);
            binding.errorMessage.setVisibility(View.GONE);
        }
    }
    
    private void showError(String message) {
        if (binding != null) {
            binding.progressBar.setVisibility(View.GONE);
            binding.categoryRecyclerView.setVisibility(View.GONE);
            binding.errorMessage.setVisibility(View.VISIBLE);
            binding.errorMessage.setText(message);
        }
    }

    private void loadCategories() {
        Log.d(TAG, "Loading categories for chapter: " + chapterNumber);
        
        databaseHelper.loadCategoriesAsync(
            chapterNumber,
            quizDao,
            categories -> {
                Log.d(TAG, "Loaded " + categories.size() + " categories: " + categories);
                
                if (categories.isEmpty()) {
                    showError("カテゴリが見つかりませんでした。\n別の章を選択してください。");
                    return Unit.INSTANCE;
                }
                
                // すべてのカテゴリ処理を追跡するカウンター
                final int[] processedCount = {0};
                final List<CategoryItem> categoryItems = Collections.synchronizedList(new ArrayList<>());
                
                // カテゴリごとの問題数を取得
                for (String category : categories) {
                    Log.d(TAG, "Getting quiz count for category: " + category);
                    databaseHelper.getQuizCountForCategoryAsync(
                        category,
                        quizDao,
                        count -> {
                            Log.d(TAG, "Category " + category + " has " + count + " quizzes");
                            synchronized (categoryItems) {
                                categoryItems.add(new CategoryItem(category, count));
                                processedCount[0]++;
                                
                                Log.d(TAG, "Processed " + processedCount[0] + " of " + categories.size() + " categories");
                                
                                // すべてのカテゴリの問題数を取得したらUIを更新
                                if (processedCount[0] >= categories.size()) {
                                    if (isAdded()) {
                                        Log.d(TAG, "All categories processed. Updating UI with " + categoryItems.size() + " items");
                                        
                                        // メインスレッドでUIを更新
                                        requireActivity().runOnUiThread(() -> {
                                            try {
                                                // 念のため再チェック
                                                if (categoryAdapter != null && isAdded()) {
                                                    Log.d(TAG, "Updating adapter with categories: " + categoryItems);
                                                    categoryAdapter.updateCategories(new ArrayList<>(categoryItems));
                                                    binding.categoryRecyclerView.scrollToPosition(0);
                                                    showLoading(false);
                                                }
                                            } catch (Exception e) {
                                                Log.e(TAG, "Error updating UI", e);
                                            }
                                        });
                                    }
                                }
                            }
                            return Unit.INSTANCE;
                        },
                        error -> {
                            Log.e(TAG, "Error getting quiz count for category " + category + ": " + error);
                            synchronized (categoryItems) {
                                processedCount[0]++;
                                
                                // エラーが発生しても、すべてのカテゴリを処理したらUIを更新
                                if (processedCount[0] >= categories.size() && isAdded()) {
                                    requireActivity().runOnUiThread(() -> {
                                        if (categoryItems.isEmpty()) {
                                            showError("カテゴリの問題数取得中にエラーが発生しました。");
                                        } else {
                                            categoryAdapter.updateCategories(new ArrayList<>(categoryItems));
                                            showLoading(false);
                                        }
                                    });
                                }
                            }
                            return Unit.INSTANCE;
                        }
                    );
                }
                return Unit.INSTANCE;
            },
            error -> {
                Log.e(TAG, "Error loading categories: " + error);
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        showError("カテゴリのロードに失敗しました。\n" + error);
                    });
                }
                return Unit.INSTANCE;
            }
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.cleanup();
        }
    }

    // カテゴリー項目を表すデータクラス
    private static class CategoryItem {
        String title;
        int questionCount;

        CategoryItem(String title, int questionCount) {
            this.title = title;
            this.questionCount = questionCount;
        }

        String getTitle() {
            return title;
        }

        int getQuestionCount() {
            return questionCount;
        }
    }

    // RecyclerViewのアダプター
    private class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
        private final List<CategoryItem> categories;

        CategoryAdapter(List<CategoryItem> categories) {
            this.categories = new ArrayList<>();
            if (categories != null) {
                this.categories.addAll(categories);
            }
        }

        public void updateCategories(List<CategoryItem> newCategories) {
            if (newCategories == null) {
                Log.e(TAG, "Attempted to update adapter with null categories");
                return;
            }
            
            Log.d(TAG, "Updating adapter with " + newCategories.size() + " categories");
            
            // カテゴリを名前でソート
            List<CategoryItem> sortedCategories = new ArrayList<>(newCategories);
            Collections.sort(sortedCategories, (cat1, cat2) -> {
                if (cat1 == null || cat2 == null || cat1.getTitle() == null || cat2.getTitle() == null) {
                    return 0;
                }
                return cat1.getTitle().compareTo(cat2.getTitle());
            });
            
            // データ更新
            this.categories.clear();
            this.categories.addAll(sortedCategories);
            
            // ログで確認
            for (CategoryItem item : this.categories) {
                Log.d(TAG, "Category in adapter: " + item.getTitle() + " (" + item.getQuestionCount() + " 問)");
            }
            
            // UI更新
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
            if (position < 0 || position >= categories.size()) {
                Log.e(TAG, "Invalid position: " + position + ", categories size: " + categories.size());
                return;
            }
            
            CategoryItem item = categories.get(position);
            if (item == null) {
                Log.e(TAG, "Null category item at position " + position);
                return;
            }
            
            holder.binding.categoryTitleText.setText(item.getTitle());
            holder.binding.questionCountText.setText(item.getQuestionCount() + "問");

            holder.itemView.setOnClickListener(v -> {
                Bundle args = new Bundle();
                args.putString("category", item.getTitle());
                
                Navigation.findNavController(v).navigate(
                    R.id.action_categoryFragment_to_questionCategoryFragment, 
                    args
                );
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