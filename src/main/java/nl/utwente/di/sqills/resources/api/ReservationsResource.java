package nl.utwente.di.sqills.resources.api;

import nl.utwente.di.sqills.dao.EmployeeDAO;
import nl.utwente.di.sqills.dao.ReservationDAO;
import nl.utwente.di.sqills.dao.RoomDAO;
import nl.utwente.di.sqills.misc.CalendarClient;
import nl.utwente.di.sqills.model.Employee;
import nl.utwente.di.sqills.model.Reservation;
import nl.utwente.di.sqills.model.Room;
import nl.utwente.di.sqills.util.Accounts;
import nl.utwente.di.sqills.util.Reservations;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ReservationsResource {
    private HttpServletRequest httpServletRequest;

    public ReservationsResource(HttpServletRequest httpServletRequest) {
        this.httpServletRequest = httpServletRequest;
    }

    /**
     * @param roomId room id of reservation (optional)
     * @return array of reservations in JSON format
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response doGet(@QueryParam("roomId") long roomId) {
        return Response.ok(new JSONArray(ReservationDAO.INSTANCE.getReservations().stream()
                .filter(Reservations.filterByRoomId(roomId))
                .map(Reservations.mapToJSONObject(Accounts.isRequestAdmin(httpServletRequest)))
                .collect(Collectors.toList())).toString()).build();
    }

    /**
     * @param string reservation in JSON format
     * @return posted reservation if post was successful,
     * otherwise 400 (bad request) with message if body is invalid or
     * 500 (internal server error) if post failed
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response doPost(String string) {
        try {
            JSONObject jsonObject = new JSONObject(string);
            Reservation reservation = new Reservation();
            Room room = RoomDAO.INSTANCE.getRoom(jsonObject.getJSONObject("room").getLong("id"));
            if (room == null) {
                return Response.status(Status.BAD_REQUEST).entity("room not found").build();
            }
            reservation.setRoom(room);
            reservation.setStartTime(jsonObject.getLong("startTime"));
            reservation.setEndTime(jsonObject.getLong("endTime"));
            Employee employee = EmployeeDAO.INSTANCE.getEmployeeByEmail(jsonObject.getJSONObject("employee")
                    .getString("email"));
            if (employee == null) {
                return Response.status(Status.BAD_REQUEST).entity("employee not found").build();
            }
            reservation.setEmployee(employee);
            Set<String> attendees = new HashSet<>();
            JSONArray jsonArray = jsonObject.getJSONArray("attendees");
            IntStream.range(0, jsonArray.length()).forEach(value -> {
                try {
                    attendees.add(jsonArray.getString(value));
                } catch (JSONException ignored) {
                }
            });
            reservation.setAttendees(attendees);
            reservation.setTitle(jsonObject.getString("title"));
            reservation.setVisible(jsonObject.getBoolean("isVisible"));
            reservation.setFromCalendar(false);
            if (ReservationDAO.INSTANCE.overlaps(reservation)) {
                return Response.status(Status.BAD_REQUEST).entity("overlap").build();
            }
            reservation = CalendarClient.INSTANCE.insertReservation(reservation);
            if (reservation == null) {
                return Response.serverError().entity("server error").build();
            }
            return Response.ok(reservation.toJSONObject(Accounts.isRequestAdmin(httpServletRequest)).toString()).build();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return Response.status(Status.BAD_REQUEST).entity("invalid body").build();
    }

    /**
     * @return overview of reservation in JSON format
     */
    @Path("overview")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response doGetOverview() {
        return Response.ok(new JSONArray(ReservationDAO.INSTANCE.getOverview().stream()
                .map(Reservations.mapToJSONObject(Accounts.isRequestAdmin(httpServletRequest)))
                .collect(Collectors.toList())).toString()).build();
    }
}
