package com.aerobush.spark

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.aerobush.spark.data.AppContainer
import com.aerobush.spark.data.AppDataContainer
import com.aerobush.spark.data.SparkConstants
import com.aerobush.spark.data.UserPreferencesRepository

private const val USER_PREFERENCES_NAME = "user_preferences"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = USER_PREFERENCES_NAME
)

class SparkApplication : Application() {
    /**
     * AppContainer instance used by the rest of classes to obtain dependencies
     */
    lateinit var container: AppContainer

    lateinit var userPreferencesRepository: UserPreferencesRepository

    override fun onCreate() {
        super.onCreate()

        container = AppDataContainer(this)

        userPreferencesRepository = UserPreferencesRepository(dataStore)

        val normalChannel = NotificationChannel(
            SparkConstants.NORMAL_CHANNEL_ID,
            getString(R.string.normal_reminders_title),
            NotificationManager.IMPORTANCE_HIGH
        )
        normalChannel.description = getString(R.string.normal_reminders)

        val urgentChannel = NotificationChannel(
            SparkConstants.URGENT_CHANNEL_ID,
            getString(R.string.urgent_reminders_title),
            NotificationManager.IMPORTANCE_HIGH
        )
        urgentChannel.description = getString(R.string.urgent_reminders)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(normalChannel)
        notificationManager.createNotificationChannel(urgentChannel)
    }
}