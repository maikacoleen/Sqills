package nl.utwente.di.sqills.dao;

import nl.utwente.di.sqills.misc.C3P0;
import nl.utwente.di.sqills.model.Room;
import nl.utwente.di.sqills.util.Rooms;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum RoomDAO {
    INSTANCE;

    private Map<Long, Room> rooms;
    private long lastUpdateTime;

    RoomDAO() {
        rooms = new HashMap<>();
        lastUpdateTime = 0;
        update();
    }

    /**
     * Updates the map if more than 1 second has passed from the last update.
     */
    private synchronized void update() {
        if (System.currentTimeMillis() - lastUpdateTime > 1000) {
            try (Connection connection = C3P0.INSTANCE.getConnection();
                 Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery("SELECT * FROM room ORDER BY id;")) {
                Map<Long, Room> rooms = new HashMap<>();
                while (resultSet.next()) {
                    rooms.put(resultSet.getLong(1), new Room().setId(resultSet.getLong(1))
                            .setName(resultSet.getString(2)));
                }
                this.rooms = rooms;
                lastUpdateTime = System.currentTimeMillis();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @return all rooms
     */
    @NotNull
    public List<Room> getRooms() {
        update();
        return new ArrayList<>(rooms.values());
    }

    /**
     * @param id id of room
     * @return room with given id or null
     */
    @Nullable
    public Room getRoom(long id) {
        update();
        return rooms.get(id);
    }

    /**
     * If given name contains a comma then the first part before the comma will be considered as name.
     * For example, "Lion, UTwente" -> "Lion".
     *
     * @param name name of room
     * @return room with given name or null
     */
    @Nullable
    public Room getRoomByName(@Nullable String name) {
        if (name == null) {
            return null;
        }
        update();
        if (name.contains(",")) {
            name = name.substring(0, name.indexOf(","));
        }
        return rooms.values().stream().filter(Rooms.filterByName(name)).findFirst().orElse(null);
    }
}
