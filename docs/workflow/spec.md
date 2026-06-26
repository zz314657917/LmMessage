---
repo: LmMessage
project_type: minecraft-plugin
qa_mode: plugin
last_verified: 2026-06-27
---

# LmMessage Product Spec

## 一句话需求
- 新写一个类似 `ChaMessage` 的 Minecraft 消息插件，但 UI/消息呈现基于 AX/ArcartX 引擎，而不是 GermPlugin HUD DoS。

## 目标与非目标
- 目标：
  - 拦截/识别聊天、广播和服务端聊天包中的指定前缀或包含文本。
  - 按配置决定是否吞掉原始消息，并把清洗后的消息推送到 AX HUD 通知 UI。
  - 支持配置化动作规则：命中文本后对目标玩家执行白名单命令或触发 AX packet 路由。
  - 提供 reload、diagnostic/debug 和最小 QA 命令面。
- 非目标：
  - 不直接兼容 Germ 的 `hudMessage<->anchor@...` / `openChild<->...` DoS 字符串。
  - 不迁移 ChaCore；保留可读、可维护的独立配置和服务层。
  - v1 不做跨服消息总线、数据库持久化或复杂聊天频道系统。

## 关键约束
- AX wiki 查证结论：官方文档存在 `Packet:网络数据包`、`ArcartXUIRegistry:UI注册器`、`UIHandler:UI处理器`、`自定义聊天栏`、`控件类型` 等对应能力；没有发现 Germ `sendHudDos` anchor 字符串一比一功能。
- 本地已验证经验：`LmWeChat` 的 AX 接入采用 `resources-ax/arcartx/ui/*.yml` + `UIRegistry` 注册 + `Packet.send(...)` 回 Java router；HUD 使用 `isHud: "true"`。
- 依赖边界：AX/ArcartX 作为硬依赖或强软依赖单独放在 `integration/arcartx`；ProtocolLib 若用于服务端 CHAT 包拦截，应集中在 `integration/protocollib`。
- 线程边界：`AsyncPlayerChatEvent` 不直接调用 Bukkit/AX API；只提取文本和玩家 UUID，再切回主线程处理需要 Bukkit/AX 的动作。
- 命令安全：动作规则不能裸执行任意配置命令；必须限制动作类型、替换变量和执行身份，避免把聊天内容直接拼成高权限命令。

## 技术方案
- 架构说明：
  - 监听层只负责捕获消息来源、提取文本和取消原始事件。
  - `MessageRuleService` 负责规则匹配、文本清洗、`{splitN}` 变量生成和目标玩家选择。
  - `ActionDispatchService` 负责命中后的动作执行，隔离命令白名单、权限和主线程调度。
  - `ArcartXMessageHudGateway` 负责 AX HUD UI 注册、open/sendPacket、reload/unregister 和缺依赖诊断。
- 关键模块：
  - 配置模型：`message-rules`、`action-rules`、`ax-hud`、`security`、`debug`。
  - UI 资源：最小 AX HUD YAML，显示 anchor/type、消息文本、可选标题/持续时间。
  - 命令面：`/lmmessage reload`、`/lmmessage debug`、`/lmmessage test hud <type> <message>`。

## Sprint 计划
- Sprint 1：项目骨架、依赖声明、配置模型、规则匹配服务和单元测试。
- Sprint 2：Bukkit/ProtocolLib 捕获链路、取消原始消息、动作路由和命令安全。
- Sprint 3：AX HUD gateway、UI YAML、注册/reload/unregister、测试 HUD 命令。
- Sprint 4：本地 1.20.1 AX 运行时验证、日志诊断、文档和 release checklist。
