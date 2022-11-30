package nl.utwente.di.sqills.model;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Reservation implements Comparable<Reservation> {
    @NotNull
    private String id;
    @NotNull
    private Room room;
    private long startTime;
    private long endTime;
    @NotNull
    private Employee employee;
    @NotNull
    private Set<String> attendees;
    @NotNull
    private String title;
    private boolean visible;
    private boolean fromCalendar;

    public Reservation() {
        id = "";
        room = new Room();
        startTime = 0;
        endTime = 0;
        employee = new Employee();
        attendees = new HashSet<>();
        title = "Meeting";
        visible = true;
        fromCalendar = false;
    }

    @NotNull
    public String getId() {
        return id;
    }

    @NotNull
    public Reservation setId(@NotNull String id) {
        this.id = id;
        return this;
    }

    @NotNull
    public Room getRoom() {
        return room;
    }

    @NotNull
    public Reservation setRoom(@NotNull Room room) {
        this.room = room;
        return this;
    }

    public long getStartTime() {
        return startTime;
    }

    @NotNull
    public Reservation setStartTime(long startTime) {
        this.startTime = startTime;
        return this;
    }

    public long getEndTime() {
        return endTime;
    }

    @NotNull
    public Reservation setEndTime(long endTime) {
        this.endTime = endTime;
        return this;
    }

    @NotNull
    public Employee getEmployee() {
        return employee;
    }

    @NotNull
    public Reservation setEmployee(@NotNull Employee employee) {
        this.employee = employee;
        return this;
    }

    @NotNull
    public Set<String> getAttendees() {
        return attendees;
    }

    @NotNull
    public Reservation setAttendees(@NotNull Set<String> attendees) {
        this.attendees = attendees;
        return this;
    }

    @NotNull
    public String getTitle() {
        return title;
    }

    @NotNull
    public Reservation setTitle(@NotNull String title) {
        this.title = title;
        return this;
    }

    public boolean isVisible() {
        return visible;
    }

    @NotNull
    public Reservation setVisible(boolean visible) {
        this.visible = visible;
        return this;
    }

    public boolean isFromCalendar() {
        return fromCalendar;
    }

    @NotNull
    public Reservation setFromCalendar(boolean fromCalendar) {
        this.fromCalendar = fromCalendar;
        return this;
    }

    /**
     * @param reservation reservation to be checked for overlap
     * @return true if this reservation overlaps with given reservation, otherwise false
     */
    public boolean overlaps(@NotNull Reservation reservation) {
        return !getId().equals(reservation.getId()) && getRoom().equals(reservation.getRoom()) &&
                getStartTime() < reservation.getEndTime() && getEndTime() > reservation.getStartTime();
    }

    /**
     * @param isAdmin if true, hidden fields (id, attendees, isFromCalendar) and
     *                invisible fields (employee, attendeesCount, title) are included
     * @return reservation in JSON format
     */
    @NotNull
    public JSONObject toJSONObject(boolean isAdmin) {
        JSONObject jsonObject = new JSONObject().put("room", getRoom().toJSONObject()).put("startTime", getStartTime())
                .put("endTime", getEndTime()).put("isVisible", isVisible());
        if (isVisible() || isAdmin) {
            jsonObject.put("employee", getEmployee().toJSONObject(isAdmin))
                    .put("attendeesCount", getAttendees().size()).put("title", getTitle());
        }
        if (isAdmin) {
            jsonObject.put("id", getId()).put("attendees", new JSONArray(getAttendees()))
                    .put("isFromCalendar", isFromCalendar());
        }
        return jsonObject;
    }

    @Override
    public int compareTo(@NotNull Reservation o) {
        if (getStartTime() != o.getStartTime()) {
            return Long.compare(getStartTime(), o.getStartTime());
        }
        if (getEndTime() != o.getEndTime()) {
            return Long.compare(getEndTime(), o.getEndTime());
        }
        return getRoom().compareTo(o.getRoom());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Reservation)) return false;
        Reservation that = (Reservation) o;
        return getStartTime() == that.getStartTime() &&
                getEndTime() == that.getEndTime() &&
                isVisible() == that.isVisible() &&
                isFromCalendar() == that.isFromCalendar() &&
                getId().equals(that.getId()) &&
                getRoom().equals(that.getRoom()) &&
                getEmployee().equals(that.getEmployee()) &&
                getAttendees().equals(that.getAttendees()) &&
                getTitle().equals(that.getTitle());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getRoom(), getStartTime(), getEndTime(), getEmployee(), getAttendees(),
                getTitle(), isVisible(), isFromCalendar());
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "id='" + id + '\'' +
                ", room=" + room +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", employee=" + employee +
                ", attendees=" + attendees +
                ", title='" + title + '\'' +
                ", visible=" + visible +
                ", fromCalendar=" + fromCalendar +
                '}';
    }
}
