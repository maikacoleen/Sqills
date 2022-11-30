package nl.utwente.di.sqills.resources;

import nl.utwente.di.sqills.model.Room;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class RoomResource {
    private ServletContext servletContext;
    private Room room;

    public RoomResource(ServletContext servletContext, Room room) {
        this.servletContext = servletContext;
        this.room = room;
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response doGet() {
        if (room == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        InputStream inputStream = servletContext.getResourceAsStream("room.html");
        if (inputStream == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
            StringBuilder stringBuilder = new StringBuilder();
            bufferedReader.lines().forEach(line -> stringBuilder.append(line.replace("{room.id}", Long.toString(room.getId())).replace("{room.name}", room.getName())));
            return Response.ok(stringBuilder.toString()).build();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Response.serverError().build();
    }
}
