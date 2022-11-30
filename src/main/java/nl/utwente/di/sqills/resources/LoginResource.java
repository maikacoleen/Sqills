package nl.utwente.di.sqills.resources;

import nl.utwente.di.sqills.dao.AccountDAO;
import nl.utwente.di.sqills.model.Account;
import nl.utwente.di.sqills.util.Accounts;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

public class LoginResource {
    private HttpServletRequest httpServletRequest;
    private ServletContext servletContext;

    public LoginResource(HttpServletRequest httpServletRequest, ServletContext servletContext) {
        this.httpServletRequest = httpServletRequest;
        this.servletContext = servletContext;
    }

    /**
     * @return login.html if session is not logged in as admin, otherwise redirects to /sqills/administrator
     * @throws 500 (internal server error) if URISyntaxException is thrown or
     *             404 (not found) if login.html is not found
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response doGet() {
        if (Accounts.isSessionAdmin(httpServletRequest.getSession().getId())) {
            try {
                return Response.seeOther(new URI("/sqills/administrator")).build();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            return Response.serverError().build();
        }
        InputStream inputStream = servletContext.getResourceAsStream("login.html");
        if (inputStream == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(inputStream).build();
    }

    /**
     * @param username username of account
     * @param password plain password of account
     * @return 204 (no content) if given password matches account with given username's password,
     * otherwise 401 (unauthorized) if account with given username was not found or
     * given password does not match or 500 (internal server error) if login failed
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response doPost(@FormParam("username") String username, @FormParam("password") String password) {
        Account account = AccountDAO.INSTANCE.getByUsername(username);
        if (account == null || !Accounts.isValidPassword(account, password)) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Either username or password is incorrect.")
                    .build();
        }
        if (!AccountDAO.INSTANCE.login(account.setSession(httpServletRequest.getSession().getId()))) {
            return Response.serverError().build();
        }
        return Response.noContent().build();
    }
}
