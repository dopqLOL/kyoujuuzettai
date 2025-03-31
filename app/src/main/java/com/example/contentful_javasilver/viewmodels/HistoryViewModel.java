package com.example.contentful_javasilver.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

// import com.example.contentful_javasilver.data.ProblemStats; // No longer needed
import com.example.contentful_javasilver.data.QuizHistory; // Import QuizHistory
import com.example.contentful_javasilver.data.QuizDao;
import com.example.contentful_javasilver.data.QuizDatabase;

import java.util.List;

public class HistoryViewModel extends AndroidViewModel {

    private final QuizDao quizDao;
    private final LiveData<List<QuizHistory>> allHistory; // Changed type

    public HistoryViewModel(@NonNull Application application) {
        super(application);
        QuizDatabase db = QuizDatabase.getDatabase(application);
        quizDao = db.quizDao();
        allHistory = quizDao.getAllHistorySortedByTimestampDesc(); // Call new DAO method
    }

    public LiveData<List<QuizHistory>> getAllHistory() { // Changed method name and return type
        return allHistory;
    }
}
