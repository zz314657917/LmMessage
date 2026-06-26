# ChaMessage 参考行为

最后更新：2026-06-27

## 来源

- 配置目录：`G:/MC/AAA测/A-0619/ChaMessage/config.yml`
- 插件 Jar：`G:/MC/AAA测/A-0619/萌芽消息ChaMessage.jar`

## 已确认行为

- `plugin.yml`：插件名 `ChaMessage`，版本 `1.1.0`，硬依赖 `ChaCore`、`GermPlugin`、`ProtocolLib`，命令 `ChaMessage` / `cmessage`。
- `message.*` 规则按 `start` 做 `startsWith` 匹配；命中后根据 `replace` 决定是否删除前缀，并把结果拼到 `anchor` 后。
- `action.*` 规则按 `contain` 做包含匹配；命中后基于空格切分生成 `{split0}`、`{split1}` 等变量，再执行配置命令列表。
- 监听链路包含 `AsyncPlayerChatEvent`、`BroadcastMessageEvent` 和 ProtocolLib `Play.Server.CHAT` 异步包监听。
- Germ 输出链路使用 `GermPacketAPI.sendHudDos(player, message)`，可选 `GermPacketAPI.playSound(player, sound)`。

## 迁移含义

- `hudMessage<->anchor@...` 是 Germ DoS 协议，不应原样搬到 AX。
- AX 版本应把 `anchor` 抽象成 `hudType` / `channel` / `layoutKey`，由 AX HUD YAML 和服务端 packet 数据解释。
- 命令动作必须重做白名单和变量转义；不要照搬高权限 `op:` 字符串拼接。
- 异步聊天事件中不要直接调用 Bukkit/AX API，必须切回主线程执行 HUD 和命令动作。

## 待验证点

- ProtocolLib 在目标 1.20.1 环境中是否仍需要拦截 `Play.Server.CHAT`，还是 Bukkit/Adventure 事件已足够覆盖目标消息来源。
- 参考配置里的 `sound` 空值和非空值在 AX 侧对应 Bukkit sound、AX 音效函数，还是保持 v1 暂不支持。
