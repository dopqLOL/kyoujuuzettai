package com.example.contentful_javasilver

import com.contentful.java.cda.CDAEntry
import com.contentful.java.cda.CDAClient
import kotlinx.coroutines.*
import java.util.concurrent.CompletableFuture
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import com.example.contentful_javasilver.data.QuizDatabase
import com.example.contentful_javasilver.data.QuizEntity
import kotlin.Unit

class AsyncHelperCoroutines(private val api: ContentfulGetApi) {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // エラーメッセージの定数
    companion object {
        private const val ERROR_NETWORK = "ネットワークエラーが発生しました。インターネット接続を確認してください。"
        private const val ERROR_TIMEOUT = "通信がタイムアウトしました。もう一度お試しください。"
        private const val ERROR_SERVER = "サーバーエラーが発生しました。しばらく時間をおいて再度お試しください。"
        private const val ERROR_UNKNOWN = "予期せぬエラーが発生しました。"
    }

    // エラーハンドリング用の関数
    private fun handleError(e: Exception): String {
        return when (e) {
            is IOException -> ERROR_NETWORK
            is SocketTimeoutException -> ERROR_TIMEOUT
            is UnknownHostException -> ERROR_NETWORK
            else -> ERROR_UNKNOWN
        }
    }

    // 🔹 コールバック方式（Java から簡単に呼び出せる）
    fun fetchEntriesAsync(contentType: String, callback: Function1<List<CDAEntry>, Unit>, errorCallback: Function1<String, Unit>) {
        scope.launch {
            try {
                val result = api.fetchEntries(contentType).items().map { it as CDAEntry }
                withContext(Dispatchers.Main) {
                    callback.invoke(result)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    errorCallback.invoke(handleError(e))
                }
            }
        }
    }

    // 🔹 CompletableFuture 方式（Java からも扱いやすい）
    fun fetchEntriesFuture(contentType: String): CompletableFuture<List<CDAEntry>> {
        val future = CompletableFuture<List<CDAEntry>>()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = api.fetchEntries(contentType).items().map { it as CDAEntry }
                future.complete(result)
            } catch (e: Exception) {
                e.printStackTrace()
                future.completeExceptionally(e)
            }
        }
        return future
    }

    // 🔹 特定のエントリを非同期取得（コールバック）
    fun fetchEntryByIdAsync(entryId: String, callback: (CDAEntry?) -> Unit, errorCallback: (String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = api.fetchEntryById(entryId)
                withContext(Dispatchers.Main) {
                    callback(result)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    errorCallback(handleError(e))
                    callback(null)
                }
            }
        }
    }

    // 🔹 CompletableFuture 方式で特定のエントリを取得
    fun fetchEntryByIdFuture(entryId: String): CompletableFuture<CDAEntry?> {
        val future = CompletableFuture<CDAEntry?>()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = api.fetchEntryById(entryId)
                future.complete(result)
            } catch (e: Exception) {
                e.printStackTrace()
                future.completeExceptionally(e)
            }
        }
        return future
    }

    // 🔹 データベース操作用のメソッド
    fun insertQuizEntitiesAsync(db: QuizDatabase, entities: List<QuizEntity>, onSuccess: Function0<Unit>, onError: Function1<String, Unit>) {
        scope.launch {
            try {
                db.quizDao().insertAll(entities)
                withContext(Dispatchers.Main) {
                    onSuccess.invoke()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    onError.invoke("データベースの更新に失敗しました")
                }
            }
        }
    }

    fun getRandomQuizzesAsync(db: QuizDatabase, count: Int, onSuccess: Function1<List<QuizEntity>, Unit>, onError: Function1<String, Unit>) {
        scope.launch {
            try {
                val quizzes = db.quizDao().getRandomQuizzesSync(count)
                withContext(Dispatchers.Main) {
                    onSuccess.invoke(quizzes)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    onError.invoke("クイズの取得に失敗しました")
                }
            }
        }
    }

    fun cleanup() {
        scope.cancel()
    }
}
