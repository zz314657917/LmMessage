---
sprint: 1
status: draft
qa_mode: plugin
last_verified: 2026-06-27
---

# Sprint 01 Contract

## Sprint 目标
- 搭建 `LmMessage` 插件项目骨架，完成配置模型和消息规则匹配核心，不接 AX runtime，不接真实事件监听。

## 范围与非范围
- 范围：
  - 创建 Java + Maven 或 Gradle Bukkit 插件骨架，目标 Java 17 / Bukkit 1.20.1。
  - 定义 `config.yml` 默认结构，覆盖 `message-rules`、`action-rules`、`ax-hud`、`security`、`debug`。
  - 实现纯 Java 规则模型和匹配服务：`startsWith`、`contains`、`replacePrefix`、`{splitN}`。
  - 添加 reload 时配置重读的服务边界，但不接入外部插件 API。
- 非范围：
  - 不实现 ProtocolLib 包拦截。
  - 不实现 AX UI 注册、HUD 打开或 packet callback。
  - 不复制/反编译 ChaMessage 代码，只复刻可观察行为。

## 输入/输出接口或命令面
- 默认命令预留：`/lmmessage reload`、`/lmmessage debug`。
- 纯服务层输出：匹配结果包含 rule id、是否取消原消息、清洗后文本、目标 HUD 类型、声音、动作变量。

## 受影响模块
- 新建插件入口、配置加载、规则服务、命令壳、单元测试。
- `docs/workflow/*` 和 `knowledge/*` 只由主 Codex 更新，worker 不直接改。

## 验收标准
- 项目可以编译。
- 默认配置可加载，缺失字段有默认值或清晰错误。
- 单元测试覆盖：
  - 前缀命中且 `replacePrefix=true` 时删除前缀。
  - 前缀命中且 `replacePrefix=false` 时保留原文。
  - 包含命中时生成 `{split0}` 等变量。
  - 未命中时不取消、不发 HUD、不触发动作。
  - 禁止空规则导致全量吞消息。

## 验证方式
- `mvn test` 或 `gradlew.bat test`。
- `mvn package -DskipTests` 或 `gradlew.bat build -x test`。
- 人工检查默认 `plugin.yml`、`config.yml` 和命令权限说明。
