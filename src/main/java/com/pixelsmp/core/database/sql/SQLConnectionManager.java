package com.pixelsmp.core.database.sql;

import com.pixelsmp.core.chat.ChatFormatter;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SQLConnectionManager
{
    private final String jdbcUrl;
    private final String hostname;
    private final String username;
    private final String password;
    private final String database;
    private final int port;
    private final ExecutorService executorService;
    private volatile Connection connection;
    private HikariConfig hikariConfig = new HikariConfig();
    private final HikariDataSource dataSource;

    /**
     * Initializes a new MySQLConnectionManager with the given connection parameters.
     *
     * @author Bradley Hooten (bradleyah02@gmail.com)
     *
     * @since 1.0.0
     *
     * @param hostname IP address or DNS name of the MySQL server
     * @param username Username to connect to the database
     * @param password Password to connect to the database
     * @param database Name of the database to connect to
     * @param port Port of the MySQL server
     */
    public SQLConnectionManager(String hostname, String username, String password, String database, int port) {
        this.hostname = hostname;
        this.username = username;
        this.password = password;
        this.database = database;
        this.port = port;
        this.jdbcUrl = "jdbc:mysql://" + hostname + ":" + port + "/" + database;
        this.executorService = Executors.newSingleThreadExecutor();

        // Set proper parameters on the HikariCP configuration
        hikariConfig.setJdbcUrl(jdbcUrl);
        hikariConfig.setUsername(username);
        hikariConfig.setPassword(password);
        hikariConfig.setMaximumPoolSize(10);
        hikariConfig.setConnectionTimeout(3000);

        dataSource = new HikariDataSource(hikariConfig);
    }

    /**
     * Connects to the database.
     * If the connection fails, an error message is printed to the console and the server is blocked from starting.
     *
     * @author Bradley Hooten (bradleyah02@gmail.com)
     *
     * @since 1.0.0
     */
    public boolean isConnectionHealthy() {
        try (Connection connection = dataSource.getConnection())
        {
            // Check if connection is healthy
            boolean healthy = connection.isValid(5);

            // Send a message to the console, depending on the result
            if(healthy)
            {
                Bukkit.getLogger().fine(ChatFormatter.formatChatMessage("Database Core",
                        "Database health check returned successfully.", false));
            }
            else
            {
                Bukkit.getLogger().warning(ChatFormatter.formatChatMessage("Database Core",
                        "Database health check failed.", true));
            }

            return healthy;
        }
        catch (SQLException e)
        {
            // Connection is clearly not healthy... lol
            return false;
        }
    }

    /**
     * Returns the connection to the database.
     *
     * @author Bradley Hooten (bradleyah02@gmail.com)
     *
     * @since 1.0.0
     *
     * @return Optional-wrapped Connection to the database
     */
    public Optional<HikariDataSource> getDataSource() {
        return Optional.ofNullable(dataSource);
    }

    /**
     * Closes the connection to the database.
     *
     * @author Bradley Hooten (bradleyah02@gmail.com)
     *
     * @since 1.0.0
     */
    public void disconnect() {
        if(dataSource != null)
        {
            dataSource.close();
            Bukkit.getLogger().info(ChatFormatter.formatChatMessage("Database Core", "Successfully closed HikariCP datasource.", false));
        }
    }
}
