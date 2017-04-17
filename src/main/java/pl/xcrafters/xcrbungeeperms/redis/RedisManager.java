package pl.xcrafters.xcrbungeeperms.redis;

import net.md_5.bungee.api.ProxyServer;
import pl.xcrafters.xcrbungeeperms.PermsPlugin;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.exceptions.JedisConnectionException;

public class RedisManager {

    PermsPlugin plugin;

    JedisPool pool;
    Jedis subscriber;

    String instance;

    public RedisManager(PermsPlugin plugin) {
        this.plugin = plugin;
        this.pool = new JedisPool(new JedisPoolConfig(), plugin.configManager.redisHost, 6379, 10000);

        this.subscriber = pool.getResource();

        this.instance = plugin.generateRandomString();
    }

    public void subscribe(final JedisPubSub pubSub, final String... channels) {
        ProxyServer.getInstance().getScheduler().runAsync(plugin, new Runnable() {
            public void run() {
                subscriber.subscribe(pubSub, channels);
            }
        });
    }

    public void sendMessage(String channel, String message) {
        Jedis jedis = pool.getResource();
        try {
            jedis.select(1);
            jedis.publish(channel, message);
        } catch (JedisConnectionException ex) {
            pool.returnBrokenResource(jedis);
        } finally {
            pool.returnResource(jedis);
        }
    }

    public String getInstance() {
        return this.instance;
    }

}
