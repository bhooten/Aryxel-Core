package com.pixelsmp.core;

import com.pixelsmp.core.chat.ChatFormatter;
import com.pixelsmp.core.database.sql.SQLConnectionManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class CorePlugin extends JavaPlugin {
    private SQLConnectionManager sqlConnectionManager;

    @Override
    public void onEnable() {
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
                config.getInt("mysql.port")
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
        Bukkit.getLogger().info(ChatFormatter.formatChatMessage("Plugin Manager",
                "PixelSMP Core has been successfully deinitialized!", false));
    }
}
