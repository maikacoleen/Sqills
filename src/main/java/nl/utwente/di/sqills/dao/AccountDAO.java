package nl.utwente.di.sqills.dao;

import nl.utwente.di.sqills.misc.C3P0;
import nl.utwente.di.sqills.model.Account;
import nl.utwente.di.sqills.util.Accounts;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum AccountDAO {
    INSTANCE;

    private Map<String, Account> accounts;
    private long lastUpdateTime;

    AccountDAO() {
        accounts = new HashMap<>();
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
                 ResultSet resultSet = statement.executeQuery("SELECT * FROM account ORDER BY username;")) {
                Map<String, Account> accounts = new HashMap<>();
                while (resultSet.next()) {
                    accounts.put(resultSet.getString(1), new Account().setUsername(resultSet.getString(1))
                            .setPassword(resultSet.getString(2)).setSalt(resultSet.getString(3))
                            .setIteration(resultSet.getInt(4)).setAdmin(resultSet.getBoolean(5))
                            .setToken(resultSet.getString(6)).setSession(resultSet.getString(7))
                            .setEmployee(EmployeeDAO.INSTANCE.getEmployee(resultSet.getLong(8))));
                }
                this.accounts = accounts;
                lastUpdateTime = System.currentTimeMillis();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @return all accounts
     */
    @NotNull
    public List<Account> getAccounts() {
        update();
        return new ArrayList<>(accounts.values());
    }

    /**
     * @param username username of account
     * @return account with given username or null
     */
    @Nullable
    public Account getByUsername(@Nullable String username) {
        update();
        return accounts.get(username);
    }

    /**
     * @param token token of account
     * @return account with given token or null
     */
    @Nullable
    public Account getAccountByToken(@Nullable String token) {
        update();
        return accounts.values().stream().filter(Accounts.filterByToken(token)).findFirst().orElse(null);
    }

    /**
     * @param session session of account
     * @return account with given session or null
     */
    @Nullable
    public Account getAccountBySession(@Nullable String session) {
        update();
        return accounts.values().stream().filter(Accounts.filterBySession(session)).findFirst().orElse(null);
    }

    /**
     * @param employeeId employee id of account
     * @return account with given employee id
     */
    @Nullable
    public Account getAccountByEmployeeId(long employeeId) {
        update();
        return accounts.values().stream().filter(Accounts.filterByEmployeeId(employeeId)).findFirst().orElse(null);
    }

    /**
     * Account should have session.
     *
     * @param account account to be logged in
     * @return true if login was successful, otherwise false
     */
    public boolean login(@Nullable Account account) {
        if (account == null || !accounts.containsKey(account.getUsername()) || account.getSession() == null) {
            return false;
        }
        update();
        try (Connection connection = C3P0.INSTANCE.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("UPDATE account SET session = ? WHERE username = ?;")) {
            preparedStatement.setString(1, account.getSession());
            preparedStatement.setString(2, account.getUsername());
            preparedStatement.executeUpdate();
            accounts.get(account.getUsername()).setSession(account.getSession());
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Account should not have session.
     *
     * @param account account to be logged out
     * @return true if logout was successful, otherwise false
     */
    public boolean logout(@Nullable Account account) {
        if (account == null || !accounts.containsKey(account.getUsername())) {
            return false;
        }
        update();
        try (Connection connection = C3P0.INSTANCE.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("UPDATE account SET session = NULL WHERE username = ?;")) {
            preparedStatement.setString(1, account.getUsername());
            preparedStatement.executeUpdate();
            accounts.get(account.getUsername()).setSession(null);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
