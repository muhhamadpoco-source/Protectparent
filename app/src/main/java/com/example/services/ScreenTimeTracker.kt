package com.example.services

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Build
import com.example.data.SyncRepository
import java.util.Calendar

class ScreenTimeTracker(private val context: Context) {
    fun fetchAndSyncScreenTime() {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        
        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startTime = calendar.timeInMillis

        val usageStatsList = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        )

        val screenTimeMap = mutableMapOf<String, Long>()
        
        usageStatsList?.forEach { stats ->
            if (stats.totalTimeInForeground > 0) {
                // Aggregate time by package name
                val current = screenTimeMap[stats.packageName] ?: 0L
                screenTimeMap[stats.packageName] = current + stats.totalTimeInForeground
            }
        }
        
        // Sync to cloud (or our local facade)
        SyncRepository.updateScreenTime(screenTimeMap)
    }
}
