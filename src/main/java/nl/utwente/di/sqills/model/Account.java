package nl.utwente.di.sqills.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.Objects;

public class Account implements Comparable<Account> {
    @NotNull
    private String username;
    @NotNull
    private String password;
    @NotNull
    private String salt;
    private int iteration;
    private boolean admin;
    @Nullable
    private String token;
    @Nullable
    private String session;
    @Nullable
    private Employee employee;

    public Account() {
        username = "";
        password = "";
        salt = "";
        iteration = 1000;
        admin = false;
        token = "";
    }

    @NotNull
    public String getUsername() {
        return username;
    }

    @NotNull
    public Account setUsername(@NotNull String username) {
        this.username = username;
        return this;
    }

    @NotNull
    public String getPassword() {
        return password;
    }

    @NotNull
    public Account setPassword(@NotNull String password) {
        this.password = password;
        return this;
    }

    @NotNull
    public String getSalt() {
        return salt;
    }

    @NotNull
    public Account setSalt(@NotNull String salt) {
        this.salt = salt;
        return this;
    }

    public int getIteration() {
        return iteration;
    }

    @NotNull
    public Account setIteration(int iteration) {
        this.iteration = iteration;
        return this;
    }

    public boolean isAdmin() {
        return admin;
    }

    @NotNull
    public Account setAdmin(boolean admin) {
        this.admin = admin;
        return this;
    }

    @Nullable
    public String getToken() {
        return token;
    }

    @NotNull
    public Account setToken(@Nullable String token) {
        this.token = token;
        return this;
    }

    @Nullable
    public String getSession() {
        return session;
    }

    @NotNull
    public Account setSession(@Nullable String session) {
        this.session = session;
        return this;
    }

    @Nullable
    public Employee getEmployee() {
        return employee;
    }

    @NotNull
    public Account setEmployee(@Nullable Employee employee) {
        this.employee = employee;
        return this;
    }

    /**
     * Only requests with admin privilege have access to this method.
     *
     * @return account in JSON format
     */
    @NotNull
    public JSONObject toJSONObject() {
        JSONObject jsonObject = new JSONObject().put("username", getUsername()).put("isAdmin", isAdmin());
        if (getEmployee() != null) {
            jsonObject.put("employee", getEmployee().toJSONObject(true));
        }
        return jsonObject;
    }

    @Override
    public int compareTo(@NotNull Account o) {
        return getUsername().compareTo(o.getUsername());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Account)) return false;
        Account account = (Account) o;
        return getIteration() == account.getIteration() &&
                isAdmin() == account.isAdmin() &&
                getUsername().equals(account.getUsername()) &&
                getPassword().equals(account.getPassword()) &&
                getSalt().equals(account.getSalt());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUsername(), getPassword(), getSalt(), getIteration(), isAdmin());
    }

    @Override
    public String toString() {
        return "Account{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", salt='" + salt + '\'' +
                ", iteration=" + iteration +
                ", admin=" + admin +
                ", token='" + token + '\'' +
                ", session='" + session + '\'' +
                ", employee=" + employee +
                '}';
    }
}
