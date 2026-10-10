package edu.cmu.cs214.booking.service;

import edu.cmu.cs214.booking.domain.Booking;
import edu.cmu.cs214.booking.domain.Room;
import edu.cmu.cs214.booking.domain.TimeInterval;
import edu.cmu.cs214.booking.domain.User;
import edu.cmu.cs214.booking.domain.WaitlistEntry;
import edu.cmu.cs214.booking.repo.BookingStore;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Coordinates bookings and the waitlist. Enforces the core invariant: a room
 * never holds two confirmed bookings whose intervals overlap. Persistence is
 * delegated to a {@link BookingStore}.
 */
public class BookingService {

    private final BookingStore store;
    private int nextBookingSeq = 1;
    private int nextWaitlistSeq = 1;

    public BookingService(BookingStore store) {
        this.store = store;
    }

    /**
     * Attempts to book {@code room} for {@code user} over {@code interval}. If the
     * room is free over that interval the booking is confirmed; otherwise the user
     * is placed on the room's waitlist.
     */
    public BookingResult book(Room room, User user, TimeInterval interval) {
        for (Booking existing : store.bookingsForRoom(room)) {
            if (existing.interval().overlaps(interval)) {
                int position = store.waitlistForRoom(room).size() + 1;
                int seq = nextWaitlistSeq++;
                store.addWaitlistEntry(new WaitlistEntry("w" + seq, room, user, interval, seq));
                return new BookingResult.Waitlisted(position);
            }
        }
        Booking booking = new Booking("b" + nextBookingSeq++, room, user, interval);
        store.addBooking(booking);
        return new BookingResult.Confirmed(booking);
    }


    /**
     * Reports whether {@code room} is free over {@code interval}, so callers can
     * check availability before attempting to book.
     */
    public boolean isAvailable(Room room, TimeInterval interval) {
        for (Booking b : store.bookingsForRoom(room)) {
            if (b.interval().start() >= interval.start() && b.interval().start() < interval.end()) {
                return false;
            }
        }
        return true;
    }

    /** Returns the confirmed bookings for {@code room}. */
    public List<Booking> listBookings(Room room) {
        return store.bookingsForRoom(room);
    }

    /**
     * Cancels the confirmed booking identified by {@code bookingId}, releasing the
     * time slot it held. If no booking with that id exists, this method does nothing.
     * After removal, the earliest waitlisted user whose interval fits the remaining
     * bookings of the same room is promoted to a confirmed booking.
     */
    public void cancelBooking(String bookingId) {
        Optional<Booking> booking = store.findBooking(bookingId);
        if (booking.isEmpty()) {
            return;
        }
        Room room = booking.get().room();
        store.removeBooking(bookingId);
        promoteFromWaitlist(room);
    }

    /**
     * Promotes at most one waitlisted user for {@code room}: the earliest (by seq)
     * waiter whose interval does not overlap any remaining confirmed booking.
     * Waiters who still conflict are skipped; if none fits, nobody is promoted.
     */
    private void promoteFromWaitlist(Room room) {
        List<WaitlistEntry> waiters = new ArrayList<>(store.waitlistForRoom(room));
        waiters.sort(Comparator.comparingInt(WaitlistEntry::seq));
        for (WaitlistEntry waiter : waiters) {
            boolean conflicts = false;
            for (Booking existing : store.bookingsForRoom(room)) {
                if (existing.interval().overlaps(waiter.interval())) {
                    conflicts = true;
                    break;
                }
            }
            if (!conflicts) {
                Booking promoted = new Booking(
                        "b" + nextBookingSeq++, room, waiter.user(), waiter.interval());
                store.addBooking(promoted);
                store.removeWaitlistEntry(waiter.id());
                break;
            }
        }
    }
}
