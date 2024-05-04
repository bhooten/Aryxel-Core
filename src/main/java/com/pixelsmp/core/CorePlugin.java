package com.pixelsmp.core;

import com.pixelsmp.core.chat.ChatFormatter;
import com.pixelsmp.core.command.permissions.SetRankCommand;
import com.pixelsmp.core.database.sql.SQLConnectionManager;
import com.pixelsmp.core.listener.ChatEventListener;
import com.pixelsmp.core.listener.PlayerJoinListener;
import com.pixelsmp.core.listener.PlayerLeaveListener;
import com.pixelsmp.core.permissions.PermissionOrchestrator;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class CorePlugin extends JavaPlugin {
    private static CorePlugin instance;
    private static SQLConnectionManager sqlConnectionManager;
    private static PermissionOrchestrator permissionOrchestrator;

    @Override
    public void onEnable() {
        instance = this;

        Bukkit.getLogger().info(ChatFormatter.formatConsoleMessage("Plugin Manager",
                "Beginning initialization of PixelSMP Core...", false));

        // Attempt to read the configuration file and collect the necessary information
        File file = new File(getDataFolder(), "config.yml");

        // Check if file exists
        if(!file.exists()) {
            // Create the file
            saveDefaultConfig();

            // Since the Core does not have a database connection (since no config existed) -> explain and shut down
            Bukkit.getLogger().warning(ChatFormatter.formatConsoleMessage("Core", "Welcome! Because this" +
                    " is your first time using the PixelSMP Core, this is expected. Please navigate to your" +
                    " configuration file, set the appropriate parameters, then reboot.", true));
            Bukkit.getServer().shutdown();
        }

        // Load the configuration file
        FileConfiguration config = getConfig();

        // Initialize the MySQL connection orchestrator
        sqlConnectionManager = new SQLConnectionManager(
                config.getString("mysql.host"),
                config.getString("mysql.username"),
                config.getString("mysql.password"),
                config.getString("mysql.database"),
                config.getInt("mysql.port"),
                config.getInt("hikaricp.maximumPoolSize"),
                config.getLong("hikaricp.timeoutMillis"),
                config.getInt("threading.sql.maximumPoolSize")
        );

        // Verify the connection
        if(!sqlConnectionManager.isConnectionHealthy())
        {
            // Connection is not valid -- send error and shut down
            Bukkit.getLogger().warning(ChatFormatter.formatConsoleMessage("Core", "Database connection " +
                    "initialization failed. Please review stacktrace and try again.", true));
            Bukkit.getServer().shutdown();
            return;
        }

        // Connection is valid -- send success message
        Bukkit.getLogger().info(ChatFormatter.formatConsoleMessage("Core", "Database connection " +
                "initialized successfully!", false));

        permissionOrchestrator = new PermissionOrchestrator();

        // Register Event Listeners
        Bukkit.getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
        Bukkit.getServer().getPluginManager().registerEvents(new ChatEventListener(), this);
        Bukkit.getServer().getPluginManager().registerEvents(new PlayerLeaveListener(), this);

        // Register Commands
        getCommand("setrank").setExecutor(new SetRankCommand());

        // As of right now, that's all we have! We'll add more features later.
        // Send a successful startup notice
        Bukkit.getLogger().info(ChatFormatter.formatConsoleMessage("Plugin Manager",
                "PixelSMP Core has been successfully initialized!", false));
    }

    @Override
    public void onDisable() {
        Bukkit.getLogger().info(ChatFormatter.formatConsoleMessage("Plugin Manager",
                "Beginning deinitialization of PixelSMP Core...", false));

        // Close the connection to the database
        if(sqlConnectionManager != null)
        {
            sqlConnectionManager.disconnect();
        }

        // Send a successful shutdown notice
        Bukkit.getLogger().info(ChatFormatter.formatConsoleMessage("Plugin Manager",
                "PixelSMP Core has been successfully deinitialized!", false));
    }

    /**
     * Returns the SQL Connection Orchestrator.
     *
     * @author Bradley Hooten (bradleyah02@gmail.com)
     *
     * @since 1.0.0
     *
     * @return SQL Connection Orchestrator instance
     */
    public static SQLConnectionManager getSQLConnectionManager() {
        return sqlConnectionManager;
    }

    /**
     * Returns the Permission Orchestrator instance.
     *
     * @author Bradley Hooten (bradleyah02@gmail.com)
     *
     * @since 1.1.0
     *
     * @return Permission Orchestrator instance
     */
    public static PermissionOrchestrator getPermissionOrchestrator()
    {
        return permissionOrchestrator;
    }

    /**
     * Returns the Core Plugin instance.
     *
     * @author Bradley Hooten (bradleyah02@gmail.com)
     *
     * @since 1.1.0
     *
     * @return Core Plugin instance
     */
    public static CorePlugin getInstance() {
        return instance;
    }
}
