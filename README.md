# BypassPower

BypassPower 是一个 Android 应用项目（Gradle / Kotlin 架构），使用 Android Gradle 插件进行构建。该仓库包含项目构建脚手架（Gradle wrapper、根 build files 等），可直接在 Android Studio 中打开进行开发与调试。

## 技术栈
- 语言：Kotlin（项目使用 Kotlin Android 插件）
- 构建：Gradle（包含 Gradle Wrapper）
- Android Gradle Plugin: 7.2.2（在根 build.gradle 中声明）

## 仓库组织（顶层）
```
BypassPower/              # 仓库根目录
  .gitignore              # 忽略规则
  README.md               # 项目自述（本文件）
  build.gradle            # 根级 Gradle 配置（插件版本等）
  gradle.properties       # Gradle 属性
  gradlew                 # Gradle wrapper (UNIX)
  gradlew.bat             # Gradle wrapper (Windows)
  local.properties        # 本地 SDK 路径（不应提交到远程）
  settings.gradle         # 包含的子模块（rootProject.name, include ':app'）
```

注意：settings.gradle 中包含了 `:app` 模块（rootProject.name = "BypassPower" 且 include ':app'），但当前仓库根目录下未能读取到 `app/` 目录（可能在子模块未提交或位于不同分支）。如果你打算运行或构建项目，请确保 `app/` 模块存在并已提交。

## 如何构建与运行
下面是从克隆到编译的最快路径：

1. 克隆仓库：

```bash
git clone https://github.com/hanhui12131/BypassPower.git
cd BypassPower
```

2. 在 Android Studio 中打开（推荐）
- 选择 `settings.gradle` 所在的项目根目录打开，Android Studio 会自动导入 Gradle 配置并下载依赖。

3. 在命令行构建（需要已安装 Android SDK 并配置 local.properties 或 ANDROID_HOME）：

```bash
# 在项目根目录
./gradlew assembleDebug    # 仅编译 debug APK
./gradlew assembleRelease  # 编译 release（需签名配置）
```

若要直接安装到已连接设备或模拟器：

```bash
./gradlew installDebug
```

环境要求：
- Android SDK（通过 local.properties 指定 sdk.dir 或设置 ANDROID_HOME）
- JDK（建议使用与 Android Gradle Plugin 兼容的版本，例如 JDK 11）

## 需要补充或检查的地方
- app/ 模块：检查并提交 app/ 目录（包含 app/build.gradle、src/ 等），否则无法编译运行。
- local.properties：通常不提交，请在本地创建并指向你的 Android SDK 路径，例如 `sdk.dir=/path/to/android/sdk`。
- 签名配置：若要构建 release，需要在 `app` 模块中添加签名配置或在 CI 中注入密钥库。

## 贡献
欢迎提交 issue 或 PR。常规流程：
1. Fork 仓库并在本地创建新的分支（feature/xxx）。
2. 提交代码并推送到你的 fork。 
3. 发起 PR，描述变更目的与测试步骤。

## 联系
如需进一步帮助，请在仓库中打开 issue，或在 PR 中 @ 提及维护者。

---
*此 README 由 Copilot 自动生成，基于仓库根目录的构建配置与文件列表。如需我把 README 推送到仓库（覆盖当前文件），请回复“更新 README”。*