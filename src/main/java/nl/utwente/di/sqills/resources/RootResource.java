package nl.utwente.di.sqills.resources;

import nl.utwente.di.sqills.dao.RoomDAO;
import nl.utwente.di.sqills.resources.api.ApiResource;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;

@Path("/")
public class RootResource {
    @Context
    private HttpServletRequest httpServletRequest;
    @Context
    private ServletContext servletContext;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response doGet() {
        InputStream inputStream = servletContext.getResourceAsStream("overview.html");
        if (inputStream == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(inputStream).build();
    }

    @Path("room/{id}")
    public RoomResource getRoomResource(@PathParam("id") long id) {
        return new RoomResource(servletContext, RoomDAO.INSTANCE.getRoom(id));
    }

    @Path("login")
    public LoginResource getLoginResource() {
        return new LoginResource(httpServletRequest, servletContext);
    }

    @Path("administrator")
    public AdministratorResource getAdministratorResource() {
        return new AdministratorResource(httpServletRequest, servletContext);
    }

    @Path("api")
    public ApiResource getApiResource() {
        return new ApiResource(httpServletRequest);
    }
}
