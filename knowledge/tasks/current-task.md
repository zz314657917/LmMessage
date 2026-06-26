# Current Task

## 背景

用户要新写一个类似 `ChaMessage` 的插件，但基于 AX/ArcartX 引擎；后续方向已调整为 `LmMessage` 做 AX 聊天栏/HUD 前端，PlayerChat 继续作为跨服聊天业务后端。

## 当前目标

完成审计意见中的 P0/P1 安全整改，并保持 QA 结论脱敏记录：确认当前构建产物、测试服加载、AX UI 注册、PlayerChat 主链路状态，并记录客户端连接链路当前阻塞点。

## 本次已完成

- 创建 Gradle KTS + Java 17 项目骨架，生成 `build/libs/LmMessage-0.1.0-SNAPSHOT.jar`。
- 新增 `plugin.yml`：硬依赖 `PlayerChat`，软依赖 `ArcartX`、`ArcartXPlugin`、`ProtocolLib`；启动时强校验 PlayerChat 与 AX 可用性。
- 新增 `config.yml`，支持 PlayerChat 默认频道/source、AX chat/hud UI id、规则、命令前缀和 debug 配置。
- 新增 AX UI YAML：`arcartx/ui/lmmessage_chat.yml` 负责输入提交 `Packet.send('lmmessage_chat_submit', 'chat_input=' + input)`；`lmmessage_hud.yml` 负责 HUD 提示。
- 新增纯 Java 规则层：`start`、`contain`、`replacePrefix`、`channel`、`sound`、`cancelOriginal`、`{splitN}` 变量。
- 新增命令动作层：支持 `console:`、`player:` 解析；`op:` 已在代码层永久拒绝；不执行 shell。
- 新增 PlayerChat 适配：AX 提交后通过 `PlayerChatApi.getInstance().sendMessage(player, channel, source, message)` 发送；监听 `PlayerChannelChatEvent` / `PlayerChannelTellEvent` 用于 AX 聊天流和 HUD 判断。
- 新增 AX 适配：反射初始化 `ArcartXAPI.getUIRegistry()`，注册 UI，注册 PACKET callback，`open/sendPacket/unregister`。
- 新增 `/lmmessage reload|debug|open|test` 命令。
- 新增 JUnit 5 单测：规则命中/前缀替换/变量、空规则拒绝、命令前缀解析与变量转义。
- 已复制 jar 到本地 ArcartX 1.20.1 测试服插件目录，源/目标 SHA256 一致。
- 已启动本地 dev-stack，MySQL 与 Redis preflight 通过。
- 已通过 BlackBoxPro `arcartx-dev-1201` 做 server-only 加载 smoke：服务端启动到 `Done`，`LmMessage` 成功启用，AX chat/hud UI 注册为 true。
- 2026-06-27 03:00 +08:00 已完成第二轮复测：`gradle test` PASS、`gradle build` PASS、jar 内容包含 `plugin.yml`/`config.yml`/AX UI YAML，测试服部署 jar 与本地构建 jar SHA256 一致。
- 第二轮 dev-stack preflight 通过：Docker daemon、MySQL、Redis 均可用，容器状态为 healthy。
- 第二轮 `arcartx-dev-1201` server-only smoke 继续通过：BlackBoxPro plugin HTTP ready，日志包含 `LmMessage enabled`、`ArcartX UI registration complete: chat=true, hud=true`、`PlayerChat backend=true`、`Done (19.122s)!`。
- 第二轮带客户端 ensure 仍失败：BlackBoxPro mod HTTP ready，但 `query_player_state` 返回 `Player not available`，`query_screen_state` 显示 `DisconnectedScreen`。
- 第二轮测试环境已停止：`Invoke-TestCell1201.ps1 -Mode stop` PASS，测试服与 HTTP 监听释放，未发现匹配测试服/测试客户端的 Java 进程残留。
- 已按静态审计意见完成一轮安全整改：永久禁用 `op:`，新增消息/字节/token/规则/命令/packet 硬上限，PlayerChat 事件先规则过滤再进入 AX UI，Tell 事件改为定向展示，玩家退出清理 AX chat buffer，命令日志改为 hash/长度脱敏，构建脚本移除本机绝对 PlayerChat jar 路径。

## 已确认事实

- 本仓库已初始化 Git 并推送到 `https://github.com/zz314657917/LmMessage.git`，当前主分支 `master`。
- 本地 PlayerChat API jar 可通过 `PLAYERCHAT_JAR` 或 `-PplayerChatJar=<path>` 提供；已确认 `PlayerChatApi#sendMessage(Player,String,String,String): boolean` 存在。
- 已确认 PlayerChat 事件类 `PlayerChannelChatEvent`、`PlayerChannelTellEvent` 存在并有消息/频道/私聊字段。
- Gradle 本机入口默认可能不是 Java 17；验证时需显式使用本地 Java 17。
- 本机 `F:/gradle/init.d/init.gradle` 会注入额外仓库，所以 `settings.gradle.kts` 使用 `RepositoriesMode.PREFER_SETTINGS` 以兼容全局 init。
- 测试服未安装 ProtocolLib；`LmMessage` 日志确认主链路仍可用：`ProtocolLib not installed; PlayerChat main route remains available.`
- 当前部署到测试服的 jar SHA256 为 `3EE7F9C46ACFBC884E50444D4CA909DC9A4DA3CB9FA26C9E8837E327BD4C987D`，与 `build/libs/LmMessage-0.1.0-SNAPSHOT.jar` 一致。
- 测试 bot 在第二轮服务端日志中到达登录阶段后断开：`lost connection: Disconnected`；本轮没有看到 `LmMessage` 导致的启用失败或运行期异常。
- 构建脚本不再包含本机绝对 PlayerChat jar 路径；本地验证通过环境变量注入该 jar。

## 待验证点

- 动作：修复/排查测试客户端连接失败。验证：BlackBoxPro mod HTTP `connect_to_server <LOCAL_TEST_SERVER>` 返回 success，`query_player_state` 不再是 `Player not available`。
- 动作：用 AX 客户端打开聊天栏并提交消息。验证：消息经 PlayerChat 发出，频道/权限/禁言/黑名单/跨服转发不被绕过。
- 动作：触发 PlayerChat 频道/私聊事件。验证：AX 聊天流刷新，命中规则时 HUD 可见。
- 动作：测试 reload。验证：AX UI 资源重新同步并注册，旧 callback 不重复触发。
- 动作：确认是否需要实现 ProtocolLib 非 PlayerChat 服务端消息包兜底。验证：安装/未安装 ProtocolLib 两条路径都能启动。

## 当前结论

代码级闭环、第二轮服务端加载复测和安全整改后的构建验证均已通过。`LmMessage` 在 1.20.1 + ArcartX + PlayerChat 测试服中成功启用，AX chat/hud UI 注册成功，服务端不因 LmMessage 报错。当前尚不能声称 AX 客户端渲染、聊天输入提交、跨服转发和 PlayerChat 业务规则已运行态验证，因为 BlackBoxPro 客户端当前连接到本地测试服仍会进入 `DisconnectedScreen`。

## 下一步

- 动作：排查测试 bot 连接测试服被断开。验证：`Invoke-TestCell1201.ps1 -Mode ensure` 不再报 `Bot failed to connect to server`。
- 动作：执行 `/lmmessage debug`、`/lmmessage open`、`/lmmessage test chat ...`、`/lmmessage test hud ...`。验证：命令输出、AX 页面、HUD 截图和服务端日志。
- 动作：做 PlayerChat 跨服链路验证。验证：频道权限、禁言、黑名单、私聊、跨服转发仍由 PlayerChat 生效。

## 验证记录

- `javap -classpath <LOCAL_PLAYERCHAT_JAR> -p cn.handyplus.chat.api.PlayerChatApi`
- `javap -classpath <LOCAL_PLAYERCHAT_JAR> -p cn.handyplus.chat.event.PlayerChannelChatEvent`
- `javap -classpath <LOCAL_PLAYERCHAT_JAR> -p cn.handyplus.chat.event.PlayerChannelTellEvent`
- `JAVA_HOME=<LOCAL_JAVA17>; gradle test --no-daemon` -> PASS
- `JAVA_HOME=<LOCAL_JAVA17>; gradle build --no-daemon` -> PASS
- `jar tf build/libs/LmMessage-0.1.0-SNAPSHOT.jar` -> 包含 `plugin.yml`、`config.yml`、AX UI YAML 和插件类。
- `Test-LmDevStack.ps1` -> Docker daemon、MySQL、Redis 均可用。
- `Invoke-TestCell1201.ps1 -Mode ensure -NoAutoStartBot` -> 服务端 ready；`latest.log` 包含 `LmMessage enabled`、`ArcartX UI registration complete: chat=true, hud=true`、`Done (20.822s)!`。
- `Invoke-TestCell1201.ps1 -Mode ensure` -> bot/mod HTTP ready，但连接失败：`DisconnectedScreen` / `Bot failed to connect to server`。
- `Invoke-TestCell1201.ps1 -Mode stop` -> PASS，serverPort/pluginHttpPort/modHttpPort 均释放。
- 2026-06-27 第二轮：`JAVA_HOME=<LOCAL_JAVA17>; gradle test --no-daemon` -> PASS。
- 2026-06-27 第二轮：`JAVA_HOME=<LOCAL_JAVA17>; gradle build --no-daemon` -> PASS。
- 2026-06-27 第二轮：`Get-FileHash` -> 本地构建 jar 与测试服插件 jar SHA256 均为 `3EE7F9C46ACFBC884E50444D4CA909DC9A4DA3CB9FA26C9E8837E327BD4C987D`。
- 2026-06-27 第二轮：`Test-LmDevStack.ps1` -> Docker daemon、MySQL、Redis 均可用，MySQL/Redis 容器 healthy。
- 2026-06-27 第二轮：`Invoke-TestCell1201.ps1 -Mode ensure -NoAutoStartBot` -> 脚本因未启动 bot 返回非零，但服务端与 plugin HTTP ready。
- 2026-06-27 第二轮：`latest.log` -> `LmMessage enabled`、`ArcartX UI registration complete: chat=true, hud=true`、`LmMessage enabled. PlayerChat backend=true, ProtocolLib not installed; PlayerChat main route remains available.`、`Done (19.122s)!`。
- 2026-06-27 第二轮：`Invoke-TestCell1201.ps1 -Mode ensure` -> bot 与 mod HTTP ready，但连接失败；`query_screen_state` 为 `DisconnectedScreen`，`query_player_state` 为 `Player not available`。
- 2026-06-27 第二轮：服务端日志显示测试 bot `lost connection: Disconnected`；`Invoke-TestCell1201.ps1 -Mode stop` -> PASS，测试服与 HTTP 监听释放，无匹配 Java 进程残留。
- 2026-06-27 安全整改：`PLAYERCHAT_JAR=<LOCAL_PLAYERCHAT_JAR>; JAVA_HOME=<LOCAL_JAVA17>; gradle test --no-daemon` -> PASS。
- 2026-06-27 安全整改：`PLAYERCHAT_JAR=<LOCAL_PLAYERCHAT_JAR>; JAVA_HOME=<LOCAL_JAVA17>; gradle build --no-daemon` -> PASS。
