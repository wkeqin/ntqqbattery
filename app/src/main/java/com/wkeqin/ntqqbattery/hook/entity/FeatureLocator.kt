package com.wkeqin.ntqqbattery.hook.entity

import android.content.Context
import android.os.Build
import com.wkeqin.ntqqbattery.data.ConfigData
import com.highcapable.yukihookapi.hook.factory.searchClass
import com.highcapable.yukihookapi.hook.log.YLog

/**
 * 在 Hook 早期统一定位高风险目标，并按 QQ 版本缓存结果。
 */
object FeatureLocator {

    // ── Cache keys ──
    private const val KEY_MINI_APP_LAUNCHER = "feature_cache_mini_app_launcher"
    private const val KEY_MSF_CONFIG = "feature_cache_msf_config"
    private const val KEY_BATTERY_CONFIG = "feature_cache_battery_config"
    private const val KEY_GL_THREAD_MANAGER = "feature_cache_gl_thread_manager"
    private const val KEY_PANDORA_EVENT_REPORT_HELPER = "feature_cache_pandora_event_report_helper"
    private const val KEY_MONITOR_REPORTER = "feature_cache_monitor_reporter"
    private const val KEY_BEACON_REPORT = "feature_cache_beacon_report"
    private const val KEY_BEACON_TASK_MANAGER = "feature_cache_beacon_task_manager"
    private const val KEY_BEACON_TASK_WRAPPER = "feature_cache_beacon_task_wrapper"
    private const val KEY_QQ_BEACON_REPORT = "feature_cache_qq_beacon_report"
    private const val KEY_NT_BEACON_REPORT = "feature_cache_nt_beacon_report"
    private const val KEY_TVK_BEACON_REPORT = "feature_cache_tvk_beacon_report"
    private const val KEY_BASE_VIDEO_CONTROLLER = "feature_cache_base_video_controller"
    private const val KEY_LIBRA_GIF_EXECUTOR = "feature_cache_libra_gif_executor"
    private const val KEY_THEME_VIDEO_CONTROLLER = "feature_cache_theme_video_controller"
    private const val KEY_SUPER_THEME_VIDEO_CONTROLLER = "feature_cache_super_theme_video_controller"
    private const val KEY_MSF_PULL_CONFIG_UTIL = "feature_cache_msf_pull_config_util"
    private const val KEY_MSF_THREAD_MANAGER = "feature_cache_msf_thread_manager"
    private const val KEY_MSF_WAKEUP_LOCK_MANAGER = "feature_cache_msf_wakeup_lock_manager"

    private val ALL_KEYS = arrayOf(
        KEY_MINI_APP_LAUNCHER, KEY_MSF_CONFIG,
        KEY_BATTERY_CONFIG, KEY_GL_THREAD_MANAGER,
        KEY_PANDORA_EVENT_REPORT_HELPER, KEY_MONITOR_REPORTER,
        KEY_BEACON_REPORT, KEY_BEACON_TASK_MANAGER, KEY_BEACON_TASK_WRAPPER,
        KEY_QQ_BEACON_REPORT, KEY_NT_BEACON_REPORT, KEY_TVK_BEACON_REPORT,
        KEY_BASE_VIDEO_CONTROLLER, KEY_LIBRA_GIF_EXECUTOR,
        KEY_THEME_VIDEO_CONTROLLER, KEY_SUPER_THEME_VIDEO_CONTROLLER,
        KEY_MSF_PULL_CONFIG_UTIL, KEY_MSF_THREAD_MANAGER, KEY_MSF_WAKEUP_LOCK_MANAGER
    )

    // ── Version-aware cache management ──

    private fun buildVersionKey(context: Context): String {
        val pkgInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getPackageInfo(context.packageName, android.content.pm.PackageManager.PackageInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(context.packageName, 0)
        }
        val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) pkgInfo.longVersionCode else @Suppress("DEPRECATION") pkgInfo.versionCode.toLong()
        return "${pkgInfo.versionName}($versionCode)"
    }

    private fun cacheKey(name: String) = "locator_$name"

    /** 哨兵值：标记此类在当前版本不存在，避免每次重启重试 */
    private const val NOT_FOUND = "__NOT_FOUND__"

    private fun readCachedClass(name: String): String? {
        val value = ConfigData.getString(cacheKey(name))
        return value.ifBlank { null }
    }

    private fun writeCachedClass(name: String, className: String?) {
        ConfigData.putString(cacheKey(name), className ?: NOT_FOUND)
    }

    private fun ensureVersionCache(context: Context) {
        val versionKey = buildVersionKey(context)
        val cachedVersion = ConfigData.getString(ConfigData.FEATURE_CACHE_VERSION)
        if (cachedVersion != versionKey) {
            ConfigData.putString(ConfigData.FEATURE_CACHE_VERSION, versionKey)
            // 清除 NOT_FOUND 哨兵 — 新版本可能修复了之前找不到的类
            var cleared = 0
            for (key in ALL_KEYS) {
                if (readCachedClass(key) == NOT_FOUND) {
                    ConfigData.putString(cacheKey(key), "")
                    cleared++
                }
            }
            // 清除所有 degraded 标记 — 新版本可能修复了方法
            for (feature in FeatureRegistry.all) {
                ConfigData.setDegraded(feature, false)
            }
            YLog.info("FeatureLocator: QQ version changed $cachedVersion -> $versionKey, cleared $cleared NOT_FOUND entries + all degraded flags")
        }
    }

    // ── Generic locate: cache read → verify → resolve fallback → cache write ──

    /**
     * 通用定位方法。读缓存 → [verify] 校验 → 调 [resolve] 回退 → 写缓存。
     * warmup 和 getter 都通过此方法统一处理，消除模板代码。
     */
    /** 纯 Java 反射加载类，避免 YukiHookAPI toClassOrNull 的 DEX 扫描开销 */
    private fun String.loadClass(loader: ClassLoader): Class<*>? {
        return runCatching { Class.forName(this, false, loader) }.getOrNull()
    }

    private fun locate(
        key: String,
        loader: ClassLoader,
        label: String,
        verify: ((Class<*>) -> Boolean)? = null,
        resolve: (ClassLoader) -> Class<*>?
    ): Class<*>? {
        val cached = readCachedClass(key)
        // 哨兵值：上次搜索已确认不存在，跳过重试
        if (cached == NOT_FOUND) return null
        // 有缓存类名 → 尝试加载 + verify
        cached?.loadClass(loader)
            ?.takeIf { verify?.invoke(it) ?: true }
            ?.let { return it }
        // 无缓存 / verify 失败 → 重新搜索
        val result = resolve(loader)
        if (result != null) {
            writeCachedClass(key, result.name)
            YLog.info("FeatureLocator cached $label -> ${result.name}")
        } else {
            writeCachedClass(key, null) // 写入 NOT_FOUND 哨兵
            YLog.info("FeatureLocator: $label not found, marked NOT_FOUND")
        }
        return result
    }

    private fun Class<*>.hasMiniAppPreloadMethod() = declaredMethods.any {
        it.name == "preloadMiniApp" &&
            it.parameterCount == 1 &&
            it.parameterTypes.firstOrNull()?.name?.contains("IECMiniAppLauncher\$MiniAppType") == true
    }

    /**
     * BatteryConfig（混淆后多为 perf.battery.a）：
     * - h()/isEnableMonitor()：总开关，init 前读
     * - 构造参数为 JSONObject
     */
    private fun Class<*>.isBatteryConfigClass(): Boolean {
        val hasEnableMonitor = declaredMethods.any {
            (it.name == "h" || it.name == "isEnableMonitor" || it.name == "getEnableMonitor") &&
                it.parameterCount == 0 &&
                (it.returnType == Boolean::class.javaPrimitiveType || it.returnType == Boolean::class.java)
        }
        val hasJsonCtor = declaredConstructors.any {
            it.parameterCount == 1 && it.parameterTypes.firstOrNull()?.name == "org.json.JSONObject"
        }
        return hasEnableMonitor && hasJsonCtor
    }

    // ── Resolve methods (contain actual search logic) ──

    private fun resolveMiniAppLauncher(loader: ClassLoader): Class<*>? {
        return sequenceOf("du.a", "qu.a")
            .mapNotNull { it.loadClass(loader) }
            .firstOrNull { it.hasMiniAppPreloadMethod() }
            ?: loader.searchClass(name = "NTQQ_Features_MiniAppLauncher") {
                implements("com.tencent.ecommerce.base.miniapp.api.IECMiniAppLauncher")
            }.all().firstOrNull { it.hasMiniAppPreloadMethod() }
    }

    private fun resolveMSFConfig(loader: ClassLoader): Class<*>? {
        return NTQQFeatures.findClassSafe(
            cacheName = "MSFConfig",
            primaryName = "com.tencent.mobileqq.msf.core.w.d",
            packageName = "com.tencent.mobileqq.msf.core.w.",
            constant = "ConfigID{"
        )
    }

    private fun resolveBatteryConfig(loader: ClassLoader): Class<*>? {
        // BatteryConfig：enableMonitor 总开关所在配置类
        return "com.tencent.mobileqq.perf.battery.a".loadClass(loader)
            ?.takeIf { it.isBatteryConfigClass() }
            ?: loader.searchClass(name = "NTQQ_Features_BatteryConfig") {
                fullName { it.startsWith("com.tencent.mobileqq.perf.battery") }
                method {
                    name { it == "h" || it == "isEnableMonitor" || it == "getEnableMonitor" }
                    emptyParam()
                    returnType = Boolean::class.javaPrimitiveType!!
                }
                constructor { param("org.json.JSONObject") }
            }.all().firstOrNull { it.isBatteryConfigClass() }
    }

    private fun resolveGLThreadManager(loader: ClassLoader): Class<*>? {
        return NTQQFeatures.findClassSafe(
            cacheName = "GLThreadManager",
            primaryName = "com.tencent.mobileqq.apollo.view.opengl.GLThreadManager",
            packageName = "com.tencent.mobileqq.apollo.view.opengl",
            constant = "[ApolloGL][GLThreadManager]"
        )
    }

    private fun resolvePandoraEventReportHelper(loader: ClassLoader): Class<*>? {
        return "com.tencent.mobileqq.qmethodmonitor.pandoraevent.PandoraEventReportHelper".loadClass(loader)
            ?: loader.searchClass(name = "NTQQ_Features_PandoraEventReportHelper") {
                fullName { it.startsWith("com.tencent.mobileqq.qmethodmonitor.pandoraevent.") }
                field {
                    modifiers { isStatic }
                    type = "java.util.concurrent.atomic.AtomicBoolean"
                }
            }.get()
    }

    private fun resolveMonitorReporter(loader: ClassLoader): Class<*>? {
        return NTQQFeatures.findClassSafe(
            cacheName = "MonitorReporter",
            primaryName = "com.tencent.qmethod.pandoraex.core.MonitorReporter",
            packageName = "com.tencent.qmethod.pandoraex.core",
            constant = "MonitorReporter"
        )
    }

    private fun resolveBeaconReport(loader: ClassLoader): Class<*>? {
        return NTQQFeatures.findClassSafe(
            cacheName = "BeaconReport",
            primaryName = "com.tencent.beacon.event.open.BeaconReport",
            packageName = "com.tencent.beacon.event.open",
            constant = "BeaconReport"
        )
    }

    private fun resolveBeaconTaskManager(loader: ClassLoader): Class<*>? {
        return "com.tencent.beacon.a.b.k".loadClass(loader)
            ?: loader.searchClass(name = "NTQQ_Features_BeaconTaskManager") {
                fullName { it.startsWith("com.tencent.beacon.") }
                method { name = "a"; param(Runnable::class.java); returnType = Void.TYPE }
                method { name = "a"; param(Int::class.javaPrimitiveType!!, Long::class.javaPrimitiveType!!, Long::class.javaPrimitiveType!!, Runnable::class.java); returnType = Void.TYPE }
                method { name = "a"; param(Long::class.javaPrimitiveType!!, Runnable::class.java); returnType = Void.TYPE }
            }.get()
    }

    private fun resolveBeaconTaskWrapper(loader: ClassLoader): Class<*>? {
        return "com.tencent.beacon.a.b.j".loadClass(loader)
            ?: loader.searchClass(name = "NTQQ_Features_BeaconTaskWrapper") {
                fullName { it.startsWith("com.tencent.beacon.") }
                method { name = "run"; returnType = Void.TYPE; emptyParam() }
                implements("java.lang.Runnable")
            }.get()
    }

    private fun resolveQQBeaconReport(loader: ClassLoader): Class<*>? {
        return NTQQFeatures.findClassSafe(
            cacheName = "QQBeaconReport",
            primaryName = "com.tencent.mobileqq.statistics.QQBeaconReport",
            packageName = "com.tencent.mobileqq.statistics",
            constant = "QQBeaconReport"
        )
    }

    private fun resolveNTBeaconReport(loader: ClassLoader): Class<*>? {
        return NTQQFeatures.findClassSafe(
            cacheName = "NTBeaconReport",
            primaryName = "com.tencent.qqnt.beacon.NTBeaconReport",
            packageName = "com.tencent.qqnt.beacon",
            constant = "NTBeaconReport"
        )
    }

    private fun resolveTVKBeaconReport(loader: ClassLoader): Class<*>? {
        return NTQQFeatures.findClassSafe(
            cacheName = "TVKBeaconReport",
            primaryName = "com.tencent.qqlive.tvkplayer.report.api.TVKBeaconReport",
            packageName = "com.tencent.qqlive.tvkplayer.report.api",
            constant = "TVKBeaconReport"
        )
    }

    private fun resolveBaseVideoController(loader: ClassLoader): Class<*>? {
        return "com.tencent.mobileqq.vas.theme.video.a".loadClass(loader)
            ?: loader.searchClass(name = "NTQQ_Features_BaseVideoController") {
                fullName { it.startsWith("com.tencent.mobileqq.vas.theme.video.") }
                implements("com.tencent.mobileqq.vas.theme.api.IThemeVideoController", "mqq.app.QActivityLifecycleCallbacks")
            }.get()
    }

    private fun resolveLibraGifExecutor(loader: ClassLoader): Class<*>? {
        return "com.tencent.libra.extension.gif.c".loadClass(loader)
            ?: loader.searchClass(name = "NTQQ_Features_LibraGifExecutor") {
                extends("com.tencent.thread.monitor.plugin.proxy.BaseScheduledThreadPoolExecutor")
                fullName { it.startsWith("com.tencent.libra.extension.gif.") }
            }.get()
    }

    private fun resolveThemeVideoController(loader: ClassLoader): Class<*>? {
        return NTQQFeatures.findClassSafe(
            cacheName = "ThemeVideoController",
            primaryName = "com.tencent.mobileqq.vas.theme.ThemeVideoController",
            packageName = "com.tencent.mobileqq.vas.theme",
            constant = "ThemeVideoController"
        )
    }

    private fun resolveSuperThemeVideoController(loader: ClassLoader): Class<*>? {
        return NTQQFeatures.findClassSafe(
            cacheName = "SuperThemeVideoController",
            primaryName = "com.tencent.mobileqq.vas.theme.video.SuperThemeVideoController",
            packageName = "com.tencent.mobileqq.vas.theme.video",
            constant = "SuperThemeVideoController"
        )
    }

    private fun resolveMsfPullConfigUtil(loader: ClassLoader): Class<*>? {
        return NTQQFeatures.findClassSafe(
            cacheName = "MsfPullConfigUtil",
            primaryName = "com.tencent.mobileqq.msf.core.net.utils.MsfPullConfigUtil",
            packageName = "com.tencent.mobileqq.msf.core.",
            constant = "MsfPullConfigUtil"
        )
    }

    private fun resolveMsfThreadManager(loader: ClassLoader): Class<*>? {
        return NTQQFeatures.findClassSafe(
            cacheName = "MsfThreadManager",
            primaryName = "com.tencent.mobileqq.msf.core.p",
            packageName = "com.tencent.mobileqq.msf.core.",
            constant = "MsfThreadManager"
        )
    }

    private fun resolveMSFWakeUpLockManager(loader: ClassLoader): Class<*>? {
        val wrapperClass = resolveMSFWakeLockWrapper(loader) ?: return null
        // Try JADX-obfuscated name first
        return runCatching { loader.loadClass("com.tencent.mobileqq.msf.core.f0.c") }.getOrNull()
            // Fallback: scan f0 package for the orchestrator (has a field of type f0.a)
            ?: runCatching {
                for (suffix in listOf("c", "b", "d", "e", "f", "g")) {
                    val candidate = runCatching {
                        loader.loadClass("com.tencent.mobileqq.msf.core.f0.$suffix")
                    }.getOrNull() ?: continue
                    if (candidate.declaredFields.any { it.type == wrapperClass }) {
                        return@runCatching candidate
                    }
                }
                null
            }.getOrNull()
    }

    private fun resolveMSFWakeLockWrapper(loader: ClassLoader): Class<*>? {
        return runCatching {
            loader.loadClass("com.tencent.mobileqq.msf.core.f0.a")
        }.getOrNull()
    }

    // ── Warmup ──

    fun warmup(context: Context, loader: ClassLoader) {
        val start = System.currentTimeMillis()
        ensureVersionCache(context)

        ConfigData.putStringBatch {
            locate(KEY_MINI_APP_LAUNCHER, loader, "MiniAppLauncher", verify = { it.hasMiniAppPreloadMethod() }) { resolveMiniAppLauncher(it) }
            locate(KEY_MSF_CONFIG, loader, "MSFConfig") { resolveMSFConfig(it) }
            // BatteryConfig 不放 warmup：按需 lazy 定位即可
            locate(KEY_GL_THREAD_MANAGER, loader, "GLThreadManager") { resolveGLThreadManager(it) }
            locate(KEY_PANDORA_EVENT_REPORT_HELPER, loader, "PandoraEventReportHelper") { resolvePandoraEventReportHelper(it) }
            locate(KEY_MONITOR_REPORTER, loader, "MonitorReporter") { resolveMonitorReporter(it) }
            locate(KEY_BEACON_REPORT, loader, "BeaconReport") { resolveBeaconReport(it) }
            locate(KEY_BEACON_TASK_MANAGER, loader, "BeaconTaskManager") { resolveBeaconTaskManager(it) }
            locate(KEY_BEACON_TASK_WRAPPER, loader, "BeaconTaskWrapper") { resolveBeaconTaskWrapper(it) }
            locate(KEY_QQ_BEACON_REPORT, loader, "QQBeaconReport") { resolveQQBeaconReport(it) }
            locate(KEY_NT_BEACON_REPORT, loader, "NTBeaconReport") { resolveNTBeaconReport(it) }
            locate(KEY_TVK_BEACON_REPORT, loader, "TVKBeaconReport") { resolveTVKBeaconReport(it) }
            locate(KEY_BASE_VIDEO_CONTROLLER, loader, "BaseVideoController") { resolveBaseVideoController(it) }
            locate(KEY_LIBRA_GIF_EXECUTOR, loader, "LibraGifExecutor") { resolveLibraGifExecutor(it) }
            locate(KEY_THEME_VIDEO_CONTROLLER, loader, "ThemeVideoController") { resolveThemeVideoController(it) }
            locate(KEY_SUPER_THEME_VIDEO_CONTROLLER, loader, "SuperThemeVideoController") { resolveSuperThemeVideoController(it) }
            locate(KEY_MSF_PULL_CONFIG_UTIL, loader, "MsfPullConfigUtil") { resolveMsfPullConfigUtil(it) }
            locate(KEY_MSF_THREAD_MANAGER, loader, "MsfThreadManager") { resolveMsfThreadManager(it) }
            locate(KEY_MSF_WAKEUP_LOCK_MANAGER, loader, "MSFWakeUpLockManager") { resolveMSFWakeUpLockManager(it) }
        }

        val elapsed = System.currentTimeMillis() - start
        val cacheHits = ALL_KEYS.count { readCachedClass(it) != null }
        YLog.info("FeatureLocator: warmup done in ${elapsed}ms, $cacheHits/${ALL_KEYS.size} cache hits")
    }

    // ── Public getters ──

    fun getMiniAppLauncher(loader: ClassLoader) =
        locate(KEY_MINI_APP_LAUNCHER, loader, "MiniAppLauncher", verify = { it.hasMiniAppPreloadMethod() }) { resolveMiniAppLauncher(it) }

    fun getMSFConfig(loader: ClassLoader) =
        locate(KEY_MSF_CONFIG, loader, "MSFConfig") { resolveMSFConfig(it) }

    fun getCachedBatteryConfig(loader: ClassLoader) =
        locate(
            KEY_BATTERY_CONFIG,
            loader,
            "BatteryConfig",
            verify = { it.isBatteryConfigClass() }
        ) { resolveBatteryConfig(it) }

    fun getCachedGLThreadManager(loader: ClassLoader) =
        locate(KEY_GL_THREAD_MANAGER, loader, "GLThreadManager") { resolveGLThreadManager(it) }

    fun getCachedPandoraEventReportHelper(loader: ClassLoader) =
        locate(KEY_PANDORA_EVENT_REPORT_HELPER, loader, "PandoraEventReportHelper") { resolvePandoraEventReportHelper(it) }

    fun getCachedMonitorReporter(loader: ClassLoader) =
        locate(KEY_MONITOR_REPORTER, loader, "MonitorReporter") { resolveMonitorReporter(it) }

    fun getCachedBeaconReport(loader: ClassLoader) =
        locate(KEY_BEACON_REPORT, loader, "BeaconReport") { resolveBeaconReport(it) }

    fun getCachedBeaconTaskManager(loader: ClassLoader) =
        locate(KEY_BEACON_TASK_MANAGER, loader, "BeaconTaskManager") { resolveBeaconTaskManager(it) }

    fun getCachedBeaconTaskWrapper(loader: ClassLoader) =
        locate(KEY_BEACON_TASK_WRAPPER, loader, "BeaconTaskWrapper") { resolveBeaconTaskWrapper(it) }

    fun getCachedQQBeaconReport(loader: ClassLoader) =
        locate(KEY_QQ_BEACON_REPORT, loader, "QQBeaconReport") { resolveQQBeaconReport(it) }

    fun getCachedNTBeaconReport(loader: ClassLoader) =
        locate(KEY_NT_BEACON_REPORT, loader, "NTBeaconReport") { resolveNTBeaconReport(it) }

    fun getCachedTVKBeaconReport(loader: ClassLoader) =
        locate(KEY_TVK_BEACON_REPORT, loader, "TVKBeaconReport") { resolveTVKBeaconReport(it) }

    fun getCachedBaseVideoController(loader: ClassLoader) =
        locate(KEY_BASE_VIDEO_CONTROLLER, loader, "BaseVideoController") { resolveBaseVideoController(it) }

    fun getCachedLibraGifExecutor(loader: ClassLoader) =
        locate(KEY_LIBRA_GIF_EXECUTOR, loader, "LibraGifExecutor") { resolveLibraGifExecutor(it) }

    fun getCachedThemeVideoController(loader: ClassLoader) =
        locate(KEY_THEME_VIDEO_CONTROLLER, loader, "ThemeVideoController") { resolveThemeVideoController(it) }

    fun getCachedSuperThemeVideoController(loader: ClassLoader) =
        locate(KEY_SUPER_THEME_VIDEO_CONTROLLER, loader, "SuperThemeVideoController") { resolveSuperThemeVideoController(it) }

    fun getCachedMsfPullConfigUtil(loader: ClassLoader) =
        locate(KEY_MSF_PULL_CONFIG_UTIL, loader, "MsfPullConfigUtil") { resolveMsfPullConfigUtil(it) }

    fun getCachedMsfThreadManager(loader: ClassLoader) =
        locate(KEY_MSF_THREAD_MANAGER, loader, "MsfThreadManager") { resolveMsfThreadManager(it) }

    fun getCachedMSFWakeUpLockManager(loader: ClassLoader) =
        locate(KEY_MSF_WAKEUP_LOCK_MANAGER, loader, "MSFWakeUpLockManager") { resolveMSFWakeUpLockManager(it) }
}
