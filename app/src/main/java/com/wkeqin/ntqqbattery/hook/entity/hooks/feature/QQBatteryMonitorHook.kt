package com.wkeqin.ntqqbattery.hook.entity.hooks.feature

import com.wkeqin.ntqqbattery.R
import com.wkeqin.ntqqbattery.data.ConfigData
import com.wkeqin.ntqqbattery.hook.entity.FeatureCategory
import com.wkeqin.ntqqbattery.hook.entity.FeatureDefinition
import com.wkeqin.ntqqbattery.hook.entity.FeatureRegistry
import com.wkeqin.ntqqbattery.hook.entity.HookPlan
import com.wkeqin.ntqqbattery.hook.entity.HookStage
import com.wkeqin.ntqqbattery.hook.entity.features.PerfFeatures
import com.wkeqin.ntqqbattery.hook.factory.HookResultTracker
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.log.YLog

/**
 * 阻断 QQ 内置电量监控。
 *
 * 切断配置总开关即可：
 *   BatteryConfig.h()/isEnableMonitor() -> false
 *     └─ QQBatteryMonitor.b()/init 内 if (aVar.h()) 整段初始化被跳过
 */
object QQBatteryMonitorHook : YukiBaseHooker() {

    val feature = FeatureDefinition(
        key = "block_qq_battery_monitor",
        titleRes = R.string.block_qq_battery_monitor,
        summaryRes = R.string.block_qq_battery_monitor,
        noteRes = R.string.block_qq_battery_monitor_note,
        category = FeatureCategory.CORE,
        defaultEnabled = false
    )

    val plan = HookPlan(
        id = "qq-battery-monitor-app-create",
        stage = HookStage.APP_CREATE
    ) {
        loadHooker(QQBatteryMonitorHook)
    }

    override fun onHook() {
        if (ConfigData.isEnabled(FeatureRegistry.blockQQBatteryMonitor).not()) return
        if (ConfigData.isDegraded(FeatureRegistry.blockQQBatteryMonitor)) {
            YLog.info("QQBatteryMonitor: degraded from last run, skipping")
            return
        }

        val tracker = HookResultTracker("QQBatteryMonitor")

        PerfFeatures.BatteryConfigClass?.apply {
            tracker.tryHook("BatteryConfig.h/isEnableMonitor") {
                method {
                    name { it == "h" || it == "isEnableMonitor" || it == "getEnableMonitor" }
                    emptyParam()
                    returnType = Boolean::class.javaPrimitiveType!!
                }.hook().before { result = false }
            }
        }

        val degraded = tracker.report()
        ConfigData.setHooked(FeatureRegistry.blockQQBatteryMonitor, tracker.hasAnySuccess)
        ConfigData.setDegraded(FeatureRegistry.blockQQBatteryMonitor, degraded)
    }
}
