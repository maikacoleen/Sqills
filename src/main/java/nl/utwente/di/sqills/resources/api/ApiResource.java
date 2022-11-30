package nl.utwente.di.sqills.resources.api;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Path;

public class ApiResource {
    private HttpServletRequest httpServletRequest;

    public ApiResource(HttpServletRequest httpServletRequest) {
        this.httpServletRequest = httpServletRequest;
    }

    @Path("reservations")
    public ReservationsResource getReservationsResource() {
        return new ReservationsResource(httpServletRequest);
    }

    @Path("rooms")
    public RoomsResource getRoomsResource() {
        return new RoomsResource();
    }

    @Path("employees")
    public EmployeesResource getEmployeessResource() {
        return new EmployeesResource(httpServletRequest);
    }

    @Path("accounts")
    public AccountsResource getAccountsResource() {
        return new AccountsResource(httpServletRequest);
    }
}
