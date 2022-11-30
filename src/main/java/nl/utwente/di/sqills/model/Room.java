package nl.utwente.di.sqills.model;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.Objects;

public class Room implements Comparable<Room> {
    private long id;
    @NotNull
    private String name;

    public Room() {
        id = 0;
        name = "";
    }

    public long getId() {
        return id;
    }

    @NotNull
    public Room setId(long id) {
        this.id = id;
        return this;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public Room setName(@NotNull String name) {
        this.name = name;
        return this;
    }

    /**
     * @return room in JSON format
     */
    @NotNull
    public JSONObject toJSONObject() {
        return new JSONObject().put("id", getId()).put("name", getName());
    }

    @Override
    public int compareTo(@NotNull Room o) {
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Room)) return false;
        Room room = (Room) o;
        return getId() == room.getId() &&
                getName().equals(room.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName());
    }

    @Override
    public String toString() {
        return "Room{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
