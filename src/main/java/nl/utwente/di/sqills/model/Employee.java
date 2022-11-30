package nl.utwente.di.sqills.model;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.Objects;

public class Employee implements Comparable<Employee> {
    private long id;
    @NotNull
    private String email;
    @NotNull
    private String firstName;
    @NotNull
    private String lastName;

    public Employee() {
        id = 0;
        email = "";
        firstName = "";
        lastName = "";
    }

    public long getId() {
        return id;
    }

    @NotNull
    public Employee setId(long id) {
        this.id = id;
        return this;
    }

    @NotNull
    public String getEmail() {
        return email;
    }

    @NotNull
    public Employee setEmail(@NotNull String email) {
        this.email = email;
        return this;
    }

    @NotNull
    public String getFirstName() {
        return firstName;
    }

    @NotNull
    public Employee setFirstName(@NotNull String firstName) {
        this.firstName = firstName;
        return this;
    }

    @NotNull
    public String getLastName() {
        return lastName;
    }

    @NotNull
    public Employee setLastName(@NotNull String lastName) {
        this.lastName = lastName;
        return this;
    }

    /**
     * @param isAdmin if true, hidden fields (id, email) are included
     * @return employee in JSON format
     */
    @NotNull
    public JSONObject toJSONObject(boolean isAdmin) {
        JSONObject jsonObject = new JSONObject().put("firstName", getFirstName()).put("lastName", getLastName());
        if (isAdmin) {
            jsonObject.put("id", getId()).put("email", getEmail());
        }
        return jsonObject;
    }

    @Override
    public int compareTo(@NotNull Employee o) {
        return Long.compare(getId(), o.getId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Employee)) return false;
        Employee employee = (Employee) o;
        return getId() == employee.getId() &&
                getEmail().equals(employee.getEmail()) &&
                getFirstName().equals(employee.getFirstName()) &&
                getLastName().equals(employee.getLastName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getEmail(), getFirstName(), getLastName());
    }

    @Override
    public String toString() {
        return "Employee{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                '}';
    }
}
