package com.aerobush.spark.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SparkTimeItemDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(sparkTimeItem: SparkTimeItem)

    @Update
    suspend fun update(sparkTimeItem: SparkTimeItem)

    @Delete
    suspend fun delete(sparkTimeItem: SparkTimeItem)

    @Query("SELECT * from spark_time_items WHERE id = :id")
    fun getItem(id: Long): Flow<SparkTimeItem>

    @Query("SELECT * from spark_time_items ORDER BY time ASC")
    fun getAllItems(): Flow<List<SparkTimeItem>>

    @Query("SELECT * from spark_time_items WHERE time >= :time ORDER BY time ASC")
    fun getRecentItems(time: Long): Flow<List<SparkTimeItem>>

    @Query("SELECT * from spark_time_items ORDER BY time DESC LIMIT 1")
    suspend fun getLastItem(): SparkTimeItem?

    @Query("DELETE from spark_time_items WHERE time < :time")
    suspend fun deleteStaleItems(time: Long)
}