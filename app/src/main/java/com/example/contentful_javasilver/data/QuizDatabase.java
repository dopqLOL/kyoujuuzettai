package com.example.contentful_javasilver.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

/**
 * QuizデータベースのRoomデータベースクラス
 */
@Database(entities = {QuizEntity.class}, version = 11, exportSchema = false)
@TypeConverters({QuizEntity.Converters.class})
public abstract class QuizDatabase extends RoomDatabase {
    private static volatile QuizDatabase INSTANCE;

    /**
     * QuizDaoを取得するための抽象メソッド
     * @return QuizDaoインスタンス
     */
    public abstract QuizDao quizDao();

    /**
     * データベースインスタンスを取得（シングルトンパターン）
     * @param context アプリケーションコンテキスト
     * @return QuizDatabaseインスタンス
     */
    public static QuizDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            synchronized (QuizDatabase.class) {
                if (INSTANCE == null) {
                    // データベースインスタンスを作成
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            QuizDatabase.class,
                            "quiz_database")
                            // スキーマバージョンが変更されたときに既存のデータベースを削除して再作成
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
} 