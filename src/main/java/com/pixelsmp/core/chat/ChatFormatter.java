package com.pixelsmp.core.chat;

import org.bukkit.ChatColor;

public class ChatFormatter
{
    public static final String NO_PERMISSIONS_ERROR = formatChatMessage("Permissions",
            "You do not have permission to execute this command.", true);
    public static final String UNKNOWN_ERROR = formatChatMessage("Core",
            "We were unable to process your request due to an unknown error.", true);

    /**
     * This method formats a chat message with the given sender and message.
     *
     * @author Bradley Hooten (bradleyah02@gmail.com)
     *
     * @since 1.0.0
     *
     * @param sender The sending service to present to the player; for example, "Punishments" or "Chat"
     * @param message The message to send to the player
     * @param error Whether the message is an error message
     *
     * @return The formatted chat message
     */
    public static String formatChatMessage(String sender, String message, boolean error)
    {
        // Check if the message is supposed to be an error
        if(error)
        {
            // Effective Format is &4&lERROR &r&8// &7{sender} &8// &7{message}
            return ChatColor.translateAlternateColorCodes('&', "&4&LERROR &R&8// &7" +
                    sender + " &8// &7" + message);
        }
        else
        {
            // Effective Format is &3&l{sender} &r&8// &7{message}
            return ChatColor.translateAlternateColorCodes('&', "&3&L" +
                    sender + "&R &8// &7" + message);
        }
    }

    /**
     * This method formats a console (plaintext) message with the given sender and message.
     *
     * @author Bradley Hooten (bradleyah02@gmail.com)
     *
     * @since 1.0.0
     *
     * @param sender The sending service to present to the console; for example, "Console" or "Chat"
     * @param message The message to send to the console
     * @param error Whether the message is an error message
     *
     * @return The formatted chat message
     */
    public static String formatConsoleMessage(String sender, String message, boolean error)
    {
        return (error ? "ERROR // " : "") + sender + " // " + message;
    }

    /**
     * This method formats a command usage message with the given command and usage.
     *
     * @author Bradley Hooten (bradleyah02@gmail.com)
     *
     * @since 1.1.0
     *
     * @param commandUsage The command usage to present to the player
     */
    public static String formatCommandUsage(String commandUsage)
    {
        return formatChatMessage("Command Usage", "Incorrect usage. The correct usage is: &c" + commandUsage + "&7.", true);
    }
}
