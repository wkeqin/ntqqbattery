package com.wkeqin.ntqqbattery.hook.entity.features

import com.wkeqin.ntqqbattery.hook.entity.FeatureLocator
import com.wkeqin.ntqqbattery.hook.entity.NTQQFeatures
import com.highcapable.yukihookapi.hook.factory.*
import com.highcapable.yukihookapi.hook.log.YLog

/**
 * 性能与监控相关特征定位
 */
object PerfFeatures {

    /**
     * 电池监控配置（enableMonitor 总开关）
     */
    val BatteryConfigClass by lazy {
        val loader = NTQQFeatures.classLoader ?: return@lazy null
        FeatureLocator.getCachedBatteryConfig(loader)?.also {
            YLog.info("Locate BatteryConfigClass -> ${it.name}")
        }
    }

    /**
     * Apollo GL 线程管理器
     */
    val GLThreadManagerClass by lazy {
        val loader = NTQQFeatures.classLoader ?: return@lazy null
        FeatureLocator.getCachedGLThreadManager(loader)?.also {
            YLog.info("Locate GLThreadManagerClass -> ${it.name}")
        }
    }

    /**
     * PandoraEx 隐私事件报告助手
     */
    val PandoraEventReportHelperClass by lazy {
        val loader = NTQQFeatures.classLoader ?: return@lazy null
        FeatureLocator.getCachedPandoraEventReportHelper(loader)?.also {
            YLog.info("Locate PandoraEventReportHelperClass -> ${it.name}")
        }
    }

    /**
     * Monitor报告器
     */
    val MonitorReporterClass by lazy {
        val loader = NTQQFeatures.classLoader ?: return@lazy null
        FeatureLocator.getCachedMonitorReporter(loader)?.also {
            YLog.info("Locate MonitorReporterClass -> ${it.name}")
        }
    }

    /**
     * XWeb 下载器
     */
    val XWebDownloaderClass by lazy {
        val loader = NTQQFeatures.classLoader ?: return@lazy null
        "com.tencent.xweb.XWebDownloader".toClassOrNull(loader)?.also {
            YLog.info("Locate XWebDownloaderClass -> ${it.name}")
        }
    }

    /**
     * XWeb HTTP 下载任务类
     */
    val XWebHttpDownloadTaskClass by lazy {
        val loader = NTQQFeatures.classLoader ?: return@lazy null
        "com.tencent.xweb.XWebDownloader${'$'}HttpDownloadTask".toClassOrNull(loader)?.also {
            YLog.info("Locate XWebHttpDownloadTaskClass -> ${it.name}")
        }
    }

    /**
     * 腾讯 Beacon 核心
     */
    val BeaconReportClass by lazy {
        val loader = NTQQFeatures.classLoader ?: return@lazy null
        FeatureLocator.getCachedBeaconReport(loader)
    }

    /**
     * 腾讯 Beacon 任务调度管理器（用于彻底阻断所有遥测任务）
     */
    val BeaconTaskManagerClass by lazy {
        val loader = NTQQFeatures.classLoader ?: return@lazy null
        FeatureLocator.getCachedBeaconTaskManager(loader)?.also {
            YLog.info("Locate BeaconTaskManagerClass -> ${it.name}")
        }
    }

    /**
     * 腾讯 Beacon 内部任务包装器（用于阻断已存在的定频任务抛出的死循环）
     */
    val BeaconTaskWrapperClass by lazy {
        val loader = NTQQFeatures.classLoader ?: return@lazy null
        FeatureLocator.getCachedBeaconTaskWrapper(loader)
    }

    /**
     * QQ Beacon 包装器
     */
    val QQBeaconReportClass by lazy {
        val loader = NTQQFeatures.classLoader ?: return@lazy null
        FeatureLocator.getCachedQQBeaconReport(loader)
    }

    /**
     * NT 架构专属 Beacon 包装器
     */
    val NTBeaconReportClass by lazy {
        val loader = NTQQFeatures.classLoader ?: return@lazy null
        FeatureLocator.getCachedNTBeaconReport(loader)
    }

    /**
     * 视频库 TVK 上报核心
     */
    val TVKBeaconReportClass by lazy {
        val loader = NTQQFeatures.classLoader ?: return@lazy null
        FeatureLocator.getCachedTVKBeaconReport(loader)
    }
}
