# Current Task

## 背景

用户要新写一个类似 `ChaMessage` 的插件，但基于 AX/ArcartX 引擎；后续方向已调整为 `LmMessage` 做 AX 聊天栏/HUD 前端，PlayerChat 继续作为跨服聊天业务后端。

## 当前目标

完成 `LmMessage: AX Chat Frontend + PlayerChat Backend` 首轮 QA：代码级验证、测试服加载验证、AX UI 注册验证，并记录客户端连接链路当前阻塞点。

## 本次已完成

- 创建 Gradle KTS + Java 17 项目骨架，生成 `build/libs/LmMessage-0.1.0-SNAPSHOT.jar`。
- 新增 `plugin.yml`：硬依赖 `PlayerChat`，软依赖 `ArcartX`、`ArcartXPlugin`、`ProtocolLib`；启动时强校验 PlayerChat 与 AX 可用性。
- 新增 `config.yml`，支持 PlayerChat 默认频道/source、AX chat/hud UI id、规则、命令前缀和 debug 配置。
- 新增 AX UI YAML：`arcartx/ui/lmmessage_chat.yml` 负责输入提交 `Packet.send('lmmessage_chat_submit', 'chat_input=' + input)`；`lmmessage_hud.yml` 负责 HUD 提示。
- 新增纯 Java 规则层：`start`、`contain`、`replacePrefix`、`channel`、`sound`、`cancelOriginal`、`{splitN}` 变量。
- 新增命令动作层：支持 `console:`、`player:`、`op:` 解析；`op:` 默认被 `security.enable-op-prefix=false` 拦截；不执行 shell。
- 新增 PlayerChat 适配：AX 提交后通过 `PlayerChatApi.getInstance().sendMessage(player, channel, source, message)` 发送；监听 `PlayerChannelChatEvent` / `PlayerChannelTellEvent` 用于 AX 聊天流和 HUD 判断。
- 新增 AX 适配：反射初始化 `ArcartXAPI.getUIRegistry()`，注册 UI，注册 PACKET callback，`open/sendPacket/unregister`。
- 新增 `/lmmessage reload|debug|open|test` 命令。
- 新增 JUnit 5 单测：规则命中/前缀替换/变量、空规则拒绝、命令前缀解析与变量转义。
- 已复制 jar 到 `F:/minecraft/dev/arcartx-1201-dev-server/plugins/LmMessage-0.1.0-SNAPSHOT.jar`，源/目标 SHA256 一致。
- 已启动本地 dev-stack，MySQL `127.0.0.1:3307` 与 Redis `127.0.0.1:6380` preflight 通过。
- 已通过 BlackBoxPro `arcartx-dev-1201` 做 server-only 加载 smoke：服务端启动到 `Done`，`LmMessage` 成功启用，AX chat/hud UI 注册为 true。

## 已确认事实

- 本仓库已初始化 Git 并推送到 `https://github.com/zz314657917/LmMessage.git`，当前主分支 `master`。
- `F:/360下载/PlayerChat-3.4.0.jar` 可用于编译；已确认 `PlayerChatApi#sendMessage(Player,String,String,String): boolean` 存在。
- 已确认 PlayerChat 事件类 `PlayerChannelChatEvent`、`PlayerChannelTellEvent` 存在并有消息/频道/私聊字段。
- Gradle 本机入口默认用 Java 8 启动；验证时必须显式设置 `JAVA_HOME=C:/Program Files/Microsoft/jdk-17.0.10.7-hotspot`。
- 本机 `F:/gradle/init.d/init.gradle` 会注入额外仓库，所以 `settings.gradle.kts` 使用 `RepositoriesMode.PREFER_SETTINGS` 以兼容全局 init。
- 测试服未安装 ProtocolLib；`LmMessage` 日志确认主链路仍可用：`ProtocolLib not installed; PlayerChat main route remains available.`

## 待验证点

- 动作：修复/排查 `arcartx-dev-1201` 客户端连接失败。验证：BlackBoxPro mod HTTP `connect_to_server localhost:25715` 返回 success，`query_player_state` 不再是 `Player not available`。
- 动作：用 AX 客户端打开聊天栏并提交消息。验证：消息经 PlayerChat 发出，频道/权限/禁言/黑名单/跨服转发不被绕过。
- 动作：触发 PlayerChat 频道/私聊事件。验证：AX 聊天流刷新，命中规则时 HUD 可见。
- 动作：测试 reload。验证：AX UI 资源重新同步并注册，旧 callback 不重复触发。
- 动作：确认是否需要实现 ProtocolLib 非 PlayerChat 服务端消息包兜底。验证：安装/未安装 ProtocolLib 两条路径都能启动。

## 当前结论

首版代码级闭环已完成并通过服务端加载验证。`LmMessage` 在 1.20.1 + ArcartX + PlayerChat 测试服中成功启用，AX chat/hud UI 注册成功，服务端不因 LmMessage 报错。当前尚不能声称 AX 客户端渲染、聊天输入提交、跨服转发和 PlayerChat 业务规则已运行态验证，因为 BlackBoxPro 客户端当前连接到 `localhost:25715` 会进入 `DisconnectedScreen`。

## 下一步

- 动作：排查 `bot_player` 连接测试服被断开。验证：`Invoke-TestCell1201.ps1 -Mode ensure` 不再报 `Bot failed to connect to server`。
- 动作：执行 `/lmmessage debug`、`/lmmessage open`、`/lmmessage test chat ...`、`/lmmessage test hud ...`。验证：命令输出、AX 页面、HUD 截图和服务端日志。
- 动作：做 PlayerChat 跨服链路验证。验证：频道权限、禁言、黑名单、私聊、跨服转发仍由 PlayerChat 生效。

## 验证记录

- `javap -classpath F:/360下载/PlayerChat-3.4.0.jar -p cn.handyplus.chat.api.PlayerChatApi`
- `javap -classpath F:/360下载/PlayerChat-3.4.0.jar -p cn.handyplus.chat.event.PlayerChannelChatEvent`
- `javap -classpath F:/360下载/PlayerChat-3.4.0.jar -p cn.handyplus.chat.event.PlayerChannelTellEvent`
- `$env:JAVA_HOME='C:/Program Files/Microsoft/jdk-17.0.10.7-hotspot'; & 'F:/gradle/bin/gradle.bat' test --no-daemon` -> PASS
- `$env:JAVA_HOME='C:/Program Files/Microsoft/jdk-17.0.10.7-hotspot'; & 'F:/gradle/bin/gradle.bat' build --no-daemon` -> PASS
- `jar tf build/libs/LmMessage-0.1.0-SNAPSHOT.jar` -> 包含 `plugin.yml`、`config.yml`、AX UI YAML 和插件类。
- `powershell -NoProfile -ExecutionPolicy Bypass -File F:/mcplugins/dev-stack/scripts/Test-LmDevStack.ps1` -> Docker daemon、MySQL、Redis 均可用。
- `Invoke-TestCell1201.ps1 -Mode ensure -NoAutoStartBot` -> 服务端 ready；`latest.log` 包含 `LmMessage enabled`、`ArcartX UI registration complete: chat=true, hud=true`、`Done (20.822s)!`。
- `Invoke-TestCell1201.ps1 -Mode ensure` -> bot/mod HTTP ready，但连接失败：`DisconnectedScreen` / `Bot failed to connect to server`。
- `Invoke-TestCell1201.ps1 -Mode stop` -> PASS，serverPort/pluginHttpPort/modHttpPort 均释放。
