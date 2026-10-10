# 实验一 · 执行顺序与每人任务清单

> 配套文件：`CONTRIBUTIONS.md`（分工总表、git 身份邮箱、留痕机制）
> 本文件回答两个问题：**谁在什么时候做什么**、**做到什么程度算完成**。
> 指导书原文中的章节号（如「1.2」「2.4」）在下方直接引用，方便对照。

---

## 一、总时间线

| 时段 | M1 `H-Canopy` 集成 | M2 `huangjiasheng567` 诊断修复 | M3 `xing-ding` 实现 | M4 `Twh743` 测试审查 | M5 `1311948` 红队 |
|---|---|---|---|---|---|
| **课前**（不占课内） | 环境、collaborator、分支架子、transcripts 流程 | 装好工具链 | 装好工具链 | 装好工具链 | 装好工具链 |
| **0–5 min** | 主持对齐：全员确认 `mvn test` = 10 tests / 1 failure | 同左 | 同左 | 同左 | 同左 |
| **5–35 min** | 记录环境证据 + README 声明 | **【关键路径】里程碑 1 诊断→修复→回归测试** | 读 `TASK.md`，拆 R1~R6，让智能体出计划 | 设计 S1~S6 期望结果 | 读 `agent-attempt.patch`，填 3.1 九行判定表 |
| **35–75 min** | review/合并 PR，每次合并后跑 `mvn test` | review M5 的 PR | 实现 `cancelBooking`（2 个小提交） | 写 `CancelBookingTest.java`，验证测试有说服力 | 在分支上试用补丁，写暴露问题的测试 |
| **75–95 min** | 合并、守 main | 支援 M5 定根因 | 支援 M4 转绿 | 把测试跑到全绿 | **里程碑 3：修复或拒绝 + 分支操作** |
| **95–100 min** | 最终验证 + 组织演示 | 本人讲解 | 本人讲解 | 本人讲解 | 本人讲解 |

**关键路径 = M2 → M3 → M5**。另外两人（M1、M4）不与关键路径争资源，可以全程并行。

---

## 二、Phase 0：全员准备（0–5 分钟，5 人都要做）

```bash
# 1. 先在 GitHub 上接受 collaborator 邀请（Twh743、1311948 目前还是 pending）
# 2. 克隆组仓库
git clone https://github.com/H-Canopy/f26-lab01.git
cd f26-lab01

# 3. 配置自己的 git 身份 —— 不做这步，你的 commit 会归到别人名下，留痕失效
git config user.name  "<你的 GitHub 用户名>"
git config user.email "<CONTRIBUTIONS.md 表里你的 noreply 邮箱>"
git config --get user.name && git config --get user.email

# 4. 切到自己的分支
git switch <你的分支>

# 5. 确认工具链
java --version      # 21+
mvn --version       # 3.8+
git --version
claude --version    # 智能体工具，且要能导出会话记录

# 6. 确认起点状态：必须恰好 1 个测试失败（这是故意埋的缺陷，不是环境问题）
mvn test
# 期望：Tests run: 10, Failures: 1
#       BookingServiceTest.sameSlotInDifferentRoomsAreBothConfirmed
```

### 如果 `mvn` 不存在 / 装不上

1. **`winget` 可能完全没响应**（实测退出码 `0x8A150001`，无任何输出）——别在它身上耗时间。
2. 直接下载二进制 zip 更可靠：<https://dlcdn.apache.org/maven/maven-3/3.9.16/binaries/apache-maven-3.9.16-bin.zip>
   （顺手校验 `.sha512`），解压后把 `bin` 目录加入 PATH，**重开终端**。
3. 装完确认 `mvn --version` 里的 `Java version` 也是 21+；不是就检查 `JAVA_HOME`。

### 如果 `git clone` / `git push` 报 `schannel: AcquireCredentialsHandle failed: SEC_E_NO_CREDENTIALS`

这是 Windows SChannel 后端在某些受限环境（含沙箱）下拿不到凭证，**不是你密码错了**。改用 OpenSSL 后端即可：

```bash
git config --global http.sslBackend openssl
```

---

## 三、M1 `H-Canopy` —— 集成 / 环境 / 课前任务

**分支**：`chore/env-setup`　**评分**：环境与课前任务 10 分

### 已完成
- fork 组仓库、加 4 位 collaborator、建好 5 个分支并推送
- `CONTRIBUTIONS.md` 分工总表（已在 `main`）

### 待办步骤

1. **记录环境证据**（新建 `docs/env-evidence.md`）：贴出
   `java --version` / `mvn --version` / `git --version` 的输出，
   以及 `mvn test` 的 **10 tests / 1 failure** 完整结果 —— 这是「能运行 `mvn test`」这个 4 分项的凭据。
2. **README 加「工具与模型声明」**（指导书 6.3）：
   ```markdown
   ## 工具与模型声明
   - 工具：Claude Code（命令行）；IDE：<你的 IDE>
   - 模型：<填写会话中实际使用的模型名称>
   ```
3. **`transcripts/` 导出流程**（指导书 6.1，⚠️ 见下方"悬而未决"）：
   在**仓库根目录**启动 `claude`，随便问一个关于本项目的问题，然后
   ```bash
   ./tools/export-transcripts.sh      # Windows 请在 Git Bash 或 WSL 中运行
   ls transcripts/                    # 期望：Exported N session(s) to transcripts/
   ```
   脚本按「启动目录」记录会话，**必须在仓库根目录启动**才能被找到。
4. **提交 → push → 开 PR**，然后**由 M5 交叉 review**（见第八节矩阵）。
5. **全程守 main**：每次合并别人的 PR 后立刻 `git switch main && git pull && mvn test`，必须全绿才能让下一个人开工。
6. **最终验证**：5 个 PR 全部合并后，在 `main` 上跑一次 `mvn test`，结果给助教看。

### 完成判据
- [ ] `README.md` 有「工具与模型声明」段
- [ ] 环境证据已入库（含 10/1 的失败输出）
- [ ] `git status` 里 **不出现** `transcripts/`（`git check-ignore -v transcripts` 能查到忽略规则）
- [ ] `main` 最终 `mvn test` 全绿

---

## 四、M2 `huangjiasheng567` —— 里程碑 1 诊断与修复（关键路径）

**分支**：`fix/m1-root-cause`　**评分**：里程碑 1 **30 分**

> ⚠️ 本节的**结论**（根因到底是哪一层、哪个方法）必须由你自己追出来——指导书把评分点就设在这里。
> 下面只给流程和产出格式，不给答案。

### 前置
`mvn test` 稳定复现 `BookingServiceTest.sameSlotInDifferentRoomsAreBothConfirmed` 失败。

### 步骤

1. **（指导书 1.1）读懂失败信息**，不要改代码、不要直接让智能体"修绿"。回答三问：
   - 这个测试构造了什么场景？（几个房间、几个用户、什么时间段？）
   - 期望得到什么？实际得到什么？
   - 按核心不变式，期望的结果是否合理？（提示：不变式的范围限定词是「一个房间」）
2. **（1.2）沿调用链逐层追踪**，每到一层问：「这一层的代码，在它依赖的东西都正确的前提下，是正确的吗？」
   按这张表逐层读，并**如实填写观察结果**：

   | 层 | 要读的代码 | 要问自己的问题 |
   |---|---|---|
   | 测试 | `sameSlotInDifferentRoomsAreBothConfirmed` | 测试本身写对了吗？期望值符合不变式吗？ |
   | 服务层 | `BookingService.book` | 如果 `bookingsForRoom(room)` 真的只返回该房间的预订，这段逻辑会出错吗？ |
   | 仓储接口 | `BookingStore.bookingsForRoom` 的注释 | 接口**承诺**了什么？ |
   | 仓储实现 | `InMemoryBookingStore` 各方法 | 实现兑现承诺了吗？和同类方法对比有什么不一致？ |
   | 领域层 | `TimeInterval.overlaps` | 冲突判断规则对吗？（现有 5 个测试是否全过？） |

   仅靠阅读无法确定时，用取证手段（任选，指导书给了三种）：
   - 在 IDE 给 `book` 的循环打断点，调试模式跑失败测试，观察 `store.bookingsForRoom(room)` 实际返回了什么；
   - 临时加一行打印（**找到原因后必须删掉**）；
   - 直接给下层写个小测试：放入两个不同房间的预订，断言某个房间只拿回属于它的那一个。
3. **产出诊断记录**：新建 `docs/milestone1-diagnosis.md`，用指导书 1.2 的 5×4 表格：

   | 步骤 | 检查的文件 / 方法 | 观察到的事实 | 结论（排除/怀疑/确认） |
   |---|---|---|---|

4. **（1.3）在根因所在的那一层修复**。自问三句：
   - 我的修改落在契约被破坏的那一层了吗？还是只是让失败测试直接调用的方法"绕开"了问题？
   - 有没有别的公共方法也依赖同一个出问题的方法？它们现在行为对吗？
   - 修复是否与同一个类里类似方法的写法一致？
5. **（1.4）证明修复正确**：
   - 全套测试为绿（`Tests run: 10+, Failures: 0`）；
   - 能论证核心不变式仍成立（修复没有让"同一房间出现两个重叠的已确认预订"成为可能）；
   - **新增**一个回归测试把根因钉住：**修复前红、修复后绿**。可以新增到 `BookingServiceTest`
     （指导书给了骨架），但**只能新增测试方法，不得改动任何已有用例**。
6. **提交**（一个提交即可，但要一句话说清）：
   ```bash
   mvn test
   git status
   git add <修改的源文件> <新增/修改的测试文件> docs/milestone1-diagnosis.md
   git commit -m "Fix <根因所在的类/方法>: <一句话说明>; add regression test"
   git push -u origin fix/m1-root-cause
   gh pr create --base main --head fix/m1-root-cause --title "Fix root cause of sameSlotInDifferentRoomsAreBothConfirmed" --body "..."
   ```

### 完成判据（助教会追问这几条）
- [ ] 能指着文件和方法说出**真正的根因**，以及为什么报错位置 ≠ 出错位置
- [ ] 修复落在**根因所在的层**，而不是让失败测试绕开问题
- [ ] 全部测试为绿 + 能口头论证不变式成立
- [ ] 回归测试在修复前失败、修复后通过
- [ ] 能回答：「除了这个测试，还有哪些行为受到同一根因的影响？」

---

## 五、M3 `xing-ding` —— 里程碑 2 实现 `cancelBooking`

**分支**：`feat/cancel-booking`　**评分**：里程碑 2 实现 15 分
**前置**：M2 已合并，`main` 全绿。**（必须在全绿起点上开工）**

### 步骤

1. **（2.1）读 `TASK.md`**，把需求拆成可检验的规则（R1~R6，见指导书表格）。要点：**取消不只是删除**——房间里可能有人在候补名单上等。
2. **（2.3）第一轮提示词只要计划**（限定范围、指明依据、要求先给计划）：
   > 请阅读 `TASK.md`、`ARCHITECTURE.md` 以及 `service/` 与 `repo/` 下的代码。
   > 我需要在 `BookingService` 中实现 `cancelBooking(String bookingId)`。
   > 先不要修改任何文件：请用要点列出你的实现计划，
   > 特别说明你将如何处理候补名单晋升（先到先得、至多一人、不得造成重叠）。
   > 请把新方法放在 `listBookings` 方法之后。不要修改任何已有的测试。
3. **审计划**，确认它说清了晋升策略，再让它分步实现。**分两轮，对应两个小提交**：
   - **提交 1**：只做删除 + 无效 id 静默返回（R1~R3）
     `Add cancelBooking: remove booking, ignore unknown ids`
   - **提交 2**：加候补晋升（R4~R6：按 `seq` 先到先得、至多一人、跳过仍冲突者、不跨房间）
     `Promote earliest fitting waiter on cancellation`
4. **每轮先读 diff 再接受**，对照指导书 2.4 清单里属于实现的项：

   | 审查项 | 依据 |
   |---|---|
   | 找不到 id 时直接返回，不抛异常、不晋升 | R3 |
   | 先确定被取消预订所属房间，再只看该房间的候补名单 | R4、不跨房间 |
   | 按 `seq` **显式排序**，不依赖列表的偶然顺序 | R4 先到先得 |
   | 用 `TimeInterval.overlaps` 与**剩余的全部**已确认预订比较（不是只和被取消那条比） | R6、不变式 |
   | 找到第一个合适者后**立即停止** | R5 至多一人 |
   | 晋升时删除其候补记录，并创建**新 id** 的已确认预订 | R4 |
   | **没有**通过调用 `book(...)` 来"晋升"（会在冲突时重新进候补，产生重复记录并打乱顺序） | 副作用 |
   | 没有修改已有测试、没有无关的格式化或重命名 | 小而可审查 |

   常见"看起来对"的错误：只删不晋升 / 晋升第一个却不检查冲突 / 把所有能晋升的都晋升 / 只和被取消那条比冲突 / 依赖列表顺序。
5. **每次提交后 `mvn test` 都应为绿**，再 push 并开 PR。

### ⚠️ 两个硬约束
- **`cancelBooking` 必须放在 `listBookings` 之后**（类末尾）。若插在 `book` 与 `listBookings` 之间，
  M5 的 `git apply changes/agent-attempt.patch` 会因上下文变化而失败。
- **不得修改任何已有测试**。

### 完成判据
- [ ] 实现满足 R1~R6
- [ ] 2~3 个小提交，每个提交后 `mvn test` 为绿
- [ ] 每一行你都能解释（"智能体写的"不是理由）

---

## 六、M4 `Twh743` —— 里程碑 2 测试与 diff 审查

**分支**：`test/cancel-booking`　**评分**：里程碑 2 测试 + 提交纪律 20 分
**前置**：可与 M3 **并行**（你只新建 `CancelBookingTest.java`，不碰 M3 的文件）
**注意**：M3 合并前你的测试会红，这是正常的。

### 步骤

1. **（2.2）先设计场景、写清期望结果，再动手写代码**。填满指导书表格的"期望结果"列，放进 PR 描述：

   | 场景 | 操作 | 期望结果 |
   |---|---|---|
   | S1 基本取消 | Alice 订 A 房 [600,660)；取消它 | |
   | S2 无效 id | 有预订和候补者；取消一个不存在的 id | |
   | S3 晋升 | Alice 订 A 房 [600,660)；Bob 申请 [630,700) 进候补；取消 Alice | |
   | S4 先到先得 + 至多一人 | Alice 订 [600,720)；Bob 候补 [600,660)（先），Carol 候补 [660,700)（后）；取消 Alice | |
   | S5 跳过仍冲突者 | Alice 订 [600,660)，Dave 订 [700,760)；Bob 候补 [630,730)（先），Carol 候补 [610,650)（后）；取消 Alice | |
   | S6 不跨房间 | Alice 订 A 房；B 房有人候补；取消 Alice | |

2. **新建 `CancelBookingTest.java`**（也可写在 `BookingServiceTest` 中），覆盖 S1~S6。
   晋升测试骨架（指导书 2.5）：
   ```java
   @Test
   void cancellingPromotesTheWaitlistedUser() {
       BookingService svc = newService();
       BookingResult first = svc.book(roomA, alice, new TimeInterval(600, 660));
       Booking a = assertInstanceOf(BookingResult.Confirmed.class, first).booking();
       assertInstanceOf(BookingResult.Waitlisted.class,
           svc.book(roomA, bob, new TimeInterval(630, 700)));

       svc.cancelBooking(a.id());

       // TODO: 断言 roomA 现在恰好有 1 个预订，且它属于 bob、区间为 [630,700)
       // TODO（建议）: 断言 bob 已不在 roomA 的候补名单上
   }
   ```
   需要检查候补名单时，在测试里自己持有 store 引用：
   `store = new InMemoryBookingStore(); svc = new BookingService(store);` 然后 `store.waitlistForRoom(roomA)`。

3. **验证测试"有说服力"**（这是评分点，别跳过）：临时注释掉 M3 的晋升代码跑一次，
   **测试必须变红**。一个在"只删除不晋升"的实现上也能通过的测试是没有价值的。
4. **S5 是重点**——它直接守护核心不变式（跳过仍冲突的候补者），务必写。
5. **填写指导书 2.4 的 10 项审查清单**，把逐项结论写进 PR 描述（其中实现类项目与 M3 交叉验证）。
6. push 并开 PR。

### 完成判据
- [ ] `CancelBookingTest.java` 覆盖 S1~S6
- [ ] 晋升测试在"只删除"的实现上**会失败**（有实证）
- [ ] S5 覆盖成功
- [ ] PR 描述里有完整的 10 项审查清单结论
- [ ] M3 合并后你的测试全绿

---

## 七、M5 `1311948` —— 里程碑 3 识别错误的生成

**分支**：`review/agent-attempt`　**评分**：里程碑 3 **25 分**
**前置**：M2 已合并，`main` 全绿（指导书明确要求从全绿出发）

> 📌 **分支名映射**：指导书里写的是 `git switch -c agent-attempt` / `git merge agent-attempt`。
> 我们的分支已建好并推送为 **`review/agent-attempt`**（这样 GitHub 上可见、留痕更完整）。
> 照抄指导书命令时，把 `agent-attempt` 一律替换为 `review/agent-attempt`。

### 步骤

1. **（3.1）先读 diff**（`changes/agent-attempt.patch`，很短）。阅读时思考：
   - `ARCHITECTURE.md` 说 `overlaps` 是"这两个区间冲突吗"这条规则的**唯一所在**。这个补丁用了它吗？没用的话，它自己写的条件与 `overlaps` 等价吗？
   - 它的条件只关心已有预订的**哪一个端点**？
   - "能通过所有测试"说明了什么？现有测试里有任何一个调用了 `isAvailable` 吗？
2. **填九行判定表**：设查询区间固定 `Q = [600,660)`，对每种"已有预订 E 与 Q 的位置关系"，
   **先判断真实是否冲突，再手算补丁会返回什么**：

   | # | 位置关系 | 已有预订 E | 真实是否冲突 | 补丁的 `isAvailable` 返回 | 是否正确 |
   |---|---|---|---|---|---|
   | 1 | E 完全在 Q 之前 | [480,540) | | | |
   | 2 | E 在 Q 之前且首尾相接 | [540,600) | | | |
   | 3 | E 先开始，延伸进 Q | [570,630) | | | |
   | 4 | E 包含 Q | [540,720) | | | |
   | 5 | E 被 Q 包含 | [610,640) | | | |
   | 6 | E 在 Q 内开始，延伸出 Q | [630,720) | | | |
   | 7 | E 与 Q 完全相同 | [600,660) | | | |
   | 8 | E 在 Q 之后且首尾相接 | [660,720) | | | |
   | 9 | E 完全在 Q 之后 | [700,760) | | | |

3. **（3.2）在分支上试用补丁**：
   ```bash
   git switch main && mvn test                   # 确认从全绿出发
   git switch review/agent-attempt
   git apply changes/agent-attempt.patch
   git commit -am "Apply the agent's isAvailable change"
   mvn test                                       # 观察：现有测试是否全部通过？
   ```
   若因 M3 的改动导致 `patch does not apply`：按 diff 手工加进去，或 `git apply -3` 后手工解决冲突。
4. **（3.3）写一个"本该通过却失败"的测试**（新建 `IsAvailableTest.java`，骨架见指导书 3.3）：
   - 一个用例：让补丁的漏判暴露出来（对照第 2 步表格里你判为"不正确"的行）；
   - 一个用例 `isAvailableAgreesWithBook`：让 `isAvailable` 的回答与随后 `book(...)` 的实际结果相互印证——
     `isAvailable` 为 true 时 `book` 应得 `Confirmed`，否则应得 `Waitlisted`。
5. **先提交"红"的测试，再提交修复**——这样历史非常清楚：
   ```bash
   mvn test          # 确认这些新测试在补丁代码上失败
   git add IsAvailableTest.java && git commit -m "Add failing tests exposing isAvailable's wrong conflict check"
   ```
6. **（3.4）恢复：修复或拒绝**（二选一，都要留下理由）：

   | 选择 | 做法 | 分支操作 |
   |---|---|---|
   | **修复** | 让 `isAvailable` 复用领域层唯一的冲突规则 `TimeInterval.overlaps`，而不是自己再写一遍条件；全绿后提交 | `git switch main` → `git merge review/agent-attempt` |
   | **拒绝** | 判断该方法不值得保留（例如与 `book` 的判断逻辑重复，或"先查询再预订"本身有隐患） | `git switch main`，分支**留在原处不合并** |

7. push 并开 PR（若走拒绝路线，PR 描述里写明拒绝理由，PR 可关闭不合并）。

### 完成判据
- [ ] 能准确指出被判错的**位置关系**（哪几行），以及为什么违背不变式
- [ ] 有暴露问题的测试，且它在补丁代码上失败
- [ ] 修复已合入 `main` **或**拒绝理由充分且 `main` 未合入
- [ ] 能解释"为什么它能通过所有测试"

---

## 八、PR 交叉审查矩阵

每人的 PR **必须由别人 review**，这一层是 GitHub 原生留痕，别省。

| PR | 作者 | 必须由谁 review | 为什么 |
|---|---|---|---|
| `chore/env-setup` | M1 | **M5** | 红队视角检查环境证据是否可信 |
| `fix/m1-root-cause` | M2 | **M1** | 关键路径，集成者复核 |
| `feat/cancel-booking` | M3 | **M4** | M4 的测试就是 M3 实现的验证 |
| `test/cancel-booking` | M4 | **M3** | 反向检查测试是否真的能暴露"只删除" |
| `review/agent-attempt` | M5 | **M2** | 定根因的人最懂"唯一规则所在" |

**只有 M1 能推 `main`**，其他人一律「开分支 → push → 开 PR」。

---

## 九、完成判据总表（对照指导书第七节）

| 项目 | 必须展示 | 完成标准 | 负责人 |
|---|---|---|---|
| 里程碑 1 | 根因（文件 + 原因）；修复；正确性论证 | 修复落在根因所在层；全部测试为绿；能论证不变式成立；能讲清"到底哪里坏了" | M2 |
| 里程碑 2 | 提交序列；晋升测试 | 至少 2~3 个小提交；实现满足 R1~R6；测试能证明晋升且会在"只删除"实现上失败 | M3 + M4 |
| 里程碑 3 | 缺陷；发现方式；处理结果 | 准确指出被判错的位置关系；有暴露问题的测试；修复已合入 `main` 或拒绝理由充分 | M5 |
| 课前任务 | 本地 `transcripts/` 导出；README 声明 | 导出文件存在；README 中有工具与模型声明 | M1 |

---

## 十、悬而未决：`transcripts/` 是否提交

指导书第六节写明「**不要提交、不要推送**」，理由是 fork 是公开的、对话记录只供教学人员查看；
`.gitignore` 也据此忽略了 `transcripts/`。但老师要求"每人留痕"，两者可能冲突。

- **路径 A（指导书原规定）**：各自本地导出，**实验课上向助教出示**，不提交。
- **路径 B（提交进仓库）**：需 `git add -f` 绕过忽略规则。**本仓库是 public**，
  提交等于把全组的提示词、智能体回复、执行过的命令公开到可被检索的地方，且可能夹带个人信息/令牌。

**动手前先问老师。** 若只是"交给教学人员看"，把文件交到课程指定渠道（教学平台 / 私聊助教）
是更安全的等价做法，仓库里不放。当前 `transcripts/` 为空，这条**尚未阻塞任何工作**。
