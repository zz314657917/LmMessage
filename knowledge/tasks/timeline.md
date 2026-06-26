# Timeline

## 2026-06-27 03:45 +08:00 - 安全审计整改

- 当前阶段：已按静态审计意见完成一轮 P0/P1 安全硬化；代码级验证通过，运行态客户端链路仍待修复后复测。
- 本段重点：永久禁用 `op:` 动作；新增消息/字节/token/规则/命令/packet 硬上限；PlayerChat 事件改为先过滤再进入 AX UI；Tell 事件改为定向展示；命令日志改为 hash/长度脱敏；构建脚本移除本机绝对 PlayerChat jar 路径。
- 已完成：补充 `op:` 永久拒绝、命令长度上限、`splitN` token 上限测试；玩家退出清理 AX chat buffer；资源同步限制为内置 AX UI 文件；默认配置关闭完整命令日志并公开预算键。
- 关键决策：旧 `security.enable-op-prefix` 保留为兼容键但不再生效；本地 PlayerChat API jar 改由 `PLAYERCHAT_JAR` 或 `-PplayerChatJar=<path>` 提供，不写入仓库。
- 验证记录：`PLAYERCHAT_JAR=<LOCAL_PLAYERCHAT_JAR>; JAVA_HOME=<LOCAL_JAVA17>; gradle test --no-daemon` PASS；`PLAYERCHAT_JAR=<LOCAL_PLAYERCHAT_JAR>; JAVA_HOME=<LOCAL_JAVA17>; gradle build --no-daemon` PASS。
- 遗留问题：受测试客户端连接失败影响，AX 实机输入/HUD、PlayerChat 跨服业务规则和 reload runtime 仍未验证。
- 下一步：排查测试客户端连接 -> 入服后复测 `/lmmessage debug/open/test`、AX packet/chat submit、Tell 定向展示和 PlayerChat 跨服链路。

## 2026-06-27 03:05 +08:00 - 第二轮 QA 复测

- 当前阶段：代码级和服务端加载复测继续通过；客户端连接链路仍阻塞 AX 实机输入/HUD 验证。
- 本段重点：`gradle test/build` 再次 PASS；本地构建 jar 与测试服部署 jar SHA256 一致；`arcartx-dev-1201` 服务端日志再次确认 `LmMessage` 启用、AX chat/hud 注册为 true、PlayerChat 后端可用。
- 已完成：dev-stack MySQL/Redis preflight 通过；BlackBoxPro server-only ensure 到 plugin HTTP ready；带客户端 ensure 已复测并抓到 `DisconnectedScreen` / `Player not available` 证据；stop 后释放测试服与 HTTP 监听，无匹配 Java 进程残留。
- 关键决策：第二轮仍只认定服务端加载和 AX UI 注册通过，不把客户端渲染、AX chat submit 或 PlayerChat 跨服规则列为已验证。
- 验证记录：`gradle test --no-daemon` PASS；`gradle build --no-daemon` PASS；`Test-LmDevStack.ps1` PASS；`Invoke-TestCell1201.ps1 -Mode ensure -NoAutoStartBot` 服务端/plugin HTTP ready；`Invoke-TestCell1201.ps1 -Mode ensure` bot/mod HTTP ready 但连接失败；`Invoke-TestCell1201.ps1 -Mode stop` PASS。
- 遗留问题：测试 bot 登录到本地测试服后服务端记录 `lost connection: Disconnected`，客户端保持 `DisconnectedScreen`；因此 `/lmmessage open/test`、AX HUD 截图、聊天提交和跨服规则仍未验证。
- 下一步：优先排查测试客户端连接失败 -> 成功入服后执行 `/lmmessage debug/open/test` -> 再验证 AX packet/chat submit 与 PlayerChat 跨服业务规则。

## 2026-06-27 02:50 +08:00 - 首轮 QA 与测试服加载验证

- 当前阶段：代码级验证和服务端加载 smoke 通过；客户端 AX 画面与聊天提交链路仍受测试客户端连接失败阻塞。
- 本段重点：`gradle test/build` 通过；dev-stack MySQL/Redis preflight 通过；`LmMessage` 在 `arcartx-dev-1201` 服务端成功启用，AX chat/hud 注册为 true。
- 已完成：jar 已部署到 `F:/minecraft/dev/arcartx-1201-dev-server/plugins/`；BlackBoxPro server-only smoke 启动到 `Done`；测试环境 stop 后端口释放。
- 关键决策：当前 QA 结论只覆盖代码级和服务端加载，不扩大成 AX 客户端渲染或 PlayerChat 跨服业务通过。
- 验证记录：`gradle test --no-daemon` PASS；`gradle build --no-daemon` PASS；`Test-LmDevStack.ps1` PASS；`Invoke-TestCell1201.ps1 -Mode ensure -NoAutoStartBot` 服务端 ready，日志显示 `LmMessage enabled` 与 `ArcartX UI registration complete: chat=true, hud=true`。
- 遗留问题：BlackBoxPro 客户端连接本地测试服返回 `DisconnectedScreen`，服务端记录测试 bot `lost connection: Disconnected`；因此 `/lmmessage open/test`、AX HUD 截图、聊天提交和跨服规则未验证。
- 下一步：先修复测试客户端登录链路 -> 再跑 `/lmmessage debug/open/test` 和 AX packet/chat submit -> 最后补 PlayerChat 跨服业务验证。

## 2026-06-27 02:00 +08:00 - AX + PlayerChat 首版实现

- 当前阶段：代码级实现完成，P/G/E 状态进入 `qa`，等待真实 1.20.1 + ArcartX + PlayerChat 运行态验证。
- 本段重点：LmMessage 已按“AX 聊天栏/HUD 前端 + PlayerChat 跨服聊天后端”落地；不重写 PlayerChat 频道、私聊、权限、禁言、黑名单和跨服转发；ProtocolLib 仅保留可选状态检测。
- 已完成：Gradle KTS/Java 17 项目、`plugin.yml`、`config.yml`、AX chat/hud YAML、规则模型、命令动作解析、AX 反射 UI bridge、PlayerChat API/事件适配、`/lmmessage` 命令和 JUnit 单测。
- 关键决策：`plugin.yml` 对 PlayerChat 硬依赖，对 `ArcartX`/`ArcartXPlugin` 软依赖并在 `onEnable` 强校验；`op:` 命令前缀后续必须保持禁用。
- 验证记录：`gradle test --no-daemon` PASS；`gradle build --no-daemon` PASS；产物 `build/libs/LmMessage-0.1.0-SNAPSHOT.jar` 包含插件类、`plugin.yml`、`config.yml` 和 AX UI YAML。
- 遗留问题：尚未做真实服务器加载、AX 客户端渲染截图、PlayerChat 跨服转发、禁言/黑名单/私聊和 reload runtime 验证；ProtocolLib 包拦截兜底未实现。
- 下一步：部署 jar 到测试服 -> 跑 `/lmmessage debug/open/test` -> 用 AX 客户端提交消息 -> 验证 PlayerChat 跨服业务规则和 HUD 命中显示。

## 2026-06-27 00:25 +08:00 - Planner 初始化与 AX wiki 查证

- 当前阶段：P/G/E `intake`，正在形成 `LmMessage` 插件总规格和 Sprint 1 contract。
- 本段重点：确认参考插件 `ChaMessage` 的行为边界；确认 AX wiki 有 Packet/UIRegistry/UIHandler/自定义聊天栏等能力；建立仓库知识入口。
- 已完成：创建 `docs/workflow/`、`knowledge/00-start-here.md`、`knowledge/02-chamessage-reference.md`、`knowledge/03-ax-wiki-findings.md`、`knowledge/tasks/current-task.md`。
- 关键决策：v1 采用 AX HUD gateway + Java 服务端规则/动作路由，不做 Germ DoS 字符串兼容层。
- 验证记录：读取参考 `config.yml`；检查 Jar 内 `plugin.yml`；用 `javap` 确认 `sendHudDos`、`playSound`、`startsWith`、`contains` 链路；访问 AX wiki 相关页面。
- 遗留问题：待确认目标 ArcartX 版本、ProtocolLib 是否必需、AX 侧音效实现路径。
- 下一步：确认总规格 -> 批准 Sprint 1 contract -> 开始插件骨架和规则服务实现。
