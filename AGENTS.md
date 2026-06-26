<!-- codex:pge-workflow:start -->
## Planner / Generator / Evaluator Workflow

- 本仓库启用 `Planner / Generator / Evaluator` 工作流。
- 默认 Agent Matrix 位于 `docs/workflow/agent-matrix.md`；命中 `P/G/E`、`Agent Matrix`、`worker` 或 `测试 worker` 时按矩阵分工执行。
- 所有需求先走 Planner；所有 Sprint 必须先 contract 后 build。
- Evaluator 审 contract 和验收结果是两个独立门禁。
- 长期任务先读 `docs/workflow/status.md`，再读 `docs/workflow/spec.md` 和当前 Sprint 文档。
- `knowledge/tasks/current-task.md` 仍是默认会话交接事实源；`knowledge/tasks/timeline.md` 记录阶段历史和最近重点。
- 当前仓库默认 `qa_mode=plugin`，验收以插件构建、加载日志、命令、事件/包拦截和 AX HUD runtime smoke 为主。
<!-- codex:pge-workflow:end -->
