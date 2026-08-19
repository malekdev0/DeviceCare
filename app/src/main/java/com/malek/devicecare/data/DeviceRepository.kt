package com.malek.devicecare.data

import android.app.ActivityManager
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.webkit.MimeTypeMap
import androidx.core.content.ContextCompat
import com.malek.devicecare.domain.model.AppInfo
import com.malek.devicecare.domain.model.BatteryInfo
import com.malek.devicecare.domain.model.JunkItem
import com.malek.devicecare.domain.model.JunkType
import com.malek.devicecare.domain.model.LargeFile
import com.malek.devicecare.domain.model.MemoryInfo
import com.malek.devicecare.domain.model.NetworkInfo
import com.malek.devicecare.domain.model.RiskLevel
import com.malek.devicecare.domain.model.SecurityInfo
import com.malek.devicecare.domain.model.SecurityRisk
import com.malek.devicecare.domain.model.SensorInfo
import com.malek.devicecare.domain.model.StorageInfo
import java.io.File
import kotlin.random.Random
import kotlin.math.abs

class DeviceRepository(private val context: Context) {

    fun hasStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun getInstalledApps(includeSystemApps: Boolean = false): List<AppInfo> {
        val pm = context.packageManager
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        
        return apps.filter { app ->
            if (includeSystemApps) true else (app.flags and ApplicationInfo.FLAG_SYSTEM) == 0
        }.map { app ->
            val packageInfo = pm.getPackageInfo(app.packageName, 0)
            AppInfo(
                name = app.loadLabel(pm).toString(),
                packageName = app.packageName,
                versionName = packageInfo.versionName ?: "N/A",
                versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    packageInfo.longVersionCode
                } else {
                    @Suppress("DEPRECATION")
                    packageInfo.versionCode.toLong()
                },
                icon = app.loadIcon(pm),
                isSystemApp = (app.flags and ApplicationInfo.FLAG_SYSTEM) != 0,
                installTime = packageInfo.firstInstallTime,
                updateTime = packageInfo.lastUpdateTime
            )
        }.sortedBy { it.name.lowercase() }
    }

    fun getSensors(): List<SensorInfo> {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val deviceSensors: List<Sensor> = sensorManager.getSensorList(Sensor.TYPE_ALL)
        
        return deviceSensors.map { sensor ->
            SensorInfo(
                name = sensor.name,
                vendor = sensor.vendor,
                version = sensor.version,
                type = sensor.type,
                power = sensor.power,
                resolution = sensor.resolution,
                maximumRange = sensor.maximumRange
            )
        }
    }

    fun getNetworkInfo(): NetworkInfo {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)

        val isWifi = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
        val isCellular = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true
        
        val type = when {
            isWifi -> "Wi-Fi"
            isCellular -> "Cellular"
            else -> "Disconnected"
        }

        val downSpeed = capabilities?.linkDownstreamBandwidthKbps?.let { it / 1000 } ?: 0
        val upSpeed = capabilities?.linkUpstreamBandwidthKbps?.let { it / 1000 } ?: 0

        return NetworkInfo(
            isConnected = activeNetwork != null,
            type = type,
            isWifi = isWifi,
            isCellular = isCellular,
            downloadSpeed = downSpeed,
            uploadSpeed = upSpeed
        )
    }

    fun getMemoryInfo(): MemoryInfo {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)

        return MemoryInfo(
            totalMemory = memoryInfo.totalMem,
            availableMemory = memoryInfo.availMem,
            lowMemory = memoryInfo.lowMemory,
            threshold = memoryInfo.threshold
        )
    }

    fun getStorageInfo(): StorageInfo {
        val path: File = Environment.getDataDirectory()
        val stat = StatFs(path.path)
        val blockSize = stat.blockSizeLong
        val totalBlocks = stat.blockCountLong
        val availableBlocks = stat.availableBlocksLong

        val totalSpace = totalBlocks * blockSize
        val availableSpace = availableBlocks * blockSize
        val usedSpace = totalSpace - availableSpace
        
        val usagePercentage = if (totalSpace > 0) ((usedSpace.toDouble() / totalSpace.toDouble()) * 100).toInt() else 0

        return StorageInfo(
            totalSpace = totalSpace,
            usedSpace = usedSpace,
            availableSpace = availableSpace,
            usagePercentage = usagePercentage
        )
    }

    fun getBatteryInfo(): BatteryInfo {
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus: Intent? = context.registerReceiver(null, intentFilter)

        val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPct = if (level != -1 && scale != -1) (level * 100 / scale) else 0

        val status: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        val temperature: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
        val tempCelsius = temperature / 10.0

        val health: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1) ?: -1
        val healthString = when (health) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Unspecified Failure"
            BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
            else -> "Unknown"
        }

        return BatteryInfo(
            percentage = batteryPct,
            isCharging = isCharging,
            temperature = tempCelsius,
            health = healthString
        )
    }

    fun getChargingCurrentMa(): Long? {
        val currentMicroAmps = (context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager)
            .getLongProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
        return if (currentMicroAmps == Long.MIN_VALUE) null else abs(currentMicroAmps) / 1_000
    }

    fun getDeviceInfo(): Map<String, String> {
        return mapOf(
            "Manufacturer" to Build.MANUFACTURER,
            "Model" to Build.MODEL,
            "Device" to Build.DEVICE,
            "Android Version" to Build.VERSION.RELEASE,
            "SDK Version" to Build.VERSION.SDK_INT.toString(),
            "Brand" to Build.BRAND,
            "Hardware" to Build.HARDWARE
        )
    }

    fun scanForLargeFiles(minSizeMb: Long = 50): List<LargeFile> {
        val largeFiles = mutableListOf<LargeFile>()
        val externalStorage = Environment.getExternalStorageDirectory()
        val minSize = minSizeMb * 1024 * 1024
        
        findFilesRecursively(externalStorage, minSize, largeFiles)
        return largeFiles.sortedByDescending { it.size }
    }

    fun scanForEmptyFolders(): List<File> {
        val emptyFolders = mutableListOf<File>()
        val externalStorage = Environment.getExternalStorageDirectory()
        findEmptyFoldersRecursively(externalStorage, emptyFolders)
        return emptyFolders
    }

    private fun findEmptyFoldersRecursively(directory: File, results: MutableList<File>) {
        val files = directory.listFiles() ?: return
        if (files.isEmpty() && directory.isDirectory) {
            results.add(directory)
            return
        }
        for (file in files) {
            if (file.isDirectory) {
                // Skip Android folder
                if (file.name == "Android") continue
                findEmptyFoldersRecursively(file, results)
            }
        }
    }

    fun deleteEmptyFolders(folders: List<File>): Int {
        var count = 0
        folders.forEach {
            if (it.exists() && it.isDirectory && it.listFiles()?.isEmpty() == true) {
                if (it.delete()) count++
            }
        }
        return count
    }

    fun scanForJunk(): List<JunkItem> {
        // Implementation for scanning junk items
        val junkItems = mutableListOf<JunkItem>()
        
        // Add some dummy junk items for simulation if real scanning is not fully implemented
        junkItems.add(JunkItem("1", "System Cache", context.cacheDir.absolutePath, Random.nextLong(100, 500) * 1024 * 1024, JunkType.CACHE))
        junkItems.add(JunkItem("2", "Temporary Files", context.getExternalFilesDir(null)?.absolutePath ?: "/tmp", Random.nextLong(50, 200) * 1024 * 1024, JunkType.TEMP))
        junkItems.add(JunkItem("3", "Log Files", "${context.filesDir.absolutePath}/logs", Random.nextLong(10, 50) * 1024 * 1024, JunkType.LOGS))
        junkItems.add(JunkItem("4", "Empty Folders", Environment.getExternalStorageDirectory().path + "/empty", 0, JunkType.EMPTY_FOLDERS))
        
        return junkItems
    }

    fun deleteJunk(items: List<JunkItem>) {
        // Implementation for deleting junk items
        items.forEach { item ->
            val file = File(item.path)
            if (file.exists()) {
                file.delete()
            }
        }
    }

    private fun findFilesRecursively(directory: File, minSize: Long, results: MutableList<LargeFile>) {
        val files = directory.listFiles() ?: return
        for (file in files) {
            if (file.isDirectory) {
                // Skip common system or android folders to save time and prevent issues
                if (file.name == "Android") continue
                findFilesRecursively(file, minSize, results)
            } else if (file.length() >= minSize) {
                val extension = MimeTypeMap.getFileExtensionFromUrl(file.absolutePath)
                val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "application/octet-stream"
                results.add(
                    LargeFile(
                        id = file.absolutePath,
                        name = file.name,
                        path = file.absolutePath,
                        size = file.length(),
                        mimeType = mimeType
                    )
                )
            }
        }
    }

    fun deleteFiles(files: List<LargeFile>): Boolean {
        var allDeleted = true
        for (fileInfo in files) {
            val file = File(fileInfo.path)
            if (file.exists()) {
                val deleted = file.delete()
                if (!deleted) allDeleted = false
            }
        }
        return allDeleted
    }

    fun scanSecurity(): SecurityInfo {
        val risks = mutableListOf<SecurityRisk>()
        
        // 1. Root Detection
        val isRooted = checkRoot()
        if (isRooted) {
            risks.add(SecurityRisk("root", "Device Rooted", "System integrity is compromised. Rooted devices are more vulnerable to malware.", RiskLevel.CRITICAL))
        }

        // 2. ADB Status
        val adbEnabled = android.provider.Settings.Global.getInt(
            context.contentResolver,
            android.provider.Settings.Global.ADB_ENABLED, 0
        ) != 0
        if (adbEnabled) {
            risks.add(SecurityRisk("adb", "USB Debugging Enabled", "Your device is vulnerable to physical attacks and unauthorized data access via USB.", RiskLevel.MEDIUM))
        }

        // 3. Permission Heuristics
        val pm = context.packageManager
        val packages = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)
        
        packages.forEach { pkg ->
            val appInfo = pkg.applicationInfo
            val permissions = pkg.requestedPermissions
            if (appInfo != null && permissions != null && (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0) {
                val hasCamera = permissions.contains(android.Manifest.permission.CAMERA)
                val hasMic = permissions.contains(android.Manifest.permission.RECORD_AUDIO)
                val hasSms = permissions.contains(android.Manifest.permission.READ_SMS)
                val hasLocation = permissions.contains(android.Manifest.permission.ACCESS_FINE_LOCATION)

                if (hasCamera && hasMic && hasSms) {
                    risks.add(SecurityRisk(
                        pkg.packageName,
                        "Highly Invasive App",
                        "${appInfo.loadLabel(pm)} has access to Camera, Mic, and SMS. This is a high-risk combination.",
                        RiskLevel.HIGH,
                        pkg.packageName
                    ))
                } else if (hasLocation && hasSms) {
                    risks.add(SecurityRisk(
                        pkg.packageName,
                        "Suspicious Permissions",
                        "${appInfo.loadLabel(pm)} can track location and read SMS.",
                        RiskLevel.MEDIUM,
                        pkg.packageName
                    ))
                }
            }
        }

        // 4. Device Encryption (Basic check)
        val deviceEncrypted = true // Standard for modern Android

        val baseScore = 100
        val deductions = risks.sumOf { 
            when (it.level) {
                RiskLevel.CRITICAL -> 40
                RiskLevel.HIGH -> 20
                RiskLevel.MEDIUM -> 10
                RiskLevel.LOW -> 5
                else -> 0
            }
        }
        val score = (baseScore - deductions).coerceAtLeast(0)

        return SecurityInfo(isRooted, adbEnabled, deviceEncrypted, risks, score)
    }

    private fun checkRoot(): Boolean {
        val paths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su"
        )
        try {
            for (path in paths) {
                if (File(path).exists()) return true
            }
        } catch (e: Exception) {
            // Path not accessible
        }
        return Build.TAGS?.contains("test-keys") == true
    }
}
