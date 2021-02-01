package eu.ialbhost.mergecraft;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class SqlDAO {
    private static HikariDataSource ds;
    private static final HikariConfig config = new HikariConfig();

    static {
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.setLeakDetectionThreshold(2000);
        config.setAutoCommit(true);
    }

    private SqlDAO() {
    }

    public static void setDs() {
        ds = new HikariDataSource(config);
    }

    public static void setJdbcUrl(String url) {
        config.setJdbcUrl(url);
    }

    public static void setUsername(String username) {
        config.setUsername(username);
    }

    public static void setPassword(String password) {
        config.setPassword(password);
    }

    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    public static void closeConnection() {
        ds.close();
    }


}
