<!-- codex:pge-workflow:start -->
## Planner / Generator / Evaluator Workflow

- 本仓库的交付流程产物位于 `docs/workflow/`。
- 默认 Agent Matrix：`docs/workflow/agent-matrix.md`；命中 `P/G/E`、`Agent Matrix`、`worker` 或 `测试 worker` 时按矩阵分工执行。
- 当前阶段阅读顺序：`docs/workflow/status.md` -> `docs/workflow/agent-matrix.md` -> `docs/workflow/spec.md` -> 当前 Sprint 的 contract/review/qa/fix-log。
- 会话暂停、续做或换人接手时，仍优先更新 `knowledge/tasks/current-task.md` 作为事实源；阶段完成或需要保留最近重点时追加 `knowledge/tasks/timeline.md`。
- 小型一次性修改可显式绕过该流程；多 Sprint 或需要验收门禁的任务默认启用。
<!-- codex:pge-workflow:end -->

# LmMessage 知识入口

## 项目目标

`LmMessage` 是计划中的 1.20.1 + AX/ArcartX 消息 HUD 插件，用来替代参考插件 `ChaMessage` 的 Germ HUD DoS 路线。

## 当前事实源

- P/G/E 状态：`docs/workflow/status.md`
- 产品规格：`docs/workflow/spec.md`
- 当前会话快照：`knowledge/tasks/current-task.md`
- 阶段时间轴：`knowledge/tasks/timeline.md`
- 参考插件行为：`knowledge/02-chamessage-reference.md`
- AX wiki 查证：`knowledge/03-ax-wiki-findings.md`

## 当前默认

- 目标运行时：Bukkit/Arclight `1.20.1` + ArcartX/AX。
- UI 路线：AX HUD YAML + Java gateway + `Packet.send(...)` / `sendPacket(...)`，不硬转 Germ DoS 字符串。
- 参考行为只复刻可观察语义，不复制 ChaMessage 反编译代码。
