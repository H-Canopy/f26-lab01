package edu.cmu.cs214.booking.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import edu.cmu.cs214.booking.domain.Room;
import edu.cmu.cs214.booking.domain.TimeInterval;
import edu.cmu.cs214.booking.domain.User;
import edu.cmu.cs214.booking.repo.InMemoryBookingStore;
import org.junit.jupiter.api.Test;

/**
 * Milestone 3: tests that expose the defect in the agent-generated
 * {@code isAvailable} method (changes/agent-attempt.patch).
 *
 * The patch checks only whether an existing booking's start endpoint falls inside
 * the queried interval, so it misses bookings that begin before the interval but
 * still overlap it. The correct conflict rule is the single one in the domain
 * layer: {@link TimeInterval#overlaps}. These tests are expected to fail on the
 * patched code and pass once {@code isAvailable} reuses {@code TimeInterval.overlaps}.
 */
class IsAvailableTest {

    private final Room roomA = new Room("A", "Alpha", 10);
    private final User alice = new User("u1", "Alice");
    private final User bob = new User("u2", "Bob");

    private BookingService newService() {
        return new BookingService(new InMemoryBookingStore());
    }

    /**
     * The patch's condition is {@code E.start() >= Q.start() && E.start() < Q.end()}.
     * For an existing booking that starts before the query interval but runs into
     * it (E = [570,630), Q = [600,660)), the patch reports available, even though
     * the intervals overlap and the room is genuinely not free.
     */
    @Test
    void notAvailableWhenAnEarlierBookingRunsIntoTheRequest() {
        BookingService svc = newService();
        svc.book(roomA, alice, new TimeInterval(570, 630));

        assertFalse(svc.isAvailable(roomA, new TimeInterval(600, 660)));
    }

    /**
     * Cross-checks {@code isAvailable} against the actual result of
     * {@code book(...)}: if the method says available, a subsequent book must be
     * confirmed; if it says unavailable, the book must be waitlisted. On the
     * patched code this fails because isAvailable claims the room is free while
     * book correctly waitlists the overlapping request.
     */
    @Test
    void isAvailableAgreesWithBook() {
        BookingService svc = newService();
        svc.book(roomA, alice, new TimeInterval(570, 630));

        boolean available = svc.isAvailable(roomA, new TimeInterval(600, 660));
        BookingResult result = svc.book(roomA, bob, new TimeInterval(600, 660));

        if (available) {
            assertInstanceOf(BookingResult.Confirmed.class, result);
        } else {
            assertInstanceOf(BookingResult.Waitlisted.class, result);
        }
    }
}
