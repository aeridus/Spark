package com.aerobush.spark.data

import kotlinx.coroutines.flow.Flow

/**
 * Repository that provides insert, update, delete, and retrieve of [SparkTimeItem] from a given data source.
 */
interface SparkTimeItemsRepository {
    /**
     * Insert item in the data source
     */
    suspend fun insertItem(item: SparkTimeItem)

    /**
     * Update item in the data source
     */
    suspend fun updateItem(item: SparkTimeItem)

    /**
     * Delete item from the data source
     */
    suspend fun deleteItem(item: SparkTimeItem)

    /**
     * Retrieve an item from the given data source that matches with the [id].
     */
    fun getItemStream(id: Long): Flow<SparkTimeItem?>

    /**
     * Retrieve all the items from the the given data source.
     */
    fun getAllItemsStream(): Flow<List<SparkTimeItem>>

    /**
     * Retrieve all the recent items from the the given data source.
     */
    fun getRecentItemsStream(time: Long): Flow<List<SparkTimeItem>>

    /**
     * Retrieve the last item from the the given data source.
     */
    suspend fun getLastItem(): SparkTimeItem?

    /**
     * Delete all the stale items from the the given data source.
     */
    suspend fun deleteStaleItems(time: Long)

    /**
     * Start the moody worker.
     */
    fun startMoodyWorker()
}