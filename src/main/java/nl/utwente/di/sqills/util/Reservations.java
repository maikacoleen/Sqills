package nl.utwente.di.sqills.util;

import nl.utwente.di.sqills.model.Reservation;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Function;
import java.util.function.Predicate;

public class Reservations {
    @NotNull
    public static Predicate<Reservation> filterByRoomId(long roomId) {
        return reservation -> roomId == 0 || reservation.getRoom().getId() == roomId;
    }

    @NotNull
    public static Predicate<Reservation> filterByEmployeeId(long employeeId) {
        return reservation -> reservation.getEmployee().getId() == employeeId;
    }

    @NotNull
    public static Predicate<Reservation> filterByDate(long date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        return reservation -> simpleDateFormat.format(new Date(reservation.getStartTime())).equals(simpleDateFormat.format(new Date(date)));
    }

    @NotNull
    public static Predicate<Reservation> matchOverlap(@NotNull Reservation reservation) {
        return r -> r.overlaps(reservation);
    }

    @NotNull
    public static Function<Reservation, JSONObject> mapToJSONObject(boolean isAdmin) {
        return reservation -> reservation.toJSONObject(isAdmin);
    }
}
