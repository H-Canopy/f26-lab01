# Lab 1 Starter: Booking Service

A small room-booking service. Users book rooms for time intervals, and if a room is
taken they land on a waitlist. It is the codebase you work in for Lab 1.

**Read `ARCHITECTURE.md` first.** It maps the three layers (domain / service / repo)
so you do not have to cold-read every file.

## Build and test

```
mvn test
```

One test fails on purpose. Diagnosing and fixing it is Milestone 1.
By the way: the fix may not be where the failing test first points you. :-)

## Where things are

- Source: `src/main/java/edu/cmu/cs214/booking/` (`domain/`, `service/`, `repo/`)
- Tests: `src/test/java/edu/cmu/cs214/booking/`
- Setup: `SETUP.md`
- Your Milestone 2 task: `TASK.md`
- A proposed change you will review in Milestone 3: `changes/agent-attempt.patch` (the handout tells you how to apply it)
- Transcript export script (for Claude Code; modify if using a different tool): `tools/export-transcripts.sh` (the handout tells you when to run it)

See the Lab 1 handout on the course page for the three milestones you show a TA.

## 工具与模型声明

- **工具**：Claude Code（命令行，v2.1.92）；IDE：Visual Studio Code
- **模型**：`kimi-k3`（经 Anthropic 兼容接口接入）

取值依据：`~/.claude/settings.json` 中 `ANTHROPIC_MODEL` 及各默认模型槽位均配置为 `kimi-k3`，
`ANTHROPIC_BASE_URL` 指向 Anthropic 兼容接口。课程政策要求「每次提交都说明所用的工具与模型」，
故按会话中实际使用的模型如实声明。

本组其余成员在本次实验中使用了其他智能体工具辅助编码（M3、M4 明确为 Doubao）。由于起始仓库的
`tools/export-transcripts.sh` 只识别 Claude Code 的 `~/.claude/projects/`，会话记录的导出统一由
M1 的 Claude Code 环境完成。详见 [`docs/实验报告.md`](docs/实验报告.md) §6.2。

## 本组的实验一材料

- 分工与留痕总表：[`CONTRIBUTIONS.md`](CONTRIBUTIONS.md)
- 执行顺序与每人任务清单：[`PLAN.md`](PLAN.md)
- 实验报告：[`docs/实验报告.md`](docs/实验报告.md)
- **截图与证据清单（每人对照执行）**：[`docs/证据清单.md`](docs/证据清单.md)
- 证据索引：[`docs/evidence/`](docs/evidence/)
- 组仓库：<https://github.com/H-Canopy/f26-lab01>
