package com.example.contentful_javasilver.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import com.contentful.java.cda.CDAEntry;
import com.example.contentful_javasilver.ContentfulGetApi;
import com.example.contentful_javasilver.AsyncHelperCoroutines;
import com.example.contentful_javasilver.DatabaseHelperCoroutines;
import com.example.contentful_javasilver.data.QuizDatabase;
import com.example.contentful_javasilver.data.QuizEntity;
import com.example.contentful_javasilver.utils.SecurePreferences;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import kotlin.Unit;
import android.util.Log;

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

    // qidリストとインデックスを更新するヘルパーメソッドは不要になったため削除
    // private void updateQidListAndIndex(List<QuizEntity> quizzes) { ... }


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
        isLoading.setValue(true);
        correctAnswerCount.setValue(0); // 正解数をリセット
        // currentQidIndex は不要
        // quizQidList は不要
        loadedQuizzes.setValue(new ArrayList<>()); // 表示リストもクリア
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
     * 次のシーケンシャルなQIDのクイズに進む
     */
    public void moveToNextQuiz() {
        QuizEntity current = currentQuiz.getValue();
        if (current == null || current.getQid() == null || current.getQid().isEmpty()) {
            Log.e(TAG, "Cannot move to next quiz, current quiz or qid is null/empty.");
            errorMessage.postValue("現在のクイズ情報を取得できませんでした。");
            return;
        }

        String currentQid = current.getQid();
        Log.d(TAG, "Current QID: " + currentQid);

        // QIDを解析 (例: "1-10")
        String[] parts = currentQid.split("-");
        if (parts.length != 2) {
            Log.e(TAG, "Invalid QID format: " + currentQid);
            errorMessage.postValue("無効な問題ID形式です: " + currentQid);
            return;
        }

        try {
            int chapter = Integer.parseInt(parts[0]);
            int questionNum = Integer.parseInt(parts[1]);

            // 次の問題番号を計算
            int nextQuestionNum = questionNum + 1;
            String nextQid = chapter + "-" + nextQuestionNum;
            Log.d(TAG, "Calculated next QID: " + nextQid);

            // 次のQIDでクイズをロード (loadQuizByQid を再利用)
            loadQuizByQid(nextQid);

        } catch (NumberFormatException e) {
            Log.e(TAG, "Error parsing QID: " + currentQid, e);
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
