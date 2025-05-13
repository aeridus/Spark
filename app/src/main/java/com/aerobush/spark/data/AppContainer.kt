package com.aerobush.spark.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

private const val USER_PREFERENCES_NAME = "user_preferences"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = USER_PREFERENCES_NAME
)

/**
 * App container for Dependency injection.
 */
interface AppContainer {
    val sparkTimeItemsRepository: SparkTimeItemsRepository
    val userPreferencesRepository: UserPreferencesRepository
}

/**
 * [AppContainer] implementation that provides instance of [OfflineSparkTimeItemsRepository]
 */
class AppDataContainer(private val context: Context) : AppContainer {
    /**
     * Implementation for [SparkTimeItemsRepository]
     */
    override val sparkTimeItemsRepository: SparkTimeItemsRepository by lazy {
        val sparkTimeItemsRepo = OfflineSparkTimeItemsRepository(
            context,
            SparkDatabase.getDatabase(context).sparkTimeItemDao()
        )

        sparkTimeItemsRepo.startMoodyWorker()

        sparkTimeItemsRepo
    }

    /**
     * Implementation for [UserPreferencesRepository]
     */
    override val userPreferencesRepository: UserPreferencesRepository by lazy {
        val userPreferencesRepo = UserPreferencesRepository(
            context.dataStore
        )

        userPreferencesRepo
    }
}