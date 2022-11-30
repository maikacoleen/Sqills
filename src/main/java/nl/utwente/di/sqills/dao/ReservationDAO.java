package nl.utwente.di.sqills.dao;

import nl.utwente.di.sqills.misc.C3P0;
import nl.utwente.di.sqills.misc.CalendarClient;
import nl.utwente.di.sqills.model.Reservation;
import nl.utwente.di.sqills.util.Reservations;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public enum ReservationDAO {
    INSTANCE;

    private final TimerTask timerTask;
    private Map<String, Reservation> reservations;
    private long lastUpdateTime;

    ReservationDAO() {
        reservations = new HashMap<>();
        lastUpdateTime = 0;
        timerTask = new TimerTask() {
            @Override
            public void run() {
                update();
            }
        };
    }

    /**
     * @return TimerTask for updating the map
     */
    @NotNull
    public TimerTask getTimerTask() {
        return timerTask;
    }

    /**
     * Updates the map if more than 1 second has passed from the last update.
     */
    private synchronized void update() {
        if (System.currentTimeMillis() - lastUpdateTime > 1000) {
            try (Connection connection = C3P0.INSTANCE.getConnection();
                 Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery("SELECT * FROM reservation WHERE end_time > clock_timestamp() ORDER BY start_time, room_id;")) {
                Map<String, Reservation> reservations = new HashMap<>();
                while (resultSet.next()) {
                    reservations.put(resultSet.getString(1), new Reservation().setId(resultSet.getString(1))
                            .setRoom(Objects.requireNonNull(RoomDAO.INSTANCE.getRoom(resultSet.getLong(2))))
                            .setStartTime(resultSet.getTimestamp(3).getTime())
                            .setEndTime(resultSet.getTimestamp(4).getTime())
                            .setEmployee(Objects.requireNonNull(EmployeeDAO.INSTANCE.getEmployee(resultSet.getLong(5))))
                            .setAttendees(new HashSet<>(Arrays.asList((String[]) resultSet.getArray(6).getArray())))
                            .setTitle(resultSet.getString(7)).setVisible(resultSet.getBoolean(8))
                            .setFromCalendar(resultSet.getBoolean(9)));
                }
                this.reservations = reservations;
                updateFromCalendar();
                this.reservations = this.reservations.entrySet().stream().sorted(Map.Entry.comparingByValue())
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                                (reservation, reservation2) -> reservation, LinkedHashMap::new));
                lastUpdateTime = System.currentTimeMillis();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Updates the map from the calendar.
     */
    private void updateFromCalendar() {
        Map<String, Reservation> reservations = CalendarClient.INSTANCE.getReservations();
        List<String> ids = new ArrayList<>();
        // Deletes reservations that are no longer on the calendar from the table.
        try (Connection connection = C3P0.INSTANCE.getConnection()) {
            boolean autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try (PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM reservation WHERE id = ?;")) {
                this.reservations.entrySet().stream().filter(entry -> !reservations.containsKey(entry.getKey())).forEach(reservation -> {
                    try {
                        preparedStatement.setString(1, reservation.getKey());
                        preparedStatement.addBatch();
                        ids.add(reservation.getKey());
                        System.out.printf("Deleted %s from the table.%n", reservation.getKey());
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                });
                preparedStatement.executeBatch();
            }
            connection.commit();
            connection.setAutoCommit(autoCommit);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        ids.forEach(id -> this.reservations.remove(id));
        try (Connection connection = C3P0.INSTANCE.getConnection()) {
            boolean autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try (PreparedStatement preparedUpdateStatement = connection.prepareStatement("UPDATE reservation SET room_id =?, start_time = ?, end_time = ?, title = ?, attendees = ?, is_visible = ? WHERE id = ?;");
                 PreparedStatement preparedInsertStatement = connection.prepareStatement("INSERT INTO reservation VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);")) {
                reservations.forEach((id, reservation) -> {
                    if (this.reservations.containsKey(id)) {
                        if (!this.reservations.get(id).equals(reservation)) {
                            if (!overlaps(reservation)) {
                                // Updates reservation in the table.
                                try {
                                    preparedUpdateStatement.setLong(1, reservation.getRoom().getId());
                                    preparedUpdateStatement.setTimestamp(2, new Timestamp(reservation.getStartTime()));
                                    preparedUpdateStatement.setTimestamp(3, new Timestamp(reservation.getEndTime()));
                                    preparedUpdateStatement.setString(4, reservation.getTitle());
                                    preparedUpdateStatement.setArray(5, connection.createArrayOf("text", reservation.getAttendees().toArray()));
                                    preparedUpdateStatement.setBoolean(6, reservation.isVisible());
                                    preparedUpdateStatement.setString(7, id);
                                    preparedUpdateStatement.addBatch();
                                    this.reservations.put(id, reservation);
                                    System.out.printf("Updated %s in the table.%n", id);
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                // Discard changes made to reservation in the calendar.
                                CalendarClient.INSTANCE.patchReservation(this.reservations.get(id));
                                System.out.printf("Patched %s in the calendar.%n", id);
                            }
                        }
                    } else {
                        if (!overlaps(reservation)) {
                            try {
                                // Inserts reservation into the table.
                                preparedInsertStatement.setString(1, reservation.getId());
                                preparedInsertStatement.setLong(2, reservation.getRoom().getId());
                                preparedInsertStatement.setTimestamp(3, new Timestamp(reservation.getStartTime()));
                                preparedInsertStatement.setTimestamp(4, new Timestamp(reservation.getEndTime()));
                                preparedInsertStatement.setLong(5, reservation.getEmployee().getId());
                                preparedInsertStatement.setArray(6, connection.createArrayOf("text", reservation.getAttendees().toArray()));
                                preparedInsertStatement.setString(7, reservation.getTitle());
                                preparedInsertStatement.setBoolean(8, reservation.isVisible());
                                preparedInsertStatement.setBoolean(9, reservation.isFromCalendar());
                                preparedInsertStatement.addBatch();
                                this.reservations.put(id, reservation);
                                System.out.printf("Inserted %s into the table.%n", id);
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        } else {
                            // Deletes reservation from the calendar.
                            CalendarClient.INSTANCE.deleteReservation(id);
                            System.out.printf("Deleted %s from the calendar.%n", id);
                        }
                    }
                });
                preparedUpdateStatement.executeBatch();
                preparedInsertStatement.executeBatch();
            }
            connection.commit();
            connection.setAutoCommit(autoCommit);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return all reservations
     */
    @NotNull
    public List<Reservation> getReservations() {
        return new ArrayList<>(reservations.values());
    }

    /**
     * @param roomId room id of reservations
     * @return reservations with given room id
     */
    @NotNull
    public List<Reservation> getReservationsByRoomId(long roomId) {
        return reservations.values().stream().filter(Reservations.filterByRoomId(roomId)).collect(Collectors.toList());
    }

    /**
     * @param employeeId employee id of reservations
     * @return reservations with given employee id
     */
    @NotNull
    public List<Reservation> getReservationsByEmployeeId(long employeeId) {
        return reservations.values().stream().filter(Reservations.filterByEmployeeId(employeeId))
                .collect(Collectors.toList());
    }

    /**
     * @param id id of reservation
     * @return reservation with given id or null
     */
    @Nullable
    public Reservation getReservation(@Nullable String id) {
        return reservations.get(id);
    }

    /**
     * Overview contains only the closest reservation on the same date as when this method was called for each room.
     * If room does not have any reservations, empty reservation (id = "") will be added.
     *
     * @return overview of reservations
     */
    @NotNull
    public List<Reservation> getOverview() {
        return RoomDAO.INSTANCE.getRooms().stream()
                .map(room -> getReservationsByRoomId(room.getId()).stream()
                        .filter(Reservations.filterByDate(System.currentTimeMillis())).findFirst()
                        .orElse(new Reservation().setRoom(room))).collect(Collectors.toList());
    }

    /**
     * Deletes reservation with given id.
     *
     * @param id id of reservation to be deleted
     */
    public void deleteReservation(@NotNull String id) {
        CalendarClient.INSTANCE.deleteReservation(id);
        reservations.remove(id);
    }

    /**
     * @param reservation reservation to be checked for overlap
     * @return true if given reservation overlaps with other reservations, otherwise false
     */
    public boolean overlaps(@NotNull Reservation reservation) {
        return reservations.values().stream().filter(Reservations.filterByRoomId(reservation.getRoom().getId()))
                .anyMatch(Reservations.matchOverlap(reservation));
    }
}
