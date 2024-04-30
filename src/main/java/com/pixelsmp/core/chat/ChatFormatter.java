package com.pixelsmp.core.chat;

import org.bukkit.ChatColor;

public class ChatFormatter
{
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
}
