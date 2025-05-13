package com.aerobush.spark.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.aerobush.spark.R
import com.aerobush.spark.data.AppDataContainer
import com.aerobush.spark.data.TimeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

private const val TAG = "MoodyWorker"

class MoodyWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            delay(DELAY_TIME_MILLIS)

            return@withContext try {
                val sparkTimeItemsRepo = AppDataContainer(applicationContext).sparkTimeItemsRepository

                val lastSparkTimeItem = sparkTimeItemsRepo.getLastItem()
                if (lastSparkTimeItem != null)
                {
                    val lastSparkTime = TimeUtils.toOffsetDateTime(lastSparkTimeItem.time)

                    var totalHours = 24L
                    TimeUtils.getDurationParts(
                        startTime = lastSparkTime,
                        endTime = TimeUtils.getCurrentTime(),
                        output =  { hours, _ ->
                            totalHours = hours
                        }
                    )

                    if ((totalHours >= 48) && (!lastSparkTimeItem.sentSecondReminder))
                    {
                        WorkerUtils.makeUrgentNotification(
                            title = applicationContext.resources.getString(R.string.time_to_mood_title),
                            message = applicationContext.resources.getString(R.string.time_to_mood),
                            context = applicationContext
                        )

                        lastSparkTimeItem.sentFirstReminder = true
                        lastSparkTimeItem.sentSecondReminder = true

                        sparkTimeItemsRepo.updateItem(lastSparkTimeItem)
                    }
                    else if ((totalHours >= 24) && (!lastSparkTimeItem.sentFirstReminder))
                    {
                        WorkerUtils.makeNormalNotification(
                            title = applicationContext.resources.getString(R.string.safe_to_mood_title),
                            message = applicationContext.resources.getString(R.string.safe_to_mood),
                            context = applicationContext
                        )

                        lastSparkTimeItem.sentFirstReminder = true

                        sparkTimeItemsRepo.updateItem(lastSparkTimeItem)
                    }
                }

                Result.success()
            } catch (throwable: Throwable) {
                Log.e(
                    TAG,
                    applicationContext.resources.getString(R.string.failed_to_notify_user),
                    throwable
                )

                Result.failure()
            }
        }
    }

    companion object {
        const val DELAY_TIME_MILLIS = 5_000L
    }
}