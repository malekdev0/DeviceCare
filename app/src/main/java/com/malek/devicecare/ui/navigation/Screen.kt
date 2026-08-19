package com.malek.devicecare.ui.navigation

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object DeviceInfo : Screen("device_info")
    object Battery : Screen("battery")
    object ChargingCurrent : Screen("charging_current")
    object Storage : Screen("storage")
    object StorageCleaner : Screen("storage_cleaner")
    object WifiTest : Screen("wifi_test")
    object Memory : Screen("memory")
    object Network : Screen("network")
    object Sensors : Screen("sensors")
    object Security : Screen("security")
    object AppManager : Screen("app_manager")
    object HardwareTests : Screen("hardware_tests")
    object DisplayTest : Screen("display_test")
    object MultiTouchTest : Screen("multitouch_test")
    object CpuRoom : Screen("cpu_room")
    object FullScan : Screen("full_scan")
    object LargeFileFinder : Screen("large_file_finder")
    object Settings : Screen("settings")
    object ScanHistory : Screen("scan_history")
}
