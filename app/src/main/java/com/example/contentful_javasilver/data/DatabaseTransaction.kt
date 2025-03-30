package com.example.contentful_javasilver.data

import androidx.room.withTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * データベーストランザクションを安全に処理するユーティリティクラス
 */
object DatabaseTransaction {
    /**
     * トランザクション内で処理を実行し、自動的にリソースを閉じる
     * @param database データベースインスタンス
     * @param block 実行するトランザクション処理
     * @return トランザクション処理の結果
     */
    suspend fun <T> executeInTransaction(database: QuizDatabase, block: suspend (QuizDao) -> T): T {
        return withContext(Dispatchers.IO) {
            // Room 2.2以降ではwithTransactionを使うとサスペンド関数を直接呼び出せる
            database.withTransaction {
                block(database.quizDao())
            }
        }
    }

    /**
     * クエリを実行して安全にリソースを閉じる
     * @param database データベースインスタンス
     * @param block 実行するクエリ処理
     * @return クエリ処理の結果
     */
    suspend fun <T> executeQuery(database: QuizDatabase, block: suspend (QuizDao) -> T): T {
        return withContext(Dispatchers.IO) {
            block(database.quizDao())
        }
    }
} 