package com.pixelsmp.core.database.sql;

import com.pixelsmp.core.chat.ChatFormatter;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SQLConnectionManager
{
    private final ExecutorService executorService;
    private HikariDataSource dataSource;

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
    public SQLConnectionManager(String hostname, String username, String password, String database, int port,
                                int hikariMaximumPoolSize, long hikariTimeoutMillis, int threadPoolSize) {
        String jdbcUrl = "jdbc:mysql://" + hostname + ":" + port + "/" + database;
        executorService = Executors.newFixedThreadPool(threadPoolSize);

        // Set proper parameters on the HikariCP configuration
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(jdbcUrl);
        hikariConfig.setUsername(username);
        hikariConfig.setPassword(password);
        hikariConfig.setMaximumPoolSize(hikariMaximumPoolSize);
        hikariConfig.setConnectionTimeout(hikariTimeoutMillis);

        try
        {
            dataSource = new HikariDataSource(hikariConfig);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Performs a health check on the SQL connection and returns the health status.
     *
     * @author Bradley Hooten (bradleyah02@gmail.com)
     *
     * @since 1.0.0
     *
     * @return Boolean for health status; true = healthy, false = unhealthy
     */
    public boolean isConnectionHealthy() {
        if(dataSource == null)
        {
            return false;
        }

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
     * @return Connection to the database
     */
    public HikariDataSource getDataSource() {
        return dataSource;
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

    /**
     * Executes a query on the database asynchronously and returns a CompletableFuture containing the result.
     *
     * @author Bradley Hooten (bradleyah02@gmail.com)
     *
     * @since 1.0.0
     *
     * @param query The MySQL query to execute
     * @param handler The handler function to process the ResultSet
     * @return CompletableFuture containing the result of the query
     * @param <T> The type of the result
     */
    public <T> CompletableFuture<T> executeQueryAsync(String query, ResultSetHandlerFunction<T> handler,
                                                      Object... parameters) {
        return CompletableFuture.supplyAsync(() -> {
            // Try-with-resources block to handle the connection, statement, and result set
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(query))
            {
                // Set the parameters for the query
                for(int i = 0; i < parameters.length; i++)
                {
                    statement.setObject(i + 1, parameters[i]);
                }

                try(ResultSet resultSet = statement.executeQuery())
                {
                    // Call the handler function with the ResultSet
                    return handler.apply(resultSet);
                }
            }
            catch (SQLException e)
            {
                // Pass exception up the call stack to be handled by the caller
                throw new CompletionException(e);
            }
        }, executorService);
    }

    /**
     * Executes an update query on the database asynchronously.
     *
     * @author Bradley Hooten (bradleyah02@gmail.com)
     *
     * @since 1.0.0
     *
     * @param query The MySQL statement to execute
     *
     * @return CompletableFuture containing the result of the query; -1 = error, 0 = no rows affected, >0 = rows affected
     */
    public CompletableFuture<Integer> executeUpdateAsync(String query, Object... parameters) {
        return CompletableFuture.supplyAsync(() -> {
            // Initialize the result to -1 to fail-first
            int result = -1;

            // Try-with-resources block to handle the connection and statement
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(query))
            {
                // Set the parameters for the query
                for(int i = 0; i < parameters.length; i++)
                {
                    statement.setObject(i + 1, parameters[i]);
                }

                // Execute the query and store the result
                result = statement.executeUpdate();
            }
            catch (SQLException e)
            {
                // Pass exception up the call stack to be handled by the caller
                throw new CompletionException(e);
            }

            // Return the result of the query
            return result;
        }, executorService);
    }

    /**
     * This functional interface is used to handle the ResultSet from a query.
     *
     * @author Bradley Hooten (bradleyah02@gmail.com)
     *
     * @since 1.0.0
     *
     * @param <R> The type of the result
     */
    public interface ResultSetHandlerFunction<R>
    {
        R apply(ResultSet rs) throws SQLException;
    }
}