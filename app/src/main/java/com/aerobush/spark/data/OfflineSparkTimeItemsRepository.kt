package com.aerobush.spark.data

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.aerobush.spark.workers.MoodyWorker
import kotlinx.coroutines.flow.Flow
import java.time.Duration

class OfflineSparkTimeItemsRepository(
    context: Context,
    private val sparkTimeItemDao: SparkTimeItemDao
) : SparkTimeItemsRepository {
    override suspend fun insertItem(item: SparkTimeItem) {
        sparkTimeItemDao.insert(item)
        sparkTimeItemDao.deleteStaleItems(
            TimeUtils.toEpochMilli(TimeUtils.getCurrentTime().minusDays(10))
        )
    }

    override suspend fun updateItem(item: SparkTimeItem) = sparkTimeItemDao.update(item)

    override suspend fun deleteItem(item: SparkTimeItem) = sparkTimeItemDao.delete(item)

    override fun getItemStream(id: Long): Flow<SparkTimeItem?> = sparkTimeItemDao.getItem(id)

    override fun getAllItemsStream(): Flow<List<SparkTimeItem>> = sparkTimeItemDao.getAllItems()

    override fun getRecentItemsStream(time: Long): Flow<List<SparkTimeItem>> = sparkTimeItemDao.getRecentItems(time)

    override suspend fun getLastItem() = sparkTimeItemDao.getLastItem()

    override suspend fun deleteStaleItems(time: Long) = sparkTimeItemDao.deleteStaleItems(time)

    // WorkManager
    private val workManager = WorkManager.getInstance(context)

    override fun startMoodyWorker() {
        val moodyBuilder = PeriodicWorkRequestBuilder<MoodyWorker>(
            repeatInterval = Duration.ofMinutes(15L)
        )

        workManager.enqueueUniquePeriodicWork(
            "MoodyWorker",
            ExistingPeriodicWorkPolicy.KEEP,
            moodyBuilder.build()
        )
    }
}