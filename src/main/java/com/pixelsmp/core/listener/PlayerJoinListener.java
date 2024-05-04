package com.pixelsmp.core.listener;

import com.pixelsmp.core.CorePlugin;
import com.pixelsmp.core.chat.ChatFormatter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener
{
    @EventHandler
    public void onJoin(PlayerJoinEvent event)
    {
        // Seed the player's rank into the in-memory store for caching
        CorePlugin.getPermissionOrchestrator().seedPlayerRank(event.getPlayer().getUniqueId())
                .thenAccept(success ->
                {
                    if (success)
                    {
                        CorePlugin.getInstance().getLogger().fine("Successfully seeded player rank for " +
                                event.getPlayer().getName() + "!");
                    }
                    else
                    {
                        CorePlugin.getInstance().getLogger().warning(ChatFormatter.formatConsoleMessage("Core",
                                "Failed to seed player rank for " + event.getPlayer().getName() + "!",
                                true));

                        // Inform the player of the error
                        event.getPlayer().sendMessage(ChatFormatter.formatChatMessage("Core",
                                "An error occurred while trying to initialize your profile. " +
                                        "Please contact an administrator.", true));
                    }
                }).exceptionally(ex ->
                {
                    CorePlugin.getInstance().getLogger().warning(ChatFormatter.formatConsoleMessage("Core",
                            "An error occurred while attempting to fetch the player's rank: " +
                                    ex.getMessage(), true));

                    event.getPlayer().sendMessage(ChatFormatter.formatChatMessage("Core",
                            "An error occurred while trying to initialize your profile. " +
                                    "Please contact an administrator.",
                            true));

                    return null;
                });
    }
}