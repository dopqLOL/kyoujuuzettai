package com.example.contentful_javasilver.viewmodels;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.contentful.java.cda.CDAEntry;
import com.example.contentful_javasilver.AsyncHelperCoroutines;
import com.example.contentful_javasilver.ContentfulGetApi;
import com.example.contentful_javasilver.DatabaseHelperCoroutines;
// import com.example.contentful_javasilver.data.ProblemStats; // No longer needed
import com.example.contentful_javasilver.data.QuizHistory; // Import QuizHistory
import com.example.contentful_javasilver.data.QuizDatabase;
import com.example.contentful_javasilver.data.QuizEntity;
import com.example.contentful_javasilver.utils.SecurePreferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import kotlin.Unit;

/**
 * クイズデータを管理するViewModel
 */
public class QuizViewModel extends AndroidViewModel {
    private static final String TAG = "QuizViewModel";
    private final QuizDatabase database;
    private final DatabaseHelperCoroutines databaseHelper;
    private final AsyncHelperCoroutines asyncHelper;
    private final ContentfulGetApi contentfulApi;
    private final Executor executor;

    // 表示中のクイズリスト（通常は1件）
    private final MutableLiveData<List<QuizEntity>> loadedQuizzes = new MutableLiveData<>();
    // エラーメッセージ
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    // 正解数 (シーケンシャルモードではあまり意味がないが、一応残す)
    private final MutableLiveData<Integer> correctAnswerCount = new MutableLiveData<>(0);
    // データロード中フラグ
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    // クイズ終了通知用は削除
    // private final MutableLiveData<Boolean> quizFinished = new MutableLiveData<>(false);

    // 現在のクイズを取得するためのMediatorLiveData
    private final MediatorLiveData<QuizEntity> currentQuiz = new MediatorLiveData<>();
    // ランダムに取得したqid
    private final MutableLiveData<String> randomQuizId = new MutableLiveData<>();

    // --- Problem List Screen ---
    // 全問題リスト
    private final MutableLiveData<List<QuizEntity>> allQuizzes = new MutableLiveData<>();
    // 検索クエリ
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>(""); // Default to empty string
    // グルーピングされた表示用リスト (ヘッダー含む)
    private final MediatorLiveData<List<Object>> groupedProblemList = new MediatorLiveData<>();
    // --- End Problem List Screen ---

    // currentQidIndex は loadedQuizzes が常に1件になるため不要
    // private final MutableLiveData<Integer> currentQidIndex = new MutableLiveData<>(0);

    public QuizViewModel(@NonNull Application application) {
        super(application);
        database = QuizDatabase.getDatabase(application);

        // 安全にAPIキーを取得
        String apiKey = SecurePreferences.getContentfulApiKey(application);
        String spaceId = SecurePreferences.getContentfulSpaceId(application);

        contentfulApi = new ContentfulGetApi(spaceId, apiKey);
        databaseHelper = new DatabaseHelperCoroutines();
        asyncHelper = new AsyncHelperCoroutines(contentfulApi);
        executor = Executors.newSingleThreadExecutor();

        // MediatorLiveDataにソースを追加
        currentQuiz.addSource(loadedQuizzes, quizzes -> updateCurrentQuiz());

        // --- Problem List Screen Sources ---
        groupedProblemList.addSource(allQuizzes, quizzes -> updateGroupedProblemList());
        groupedProblemList.addSource(searchQuery, query -> updateGroupedProblemList());
        // --- End Problem List Screen Sources ---

        // currentQidIndex は不要になったため削除
        // currentQuiz.addSource(currentQidIndex, index -> updateCurrentQuiz());
    }

    // currentQuizを更新するヘルパーメソッド
    private void updateCurrentQuiz() {
        List<QuizEntity> quizzes = loadedQuizzes.getValue();
        // loadedQuizzes は常に1件のはずなので、最初の要素をセット
        if (quizzes != null && !quizzes.isEmpty()) {
            currentQuiz.setValue(quizzes.get(0));
            Log.d(TAG, "Setting current quiz: " + quizzes.get(0).getQid());
        } else {
            Log.w(TAG, "Quizzes list is null or empty.");
            currentQuiz.setValue(null); // データがない場合はnullを設定
        }
    }

    /**
     * 次のクイズをロードする（ランダムモードかシーケンシャルモードかを判断）
     * @param isRandomMode ランダムモードの場合はtrue
     */
    public void loadNextQuiz(boolean isRandomMode) {
        if (isRandomMode) {
            // ランダムモードの場合は、新しいランダムなqidをロードする
            // loadRandomQuizId() は内部で loadedQuizzes も更新する
            loadRandomQuizId();
        } else {
            // シーケンシャルモードの場合は、次のqidに進む
            moveToNextQuiz();
        }
    }

    // qidリストとインデックスを更新するヘルパーメソッドは不要になったため削除
    // private void updateQidListAndIndex(List<QuizEntity> quizzes) { ... }


    // --- Problem List Screen Logic ---

    /**
     * 全ての問題をデータベースから非同期でロードする
     */
    public void loadAllProblems() {
        isLoading.setValue(true);
        executor.execute(() -> {
            try {
                List<QuizEntity> quizzes = database.quizDao().getAllQuizzesSorted(); // Assume this DAO method exists
                allQuizzes.postValue(quizzes);
                Log.d(TAG, "Loaded " + (quizzes != null ? quizzes.size() : 0) + " problems for the list.");
            } catch (Exception e) {
                Log.e(TAG, "Error loading all problems", e);
                errorMessage.postValue("問題リストの読み込みに失敗しました: " + e.getMessage());
                allQuizzes.postValue(Collections.emptyList()); // Post empty list on error
            } finally {
                isLoading.postValue(false);
            }
        });
    }

    /**
     * 検索クエリを設定する
     * @param query 検索文字列
     */
    public void setSearchQuery(String query) {
        searchQuery.setValue(query == null ? "" : query.trim());
    }

    /**
     * allQuizzes と searchQuery に基づいて groupedProblemList を更新する
     */
    private void updateGroupedProblemList() {
        List<QuizEntity> currentAllQuizzes = allQuizzes.getValue();
        String currentQuery = searchQuery.getValue();

        if (currentAllQuizzes == null) {
            groupedProblemList.setValue(Collections.emptyList());
            return;
        }

        isLoading.setValue(true); // Start loading state for filtering/grouping

        executor.execute(() -> {
            List<QuizEntity> filteredList;
            // Filter based on search query
            if (currentQuery == null || currentQuery.isEmpty()) {
                filteredList = new ArrayList<>(currentAllQuizzes); // No filter, use all
            } else {
                String lowerCaseQuery = currentQuery.toLowerCase();
                filteredList = currentAllQuizzes.stream()
                        .filter(quiz -> (quiz.getQid() != null && quiz.getQid().toLowerCase().contains(lowerCaseQuery)) ||
                                        (quiz.getQuestionCategory() != null && quiz.getQuestionCategory().toLowerCase().contains(lowerCaseQuery)))
                        .collect(Collectors.toList());
            }

            // Sort filteredList numerically by qid before grouping
            Collections.sort(filteredList, (q1, q2) -> {
                String qid1 = q1.getQid();
                String qid2 = q2.getQid();
                if (qid1 == null || qid2 == null) return 0; // Handle null qids

                String[] parts1 = qid1.split("-");
                String[] parts2 = qid2.split("-");

                if (parts1.length != 2 || parts2.length != 2) {
                    // Fallback to string comparison for invalid formats
                    return qid1.compareTo(qid2);
                }

                try {
                    int chapter1 = Integer.parseInt(parts1[0]);
                    int num1 = Integer.parseInt(parts1[1]);
                    int chapter2 = Integer.parseInt(parts2[0]);
                    int num2 = Integer.parseInt(parts2[1]);

                    int chapterCompare = Integer.compare(chapter1, chapter2);
                    if (chapterCompare != 0) {
                        return chapterCompare;
                    }
                    return Integer.compare(num1, num2);
                } catch (NumberFormatException e) {
                    // Fallback to string comparison if parsing fails
                    return qid1.compareTo(qid2);
                }
            });


            // Group by chapter and add headers
            Map<String, List<QuizEntity>> groupedMap = new LinkedHashMap<>(); // Use LinkedHashMap to preserve chapter order
            for (QuizEntity quiz : filteredList) {
                // Ensure chapter is not null or empty before creating header
                String chapterStr = quiz.getChapter();
                if (chapterStr != null && !chapterStr.isEmpty()) {
                    String chapterHeader = "第" + chapterStr ; // Correctly format the header
                    groupedMap.computeIfAbsent(chapterHeader, k -> new ArrayList<>()).add(quiz);
                } else {
                    // Handle cases where chapter might be missing (e.g., group under "その他")
                    groupedMap.computeIfAbsent("その他", k -> new ArrayList<>()).add(quiz);
                }
            }

            List<Object> displayList = new ArrayList<>();
            for (Map.Entry<String, List<QuizEntity>> entry : groupedMap.entrySet()) {
                displayList.add(entry.getKey()); // Add header
                displayList.addAll(entry.getValue()); // Add problems for this chapter
            }

            groupedProblemList.postValue(displayList);
            isLoading.postValue(false); // End loading state
            Log.d(TAG, "Updated grouped list. Query: '" + currentQuery + "', Items: " + displayList.size());
        });
    }

    // --- End Problem List Screen Logic ---


    /**
     * 指定されたカテゴリのクイズを取得 (ランダムに **1件** )
     * @param category カテゴリ
     */
    public void loadQuizzesByCategory(String category) {
        isLoading.setValue(true);
        correctAnswerCount.setValue(0); // 正解数をリセット
        // currentQidIndex は不要
        // quizQidList は不要
        loadedQuizzes.setValue(new ArrayList<>()); // 表示リストもクリア
        executor.execute(() -> {
            try {
                // DAOの同期メソッドを使用してカテゴリでフィルタリングし、ランダムに **1件** 取得
                List<QuizEntity> quizzes = database.quizDao().getRandomQuizzesByCategorySync(category, 1);
                Log.d(TAG, "Loaded " + quizzes.size() + " random quiz for category: " + category);

                if (quizzes.isEmpty()) {
                    Log.d(TAG, "No quizzes found in DB for category: " + category + ". Fetching from Contentful.");
                    fetchFromContentful(category); // カテゴリ情報を渡す
                } else {
                    // updateQidListAndIndex は不要
                    loadedQuizzes.postValue(quizzes); // LiveDataを更新 (1件のリスト)
                    isLoading.postValue(false);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading quiz by category: " + category, e);
                errorMessage.postValue("データの読み込みに失敗しました: " + e.getMessage());
                isLoading.postValue(false);
            }
        });
    }

    /**
     * QIDでクイズを取得 (通常は1件のはず)
     * @param qid クイズID
     */
    public void loadQuizByQid(String qid) {
        // Use postValue as this method might be called from a background thread (e.g., from loadRandomQuizId)
        isLoading.postValue(true);
        correctAnswerCount.postValue(0); // 正解数をリセット
        // currentQidIndex は不要
        // quizQidList は不要
        loadedQuizzes.postValue(new ArrayList<>()); // 表示リストもクリア
        executor.execute(() -> {
            try {
                List<QuizEntity> quizzes = database.quizDao().getQuizzesByQid(qid); // qidで取得
                Log.d(TAG, "Loaded " + quizzes.size() + " quiz for qid: " + qid);
                if (quizzes.isEmpty()) {
                    Log.w(TAG, "No quiz found in DB for qid: " + qid + ". Fetching from Contentful might be needed or show error.");
                    // qid指定で見つからない場合のエラー処理
                    errorMessage.postValue("指定されたクイズが見つかりません: " + qid);
                    isLoading.postValue(false);
                } else {
                    // updateQidListAndIndex は不要
                    loadedQuizzes.postValue(quizzes); // LiveDataを更新 (1件のリスト)
                    isLoading.postValue(false);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading quiz by qid: " + qid, e);
                errorMessage.postValue("データの読み込みに失敗しました: " + e.getMessage());
                isLoading.postValue(false);
            }
        });
    }

    /**
     * Contentfulからデータを取得し、DBに保存後、指定されたカテゴリ（またはランダム **1件** ）で再読み込み
     * @param categoryToLoadAfterFetch 取得後に読み込むカテゴリ (nullの場合はランダム1件)
     */
    private void fetchFromContentful(String categoryToLoadAfterFetch) {
        Log.d(TAG, "Fetching data from Contentful...");
        asyncHelper.fetchEntriesAsync("javaSilverQ",
            entries -> {
                Log.d(TAG, "Received " + entries.size() + " entries from Contentful");
                List<QuizEntity> entities = new ArrayList<>();
                for (CDAEntry entry : entries) {
                    try {
                        String qid = getField(entry, "qid");
                        String chapter = getField(entry, "chapter");
                        String category = getField(entry, "category");
                        String questionCategory = getField(entry, "questionCategory", getField(entry, "questioncategory", "")); // ネストして取得試行
                        String difficulty = getField(entry, "difficulty");
                        String code = getField(entry, "code");
                        String questionText = getField(entry, "questionText");
                        List<String> choices = entry.getField("choices");
                        List<Double> rawAnswers = entry.getField("answer");
                        List<Integer> intAnswers = new ArrayList<>();
                        if (rawAnswers != null) {
                            for (Double answer : rawAnswers) {
                                if (answer != null) intAnswers.add(answer.intValue());
                            }
                        }
                        String explanation = getField(entry, "explanation");

                        // 必須フィールドチェック
                        if (qid.isEmpty() || questionText.isEmpty() || choices == null || choices.isEmpty() || intAnswers.isEmpty()) {
                            Log.w(TAG, "Skipping entry due to missing required fields: " + entry.id());
                            continue;
                        }

                        QuizEntity entity = new QuizEntity(
                            qid, chapter, category, questionCategory, difficulty, code,
                            questionText, choices, intAnswers, explanation
                        );
                        entities.add(entity);
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing entry: " + entry.id(), e);
                    }
                }

                if (entities.isEmpty()) {
                    Log.w(TAG, "No valid entities were created from Contentful entries");
                    errorMessage.postValue("有効なデータが見つかりませんでした");
                    isLoading.postValue(false);
                    return Unit.INSTANCE;
                }

                Log.d(TAG, "Saving " + entities.size() + " entities to database");
                asyncHelper.insertQuizEntitiesAsync(database, entities,
                    () -> {
                        Log.d(TAG, "Entities saved successfully. Reloading quiz.");
                        // 保存完了後、指定されたカテゴリまたはランダムで再読み込み
                        if (categoryToLoadAfterFetch != null) {
                            loadQuizzesByCategory(categoryToLoadAfterFetch); // カテゴリ指定で1件読み込み
                        } else {
                            loadRandomQuizzes(1); // ランダムで1件読み込み
                        }
                        return Unit.INSTANCE;
                    },
                    error -> {
                        Log.e(TAG, "Error saving entities: " + error);
                        errorMessage.postValue(error);
                        isLoading.postValue(false);
                        return Unit.INSTANCE;
                    }
                );
                return Unit.INSTANCE;
            },
            error -> {
                Log.e(TAG, "Error fetching from Contentful: " + error);
                errorMessage.postValue(error);
                isLoading.postValue(false);
                return Unit.INSTANCE;
            }
        );
    }

    /**
     * ランダムに **1件** のクイズを取得
     * @param count 取得するクイズ数 (引数は残すが、内部で1に固定)
     */
    public void loadRandomQuizzes(int count) {
        isLoading.setValue(true);
        correctAnswerCount.setValue(0);
        // currentQidIndex は不要
        // quizQidList は不要
        loadedQuizzes.setValue(new ArrayList<>());
        executor.execute(() -> {
            try {
                List<QuizEntity> quizzes = database.quizDao().getRandomQuizzesSync(1); // 常に1件取得
                Log.d(TAG, "Loaded " + quizzes.size() + " random quiz.");
                if (quizzes.isEmpty()) {
                    Log.d(TAG, "No random quiz found in DB. Fetching from Contentful.");
                    fetchFromContentful(null); // ランダム取得後にランダムで1件再読み込み
                } else {
                    // updateQidListAndIndex は不要
                    loadedQuizzes.postValue(quizzes); // LiveDataを更新 (1件のリスト)
                    isLoading.postValue(false);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading random quiz", e);
                errorMessage.postValue("データの読み込みに失敗しました: " + e.getMessage());
                isLoading.postValue(false);
            }
        });
    }


    /**
     * エントリからフィールドを安全に取得するヘルパーメソッド (デフォルト値付き)
     */
    private String getField(CDAEntry entry, String fieldName, String defaultValue) {
        try {
            Object value = entry.getField(fieldName);
            return value != null ? value.toString() : defaultValue;
        } catch (Exception e) {
            // Log.w(TAG, "Field not found or error getting field " + fieldName + ": " + e.getMessage());
            return defaultValue;
        }
    }
     /**
     * エントリからフィールドを安全に取得するヘルパーメソッド (デフォルト値付き、オーバーロード)
     */
    private String getField(CDAEntry entry, String fieldName) {
        return getField(entry, fieldName, "");
    }


    /**
     * 次のシーケンシャルなQIDのクイズに進む (内部利用メソッドに変更)
     */
    private void moveToNextQuiz() {
        QuizEntity current = currentQuiz.getValue();
        if (current == null || current.getQid() == null || current.getQid().isEmpty()) {
            Log.e(TAG, "Cannot move to next quiz, current quiz or qid is null/empty.");
            errorMessage.postValue("現在のクイズ情報を取得できませんでした。");
            return;
        }

        String currentQid = current.getQid();
        Log.d(TAG, "Current QID for sequential move: " + currentQid);

        // QIDを解析 (例: "1-10")
        String[] parts = currentQid.split("-");
        if (parts.length != 2) {
            Log.e(TAG, "Invalid QID format for sequential move: " + currentQid);
            errorMessage.postValue("無効な問題ID形式です: " + currentQid);
            return;
        }

        try {
            int chapter = Integer.parseInt(parts[0]);
            int questionNum = Integer.parseInt(parts[1]);

            // 次の問題番号を計算
            int nextQuestionNum = questionNum + 1;
            String nextQid = chapter + "-" + nextQuestionNum;
            Log.d(TAG, "Calculated next sequential QID: " + nextQid);

            // 次のQIDでクイズをロード (loadQuizByQid を再利用)
            // loadQuizByQid は isLoading を true にし、完了時に false にする
            loadQuizByQid(nextQid);

        } catch (NumberFormatException e) {
            Log.e(TAG, "Error parsing QID for sequential move: " + currentQid, e);
            errorMessage.postValue("問題IDの解析に失敗しました: " + currentQid);
        }
    }


    /**
     * 正解をカウント
     */
    public void incrementCorrectAnswerCount() {
        Integer count = correctAnswerCount.getValue();
        if (count != null) {
            correctAnswerCount.setValue(count + 1);
        }
    }

    /**
     * 表示用のクイズリストLiveData (常に1件のはず)
     */
    public LiveData<List<QuizEntity>> getLoadedQuizzes() {
        return loadedQuizzes;
    }

    /**
     * 現在のクイズLiveData
     */
    public LiveData<QuizEntity> getCurrentQuiz() {
        return currentQuiz;
    }

    /**
     * 現在のクイズインデックスLiveData は不要
     */
     // public LiveData<Integer> getCurrentQuizIndex() { ... }

    // --- Problem List Screen LiveData ---
    /**
     * グルーピングされた表示用リストLiveData
     */
    public LiveData<List<Object>> getGroupedProblemList() {
        return groupedProblemList;
    }
    // --- End Problem List Screen LiveData ---

    /**
     * エラーメッセージLiveData
     */
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    /**
     * 正解数LiveData
     */
    public LiveData<Integer> getCorrectAnswerCount() {
        return correctAnswerCount;
    }

    /**
     * ローディング状態LiveData
     */
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    /**
     * クイズ終了通知LiveData は不要
     */
    // public LiveData<Boolean> getQuizFinished() { ... }

    /**
     * 現在のqidリストのサイズを取得 は不要 (常に1のはず)
     */
    // public int getTotalQuizCount() { ... }

    /**
     * ランダムなクイズIDを取得するためのLiveData
     */
    public LiveData<String> getRandomQuizId() {
        return randomQuizId;
    }

    /**
     * データベースからランダムなクイズIDを1件取得し、randomQuizId LiveDataを更新する
     */
    public void loadRandomQuizId() {
        isLoading.setValue(true); // ローディング開始
        executor.execute(() -> {
            try {
                List<QuizEntity> randomQuizzes = database.quizDao().getRandomQuizzesSync(1);
                if (randomQuizzes != null && !randomQuizzes.isEmpty()) {
                    String qid = randomQuizzes.get(0).getQid();
                    // ランダムIDを更新すると同時に、そのIDでクイズをロードする
                    randomQuizId.postValue(qid);
                    loadQuizByQid(qid); // Load the quiz for the new random ID
                    Log.d(TAG, "Loaded random quiz ID and initiated load: " + qid);
                } else {
                    Log.w(TAG, "Could not get random quiz ID from DB.");
                    // DBが空の場合、Contentfulから取得するロジックもここに追加可能
                    // 今回はエラーメッセージを表示し、nullをpostする
                    randomQuizId.postValue(null);
                    errorMessage.postValue("ランダムな問題を取得できませんでした。");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading random quiz ID", e);
                errorMessage.postValue("ランダムな問題IDの読み込みに失敗しました: " + e.getMessage());
                randomQuizId.postValue(null); // エラー時もnullをpost
            } finally {
                isLoading.postValue(false); // ローディング終了
            }
        });
    }

    /**
     * Records the result of an answer for a specific problem.
     * This ensures the problem stat record exists and increments the correct/incorrect count.
     * @param problemId The ID of the problem answered.
     * @param isCorrect True if the answer was correct, false otherwise.
     */
    public void recordAnswerHistory(String problemId, boolean isCorrect) { // Renamed method
        if (problemId == null || problemId.isEmpty()) {
            Log.w(TAG, "Cannot record answer history, problemId is null or empty.");
            return; // Exit if problemId is invalid
        }
        executor.execute(() -> {
            try {
                long currentTime = System.currentTimeMillis();
                QuizHistory historyRecord = new QuizHistory(problemId, isCorrect, currentTime);
                database.quizDao().insertHistory(historyRecord);
                Log.d(TAG, "Inserted quiz history for problem: " + problemId + ", Correct: " + isCorrect);
            } catch (Exception e) {
                Log.e(TAG, "Error recording answer history for problem: " + problemId, e);
                // Optionally post an error message to LiveData if needed
            }
        });
    }


    /**
     * リソースをクリーンアップ
     */
    @Override
    protected void onCleared() {
        super.onCleared();
        databaseHelper.cleanup();
        asyncHelper.cleanup();
        // executorのシャットダウンも考慮
    }
}
