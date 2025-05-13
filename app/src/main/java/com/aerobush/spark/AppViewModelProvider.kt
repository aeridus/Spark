package com.aerobush.spark

import android.app.Application
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.aerobush.spark.ui.item.SparkTimeItemViewModel

/**
 * Provides Factory to create instance of ViewModel for the entire Spark app
 */
object AppViewModelProvider {
    val Factory = viewModelFactory {
        // Initializer for SparkTimeItemViewModel
        initializer {
            SparkTimeItemViewModel(
                sparkApplication().container.sparkTimeItemsRepository,
                sparkApplication().container.userPreferencesRepository
            )
        }
    }
}

/**
 * Extension function to queries for [Application] object and returns an instance of
 * [SparkApplication].
 */
fun CreationExtras.sparkApplication(): SparkApplication =
    (this[AndroidViewModelFactory.APPLICATION_KEY] as SparkApplication)