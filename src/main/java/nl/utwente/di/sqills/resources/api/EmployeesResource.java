package nl.utwente.di.sqills.resources.api;

import nl.utwente.di.sqills.dao.EmployeeDAO;
import nl.utwente.di.sqills.util.Accounts;
import nl.utwente.di.sqills.util.Employees;
import org.json.JSONArray;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.stream.Collectors;

public class EmployeesResource {
    private HttpServletRequest httpServletRequest;

    public EmployeesResource(HttpServletRequest httpServletRequest) {
        this.httpServletRequest = httpServletRequest;
    }

    /**
     * @return array of employees in JSON format
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response doGet() {
        return Response.ok(new JSONArray(EmployeeDAO.INSTANCE.getEmployees().stream()
                .map(Employees.mapToJSONObject(Accounts.isRequestAdmin(httpServletRequest)))
                .collect(Collectors.toList())).toString()).build();
    }
}
