package nl.utwente.di.sqills.resources.api;

import nl.utwente.di.sqills.dao.RoomDAO;
import nl.utwente.di.sqills.util.Rooms;
import org.json.JSONArray;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.stream.Collectors;

public class RoomsResource {
    /**
     * @return array of rooms in JSON format
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response doGet() {
        return Response.ok(new JSONArray(RoomDAO.INSTANCE.getRooms().stream().map(Rooms.mapToJSONObject())
                .collect(Collectors.toList())).toString()).build();
    }
}
