package nl.utwente.di.sqills.resources;

import nl.utwente.di.sqills.dao.AccountDAO;
import nl.utwente.di.sqills.dao.ReservationDAO;
import nl.utwente.di.sqills.dao.RoomDAO;
import nl.utwente.di.sqills.misc.CalendarClient;
import nl.utwente.di.sqills.model.Reservation;
import nl.utwente.di.sqills.model.Room;
import nl.utwente.di.sqills.resources.api.EmployeesResource;
import nl.utwente.di.sqills.resources.api.ReservationsResource;
import nl.utwente.di.sqills.resources.api.RoomsResource;
import nl.utwente.di.sqills.util.Accounts;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.stream.Collectors;

public class AdministratorResource {
    private HttpServletRequest httpServletRequest;
    private ServletContext servletContext;

    public AdministratorResource(HttpServletRequest httpServletRequest, ServletContext servletContext) {
        this.httpServletRequest = httpServletRequest;
        this.servletContext = servletContext;
    }

    /**
     * @return administrator.html if session is logged in as admin, otherwise redirects to /sqills/login
     * @throws 500 (internal server error) if URISyntaxException is thrown or
     *             404 (not found) if administrator.html is not found
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response doGet() {
        if (!Accounts.isSessionAdmin(httpServletRequest.getSession().getId())) {
            try {
                return Response.seeOther(new URI("/sqills/login")).build();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            return Response.serverError().build();
        }
        InputStream inputStream = servletContext.getResourceAsStream("administrator.html");
        if (inputStream == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(inputStream).build();
    }

    /**
     * @return 204 (no content) if logout was successful, otherwise 500 (internal server error)
     */
    @DELETE
    public Response doDelete() {
        if (!AccountDAO.INSTANCE.logout(AccountDAO.INSTANCE.getAccountBySession(httpServletRequest.getSession()
                .getId()))) {
            return Response.serverError().build();
        }
        return Response.noContent().build();
    }

    @Path("employees")
    public Employees getEmployees() {
        return new Employees();
    }

    @Path("reservations")
    public Reservations getReservations() {
        return new Reservations();
    }

    @Path("rooms")
    public Rooms getRooms() {
        return new Rooms();
    }

    public final class Employees {
        /**
         * @return array of employees in JSON format if request has admin privilege, otherwise 401 (unauthorized)
         * @see EmployeesResource#doGet()
         */
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public Response doGet() {
            if (!Accounts.isSessionAdmin(httpServletRequest.getSession().getId())) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
            return new EmployeesResource(httpServletRequest).doGet();
        }
    }

    public final class Reservations {
        /**
         * @param roomId room id of reservation (optional)
         * @return array of reservations in JSON format if request has admin privilege, otherwise 401 (unauthorized)
         */
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public Response doGet(@QueryParam("roomId") long roomId) {
            if (!Accounts.isSessionAdmin(httpServletRequest.getSession().getId())) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
            return new ReservationsResource(httpServletRequest).doGet(roomId);
        }

        /**
         * @param string reservation in JSON format
         * @return posted reservation if request has admin privilege, otherwise 401 (unauthorized)
         * @see ReservationsResource#doPost(String)
         */
        @POST
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        public Response doPost(String string) {
            if (!Accounts.isSessionAdmin(httpServletRequest.getSession().getId())) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
            return new ReservationsResource(httpServletRequest).doPost(string);
        }

        /**
         * @param string reservation in JSON format
         * @return put reservation if request has admin privilege,
         * otherwise 401 (unauthorized), 400 (bad request) if body is invalid, or
         * 500 (internal server error) if put failed
         */
        @PUT
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        public Response doPut(String string) {
            if (!Accounts.isSessionAdmin(httpServletRequest.getSession().getId())) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
            try {
                JSONObject jsonObject = new JSONObject(string);
                Reservation reservation = ReservationDAO.INSTANCE.getReservation(jsonObject.getString("id"));
                if (reservation == null) {
                    return Response.status(Response.Status.BAD_REQUEST).entity("reservation not found").build();
                }
                Room room = RoomDAO.INSTANCE.getRoom(jsonObject.getJSONObject("room").getLong("id"));
                if (room == null) {
                    return Response.status(Response.Status.BAD_REQUEST).entity("room not found").build();
                }
                reservation.setRoom(room);
                reservation.setStartTime(jsonObject.getLong("startTime"));
                reservation.setEndTime(jsonObject.getLong("endTime"));
                reservation.setAttendees(jsonObject.getJSONArray("attendees").toList().stream().map(o -> (String) o)
                        .collect(Collectors.toSet()));
                reservation.setTitle(jsonObject.getString("title"));
                reservation.setVisible(jsonObject.getBoolean("isVisible"));
                reservation = CalendarClient.INSTANCE.patchReservation(reservation);
                if (reservation == null) {
                    return Response.serverError().entity("server error").build();
                }
                return Response.ok(reservation.toJSONObject(true).toString()).build();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return Response.status(Response.Status.BAD_REQUEST).entity("invalid body").build();
        }

        /**
         * @param id id of reservation
         * @return 204 (no content) if request has admin privilege, otherwise 401 (unauthorized)
         */
        @DELETE
        public Response doDeleteReservations(@QueryParam("id") String id) {
            if (!Accounts.isSessionAdmin(httpServletRequest.getSession().getId())) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
            ReservationDAO.INSTANCE.deleteReservation(id);
            return Response.noContent().build();
        }
    }

    public final class Rooms {
        /**
         * @return array of rooms in JSON format if request has admin privilege, otherwise 401 (unauthorized)
         * @see RoomsResource#doGet()
         */
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public Response doGet() {
            if (!Accounts.isSessionAdmin(httpServletRequest.getSession().getId())) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
            return new RoomsResource().doGet();
        }
    }
}
