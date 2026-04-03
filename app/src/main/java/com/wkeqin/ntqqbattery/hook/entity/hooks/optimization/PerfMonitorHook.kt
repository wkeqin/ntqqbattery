package com.wkeqin.ntqqbattery.hook.entity.hooks.optimization

import com.wkeqin.ntqqbattery.data.ConfigData
import com.wkeqin.ntqqbattery.R
import com.wkeqin.ntqqbattery.hook.entity.FeatureCategory
import com.wkeqin.ntqqbattery.hook.entity.FeatureDefinition
import com.wkeqin.ntqqbattery.hook.entity.FeatureRegistry
import com.wkeqin.ntqqbattery.hook.entity.HookPlan
import com.wkeqin.ntqqbattery.hook.entity.HookStage
import com.wkeqin.ntqqbattery.hook.entity.NTQQHooker
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.*
import com.highcapable.yukihookapi.hook.log.YLog

object PerfMonitorHook : YukiBaseHooker() {

    val feature = FeatureDefinition(
        key = "block_perf_monitor",
        titleRes = R.string.block_perf_monitor,
        summaryRes = R.string.block_perf_monitor_summary,
        category = FeatureCategory.CORE,
        defaultEnabled = true
    )

    private val hardBlockThreadNames = setOf(
        "Perf_Monitor_Th",
        "Profile"
    )

    val plan = HookPlan(
        id = "perf-monitor-app-create",
        stage = HookStage.APP_CREATE
    ) {
        loadHooker(PerfMonitorHook)
    }

    override fun onHook() {
        if (ConfigData.isEnabled(FeatureRegistry.blockPerfMonitor).not()) return

        hookStatisticCollector()
        hookNamedThreadStart()
    }

    private fun hookStatisticCollector() {
        "com.tencent.mobileqq.statistics.StatisticCollector".toClassOrNull()?.apply {
            runCatching {
                method {
                    name {
                        it == "collectPerformance" ||
                            it.startsWith("reportActionCount") ||
                            it == "reportClickEvent" ||
                            it == "reportKVEvent"
                    }
                }.hookAll().before {
                    YLog.debug("PerfMonitor: blocked StatisticCollector.${method.name}()")
                    result = NTQQHooker.safeReturn(method)
                }
            }.onFailure {
                YLog.warn("PerfMonitor: failed to hook StatisticCollector, ${it.message}")
            }
        }
    }

    private fun hookNamedThreadStart() {
        runCatching {
            Thread::class.java.method {
                name = "start"
                emptyParam()
            }.hook().before {
                val thread = instance as? Thread ?: return@before
                if (thread.name !in hardBlockThreadNames) return@before
                YLog.info("PerfMonitor: blocked thread start -> ${thread.name}")
                result = null
            }
        }.onFailure {
            YLog.warn("PerfMonitor: failed to hook Thread.start(), ${it.message}")
        }
    }
}
