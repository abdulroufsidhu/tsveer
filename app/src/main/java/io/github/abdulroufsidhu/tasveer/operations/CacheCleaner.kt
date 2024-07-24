package io.github.abdulroufsidhu.tasveer.operations

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit


class CacheCleaner(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        try {
            applicationContext.cacheDir.deleteRecursively()
            applicationContext.externalCacheDir?.deleteRecursively()
            applicationContext.externalCacheDirs?.forEach { it.deleteRecursively() }
            return Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.failure()
        }
    }

    companion object {
        fun schedule(context: Context) {
            val worker = PeriodicWorkRequestBuilder<CacheCleaner>(1, TimeUnit.DAYS)
                .build()
            val wm = ContextCompat.getSystemService(context, WorkManager::class.java)
            wm?.enqueue(worker)
        }
    }

}
