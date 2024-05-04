package com.pixelsmp.core.util;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PlayerUtils
{
    private static final HttpClient client;

    static
    {
        client = HttpClient.newHttpClient();
    }

    /**
     * Get the UUID of a player by their username asynchronously.
     *
     * @param username The username of the player to fetch the UUID of
     *
     * @return The UUID of the player
     *
     * @author Bradley Hooten (bradleyah02@gmail.com)
     * @since 1.1.0
     */
    public static CompletableFuture<UUID> fetchUUIDbyNameAsync(String username)
    {
        String url = "https://api.mojang.com/users/profiles/minecraft/" + username;
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(HttpResponse::body).thenApply(body ->
        {
            if (body == null || body.isEmpty())
            {
                return null; // No data returned
            }
            String uuidStr = extractUUID(body);
            if (uuidStr != null)
            {
                return UUID.fromString(insertDashUUID(uuidStr));
            }
            return null;
        });
    }

    private static String extractUUID(String jsonResponse)
    {
        // Simplified extraction logic assuming response is a simple JSON with id as a key
        // Example JSON: {"id":"uuidwithoutdashes","name":"playerName"}
        // A real implementation should use a JSON parser
        try
        {
            String[] parts = jsonResponse.split("\"");
            int index = -1;
            for (int i = 0; i < parts.length; i++)
            {
                if ("id".equals(parts[i]))
                {
                    index = i + 2;
                    break;
                }
            }
            if (index != -1 && index < parts.length)
            {
                return parts[index];
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    private static String insertDashUUID(String uuid)
    {
        // Insert dashes into UUID because the Mojang API returns UUID without dashes
        return uuid.replaceFirst("(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5");
    }
}
