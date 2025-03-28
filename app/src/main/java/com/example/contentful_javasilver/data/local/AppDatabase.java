package com.example.contentful_javasilver.data.local;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.contentful_javasilver.data.local.dao.QuizDao;
import com.example.contentful_javasilver.data.local.entity.QuizEntity;

@Database(
    entities = {QuizEntity.class},
    version = 1,
    exportSchema = true
)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract QuizDao quizDao();
} 