package com.aerobush.spark.data

import java.time.Duration
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

class TimeUtils {
    companion object {
        fun getCurrentTime(): OffsetDateTime
        {
            return OffsetDateTime.now(ZoneOffset.UTC)
        }

        fun toEpochMilli(time: OffsetDateTime): Long
        {
            return time.toInstant().toEpochMilli()
        }

        fun toOffsetDateTime(time: Long): OffsetDateTime
        {
            return OffsetDateTime.ofInstant(
                Instant.ofEpochMilli(time),
                ZoneOffset.UTC
            )
        }

        fun getDayThresholdEpochMilli(dayThresholdHour: Int): Long {
            var dayThreshold = OffsetDateTime.now()

            // We want to look at the data from before midnight
            if (dayThreshold.hour < dayThresholdHour)
            {
                dayThreshold = dayThreshold.minusDays(1)
            }

            dayThreshold = dayThreshold
                .withHour(dayThresholdHour)
                .withMinute(0)
                .withSecond(0)
                .withNano(0)

            return toEpochMilli(dayThreshold)
        }

        fun getDurationParts(
            startTime: OffsetDateTime,
            endTime: OffsetDateTime,
            output: (hours: Long, minutes: Long) -> Unit
        ) {
            var totalHours = 24L
            var totalMinutes = 0L
            val durationSinceLastSpark = Duration.between(startTime, endTime)
            if (durationSinceLastSpark < Duration.ofHours(24L)) {
                totalMinutes = durationSinceLastSpark.toMinutes()
                if (totalMinutes >= 60L)
                {
                    totalHours = totalMinutes / 60L
                    totalMinutes -= totalHours * 60L
                }
                else
                {
                    totalHours = 0L
                }
            }

            output(totalHours, totalMinutes)
        }
    }
}