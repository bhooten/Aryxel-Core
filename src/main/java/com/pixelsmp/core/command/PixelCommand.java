package com.pixelsmp.core.command;

import com.pixelsmp.core.CorePlugin;
import com.pixelsmp.core.chat.ChatFormatter;
import com.pixelsmp.core.permissions.PermissionRank;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public abstract class PixelCommand implements CommandExecutor
{
    private final String baseCommand;
    private final PermissionRank[] allowedRanks;
    private final boolean consoleExecutionAllowed;

    public PixelCommand(String baseCommand, PermissionRank[] allowedRanks,
                        boolean consoleExecutionAllowed)
    {
        this.baseCommand = baseCommand;
        this.allowedRanks = allowedRanks;
        this.consoleExecutionAllowed = consoleExecutionAllowed;
    }

    @Override
    public final boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings)
    {
        if(command.getName().equalsIgnoreCase(baseCommand))
        {
            // Check if the sender is a player or console (and do not include command blocks)
            if(consoleExecutionAllowed && commandSender instanceof ConsoleCommandSender)
            {
                // Console execution is allowed and the sender is not a player, pass it!
                return execute(commandSender, command, s, strings);
            }
            else if(commandSender instanceof Player)
            {
                // The sender is a player, fetch rank and check against allowed ranks
                CorePlugin.getPermissionOrchestrator().getPlayerRank(((Player) commandSender).getUniqueId()).thenApply(rank ->
                {
                    // The player does not have a rank, set it to MEMBER
                    // Something is clearly wrong, as any player should get the rank upon join.
                    return rank.orElse(PermissionRank.MEMBER);
                }).thenAccept(rank ->
                {
                    // Check if the rank is allowed to execute the command
                    if(rank != null && allowedRanks != null)
                    {
                        for(PermissionRank allowedRank : allowedRanks)
                        {
                            if(allowedRank == rank)
                            {
                                // The rank is allowed to execute the command, pass it!
                                execute(commandSender, command, s, strings);
                                return;
                            }
                        }
                    }

                    // The rank is not allowed to execute the command, send a message
                    commandSender.sendMessage(ChatFormatter.NO_PERMISSIONS_ERROR);
                }).exceptionally(ex ->
                {
                    // Something went wrong while fetching the rank, send a message
                    commandSender.sendMessage(ChatFormatter.UNKNOWN_ERROR);
                    ex.printStackTrace();

                    return null;
                });
            }
            else
            {
                // The sender is not a player and console execution is not allowed, send a message
                commandSender.sendMessage(ChatFormatter.formatChatMessage("Command Orchestrator",
                        "This command can only be executed by a player.", true));
            }

            return true;
        }
        return false;
    }

    public abstract boolean execute(CommandSender commandSender, Command command, String s, String[] strings);
}
