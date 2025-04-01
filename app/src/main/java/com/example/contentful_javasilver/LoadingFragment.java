package com.example.contentful_javasilver;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock; // Import SystemClock
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
// Removed ProgressBar import
// Removed TextView import
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
// Removed LifecycleOwner import as lifecycleScope is removed
import androidx.navigation.Navigation;

import com.contentful.java.cda.CDAEntry;
import com.example.contentful_javasilver.data.QuizDao; // Added explicit QuizDao import
import com.example.contentful_javasilver.data.QuizDatabase;
import com.example.contentful_javasilver.data.QuizEntity;
import com.example.contentful_javasilver.utils.SecurePreferences;
// Removed QuizViewModel import as it wasn't used directly here
// Removed DatabaseTransaction import as we use Room's runInTransaction directly

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService; // Added ExecutorService import
import java.util.concurrent.Executors;   // Added Executors import

import kotlin.Unit;
// Removed kotlinx.coroutines imports

public class LoadingFragment extends Fragment {

    private static final String TAG = "LoadingFragment";
    private static final long MIN_DISPLAY_TIME_MS = 3000; // 3 seconds minimum display time
    // Removed progressBar and statusText fields
    private final Handler handler = new Handler(Looper.getMainLooper()); // Handler for UI updates
    private long startTimeMs; // To track start time
    private boolean dataLoadComplete = false; // Flag to track data loading status

    private AsyncHelperCoroutines asyncHelper; // Keep this for Contentful fetching
    private QuizDatabase database;
    private ExecutorService databaseExecutor; // Executor for background DB tasks

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_loading, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        startTimeMs = SystemClock.elapsedRealtime(); // Record start time
        // Removed progressBar and statusText initialization

        // Initialize Database and Executor
        database = QuizDatabase.getDatabase(requireContext());
        databaseExecutor = Executors.newSingleThreadExecutor(); // Create a single-threaded executor

        // APIキーの取得
        String apiKey = SecurePreferences.getContentfulApiKey(requireContext());
        String spaceId = SecurePreferences.getContentfulSpaceId(requireContext());

        // API初期化 (Assuming AsyncHelperCoroutines handles its own coroutine scope internally)
        ContentfulGetApi contentfulApi = new ContentfulGetApi(spaceId, apiKey);
        asyncHelper = new AsyncHelperCoroutines(contentfulApi);

        // データベース確認とデータロードを開始
        checkDatabaseAndLoadData();
    }

    private void checkDatabaseAndLoadData() {
        // Removed status update and progress bar setting

        databaseExecutor.execute(() -> {
            try {
                int quizCount = database.quizDao().getQuizCountSync();
                handler.post(() -> { // UIスレッドで処理
                    if (isAdded()) { // FragmentがまだActivityにアタッチされているか確認
                        // Removed progress bar setting
                        if (quizCount > 0) {
                            // データが存在する場合
                            Log.d(TAG, "データベースに既存データあり (" + quizCount + "件)。ダウンロードをスキップします。");
                            // Removed status update
                            dataLoadComplete = true; // Mark data load as complete
                            tryNavigateToHome(); // Attempt navigation
                        } else {
                            // データが存在しない場合
                            Log.d(TAG, "データベースにデータなし。Contentfulからダウンロードを開始します。");
                            // Removed status update
                            downloadAllData();
                        }
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "データベースのカウント取得に失敗", e);
                handler.post(() -> {
                    if (isAdded()) {
                        // Removed status update
                        showError("データベース確認エラー: " + e.getMessage());
                        // Consider how to handle errors - maybe navigate after delay?
                        // For now, we won't navigate on error.
                    }
                });
            }
        });
    }

    // checkForUpdates method is removed as it was commented out
    /*
    private void checkForUpdates() {
        // ... (code removed for brevity)
    }
    */

    private void downloadAllData() {
        // Assuming asyncHelper works correctly from Java
        asyncHelper.fetchAllEntriesAsync(
                "javaSilverQ",
                (progress, status) -> {
                    // Progress updates are no longer displayed
                    // updateProgressAndStatus(progress, status);
                    return Unit.INSTANCE;
                },
                entries -> {
                    Log.d(TAG, entries.size() + "件のデータをダウンロードしました");
                    // Removed status update

                    List<QuizEntity> entities = convertToQuizEntities(entries);
                    saveToDatabase(entities); // Call the refactored save method

                    return Unit.INSTANCE;
                },
                error -> {
                    Log.e(TAG, "データのダウンロードに失敗しました: " + error);
                    // Removed status update
                    showError(error);
                    // Consider how to handle errors - maybe navigate after delay?
                    // For now, we won't navigate on error.
                    return Unit.INSTANCE;
                }
        );
    }

    @SuppressWarnings("unchecked")
    private List<QuizEntity> convertToQuizEntities(List<? extends CDAEntry> entries) {
        List<QuizEntity> entities = new ArrayList<>();
        for (CDAEntry entry : entries) {
            try {
                String qid = getFieldAsString(entry, "qid");
                String chapter = getFieldAsString(entry, "chapter");
                String category = getFieldAsString(entry, "category");
                String questionCategory = "";
                try {
                    questionCategory = getFieldAsString(entry, "questionCategory");
                    if (questionCategory.isEmpty()) {
                        questionCategory = getFieldAsString(entry, "questioncategory");
                    }
                } catch (Exception e) {
                    try {
                        questionCategory = getFieldAsString(entry, "questioncategory");
                    } catch (Exception ex) { /* ignore */ }
                }
                String difficulty = getFieldAsString(entry, "difficulty");
                String code = getFieldAsString(entry, "code");
                String questionText = getFieldAsString(entry, "questionText");
                List<String> choices = (List<String>) entry.getField("choices");
                List<Double> rawAnswers = (List<Double>) entry.getField("answer");
                List<Integer> intAnswers = new ArrayList<>();
                if (rawAnswers != null) {
                    for (Double answer : rawAnswers) {
                        if (answer != null) intAnswers.add(answer.intValue());
                    }
                }
                String explanation = getFieldAsString(entry, "explanation");

                QuizEntity entity = new QuizEntity(
                        qid, chapter, category, questionCategory, difficulty, code,
                        questionText, choices, intAnswers, explanation, false // Add default bookmark status
                );
                entities.add(entity);
            } catch (Exception e) {
                Log.e(TAG, "エントリの変換に失敗しました: " + entry.id(), e);
            }
        }
        return entities;
    }

    private String getFieldAsString(CDAEntry entry, String fieldName) {
        try {
            Object value = entry.getField(fieldName);
            return value != null ? value.toString() : "";
        } catch (Exception e) {
            return "";
        }
    }

    private void saveToDatabase(List<QuizEntity> entities) {
        if (entities.isEmpty()) {
            // Post UI update to main thread
            handler.post(() -> {
                 if (isAdded()) {
                    // Removed call to updateProgressAndStatus as it no longer exists
                    showError("変換されたデータがありません"); // Keep specific error message
                 }
            });
            return;
        }

        // Use ExecutorService for background database operation
        databaseExecutor.execute(() -> {
            try {
                // Perform the database transaction directly on the background thread
                database.runInTransaction(() -> {
                    QuizDao dao = database.quizDao();
                    dao.deleteAll();
                    dao.insertAll(entities);
                });

                // Post success UI update back to the main thread
                handler.post(() -> {
                    if (isAdded()) {
                        // Removed status update
                        Log.d(TAG, entities.size() + "件のクイズを保存しました");
                        dataLoadComplete = true; // Mark data load as complete
                        tryNavigateToHome(); // Attempt navigation
                    }
                });

            } catch (Exception dbException) {
                Log.e(TAG, "Database transaction failed", dbException);
                // Post error UI update back to the main thread
                handler.post(() -> {
                    if (isAdded()) {
                        // Removed status update
                        showError("データベースエラー: " + dbException.getMessage()); // Keep specific error message
                        // Consider how to handle errors - maybe navigate after delay?
                        // For now, we won't navigate on error.
                    }
                });
            }
        });
    }

    // Removed updateProgressAndStatus and updateStatus methods

    private void showError(String message) {
        if (isAdded()) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
        }
    }

    private synchronized void tryNavigateToHome() {
        if (!isAdded() || !dataLoadComplete) {
            return; // Don't navigate if fragment detached or data not ready
        }

        long elapsedTimeMs = SystemClock.elapsedRealtime() - startTimeMs;
        long remainingTimeMs = MIN_DISPLAY_TIME_MS - elapsedTimeMs;

        if (remainingTimeMs <= 0) {
            // Minimum time has passed, navigate now
            navigateToHomeInternal();
        } else {
            // Minimum time hasn't passed, schedule navigation
            handler.postDelayed(this::navigateToHomeInternal, remainingTimeMs);
        }
    }

    private void navigateToHomeInternal() {
        // Ensure navigation happens only once and on the main thread
        handler.removeCallbacks(this::navigateToHomeInternal); // Remove any pending calls
        if (isAdded()) {
            try {
                // Check if already navigated (to prevent crashes on rapid calls)
                if (Navigation.findNavController(requireView()).getCurrentDestination() != null &&
                    Navigation.findNavController(requireView()).getCurrentDestination().getId() == R.id.loadingFragment) {
                    Navigation.findNavController(requireView()).navigate(R.id.action_loading_to_home);
                }
            } catch (IllegalStateException | IllegalArgumentException e) {
                // IllegalStateException: Fragment not associated with a NavController
                // IllegalArgumentException: Navigation action/destination not found or already navigating
                Log.e(TAG, "ホーム画面への遷移に失敗しました (すでに遷移中または無効な状態)", e);
            } catch (Exception e) { // Catch other potential exceptions
                 Log.e(TAG, "ホーム画面への遷移中に予期せぬエラーが発生しました", e);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacksAndMessages(null); // Clear handler messages

        // Shutdown the executor service
        if (databaseExecutor != null && !databaseExecutor.isShutdown()) {
            databaseExecutor.shutdown();
        }

        // AsyncHelperのリソース解放 (If it needs cleanup)
        if (asyncHelper != null) {
            // Assuming asyncHelper has a cleanup method that's safe to call
            // If asyncHelper uses coroutines internally, its cleanup needs to handle that.
            // asyncHelper.cleanup(); // Uncomment if cleanup is necessary and safe
        }
    }
}
