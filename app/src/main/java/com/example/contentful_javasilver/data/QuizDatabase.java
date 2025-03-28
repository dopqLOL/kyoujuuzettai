package com.example.contentful_javasilver.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {QuizEntity.class}, version = 5)
@TypeConverters({QuizEntity.Converters.class})
public abstract class QuizDatabase extends RoomDatabase {
    public abstract QuizDao quizDao();

    private static volatile QuizDatabase INSTANCE;

    public static QuizDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (QuizDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            QuizDatabase.class,
                            "quiz_database"
                    )
                    .fallbackToDestructiveMigration()
                    .build();
                }
            }
        }
        return INSTANCE;
    }
} 