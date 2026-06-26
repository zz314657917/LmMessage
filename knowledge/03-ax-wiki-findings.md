# AX Wiki 功能查证

最后更新：2026-06-27

## 查证入口

- 官方文档站：`https://wiki.arcartx.com/`
- 已访问页面：
  - `https://wiki.arcartx.com/docs/arcartx_v1/shimmer/3_arcartx/1_functions/10_packet`
  - `https://wiki.arcartx.com/docs/arcartx_v1/server_api/9_ui_registry`
  - `https://wiki.arcartx.com/docs/arcartx_v1/server_api/10_ui_handler`
  - `https://wiki.arcartx.com/docs/arcartx_v1/core/8_ui/13_chat`
  - `https://wiki.arcartx.com/docs/arcartx_v1/core/8_ui/8_controls_type`

## 结论

- AX wiki 有对应的 UI 通讯与注册能力：`Packet:网络数据包`、`ArcartXUIRegistry:UI注册器`、`UIHandler:UI处理器`。
- AX wiki 有聊天/消息展示相关能力：`自定义聊天栏`、控件类型、Shimmer/Packet 函数。
- 没有确认到 Germ `GermPacketAPI.sendHudDos` 或 `hudMessage<->anchor@...` 的一比一等价功能。
- 因此 `LmMessage` 不应把 Germ DoS 字符串直接转成 AX 字符串；应该实现 AX HUD UI + Java 数据推送网关。

## 本地可复用依据

- `F:/mcplugins/LmWeChat/knowledge/13-arcartx-ax-runtime-lessons.md` 已记录实测 AX 接入经验。
- 已验证模式：`src/main/resources-ax/arcartx/ui/*.yml`、`isHud: "true"`、`UIRegistry` 注册、`sendPacket(...)` 推送状态、YAML 中 `Packet.send(...)` 回 Java router。
- 运行时验收不能只看服务端 `open=true`；必须结合 UI 注册、packet/debug 状态、截图或 BlackBoxPro runtime smoke。

## 设计取舍

- v1 采用 AX HUD 通知壳承载消息文本，服务端保持规则匹配和动作执行权威。
- 原 ChaMessage 的 `anchor` 字段迁移为逻辑 `channel`，由配置映射到 AX HUD UI 的展示位置/样式。
- 若以后要支持自定义聊天栏，可作为独立 Sprint；当前替代 ChaMessage 的关键是 HUD/通知通道，而不是重做完整聊天输入 UI。
