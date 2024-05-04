package com.pixelsmp.core.listener;

import com.pixelsmp.core.CorePlugin;
import com.pixelsmp.core.chat.ChatFormatter;
import com.pixelsmp.core.permissions.PermissionRank;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Optional;

public class ChatEventListener implements Listener
{
    @EventHandler
    public void onChat(AsyncPlayerChatEvent event)
    {
        // Fetch the player's rank
        CorePlugin.getPermissionOrchestrator().getPlayerRank(event.getPlayer().getUniqueId()).thenAccept(rank ->
        {
            // If the rank is empty, set it to MEMBER
            if (rank.isEmpty())
            {
                CorePlugin.getPermissionOrchestrator().upsertPlayerRank(event.getPlayer().getUniqueId(), PermissionRank.MEMBER);
                rank = Optional.of(PermissionRank.MEMBER);
            }

            event.setFormat(ChatColor.translateAlternateColorCodes('&', "&" +
                    rank.get().getColorCode() + "&l" + rank.get().getName() + " &r&7" + event.getPlayer().getName() +
                    " &r&7// " + event.getMessage()));
        }).exceptionally(ex ->
        {
            CorePlugin.getInstance().getLogger().severe(ChatFormatter.formatConsoleMessage("Core",
                    "An error occurred while attempting to fetch the player's rank: " + ex.getMessage(),
                    true));

            event.getPlayer().sendMessage(ChatFormatter.formatChatMessage("Core",
                    "An error occurred while trying to initialize your profile. " +
                            "Please contact an administrator.",
                    true));

            return null;
        });
    }
}
