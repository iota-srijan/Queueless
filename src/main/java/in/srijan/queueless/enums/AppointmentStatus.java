package in.srijan.queueless.enums;

public enum AppointmentStatus {
    BOOKED,
    CANCELLED,
    COMPLETED,
    NO_SHOW;

    // A5: the only legal transitions. Anything not listed here is rejected.
    public boolean canMoveTo(AppointmentStatus next) {
        if (this != BOOKED) {
            return false;
        }
        return next == CANCELLED || next == COMPLETED || next == NO_SHOW;
    }
}
