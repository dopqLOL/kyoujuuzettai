package com.example.contentful_javasilver.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration; // Add import
import androidx.sqlite.db.SupportSQLiteDatabase; // Add import
import androidx.annotation.NonNull; // Add import

/**
 * QuizデータベースのRoomデータベースクラス
 */
@Database(entities = {QuizEntity.class, QuizHistory.class}, version = 14, exportSchema = false) // Increment version to 14
@TypeConverters({QuizEntity.Converters.class})
public abstract class QuizDatabase extends RoomDatabase {
    private static volatile QuizDatabase INSTANCE;

    // Migration from version 13 to 14: Add isBookmarked column
    static final Migration MIGRATION_13_14 = new Migration(13, 14) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE quizzes ADD COLUMN isBookmarked INTEGER NOT NULL DEFAULT 0");
        }
    };

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
                            // Add migration from 13 to 14
                            .addMigrations(MIGRATION_13_14)
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
