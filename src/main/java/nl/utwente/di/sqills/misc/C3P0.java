package nl.utwente.di.sqills.misc;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.SQLException;

public enum C3P0 {
    INSTANCE;

    private ComboPooledDataSource comboPooledDataSource;

    C3P0() {
        try {
            comboPooledDataSource = new ComboPooledDataSource();
            comboPooledDataSource.setDriverClass("org.postgresql.Driver");
            comboPooledDataSource.setJdbcUrl("jdbc:postgresql://castle.ewi.utwente.nl:5432/di031?currentSchema=sqills");
            comboPooledDataSource.setUser("di031");
            comboPooledDataSource.setPassword("pWBG+9Ce");
        } catch (PropertyVetoException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return a connection to the data source
     * @throws SQLException if a database access error occurs
     */
    public Connection getConnection() throws SQLException {
        return comboPooledDataSource.getConnection();
    }

    /**
     * Closes connection pool.
     */
    public void close() {
        comboPooledDataSource.close();
    }
}
