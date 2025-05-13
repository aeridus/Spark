package com.aerobush.spark.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [SparkTimeItem::class], version = 2, exportSchema = false)
abstract class SparkDatabase : RoomDatabase() {
    abstract fun sparkTimeItemDao(): SparkTimeItemDao

    companion object {
        @Volatile
        private var Instance: SparkDatabase? = null

        fun getDatabase(context: Context): SparkDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, SparkDatabase::class.java, "spark_database")
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }
}