package com.example.contentful_javasilver.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.contentful_javasilver.data.ProblemStats; // Import ProblemStats
import com.example.contentful_javasilver.data.QuizDao;
import com.example.contentful_javasilver.data.QuizDatabase;

import java.util.List;

public class StatisticsViewModel extends AndroidViewModel {

    private final QuizDao quizDao;
    private final LiveData<List<ProblemStats>> problemStatistics;

    public StatisticsViewModel(@NonNull Application application) {
        super(application);
        QuizDatabase db = QuizDatabase.getDatabase(application);
        quizDao = db.quizDao();
        problemStatistics = quizDao.getProblemStatistics(); // Call the new DAO method
    }

    public LiveData<List<ProblemStats>> getProblemStatistics() {
        return problemStatistics;
    }
}
