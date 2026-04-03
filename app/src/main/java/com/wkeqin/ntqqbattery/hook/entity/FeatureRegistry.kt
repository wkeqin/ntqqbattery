package com.wkeqin.ntqqbattery.hook.entity

import com.wkeqin.ntqqbattery.R
import com.wkeqin.ntqqbattery.hook.entity.hooks.feature.MiniAppPreloadHook
import com.wkeqin.ntqqbattery.hook.entity.hooks.feature.MiniGamePreloadHook
import com.wkeqin.ntqqbattery.hook.entity.hooks.feature.QQBatteryMonitorHook
import com.wkeqin.ntqqbattery.hook.entity.hooks.feature.SplashPreloadHook
import com.wkeqin.ntqqbattery.hook.entity.hooks.feature.ZPlanRenderHook
import com.wkeqin.ntqqbattery.hook.entity.hooks.optimization.BeaconReportHook
import com.wkeqin.ntqqbattery.hook.entity.hooks.optimization.GPUResourceHook
import com.wkeqin.ntqqbattery.hook.entity.hooks.optimization.MSFNetworkSuppressionHook
import com.wkeqin.ntqqbattery.hook.entity.hooks.optimization.MSFOptimizationHook
import com.wkeqin.ntqqbattery.hook.entity.hooks.optimization.PerfMonitorHook
import com.wkeqin.ntqqbattery.hook.entity.hooks.optimization.PowerSaveModeHook
import com.wkeqin.ntqqbattery.hook.entity.hooks.optimization.TVKReportHook
import com.wkeqin.ntqqbattery.hook.entity.hooks.optimization.ThemeVideoOptimizationHook
import com.wkeqin.ntqqbattery.hook.entity.hooks.service.CoreServiceHook
import com.wkeqin.ntqqbattery.hook.entity.hooks.service.HeavyBackgroundServiceHook
import com.wkeqin.ntqqbattery.hook.entity.hooks.system.BackgroundVibrateHook
import com.wkeqin.ntqqbattery.hook.entity.hooks.system.SystemWakeLockHook
import com.wkeqin.ntqqbattery.hook.entity.hooks.ui.GifOptimizationHook

object FeatureRegistry {

    val blockSystemWakeLock = SystemWakeLockHook.feature
    val blockQQBatteryMonitor = QQBatteryMonitorHook.feature
    val blockCoreService = CoreServiceHook.feature
    val blockBeacon = BeaconReportHook.feature
    val blockTVKReport = TVKReportHook.feature
    val blockPerfMonitor = PerfMonitorHook.feature
    val blockMiniAppPreload = MiniAppPreloadHook.feature
    val blockMiniGamePreload = MiniGamePreloadHook.feature
    val blockSplashPreload = SplashPreloadHook.feature
    val blockZPlanUERender = ZPlanRenderHook.feature
    val blockThemeVideo = ThemeVideoOptimizationHook.feature
    val blockGPUResources = GPUResourceHook.feature
    val blockGifOptimization = GifOptimizationHook.feature

    val blockLockScreenKeepAlive = FeatureDefinition(
        key = "block_lock_screen_keep_alive",
        titleRes = R.string.block_lock_screen_keep_alive,
        category = FeatureCategory.OPTIMIZATION,
        defaultEnabled = true
    )

    val blockHeavyBackgroundService = HeavyBackgroundServiceHook.feature

    val enableTombstoneMode = FeatureDefinition(
        key = "enable_tombstone_mode",
        titleRes = R.string.enable_tombstone_mode,
        summaryRes = R.string.enable_tombstone_mode_summary,
        category = FeatureCategory.OPTIMIZATION,
        defaultEnabled = false
    )

    val forcePowerSaveMode = PowerSaveModeHook.feature
    val suppressNetworkRequest = MSFNetworkSuppressionHook.feature
    val optimizeMsfStrategy = MSFOptimizationHook.optimizeFeature
    val aggressiveMsfOptimization = MSFOptimizationHook.aggressiveFeature
    val blockBackgroundVibrate = BackgroundVibrateHook.feature

    val all = listOf(
        blockSystemWakeLock,
        blockQQBatteryMonitor,
        blockCoreService,
        blockBeacon,
        blockTVKReport,
        blockPerfMonitor,
        blockMiniAppPreload,
        blockMiniGamePreload,
        blockSplashPreload,
        blockZPlanUERender,
        blockThemeVideo,
        blockGPUResources,
        blockGifOptimization,
        blockLockScreenKeepAlive,
        blockHeavyBackgroundService,
        enableTombstoneMode,
        forcePowerSaveMode,
        suppressNetworkRequest,
        optimizeMsfStrategy,
        aggressiveMsfOptimization,
        blockBackgroundVibrate
    )
}
