# 实验一 · 5 人分工与留痕总表

> 组仓库：<https://github.com/H-Canopy/f26-lab01>（`CMU-17-214/f26-lab01` 的 public fork）
> 助教以**本仓库**为检查对象。本文件是全组的任务索引与留痕凭据表。

---

## 0. 每人第一件事：配置自己的 git 身份

**这一步不做，你所有的 commit 都会被算到别人名下，留痕直接失效。**

GitHub 只把 commit 归到某个账号，**当且仅当 commit 里的 email 是该账号已验证的邮箱**。
用下表的专属 noreply 邮箱，100% 能正确归属：

```bash
git config user.name  "<你的 GitHub 用户名>"
git config user.email "<你的专属 noreply 邮箱>"
git config --get user.name && git config --get user.email   # 确认
```

| 成员 | GitHub | 专属 noreply 邮箱 |
|---|---|---|
| M1 集成 | `H-Canopy` | `240248359+H-Canopy@users.noreply.github.com` |
| M2 诊断+修复 | `huangjiasheng567` | `201459503+huangjiasheng567@users.noreply.github.com` |
| M3 实现 | `xing-ding` | `205594322+xing-ding@users.noreply.github.com` |
| M4 测试+审查 | `Twh743` | `265459242+Twh743@users.noreply.github.com` |
| M5 红队 | `1311948` | `332867202+1311948@users.noreply.github.com` |

> ⚠️ `Twh743` 与 `1311948` 的 collaborator 邀请还是 **pending** 状态，需先在 GitHub 上接受，否则无法 push。

---

## 1. 分工表

| # | 成员 | 负责任务 | 分支 | 交付物 | 对应评分 |
|---|---|---|---|---|---|
| M1 | `H-Canopy` | 步骤 0 环境；课前任务；**维护 main**（只由 M1 合并 PR，每次合并后跑 `mvn test`） | `chore/env-setup` | README「工具与模型」声明；`mvn test` 复现 `Tests run: 10, Failures: 1` 的日志；transcripts 导出流程；合并记录 | 环境与课前任务 10 |
| M2 | `huangjiasheng567` | 里程碑 1 全流程（指导书 1.1 → 1.4） | `fix/m1-root-cause` | 诊断记录表；**根因所在层**的修复提交；回归测试；不变式论证 | 里程碑 1 **30** |
| M3 | `xing-ding` | 里程碑 2 的 `cancelBooking` 本体（R1~R6），拆 3 个小提交 | `feat/cancel-booking` | `BookingService.java` 改动 | 里程碑 2 实现 15 |
| M4 | `Twh743` | 里程碑 2 的测试与 diff 审查（S1~S6、晋升测试、指导书 2.4 的 10 项清单） | `test/cancel-booking` | 新建 `CancelBookingTest.java`；逐项审查记录 | 里程碑 2 测试+提交纪律 20 |
| M5 | `1311948` | 里程碑 3：补丁审查、暴露问题的测试、修复或拒绝、分支操作 | `review/agent-attempt` | 指导书 3.1 的九行判定表；`IsAvailableTest.java`；修复 PR 或拒绝理由 | 里程碑 3 **25** |

### 为什么这么切

- **M3 只改 `BookingService.java`，M4 只新建 `CancelBookingTest.java`** → 不同文件，可真正并行；
- M4 写的测试正好是 M3 实现的验证 → **天然的交叉审查**，比自测自更有说服力；
- 「M1 诊断」与「M1 修复」**必须同一个人**（诊断和修复强耦合，拆开是外行切法，且指导书评分点就是"能否自己追出根因"）。

---

## 2. 依赖顺序（不守会返工）

```
M1 chore/env-setup ────────────────────────────────(全程并行, 负责合并)
M2 fix/m1-root-cause ──> main 全绿 ──┬──> M3 feat/cancel-booking ──┐
                                     └──> M4 test/cancel-booking ──┴──> M5 review/agent-attempt
```

1. **M2 必须先完成**：main 变全绿后 M5 才能开工（指导书要求里程碑 3 从全绿出发）。
2. **M3 必须把 `cancelBooking` 加在 `listBookings` 之后**（类的末尾）。若加在 `book` 与 `listBookings` 之间，M5 的 `git apply changes/agent-attempt.patch` 会因上下文变化而失败（指导书 FAQ 第 9 条）。
3. M3 与 M4 可并行（不同文件）；M4 的测试在 M3 合并前跑会红，属于正常现象。
4. **只有 M1 能推 main**。其他人一律「开分支 → push → 开 PR」，不得直接 push main。

---

## 3. 留痕机制（4 层，缺一不可）

| 层 | 机制 | 由谁保证 |
|---|---|---|
| 1 | **commit author** 归属于本人 GitHub 账号 | 每人按第 0 节配置 git 身份 |
| 2 | **每人一个分支 + 一个 PR**：PR 作者、描述、reviewer、合并记录全部由 GitHub 原生记录 | 每人 |
| 3 | **各自本机的智能体会话导出**（`transcripts/`） | 每人，见第 7 节 |
| 4 | **现场演示时谁做的谁讲**：助教会追问，谁做的谁答 | 每人 |

---

## 4. 硬约束

1. **不要直接 push `main`** —— 一律走 PR。
2. **`transcripts/` 相关操作见第 7 节**（默认被 `.gitignore` 忽略，是否提交需老师明确）。
3. **不要修改已有测试**（`TimeIntervalTest`、`BookingServiceTest` 的既有用例）——指导书审查清单明确列为扣分项。
4. **每个提交只做一件能用一句话说清的事**，合并前 `mvn test` 必须为绿。

---

## 5. 每人的 PR 流程

```bash
git switch main && git pull origin main        # 从最新 main 出发
git switch -c <你的分支>                        # 分支名见分工表
# ... 干活，小步提交 ...
git push -u origin <你的分支>
gh pr create --base main --head <你的分支> --title "<一句话>" --body "<对照评分点写>"
```

PR 描述里必须写清：**做了什么 / 对应哪个评分点 / 怎么验证的（贴 `mvn test` 结果）/ 已知限制**。
M1 负责 review 并合并；合并后再跑一次 `mvn test`。

---

## 6. 提交前自检（对照指导书附录 D）

- [ ] 已 fork（而非直接 clone）课程仓库；`git remote -v` 指向组仓库
- [ ] JDK 21+、Maven 3.8+、Git、IDE、智能体工具均可用
- [ ] 能说出里程碑 1 的真正根因（文件、方法、原因），并已在正确的层修复
- [ ] 全部测试为绿，并能论证核心不变式仍然成立（建议有回归测试）
- [ ] `cancelBooking` 满足 R1～R6；已逐项审查智能体 diff
- [ ] 里程碑 2 由几个小提交组成；晋升测试会在「只删除」的实现上失败
- [ ] 已在 `agent-attempt` 分支上应用补丁、写出暴露问题的测试
- [ ] 已修复并合并回 main，或已拒绝并把分支留在原处；当前位于 main
- [ ] 本地 `transcripts/` 中有至少一次会话导出
- [ ] README 中声明了所用工具与模型；所有提交已推送到组仓库

---

## 7. ⚠️ 待确认：transcripts 的处理方式

指导书第六节的规定与「每人留痕」的要求存在冲突，**动手前必须由老师确认**：

| 路径 | 做法 | 风险 |
|---|---|---|
| **A（指导书原规定）** | 各自本地导出，**实验课上向助教出示，不提交、不推送** | 无。`.gitignore` 已忽略 `transcripts/`，`.gitignore` 原文注释即写明「Lab forks are public; transcripts stay local」 |
| **B（提交进仓库）** | 需要 `git add -f` 绕过忽略规则（或改 `.gitignore`），再 push | **本仓库是 public**：全体组员的提示词、智能体回复、它执行过的命令会公开可检索，且可能夹带个人信息 / 令牌。课程政策原文写明「对话记录只供课程教学人员查看」 |

**若走路径 B，至少做到**：先通读导出文件删掉敏感内容，并请老师书面确认豁免。
**若只是要「交给教学人员看」**，更安全的等价做法是把导出文件交到课程指定渠道（教学平台 / 私聊助教），而不是进公开仓库。

> 当前 `transcripts/` 为空（还没人在本仓库根目录跑过 Claude Code 会话），
> 所以这条**尚未阻塞任何工作**，可以边做边问。
