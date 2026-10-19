# BypassPower

BypassPower 是一个针对 OnePlus（和兼容机型）的一键 Root 模块/应用，目标是在已 Root 的设备上添加“旁路充电”（Bypass Charging）功能，允许在特定场景下改变充电路径或充电策略以实现特殊需求。该项目包含 Android/Gradle 构建脚手架与模块化代码（app 模块应位于仓库中）。

> 警告：修改充电线路或充电策略存在风险，可能导致设备发热、损坏或电池寿命下降；请仅在了解风险并备份数据后在可接受的环境中测试。作者和维护者对因使用本软件导致的任何损失不承担责任。

## 功能（示例）
- 在支持设备上启用旁路充电逻辑（需要 Root 权限）。
- 提供可配置的触发条件（屏幕状态、充电器类型、电池电量阈值等）。
- 提供日志输出与调试开关，便于定位问题。

> 注：具体功能以代码实现为准；若仓库中尚未包含 `app/` 模块或配置文件，需先补齐模块代码才能编译运行完整功能。

## 支持与要求
- 需要已 Root 的 OnePlus 设备（或内核/ROM 与 OnePlus 兼容的设备）。
- Android 系统版本：视实现而定，请以代码或 app 模块中的 manifest 为准。
- 需授予 su 权限以执行低层系统操作。
- 推荐在测试设备或已备份数据的环境中运行。

## 风险与安全
- 修改充电路径或相关内核参数非常危险，可能引起过热或电池损坏。
- 请勿在主要/生产设备上直接运行未经测试的版本。先在测试机上验证。
- 推荐启用日志并观察设备温度与电池状态，出现异常立即停止并恢复出厂设置或断电处理。

## 安装（示例流程）
下面给出通用的安装思路，具体步骤取决于你项目中如何打包为模块（Magisk 模块、系统 app、Xposed 模块等）：

1. 构建 APK（在仓库根目录运行）：

```bash
git clone https://github.com/hanhui12131/BypassPower.git
cd BypassPower
./gradlew assembleDebug
```

2. 若项目以 Magisk 模块形式发布：
- 将生成的模块 ZIP 放到手机，在 Magisk Manager 中选择安装，或把模块目录放到 `/data/adb/modules/<module_name>` 并重启。

3. 若为普通需要 su 的 APK：
- 将 APK 安装到设备（adb install）并在应用首次运行时授予 su 权限，或使用���本在 adb shell 下以 root 权限运行模块脚本。

注意：仓库当前仅包含根级 Gradle 配置与 wrapper；请确保 `app/` 模块（包含 AndroidManifest、实现代码、module-level build.gradle 等）已提交到仓库，以便生成 APK 或模块 ZIP。

## 如何运行与调试
- 使用 Android Studio 打开项目（选择 settings.gradle 所在目录）。
- 在运行前确保设备已连接并授予开发者选项与 USB 调试权限。
- 在设备上授予 Root 权限（Magisk 或其他 su 管理器）。
- 查看日志：adb logcat | grep BypassPower 或项目中指定的 tag。

## 构建命令
```bash
./gradlew assembleDebug
./gradlew assembleRelease
```

## 仓库组织（顶层）
```
BypassPower/
  .gitignore
  README.md
  build.gradle
  gradle.properties
  gradlew
  gradlew.bat
  local.properties
  settings.gradle   # 包含 ':app' 模块（请确保 app/ 已提交）
  app/              # (应包含) Android 模块：源代码、AndroidManifest、资源
```

## 贡献
欢迎提交 issue 或 PR。提出变更时请包含：
- 变更说明与动机
- 测试设备与 Android 版本
- 重现步骤与日志片段

PR 流程：Fork -> 新分支 -> 提交 -> 发起 PR。

## 联系
在仓库中打开 issue 或在 PR 中 @ 提及维护者。

---
*此 README 已根据你提供的信息更新为“一加手机的 Root 模块应用，用于添加旁路充电功能”。如需我把安装步骤改为针对具体模块类型（例如 Magisk 模块模板、系统 app 替换脚本或具体 OnePlus 型号兼容列表），请提供更多实现细节或回复“按 Magisk 模块说明更新 README”。*