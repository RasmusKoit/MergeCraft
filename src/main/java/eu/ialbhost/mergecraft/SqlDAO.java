package eu.ialbhost.mergecraft;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class SqlDAO {
    private static HikariDataSource ds;
    private static HikariConfig config = new HikariConfig();

    static {
        config.setJdbcUrl("jdbc:mysql://eu.1.node.ialbhost.eu:3306/s14_mergecraft");
        config.setUsername("u14_JjdGCUZRel");
        config.setPassword("8+QOQuQON=CV^HZe0QNda1i1");
        config.addDataSourceProperty( "cachePrepStmts" , "true" );
        config.addDataSourceProperty( "prepStmtCacheSize" , "250" );
        config.addDataSourceProperty( "prepStmtCacheSqlLimit" , "2048" );
        config.setLeakDetectionThreshold(2000);
        config.setAutoCommit(true);
        ds = new HikariDataSource(config);
    }
    private SqlDAO(){}

    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    public static void closeConnection() {
        ds.close();
    }






}
