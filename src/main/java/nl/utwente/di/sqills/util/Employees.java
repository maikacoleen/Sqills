package nl.utwente.di.sqills.util;

import nl.utwente.di.sqills.model.Employee;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.function.Function;
import java.util.function.Predicate;

public class Employees {
    /**
     * @param email email of employee
     * @return predicate filtering employees by email
     */
    @NotNull
    public static Predicate<Employee> filterByEmail(@NotNull String email) {
        return employee -> employee.getEmail().equals(email);
    }

    /**
     * @param isAdmin if true, fields that require admin privilege will be included
     * @return function mapping employees to JSONObject
     */
    @NotNull
    public static Function<Employee, JSONObject> mapToJSONObject(boolean isAdmin) {
        return employee -> employee.toJSONObject(isAdmin);
    }
}
