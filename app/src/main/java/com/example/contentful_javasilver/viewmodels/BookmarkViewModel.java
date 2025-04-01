package com.example.contentful_javasilver.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.contentful_javasilver.data.QuizDao;
import com.example.contentful_javasilver.data.QuizDatabase;
import com.example.contentful_javasilver.data.QuizEntity;

import java.util.List;

public class BookmarkViewModel extends AndroidViewModel {

    private QuizDao quizDao;
    private LiveData<List<QuizEntity>> bookmarkedQuizzes;

    public BookmarkViewModel(@NonNull Application application) {
        super(application);
        QuizDatabase db = QuizDatabase.getDatabase(application);
        quizDao = db.quizDao();
        bookmarkedQuizzes = quizDao.getBookmarkedQuizzes();
    }

    public LiveData<List<QuizEntity>> getBookmarkedQuizzes() {
        return bookmarkedQuizzes;
    }
}
