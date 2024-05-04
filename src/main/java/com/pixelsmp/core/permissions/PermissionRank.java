package com.pixelsmp.core.permissions;

import java.util.Arrays;

public enum PermissionRank
{
    OWNER("OWNER", '4', 100),
    ENGINEER("ENGINEER", '5', 95), // This is a developer rank (not a staff rank)
    ADMINISTRATOR("ADMINISTRATOR", 'c', 90),
    MODERATOR("MODERATOR", '6', 80),
    HELPER("HELPER", '9', 40),
    BUILDER("BUILDER", '1', 30),
    MEDIA("MEDIA", 'd', 25),
    MVP_PLUS("MVP+", 'b', 20),
    MVP("MVP", '3', 15),
    VIP_PLUS("VIP+", 'a', 10),
    VIP("VIP", '2', 5),
    MEMBER("MEMBER", '8', 0);

    private final char colorCode;
    private final int permissionLevel;
    private final String name;

    PermissionRank(String name, char colorCode, int permissionLevel)
    {
        this.name = name;
        this.colorCode = colorCode;
        this.permissionLevel = permissionLevel;
    }

    /**
     * Returns the color code used for the permission rank.
     *
     * @author Bradley Hooten (bradleyah02@gmail.com)
     *
     * @since 1.1.0
     *
     * @return The color code of the permission rank
     */
    public char getColorCode()
    {
        return colorCode;
    }

    /**
     * Returns the level of the permission rank.
     *
     * @author Bradley Hooten (bradleyah02@gmail.com)
     *
     * @since 1.1.0
     *
     * @return The level of the permission rank
     */
    public int getPermissionLevel()
    {
        return permissionLevel;
    }

    /**
     * Returns the name of the permission rank.
     *
     * @author Bradley Hooten (bradleyah02@gmail.com)
     *
     * @since 1.1.0
     *
     * @return The name of the permission rank
     */
    public String getName()
    {
        return name;
    }

    /**
     * Gets all permission ranks with a level greater than or equal to the given level.
     *
     * @author Bradley Hooten (bradleyah02@gmail.com)
     *
     * @since 1.1.0
     *
     * @return Array of permission ranks with a level greater than or equal to the given level
     */
    public static PermissionRank[] getRanksWithLevelGreaterThan(int level)
    {
        return Arrays.stream(PermissionRank.values()).filter(rank -> rank.getPermissionLevel() >= level)
                .toArray(PermissionRank[]::new);
    }

    /**
     * Gets all permission ranks.
     *
     * @author Bradley Hooten (bradleyah02@gmail.com)
     *
     * @since 1.1.0
     *
     * @return Array of all permission ranks
     */
    public static PermissionRank[] getAllRanks()
    {
        return PermissionRank.values();
    }
}
