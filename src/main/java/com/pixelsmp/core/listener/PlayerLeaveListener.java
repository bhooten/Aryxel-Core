package com.pixelsmp.core.listener;

import com.pixelsmp.core.CorePlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerLeaveListener implements Listener
{
    @EventHandler
    public void onLeave(PlayerQuitEvent event)
    {
        CorePlugin.getPermissionOrchestrator().purgePermissionCache(event.getPlayer().getUniqueId());
    }
}
