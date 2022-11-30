package nl.utwente.di.sqills.util;

import nl.utwente.di.sqills.model.Room;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.function.Function;
import java.util.function.Predicate;

public class Rooms {
    /**
     * @param name name of room
     * @return predicate filtering rooms by name
     */
    @NotNull
    public static Predicate<Room> filterByName(@NotNull String name) {
        return room -> room.getName().equals(name);
    }

    /**
     * @return function mapping rooms to JSONObject
     */
    @NotNull
    public static Function<Room, JSONObject> mapToJSONObject() {
        return Room::toJSONObject;
    }
}
