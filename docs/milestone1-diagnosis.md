# 里程碑 1 诊断记录 · M2（huangjiasheng567）

> 分支：`fix/m1-root-cause`　依据：实验指导书 §1.1–1.4
> 目标：找出 `BookingServiceTest.sameSlotInDifferentRoomsAreBothConfirmed` 失败的根本原因，
> 在根因所在层修复，并用回归测试把根因钉住。

---

## 1. 失败信息（§1.1 读懂失败信息）

- **测试构造的场景**：2 个房间（`roomA`、`roomB`）、2 个用户（Alice、Bob）、同一时间段 `[600,660)`。
  先在 A 房为 Alice 预订，再在 B 房为 Bob 预订同一个时段。
- **期望**：第二个 `book(roomB, bob, [600,660))` 返回 `BookingResult.Confirmed`。
- **实际**：返回 `BookingResult.Waitlisted`（被误判为与 A 房预订冲突）。
- **按核心不变式判断**：不变式的范围限定词是「**一个房间**」——「一个房间永远不会同时持有两个
  时间区间相互重叠的已确认预订」。两个不同房间在同一时段各有预订完全合法，因此**期望结果合理**，
  是系统实现出了问题，而不是测试或不变式的问题。

## 2. 沿调用链逐层追踪（§1.2）

| 步骤 | 检查的文件 / 方法 | 观察到的事实 | 结论（排除 / 怀疑 / 确认） |
|---|---|---|---|
| 1 | 测试 `sameSlotInDifferentRoomsAreBothConfirmed`（`BookingServiceTest`） | 场景、期望值均符合不变式；`book` 的入参房间不同（A vs B） | **排除**：测试本身正确 |
| 2 | 服务层 `BookingService.book` | 循环 `for (Booking existing : store.bookingsForRoom(room))` 用 `existing.interval().overlaps(interval)` 判冲突；若 `bookingsForRoom(room)` 真的只返回该房间的预订，这段逻辑不会把 B 房误判为冲突 | **排除**：服务层逻辑在契约兑现的前提下正确（先假设契约成立） |
| 3 | 仓储接口 `BookingStore.bookingsForRoom` 注释 | 接口承诺：「**The confirmed bookings held for {@code room}**」（只返回该房间持有的已确认预订） | 契约定义明确 |
| 4 | 仓储实现 `InMemoryBookingStore.bookingsForRoom` | 起始代码为 `return List.copyOf(bookings);` —— **返回全部预订，完全无视 `room` 参数**。对照：同类的 `waitlistForRoom` 却正确写了 `waitlist.stream().filter(w -> w.room().id().equals(room.id()))` | **确认**：契约在此被破坏（根因） |
| 5 | 领域层 `TimeInterval.overlaps` | `start < other.end && other.start < end`，半开区间、首尾相接不重叠；`TimeIntervalTest` 5 个用例全过 | **排除**：冲突规则正确 |

**取证交叉验证**（指导书允许任选，此处为直接针对下层的测试）：
新增的回归测试 `bookingsForRoomReturnsOnlyBookingsForThatRoom` 直接向仓储放入 A、B 两房的预订，
断言 `store.bookingsForRoom(roomA)` 只返回 1 条且属于 A 房。
- 在未修复代码上运行：`expected: <1> but was: <2>` —— 证实 `bookingsForRoom` 把 B 房预订也返回了。
- 在修复代码上运行：通过。

## 3. 根因结论

- **出错位置 ≠ 报错位置**。报错发生在 `BookingServiceTest` 的断言处（测试层），真正的错误在
  **仓储实现 `InMemoryBookingStore.bookingsForRoom`**：它没有兑现接口注释承诺的契约
  「只为指定房间返回已确认预订」，而是返回了全部预订。
- 服务层 `BookingService.book` 完全信任该契约（这是分层架构的既定前提），用返回结果做冲突检查，
  于是「不同房间同一时段」被误判为「同房间重叠」，把本该确认的预订放进了候补名单。
- 根因范围：**仅 `bookingsForRoom` 一个方法**。对照 `waitlistForRoom` 可以看出起始代码的缺陷是
  被故意埋在 `bookingsForRoom` 里的（`waitlistForRoom` 的过滤写法即修复的参照模板）。

## 4. 修复（§1.3，修复落在根因所在层）

文件：`src/main/java/edu/cmu/cs214/booking/repo/InMemoryBookingStore.java`

```java
@Override
public List<Booking> bookingsForRoom(Room room) {
    return bookings.stream().filter(b -> b.room().id().equals(room.id())).toList();
}
```

- **落在契约被破坏的那一层**：在实现里兑现接口承诺，而不是让服务层「绕开」问题（若在 `book`
  里加同房间判断，其他依赖同一契约的调用者仍会受害）。
- **与同类方法写法一致**：与 `waitlistForRoom` 的过滤方式完全相同（`w.room().id().equals(room.id())`），
  保持类的内部一致性。
- **语义一致**：返回不可变视图（`toList()`），与原来 `List.copyOf` 的不可变语义一致。

### 除了这个测试，还有哪些行为受同一根因影响？

| 受影响的行为 | 影响方式 | 修复后 |
|---|---|---|
| `BookingService.book` 的冲突检查 | 不同房间的预订被误判为重叠 → 错误进候补（本次失败测试即此症状） | 恢复正确 |
| `BookingService.listBookings(room)` | 会列出**所有房间**的预订，而不是只列该房间的（返回结果直接透传 `bookingsForRoom`） | 恢复正确 |
| 未来任何新调用 `bookingsForRoom` 的公共方法 | 同样会拿到错误的数据 | 恢复正确 |
| `BookingService.book` 的候补位置计算 `waitlistForRoom(room).size() + 1` | **不受影响**（`waitlistForRoom` 起始代码已正确按房间过滤） | 无需改动 |

## 5. 正确性论证（§1.4）

- **全部测试为绿**：`mvn clean test` → `Tests run: 11, Failures: 0, Errors: 0`（BUILD SUCCESS）。
- **核心不变式仍成立**：「一个房间永远不会同时持有两个时间区间相互重叠的已确认预订」。
  修复没有触碰 `BookingService.book` 的冲突判定逻辑，也没有触碰 `TimeInterval.overlaps`——
  冲突检查依旧只与**同房间**的已确认预订比较（现在 `bookingsForRoom` 如实只返回该房间的预订），
  因此「同一房间出现两个重叠已确认预订」仍然不可能发生。`bookWaitlistsWhenSlotIsTaken`、
  `backToBackBookingsAreConfirmed` 等既有用例继续通过，从行为上印证不变式未被破坏。

## 6. 回归测试（修复前红、修复后绿）

文件：`src/test/java/edu/cmu/cs214/booking/service/BookingServiceTest.java`
方法：`bookingsForRoomReturnsOnlyBookingsForThatRoom`（**仅新增方法，未改动任何既有用例**）

```java
@Test
void bookingsForRoomReturnsOnlyBookingsForThatRoom() {
    InMemoryBookingStore store = new InMemoryBookingStore();
    store.addBooking(new Booking("b1", roomA, alice, new TimeInterval(600, 660)));
    store.addBooking(new Booking("b2", roomB, bob, new TimeInterval(600, 660)));

    List<Booking> roomABookings = store.bookingsForRoom(roomA);
    assertEquals(1, roomABookings.size());
    assertEquals("b1", roomABookings.get(0).id());

    List<Booking> roomBBookings = store.bookingsForRoom(roomB);
    assertEquals(1, roomBBookings.size());
    assertEquals("b2", roomBBookings.get(0).id());
}
```

**实证**（在未修复的 `bookingsForRoom` 上运行）：

```
[ERROR] BookingServiceTest.sameSlotInDifferentRoomsAreBothConfirmed:53
        Unexpected type, expected: <...Confirmed> but was: <...Waitlisted>
[ERROR] BookingServiceTest.bookingsForRoomReturnsOnlyBookingsForThatRoom:74
        expected: <1> but was: <2>
[ERROR] Tests run: 11, Failures: 2, Errors: 0, Skipped: 0
```

修复后：`Tests run: 11, Failures: 0`。回归测试直接钉住缺陷层，在「修复前红、修复后绿」上可复现。

## 7. 环境备注

- pom.xml 要求 `<maven.compiler.release>21</maven.compiler.release>`；本机默认 `JAVA_HOME` 为
  JDK 17，会报 `release version 21 not supported`。实际构建使用已安装的 **JDK 25.0.2**
  （`C:\Program Files\Java\jdk-25.0.2`，支持 `--release 21`），`mvn clean test` 全绿。
- 构建命令（PowerShell）：
  ```powershell
  $env:JAVA_HOME = 'C:\Program Files\Java\jdk-25.0.2'
  $env:Path = 'C:\Program Files\Java\jdk-25.0.2\bin;' + $env:Path
  mvn clean test
  ```
