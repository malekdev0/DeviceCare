package com.malek.devicecare.data

import android.content.Context
import com.malek.devicecare.domain.model.HealthScanRecord
import com.malek.devicecare.domain.model.HealthStatus
import org.json.JSONArray
import org.json.JSONObject

class ScanHistoryRepository(context: Context) {
    private val preferences = context.getSharedPreferences("scan_history", Context.MODE_PRIVATE)

    fun getRecords(): List<HealthScanRecord> = runCatching {
        val entries = JSONArray(preferences.getString(RECORDS_KEY, "[]"))
        buildList {
            for (index in 0 until entries.length()) {
                val entry = entries.getJSONObject(index)
                add(
                    HealthScanRecord(
                        timestamp = entry.getLong("timestamp"),
                        score = entry.getInt("score"),
                        batteryStatus = HealthStatus.valueOf(entry.getString("battery")),
                        storageStatus = HealthStatus.valueOf(entry.getString("storage")),
                        memoryStatus = HealthStatus.valueOf(entry.getString("memory")),
                        networkStatus = HealthStatus.valueOf(entry.getString("network")),
                        securityStatus = HealthStatus.valueOf(entry.getString("security"))
                    )
                )
            }
        }.sortedByDescending { it.timestamp }
    }.getOrDefault(emptyList())

    fun add(record: HealthScanRecord) {
        val records = getRecords().toMutableList().apply {
            add(0, record)
            sortByDescending { it.timestamp }
            if (size > MAX_RECORDS) subList(MAX_RECORDS, size).clear()
        }
        val entries = JSONArray()
        records.forEach { record ->
            entries.put(
                JSONObject()
                    .put("timestamp", record.timestamp)
                    .put("score", record.score)
                    .put("battery", record.batteryStatus.name)
                    .put("storage", record.storageStatus.name)
                    .put("memory", record.memoryStatus.name)
                    .put("network", record.networkStatus.name)
                    .put("security", record.securityStatus.name)
            )
        }
        preferences.edit().putString(RECORDS_KEY, entries.toString()).apply()
    }

    private companion object {
        const val RECORDS_KEY = "records"
        const val MAX_RECORDS = 26
    }
}
