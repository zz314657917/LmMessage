---
phase: qa
current_sprint: 5
total_sprints: 5
pending_action: runtime-verification
project_type: minecraft-plugin
qa_mode: plugin
approval_required: false
last_verified: 2026-06-27
---

# Workflow Status

- 当前阶段：`qa`
- 下一合法动作：在 1.20.1 + ArcartX/AX + PlayerChat 测试服做插件运行态 QA。
- 状态推进规则：当前代码级 build/test 已通过；只有完成真实服务器加载、AX 客户端输入/HUD、PlayerChat 跨服链路验证后，才能进入 `done`。
- 当前目标：验证 `LmMessage` 作为 AX 聊天栏/HUD 前端，是否能把玩家输入交给 PlayerChat，并把 PlayerChat 频道/私聊事件显示到 AX。
- 当前默认：PlayerChat 是跨服聊天真相源；LmMessage 不重写频道、私聊、权限、禁言、黑名单、AI 审核或跨服转发。

## Build Evidence

- `gradle test --no-daemon`：PASS，需显式 Java 17 `JAVA_HOME`。
- `gradle build --no-daemon`：PASS，产物 `build/libs/LmMessage-0.1.0-SNAPSHOT.jar`。
- 运行态未验证：AX 客户端渲染、PlayerChat 跨服链路、ProtocolLib 可选兜底。
