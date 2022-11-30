package nl.utwente.di.sqills.resources.api;

import nl.utwente.di.sqills.dao.AccountDAO;
import nl.utwente.di.sqills.util.Accounts;
import org.json.JSONArray;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.stream.Collectors;

public class AccountsResource {
    private HttpServletRequest httpServletRequest;

    public AccountsResource(HttpServletRequest httpServletRequest) {
        this.httpServletRequest = httpServletRequest;
    }

    /**
     * @return array of accounts in JSON format if request has admin privilege, otherwise 401 (unauthorized)
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response doGet() {
        if (!Accounts.isRequestAdmin(httpServletRequest)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        return Response.ok(new JSONArray(AccountDAO.INSTANCE.getAccounts().stream().map(Accounts.mapToJSONObject())
                .collect(Collectors.toList())).toString()).build();
    }
}
