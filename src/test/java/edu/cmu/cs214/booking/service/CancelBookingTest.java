package edu.cmu.cs214.booking.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.cmu.cs214.booking.domain.Booking;
import edu.cmu.cs214.booking.domain.Room;
import edu.cmu.cs214.booking.domain.TimeInterval;
import edu.cmu.cs214.booking.domain.User;
import edu.cmu.cs214.booking.repo.InMemoryBookingStore;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link BookingService#cancelBooking(String)}, scenarios S1..S6 from
 * the lab handout §2.2: basic cancellation, unknown ids, waitlist promotion,
 * first-come-first-served with at-most-one promotion, skipping a waiter who
 * still conflicts, and room isolation.
 *
 * <p>The promotion tests (S3, S4, S5) intentionally fail on a "delete-only"
 * implementation: they assert that a waitlisted user ends up holding a
 * confirmed booking after the cancellation.
 */
class CancelBookingTest {

    private final Room roomA = new Room("A", "Alpha", 10);
    private final Room roomB = new Room("B", "Beta", 4);
    private final User alice = new User("u1", "Alice");
    private final User bob = new User("u2", "Bob");
    private final User carol = new User("u3", "Carol");
    private final User dave = new User("u4", "Dave");

    private final InMemoryBookingStore store = new InMemoryBookingStore();
    private final BookingService svc = new BookingService(store);

    /** Books and asserts the booking is confirmed, returning the Booking. */
    private Booking confirmed(Room room, User user, int start, int end) {
        BookingResult r = svc.book(room, user, new TimeInterval(start, end));
        return assertInstanceOf(BookingResult.Confirmed.class, r).booking();
    }

    /** Books and asserts the booking is waitlisted. */
    private void waitlisted(Room room, User user, int start, int end) {
        BookingResult r = svc.book(room, user, new TimeInterval(start, end));
        assertInstanceOf(BookingResult.Waitlisted.class, r);
    }

    // S1 基本取消：删除该预订，释放时段
    @Test
    void s1BasicCancelRemovesTheBooking() {
        Booking a = confirmed(roomA, alice, 600, 660);

        svc.cancelBooking(a.id());

        assertTrue(store.bookingsForRoom(roomA).isEmpty());
    }

    // S2 无效 id：有预订和候补者；取消不存在的 id，什么都不变
    @Test
    void s2UnknownIdDoesNothing() {
        Booking a = confirmed(roomA, alice, 600, 660);
        waitlisted(roomA, bob, 630, 700);

        svc.cancelBooking("does-not-exist");

        assertEquals(1, store.bookingsForRoom(roomA).size());
        assertEquals(1, store.waitlistForRoom(roomA).size());
    }

    // S3 晋升：取消后最早合适候补者转为已确认预订
    @Test
    void s3CancellingPromotesTheWaitlistedUser() {
        Booking a = confirmed(roomA, alice, 600, 660);
        waitlisted(roomA, bob, 630, 700);

        svc.cancelBooking(a.id());

        assertEquals(1, store.bookingsForRoom(roomA).size());
        Booking promoted = store.bookingsForRoom(roomA).get(0);
        assertEquals(bob, promoted.user());
        assertEquals(new TimeInterval(630, 700), promoted.interval());
        assertTrue(store.waitlistForRoom(roomA).isEmpty());
    }

    // S4 先到先得 + 至多一人：只晋升 seq 最早的 Bob，Carol 仍留在候补名单
    @Test
    void s4FcfsPromotesOnlyTheEarliestFittingWaiter() {
        Booking a = confirmed(roomA, alice, 600, 720);
        waitlisted(roomA, bob, 600, 660);   // 先到（seq 1）
        waitlisted(roomA, carol, 660, 700); // 后到（seq 2）

        svc.cancelBooking(a.id());

        assertEquals(1, store.bookingsForRoom(roomA).size());
        Booking promoted = store.bookingsForRoom(roomA).get(0);
        assertEquals(bob, promoted.user());
        assertEquals(new TimeInterval(600, 660), promoted.interval());
        assertEquals(1, store.waitlistForRoom(roomA).size());
        assertEquals(carol, store.waitlistForRoom(roomA).get(0).user());
    }

    // S5 跳过仍冲突者：Bob 仍与 Dave 冲突被跳过，Carol 合适则被晋升 —— 守护核心不变式
    @Test
    void s5SkipsConflictingWaiterAndPromotesTheNextFittingOne() {
        Booking a = confirmed(roomA, alice, 600, 660);
        confirmed(roomA, dave, 700, 760);
        waitlisted(roomA, bob, 630, 730);   // seq 1：取消 Alice 后仍与 Dave 冲突
        waitlisted(roomA, carol, 610, 650); // seq 2：Alice 取消后无冲突

        svc.cancelBooking(a.id());

        // Dave 仍确认；Carol 被晋升；Bob 被跳过仍候补
        assertEquals(2, store.bookingsForRoom(roomA).size());
        assertTrue(store.bookingsForRoom(roomA).stream()
                .anyMatch(b -> b.user().equals(carol)));
        assertEquals(1, store.waitlistForRoom(roomA).size());
        assertEquals(bob, store.waitlistForRoom(roomA).get(0).user());
    }

    // S6 不跨房间：取消 A 房不影响 B 房的候补
    @Test
    void s6CancellationInOneRoomDoesNotPromoteAnotherRoom() {
        Booking d = confirmed(roomB, dave, 600, 660);
        waitlisted(roomB, bob, 630, 700);   // Bob 在 B 房候补
        Booking a = confirmed(roomA, alice, 600, 660);

        svc.cancelBooking(a.id());

        assertTrue(store.bookingsForRoom(roomA).isEmpty());
        // B 房原样：Dave 仍确认，Bob 仍候补
        assertEquals(1, store.bookingsForRoom(roomB).size());
        assertEquals(d, store.bookingsForRoom(roomB).get(0));
        assertEquals(1, store.waitlistForRoom(roomB).size());
        assertEquals(bob, store.waitlistForRoom(roomB).get(0).user());
    }
}
