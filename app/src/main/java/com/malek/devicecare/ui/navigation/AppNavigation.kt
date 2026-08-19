package com.malek.devicecare.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.malek.devicecare.ui.screens.*
import com.malek.devicecare.ui.theme.ThemeViewModel

@Composable
fun AppNavigation(themeViewModel: ThemeViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route
    ) {
        composable(Screen.Dashboard.route) {
            val storageCleanerViewModel: StorageCleanerViewModel = viewModel(factory = StorageCleanerViewModel.Factory)
            val fullScanViewModel: FullScanViewModel = viewModel(factory = FullScanViewModel.Factory)
            
            DashboardScreen(
                onNavigateToInfo = { navController.navigate(Screen.DeviceInfo.route) },
                onNavigateToBattery = { navController.navigate(Screen.Battery.route) },
                onNavigateToStorage = { 
                    storageCleanerViewModel.reset()
                    navController.navigate(Screen.Storage.route) 
                },
                onNavigateToMemory = { navController.navigate(Screen.Memory.route) },
                onNavigateToNetwork = { navController.navigate(Screen.Network.route) },
                onNavigateToSensors = { navController.navigate(Screen.Sensors.route) },
                onNavigateToHardwareTests = { navController.navigate(Screen.HardwareTests.route) },
                onNavigateToAppManager = { navController.navigate(Screen.AppManager.route) },
                onNavigateToSecurity = { navController.navigate(Screen.Security.route) },
                onNavigateToCpuRoom = { navController.navigate(Screen.CpuRoom.route) },
                onNavigateToScan = { 
                    fullScanViewModel.reset()
                    navController.navigate(Screen.FullScan.route) 
                },
                onNavigateToHistory = { navController.navigate(Screen.ScanHistory.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }
        composable(Screen.DeviceInfo.route) {
            DeviceInfoScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Battery.route) {
            BatteryScreen(
                onNavigateToChargingCurrent = { navController.navigate(Screen.ChargingCurrent.route) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.ChargingCurrent.route) {
            ChargingCurrentScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Storage.route) {
            StorageScreen(
                onNavigateToCleaner = { navController.navigate(Screen.StorageCleaner.route) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.StorageCleaner.route) {
            StorageCleanerScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.WifiTest.route) {
            WifiTestScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Memory.route) {
            MemoryScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Network.route) {
            val wifiTestViewModel: WifiTestViewModel = viewModel(factory = WifiTestViewModel.Factory)
            NetworkScreen(
                onNavigateToWifiTest = {
                    wifiTestViewModel.reset()
                    navController.navigate(Screen.WifiTest.route)
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Sensors.route) {
            SensorsScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.AppManager.route) {
            AppManagerScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.HardwareTests.route) {
            HardwareTestsScreen(
                onNavigateToDisplayTest = { navController.navigate(Screen.DisplayTest.route) },
                onNavigateToMultiTouchTest = { navController.navigate(Screen.MultiTouchTest.route) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.DisplayTest.route) {
            DisplayTestScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.MultiTouchTest.route) {
            MultiTouchTestScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.CpuRoom.route) {
            CpuRoomScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Security.route) {
            SecurityScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.LargeFileFinder.route) {
            LargeFileScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                themeViewModel = themeViewModel,
                onNavigateToInfo = { navController.navigate(Screen.DeviceInfo.route) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.ScanHistory.route) {
            ScanHistoryScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.FullScan.route) {
            FullScanScreen(
                onNavigateToBattery = { navController.navigate(Screen.Battery.route) },
                onNavigateToStorage = { navController.navigate(Screen.Storage.route) },
                onNavigateToMemory = { navController.navigate(Screen.Memory.route) },
                onNavigateToNetwork = { navController.navigate(Screen.Network.route) },
                onNavigateToSecurity = { navController.navigate(Screen.Security.route) },
                onNavigateToLargeFiles = { navController.navigate(Screen.StorageCleaner.route) },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
