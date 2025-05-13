package com.aerobush.spark.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "spark_time_items")
data class SparkTimeItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val time: Long,
    @ColumnInfo(name = "spark_choice")
    val sparkChoice: String,
    @ColumnInfo(name = "sent_first_reminder")
    var sentFirstReminder: Boolean,
    @ColumnInfo(name = "sent_second_reminder")
    var sentSecondReminder: Boolean
)