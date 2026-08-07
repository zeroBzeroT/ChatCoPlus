package org.zeroBzeroT.chatCo;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class Redis {

    private static boolean warnedDown = false;

    private static HostAndPort getHost() {
        return new HostAndPort(
            Main.getInstance().getConfig().getString("ChatCo.redis.host", "127.0.0.1"),
            Main.getInstance().getConfig().getInt("ChatCo.redis.port", 6379));
    }

    private static Optional<String> read(String key) {
        final HostAndPort host = getHost();
        final Jedis jedis;
        final String value;
        try {
            jedis = new Jedis(host);
            value = jedis.get(key);
        } catch (JedisConnectionException e) {
            if (!warnedDown) {
                warnedDown = true;
                Bukkit.getLogger().warning("[ChatCo] Redis not reachable at "
                    + host.getHost() + ":" + host.getPort()
                    + " (" + e.getMessage() + "). Gating will fail closed.");
            }
            return Optional.empty();
        }
        jedis.close();
        if (value == null) return Optional.empty();
        return Optional.of(value);
    }

    public static CompletableFuture<Optional<String>> readAsync(String key) {
        return CompletableFuture.supplyAsync(() -> read(key));
    }

    public static Optional<String> readSync(String key) {
        return read(key);
    }
}
