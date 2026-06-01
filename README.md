<div align="center">
    <h1> NTQQBattery </h1>

[![GitHub release](https://img.shields.io/github/release/wkeqin/ntqqbattery.svg)](https://github.com/wkeqin/ntqqbattery/releases/latest)
[![main](https://github.com/wkeqin/ntqqbattery/actions/workflows/build.yml/badge.svg)](https://github.com/wkeqin/ntqqbattery/actions/workflows/build.yml)
[![Telegram](https://img.shields.io/static/v1?label=Telegram&message=Chat&color=0088cc)](https://t.me/QAuxiliaryChat)

</div>

---

`NTQQBattery` 是一个面向 NTQQ 的 Xposed / LSPosed 省电优化模块。

项目基于 Kotlin、YukiHookAPI 和 KSP 构建，主要针对 NTQQ 常见的耗电来源进行拦截和优化，例如频繁 WakeLock、前台保活服务、预加载、行为上报以及重型后台组件等。

建议结合 thanox/墓碑 等使用，来关闭非必要进程减少内存占用
<table>
  <tr>
    <td><img src="docs/image-1.png" alt="预览图1" width="400"></td>
    <td><img src="docs/image.png" alt="预览图2" width="400"></td>
  </tr>
</table>

## 功能特性

当前模块提供的可选功能包括：

- 拦截系统 WakeLock 的申请与释放
- 拦截 NTQQ 电量监控
- 拦截 CoreService 前台保活
- 拦截小程序预加载
- 拦截小游戏闪屏 / 进程预加载
- 拦截 SplashActivity UI 预加载
- 隐藏超级 QQ 秀弹窗
- 拦截主题视频初始化与动画
- 抑制重型引擎资源占用
- 拦截重型后台服务
- 启用后台墓碑 / 深度休眠模式
- 抑制高频网络请求
- 强制启用 PowerSaveMode 行为
- 优化 MSF 内部策略
- 启用激进型 MSF 优化
- 拦截后台振动
- 拦截 Beacon 行为上报
- 拦截 TVK 视频统计上报
- 优化 GIF 渲染行为，防止恶意 GIF

## 注意

- 作者仅在 **NTQQ 9.2.70+** 做过测试，其他版本可能存在兼容性！！
- 某些激进优化可能影响部分功能行为，建议逐项开启并自行验证常用功能是否正常。
- 一部分优化本质上是用后台活动能力、上报能力或保活能力来换取更低功耗。

## 免责声明

本项目仅供学习、研究与个人使用。对商业应用进行 Hook 可能违反目标应用条款，也可能在应用更新后失效或引发异常行为。使用前请自行评估风险。
