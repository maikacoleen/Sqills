package nl.utwente.di.sqills.util;

import nl.utwente.di.sqills.dao.AccountDAO;
import nl.utwente.di.sqills.model.Account;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.DatatypeConverter;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

public class Accounts {
    /**
     * @param token token of account
     * @return predicate filtering accounts by token
     */
    @NotNull
    public static Predicate<Account> filterByToken(@Nullable String token) {
        return account -> Objects.equals(account.getToken(), token);
    }

    /**
     * @param session session of account
     * @return predicate filtering accounts by session
     */
    @NotNull
    public static Predicate<Account> filterBySession(@Nullable String session) {
        return account -> Objects.equals(account.getSession(), session);
    }

    /**
     * @param employeeId employee id of account
     * @return predicate filtering accounts by employee id
     */
    @NotNull
    public static Predicate<Account> filterByEmployeeId(long employeeId) {
        return account -> account.getEmployee().getId() == employeeId;
    }

    /**
     * @return function mapping accounts to JSONObject
     */
    @NotNull
    public static Function<Account, JSONObject> mapToJSONObject() {
        return Account::toJSONObject;
    }

    /**
     * @param token token of account
     * @return true if account with given token is admin, otherwise false
     */
    public static boolean isTokenAdmin(@Nullable String token) {
        Account account = AccountDAO.INSTANCE.getAccountByToken(token);
        return account != null && account.isAdmin();
    }

    /**
     * @param session session of account
     * @return true if account with given session is admin, otherwise false
     */
    public static boolean isSessionAdmin(@Nullable String session) {
        Account account = AccountDAO.INSTANCE.getAccountBySession(session);
        return account != null && account.isAdmin();
    }

    /**
     * @param httpServletRequest HTTP request
     * @return true if given HTTP request is logged in as admin account, otherwise false
     */
    public static boolean isRequestAdmin(@NotNull HttpServletRequest httpServletRequest) {
        return isTokenAdmin(httpServletRequest.getHeader("Authorization")) || isSessionAdmin(httpServletRequest.getSession().getId());
    }

    /**
     * @param account  account to be checked for password
     * @param password plain password
     * @return true if given plain password matches given account's hashed password
     */
    public static boolean isValidPassword(Account account, String password) {
        try {
            byte[] bytes = DatatypeConverter.parseHexBinary(account.getPassword());
            byte[] bytes1 = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(new PBEKeySpec(password.toCharArray(), DatatypeConverter.parseHexBinary(account.getSalt()), account.getIteration(), bytes.length * 8)).getEncoded();
            int i = bytes.length ^ bytes1.length;
            for (int j = 0; j < bytes.length && j < bytes1.length; j++) {
                i |= bytes[j] ^ bytes1[j];
            }
            return i == 0;
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return false;
    }
}
