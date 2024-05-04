package com.pixelsmp.core.command.permissions;

import com.pixelsmp.core.CorePlugin;
import com.pixelsmp.core.chat.ChatFormatter;
import com.pixelsmp.core.command.PixelCommand;
import com.pixelsmp.core.permissions.PermissionRank;
import com.pixelsmp.core.util.PlayerUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.concurrent.CompletableFuture;

public class SetRankCommand extends PixelCommand
{
    private static final PermissionRank[] ALLOWED_RANKS = new PermissionRank[]{PermissionRank.OWNER};

    public SetRankCommand()
    {
        super("setrank", ALLOWED_RANKS, true);
    }

    @Override
    public boolean execute(CommandSender commandSender, Command command, String s, String[] strings)
    {
        if (strings.length != 2)
        {
            // Incorrect number of parameters
            commandSender.sendMessage(ChatFormatter.formatCommandUsage("/setrank <player> <rank>"));
            return false;
        }
        else
        {
            // Parse the rank
            PermissionRank rank = PermissionRank.valueOf(strings[1]);

            PlayerUtils.fetchUUIDbyNameAsync(strings[0]).thenCompose(uuid ->
            {
                if (uuid == null)
                {
                    commandSender.sendMessage(ChatFormatter.formatChatMessage("Permissions",
                            "Unable to locate a player with the name &c" + strings[0] + "&7.", true));
                    return CompletableFuture.completedFuture(null);
                }
                else
                {
                    return CorePlugin.getPermissionOrchestrator().upsertPlayerRank(uuid, rank);
                }
            }).thenAccept(success ->
            {
                if (success)
                {
                    commandSender.sendMessage(ChatFormatter.formatChatMessage("Permissions",
                            "&a" + strings[1] + "&7's rank has successfully been updated to &a" + rank.getName() + "&7.", false));
                }
                else
                {
                    commandSender.sendMessage(ChatFormatter.formatChatMessage("Permissions",
                            "Failed " + "to set &c" + strings[1] + "&7's rank to &c" + rank.getName() + "&7.", true));
                }
            });
        }
        return false;
    }
}
