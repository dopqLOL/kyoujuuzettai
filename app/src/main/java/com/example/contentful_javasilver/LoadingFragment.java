package com.example.contentful_javasilver;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
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
    private ProgressBar progressBar;
    private TextView statusText;
    private final Handler handler = new Handler(Looper.getMainLooper()); // Handler for UI updates

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

        progressBar = view.findViewById(R.id.loadingProgressBar);
        statusText = view.findViewById(R.id.loadingStatusText);

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
        updateStatus(getString(R.string.loading_status_checking)); // "データを確認中..."
        progressBar.setIndeterminate(true); // 確認中は不定プログレス

        databaseExecutor.execute(() -> {
            try {
                int quizCount = database.quizDao().getQuizCountSync();
                handler.post(() -> { // UIスレッドで処理
                    if (isAdded()) { // FragmentがまだActivityにアタッチされているか確認
                        progressBar.setIndeterminate(false); // プログレスを確定に戻す
                        if (quizCount > 0) {
                            // データが存在する場合
                            Log.d(TAG, "データベースに既存データあり (" + quizCount + "件)。ダウンロードをスキップします。");
                            updateProgressAndStatus(100, getString(R.string.loading_status_loading)); // "データを読み込み中..."
                            // 少し待ってからホームへ遷移 (ユーザーがメッセージを読めるように)
                            handler.postDelayed(this::navigateToHome, 1000);
                        } else {
                            // データが存在しない場合
                            Log.d(TAG, "データベースにデータなし。Contentfulからダウンロードを開始します。");
                            updateProgressAndStatus(10, getString(R.string.loading_status_downloading)); // "最新データをダウンロード中..."
                            downloadAllData();
                        }
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "データベースのカウント取得に失敗", e);
                handler.post(() -> {
                    if (isAdded()) {
                        updateProgressAndStatus(0, getString(R.string.loading_status_error_saving)); // DBエラー表示
                        showError("データベース確認エラー: " + e.getMessage());
                    }
                });
            }
        });
    }

    // checkForUpdatesメソッドは不要になったため削除可能ですが、一旦コメントアウトします
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
                    // This callback likely runs on the main thread if asyncHelper is designed well
                    updateProgressAndStatus(progress, status);
                    return Unit.INSTANCE;
                },
                entries -> {
                    Log.d(TAG, entries.size() + "件のデータをダウンロードしました");
                    // Use new string resource for saving status
                    String savingStatus = getString(R.string.loading_status_saving) + " " + entries.size() + "件";
                    updateProgressAndStatus(70, savingStatus);

                    List<QuizEntity> entities = convertToQuizEntities(entries);
                    saveToDatabase(entities); // Call the refactored save method

                    return Unit.INSTANCE;
                },
                error -> {
                    Log.e(TAG, "データのダウンロードに失敗しました: " + error);
                    // Use new string resource for download error
                    updateProgressAndStatus(0, getString(R.string.loading_status_error_download));
                    showError(error);
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
                        questionText, choices, intAnswers, explanation
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
                    // Use new string resource for no data error
                    updateProgressAndStatus(0, getString(R.string.loading_status_error_no_data));
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
                        // Use new string resource for completion status
                        String completeStatus = getString(R.string.loading_status_complete) + " " + entities.size() + "件のクイズを保存しました";
                        updateProgressAndStatus(100, completeStatus);
                        // Use the same handler for delayed navigation
                        handler.postDelayed(this::navigateToHome, 1500);
                    }
                });

            } catch (Exception dbException) {
                Log.e(TAG, "Database transaction failed", dbException);
                // Post error UI update back to the main thread
                handler.post(() -> {
                    if (isAdded()) {
                        // Use new string resource for saving error
                        updateProgressAndStatus(0, getString(R.string.loading_status_error_saving));
                        showError("データベースエラー: " + dbException.getMessage()); // Keep specific error message
                    }
                });
            }
        });
    }

    private void updateProgressAndStatus(int progress, String status) {
        if (isAdded()) {
            // Ensure progress bar is not indeterminate when setting progress
            if (progressBar.isIndeterminate()) {
                progressBar.setIndeterminate(false);
            }
            progressBar.setProgress(progress, true); // Animate progress change
            updateStatus(status);
        }
    }

    private void updateStatus(String status) {
        if (statusText != null) {
            statusText.setText(status);
        }
    }

    private void showError(String message) {
        if (isAdded()) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
        }
    }

    private void navigateToHome() {
        if (isAdded()) {
            try {
                Navigation.findNavController(requireView()).navigate(R.id.action_loading_to_home);
            } catch (Exception e) {
                Log.e(TAG, "ホーム画面への遷移に失敗しました", e);
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
