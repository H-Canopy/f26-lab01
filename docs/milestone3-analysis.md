# 里程碑 3 · 审查记录（M5 红队）

> 审查对象：`changes/agent-attempt.patch` 中新增的 `BookingService.isAvailable(Room, TimeInterval)`。
> 结论：**补丁存在缺陷，采取修复路线**。修复已合入本分支，PR #9 待 M1 合并。

## 3.1 九行判定表

查询区间固定为 `Q = [600, 660)`。对每种"已有预订 E 与 Q 的位置关系"，先判断**真实是否冲突**
（依据领域层唯一冲突规则 `TimeInterval.overlaps`：`E.start() < Q.end() && Q.start() < E.end()`），
再手算补丁的 `isAvailable` 会返回什么。

补丁的条件是：`E.start() >= Q.start() && E.start() < Q.end()`，即**只检查 E 的开始端点是否落在 Q 内**。

| # | 位置关系 | 已有预订 E | 真实是否冲突 | 补丁的 isAvailable 返回 | 是否正确 |
|---|---|---|---|---|---|
| 1 | E 完全在 Q 之前 | [480,540) | 否（600 < 540 为假） | true | 正确 |
| 2 | E 在 Q 之前且首尾相接 | [540,600) | 否（600 < 600 为假，半开区间） | true | 正确 |
| 3 | E 先开始，延伸进 Q | [570,630) | **是**（570<660 且 600<630） | **true（漏判！）** | **错误** |
| 4 | E 包含 Q | [540,720) | **是**（540<660 且 600<720） | **true（漏判！）** | **错误** |
| 5 | E 被 Q 包含 | [610,640) | 是（610<660 且 600<640） | false | 正确 |
| 6 | E 在 Q 内开始，延伸出 Q | [630,720) | 是（630<660 且 600<720） | false | 正确 |
| 7 | E 与 Q 完全相同 | [600,660) | 是（600<660 且 600<660） | false | 正确 |
| 8 | E 在 Q 之后且首尾相接 | [660,720) | 否（660<660 为假） | true | 正确 |
| 9 | E 完全在 Q 之后 | [700,760) | 否（700<660 为假） | true | 正确 |

**判错的行：第 3、4 行。** 这两类位置关系的共同点是 **E 的开始端点早于 Q 的开始端点**
（`E.start() < Q.start()`），但 E 的区间仍与 Q 重叠。补丁只检查 `E.start()` 这一个端点，
自然永远漏掉"E 先于 Q 开始、却仍延伸进/覆盖 Q"的情形。

## 为什么补丁违背核心不变式

核心不变式：**一个房间永远不会同时持有两个时间区间相互重叠的已确认预订。**

`isAvailable` 的语义是"可用性查询"——它应当回答"在这个区间预订是否会与已有预订冲突"。
补丁在第 3、4 行场景下返回 `true`（声称可用），但实际 `book(...)` 会因冲突而把请求放入候补名单
（返回 `Waitlisted`）。因此调用者若信任 `isAvailable` 的返回值，就会对"不可用"的时段做出错误判断——
这正是对"可用性=无重叠"这一规则的破坏。

## 为什么补丁能通过所有测试

现有测试（`TimeIntervalTest` 5 个 + `BookingServiceTest` 6 个）**没有任何一个调用 `isAvailable`**。
补丁新增的方法对既有行为零影响，因此全部 11 个测试照常通过。"能通过所有测试"只能说明
`isAvailable` 没有被覆盖，不能说明它正确——这是典型的"测试盲区掩盖缺陷"。

## 3.2 试用补丁的实证

在基于最新 main 重建的 `review/agent-attempt` 分支上执行 `git apply changes/agent-attempt.patch`
后运行 `mvn test`：**Tests run: 11, Failures: 0**（补丁可应用、现有测试全过）。

## 3.3 暴露问题的测试

新增 `IsAvailableTest.java`（在补丁代码上**失败**，修复后**通过**）：

- `notAvailableWhenAnEarlierBookingRunsIntoTheRequest`：已有 `[570,630)`（第 3 行场景），
  断言 `isAvailable(roomA, [600,660))` 为 `false`。补丁返回 `true` → **红**。
- `isAvailableAgreesWithBook`：`isAvailable` 返回与随后 `book(...)` 的实际结果互证。
  补丁场景下 `isAvailable` 报可用、`book` 却 `Waitlisted` → **红**。

实证（补丁代码上）：`Tests run: 13, Failures: 2`（失败均为 `IsAvailableTest`）。

## 3.4 修复

让 `isAvailable` **复用领域层唯一的冲突规则 `TimeInterval.overlaps`**，而不是自己重写条件：

```java
public boolean isAvailable(Room room, TimeInterval interval) {
    for (Booking b : store.bookingsForRoom(room)) {
        if (b.interval().overlaps(interval)) {
            return false;
        }
    }
    return true;
}
```

修复后 `mvn test`：**Tests run: 13, Failures: 0**，全部通过。

## 提交序列

```
47527e2 Apply the agent's isAvailable change                          （应用补丁）
c6fcb7b Add failing tests exposing isAvailable's wrong conflict check （红测试）
9218b96 Fix isAvailable: reuse TimeInterval.overlaps                 （修复，全绿）
```

PR #9：https://github.com/H-Canopy/f26-lab01/pull/9 （依交叉审查矩阵请 M2 审查；本仓库规定仅 M1 能推 main）
