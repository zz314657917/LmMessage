# Timeline

## 2026-06-27 02:00 +08:00 - AX + PlayerChat 首版实现

- 当前阶段：代码级实现完成，P/G/E 状态进入 `qa`，等待真实 1.20.1 + ArcartX + PlayerChat 运行态验证。
- 本段重点：LmMessage 已按“AX 聊天栏/HUD 前端 + PlayerChat 跨服聊天后端”落地；不重写 PlayerChat 频道、私聊、权限、禁言、黑名单和跨服转发；ProtocolLib 仅保留可选状态检测。
- 已完成：Gradle KTS/Java 17 项目、`plugin.yml`、`config.yml`、AX chat/hud YAML、规则模型、命令动作解析、AX 反射 UI bridge、PlayerChat API/事件适配、`/lmmessage` 命令和 JUnit 单测。
- 关键决策：`plugin.yml` 对 PlayerChat 硬依赖，对 `ArcartX`/`ArcartXPlugin` 软依赖并在 `onEnable` 强校验；`op:` 命令前缀默认禁用，只在配置显式开启后执行。
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
