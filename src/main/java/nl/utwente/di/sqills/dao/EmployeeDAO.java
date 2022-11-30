package nl.utwente.di.sqills.dao;

import nl.utwente.di.sqills.misc.C3P0;
import nl.utwente.di.sqills.model.Employee;
import nl.utwente.di.sqills.util.Employees;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public enum EmployeeDAO {
    INSTANCE;

    private Map<Long, Employee> employees;
    private long lastUpdateTime;

    EmployeeDAO() {
        employees = new HashMap<>();
        lastUpdateTime = 0;
        update();
    }

    /**
     * Updates the map if more than 1 second has passed from the last update.
     */
    private synchronized void update() {
        if (System.currentTimeMillis() - lastUpdateTime > 1000) {
            try (Connection connection = C3P0.INSTANCE.getConnection();
                 Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery("SELECT * FROM employee ORDER BY id;")) {
                Map<Long, Employee> employees = new HashMap<>();
                while (resultSet.next()) {
                    employees.put(resultSet.getLong(1), new Employee().setId(resultSet.getLong(1))
                            .setEmail(resultSet.getString(2)).setFirstName(resultSet.getString(3))
                            .setLastName(resultSet.getString(4)));
                }
                this.employees = employees;
                lastUpdateTime = System.currentTimeMillis();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @return all employees
     */
    @NotNull
    public List<Employee> getEmployees() {
        update();
        return employees.values().stream().sorted().collect(Collectors.toList());
    }

    /**
     * @param id id of employee
     * @return employee with given id or null
     */
    @Nullable
    public Employee getEmployee(long id) {
        update();
        return employees.get(id);
    }

    /**
     * @param email email of employee
     * @return employee with given email
     */
    @Nullable
    public Employee getEmployeeByEmail(@Nullable String email) {
        if (email == null) {
            return null;
        }
        update();
        return employees.values().stream().filter(Employees.filterByEmail(email)).findFirst().orElse(null);
    }
}
