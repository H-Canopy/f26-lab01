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

课程政策要求「每次提交都说明所用的工具与模型」。**本组并非全员使用同一工具**，故按成员如实声明：

| 成员 | 智能体工具 | 模型 | 依据 |
|---|---|---|---|
| M1 `H-Canopy` | Claude Code（命令行 v2.1.92）；IDE：Visual Studio Code | `kimi-k3`（经 Anthropic 兼容接口接入） | `~/.claude/settings.json` 的 `ANTHROPIC_MODEL` / `ANTHROPIC_BASE_URL`；用于完成会话记录导出验证 |
| M2 `huangjiasheng567` | 待本人确认 | 待本人确认 | — |
| M3 `xing-ding` | **Doubao** | 待本人确认 | M3 在证据文件中主动披露：`docs/evidence/M3-m2-01-plan-review.txt` |
| M4 `Twh743` | **Doubao** | 待本人确认 | 其终端截图的会话路径为 `…\Doubao\chats\…` |
| M5 `1311948` | 待本人确认 | 待本人确认 | — |

> ⚠️ **提交前必须补全本表**。声明不实属于诚信问题。
> 另注：起始仓库的 `tools/export-transcripts.sh` **只识别 Claude Code** 的 `~/.claude/projects/`，
> 使用 Doubao 的成员无法由该脚本导出会话记录，需向教师确认能否以其他方式提供等价记录。
> 报告中的同一张表见 [`docs/实验报告.md`](docs/实验报告.md) §6.2。

## 本组的实验一材料

- 分工与留痕总表：[`CONTRIBUTIONS.md`](CONTRIBUTIONS.md)
- 执行顺序与每人任务清单：[`PLAN.md`](PLAN.md)
- 实验报告：[`docs/实验报告.md`](docs/实验报告.md)
- **截图与证据清单（每人对照执行）**：[`docs/证据清单.md`](docs/证据清单.md)
- 证据索引：[`docs/evidence/`](docs/evidence/)
- 组仓库：<https://github.com/H-Canopy/f26-lab01>
