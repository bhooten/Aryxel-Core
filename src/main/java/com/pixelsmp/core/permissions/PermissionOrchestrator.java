package com.pixelsmp.core.permissions;

import com.pixelsmp.core.CorePlugin;
import com.pixelsmp.core.chat.ChatFormatter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class PermissionOrchestrator
{
    private final ConcurrentHashMap<UUID, PermissionRank> _playerPermissions = new ConcurrentHashMap<>();

    /**
     * Initializes a new PermissionOrchestrator.
     *
     * @author Bradley Hooten (bradleyah02@gmail.com)
     * @see PermissionRank
     * @since 1.1.0
     */
    public PermissionOrchestrator()
    {
        // Check to see if the orchestrator has already been initialized
        if (CorePlugin.getPermissionOrchestrator() != null)
        {
            // Throw an exception if it has
            throw new IllegalStateException("PermissionOrchestrator has already been initialized!");
        }

        // Initialize the permissions table in the database
        CorePlugin.getSQLConnectionManager().executeUpdateAsync(
                        "CREATE TABLE IF NOT EXISTS core_permissions (player_uuid VARCHAR(36) PRIMARY KEY, rank VARCHAR(16) NOT NULL DEFAULT 'MEMBER');")
                .thenCompose(result ->
                {
                    UUID[] onlinePlayerUUIDs = Bukkit.getServer().getOnlinePlayers().stream().map(Player::getUniqueId).toArray(UUID[]::new);

                    // Seed the player ranks, if any are online
                    if(onlinePlayerUUIDs.length != 0)
                    {
                        // Build the SQL statement to fetch all player UUIDs
                        StringBuilder baseQuery = new StringBuilder("SELECT * FROM core_permissions WHERE player_uuid IN (");

                        for (UUID uuid : onlinePlayerUUIDs)
                        {
                            baseQuery.append("'").append(uuid.toString()).append("',");
                        }

                        baseQuery.delete(baseQuery.length() - 1, baseQuery.length()).append(");");

                        return CorePlugin.getSQLConnectionManager().executeQueryAsync(baseQuery.toString(), rs ->
                        {
                            // Iterate through the ResultSet to seed the in-memory value store
                            while (rs.next())
                            {
                                PermissionRank rank = PermissionRank.valueOf(rs.getString("rank"));

                                _playerPermissions.put(UUID.fromString(rs.getString("player_uuid")), rank);
                            }

                            Bukkit.getServer().getLogger()
                                    .info(ChatFormatter.formatConsoleMessage("Core", "Successfully seeded player rank data.", false));

                            return null;
                        });
                    }

                    return CompletableFuture.completedFuture(null);
                }).exceptionally(ex ->
                {
                    Bukkit.getLogger().severe(ChatFormatter.formatConsoleMessage("Core", "Failed to initialize permissions.", true));

                    CorePlugin.getInstance().getPluginLoader().disablePlugin(CorePlugin.getInstance());
                    ex.printStackTrace();

                    return null;
                });
    }

    /**
     * Fetches the provided player's rank from the in-memory value store.
     *
     * @param playerUUID The player to fetch the rank for
     *
     * @return The player's rank, if it exists
     *
     * @author Bradley Hooten (bradleyah02@gmail.com)
     * @since 1.1.0
     */
    public CompletableFuture<Optional<PermissionRank>> getPlayerRank(UUID playerUUID)
    {
        if (_playerPermissions.containsKey(playerUUID))
        {
            return CompletableFuture.completedFuture(Optional.ofNullable(_playerPermissions.get(playerUUID)));
        }
        else
        {
            // Fetch the player's rank from the database
            return CorePlugin.getSQLConnectionManager().executeQueryAsync("SELECT rank FROM core_permissions WHERE player_uuid = ?;", rs ->
            {
                if (rs.next())
                {
                    PermissionRank rank = PermissionRank.valueOf(rs.getString("rank"));
                    _playerPermissions.put(playerUUID, rank);

                    return Optional.of(rank);
                }
                else
                {
                    return Optional.empty();
                }
            }, playerUUID.toString());
        }
    }

    /**
     * Upserts the provided player's rank in the in-memory value store and the database.
     *
     * @return CompletableFuture-wrapped boolean indicating success or failure
     *
     * @author Bradley Hooten (bradleyah02@gmail.com)
     * @since 1.1.0
     */
    public CompletableFuture<Boolean> upsertPlayerRank(UUID uuid, PermissionRank rank)
    {
        // Update the database
        return CorePlugin.getSQLConnectionManager()
                .executeUpdateAsync("INSERT INTO core_permissions (player_uuid, rank) VALUES (?, ?) " + "ON DUPLICATE KEY UPDATE rank = ?;",
                        uuid.toString(), rank.toString(), rank.toString()).thenApply(result ->
                {
                    // Check if the update was successful
                    if (result > 0)
                    {
                        // Update the in-memory value store
                        if (Bukkit.getServer().getPlayer(uuid) != null)
                        {
                            _playerPermissions.put(uuid, rank);
                        }

                        return true;
                    }
                    else
                    {
                        return false;
                    }
                });
    }

    /**
     * Seeds the provided player's rank in the in-memory value store and the database.
     *
     * @return CompletableFuture-wrapped boolean indicating success or failure
     *
     * @author Bradley Hooten (bradleyah02@gmail.com)
     * @since 1.1.0
     */
    public CompletableFuture<Boolean> seedPlayerRank(UUID playerUUID)
    {
        // Fetch the player's rank from the database
        return CorePlugin.getSQLConnectionManager().executeQueryAsync("SELECT rank FROM core_permissions WHERE player_uuid = ?;", rs ->
        {
            if (rs.next())
            {
                _playerPermissions.put(playerUUID, PermissionRank.valueOf(rs.getString("rank")));

                return CompletableFuture.completedFuture(true);
            }
            else
            {
                return CorePlugin.getPermissionOrchestrator().upsertPlayerRank(playerUUID, PermissionRank.MEMBER);
            }
        }, playerUUID.toString()).thenCompose(Function.identity()).thenApply(success ->
        {
            if (success)
            {
                CorePlugin.getInstance().getLogger().fine("Successfully seeded player rank for " + playerUUID + "!");
            }
            else
            {
                CorePlugin.getInstance().getLogger().warning(
                        ChatFormatter.formatConsoleMessage("Core", "Failed to seed player rank for " + playerUUID + "!", true));
            }

            return success;
        });
    }

    /**
     * Purges the provided player's rank from the in-memory value store and the database.
     *
     * @param playerUUID Player to purge
     *
     * @author Bradley Hooten (bradleyah02@gmail.com)
     * @since 1.1.0
     */
    public void purgePermissionCache(UUID playerUUID)
    {
        _playerPermissions.remove(playerUUID);
    }
}
