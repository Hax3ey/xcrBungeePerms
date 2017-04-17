package pl.xcrafters.xcrbungeeperms.listeners;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import pl.xcrafters.xcrbungeeperms.PermsPlugin;
import pl.xcrafters.xcrbungeeperms.data.DataGroup;
import pl.xcrafters.xcrbungeeperms.data.DataInheritance;
import pl.xcrafters.xcrbungeeperms.data.DataPermission;
import pl.xcrafters.xcrbungeeperms.data.DataUser;
import redis.clients.jedis.JedisPubSub;

import java.util.UUID;

public class RedisListener extends JedisPubSub {

    PermsPlugin plugin;

    public RedisListener(PermsPlugin plugin) {
        this.plugin = plugin;
        plugin.redisManager.subscribe(this, "PermInsertUser", "PermInsertGroup", "PermInsertPermission", "PermInsertInheritance", "PermUpdateUser", "PermUpdateGroup", "PermUpdatePermission", "PermUpdateInheritance", "PermDeleteUser", "PermDeleteGroup", "PermDeletePermission", "PermDeleteInheritance");
    }

    Gson gson = new Gson();

    public void onMessage(String channel, final String json) {
        try {
            JsonObject object = gson.fromJson(json, JsonObject.class);

            int id = object.get("id").getAsInt();
            String instance = object.get("instance").getAsString();

            if(instance.equals(plugin.redisManager.getInstance())) {
                return;
            }

            if (channel.equals("PermInsertUser")) {
                DataUser user = plugin.mysqlManager.loadUser(id);
                plugin.dataManager.users.put(user.getNickname().toLowerCase(), user);
                if(user.getUUID() != null) {
                    plugin.dataManager.usersByUUID.put(user.getUUID(), user);
                }
            }
            if (channel.equals("PermInsertGroup")) {
                DataGroup group = plugin.mysqlManager.loadGroup(id);
                plugin.dataManager.groups.put(group.getGroupName().toLowerCase(), group);
            }
            if (channel.equals("PermInsertPermission")) {
                DataPermission perm = plugin.mysqlManager.loadPermission(id);
                plugin.dataManager.permissions.add(perm);
            }
            if (channel.equals("PermInsertInheritance")) {
                DataInheritance inherit = plugin.mysqlManager.loadInheritance(id);
                plugin.dataManager.inheritances.add(inherit);
            }
            if (channel.equals("PermUpdateUser")) {
                DataUser user = plugin.dataManager.getUserById(id);

                UUID uuid = user.getUUID();

                user.synchronize();

                UUID afterUUID = user.getUUID();

                if(uuid == null && afterUUID != null) {
                    plugin.dataManager.usersByUUID.put(user.getUUID(), user);
                }
            }
            if (channel.equals("PermUpdateGroup")) {
                DataGroup group = plugin.dataManager.getGroupById(id);
                group.synchronize();
            }
            if(channel.equals("PermUpdatePermission")){
                DataPermission perm = plugin.dataManager.getPermissionById(id);
                perm.synchronize();
            }
            if (channel.equals("PermDeleteUser")) {
                DataUser user = plugin.dataManager.getUserById(id);
                plugin.dataManager.users.remove(user.getNickname().toLowerCase());
            }
            if (channel.equals("PermDeleteGroup")) {
                DataGroup group = plugin.dataManager.getGroupById(id);
                plugin.dataManager.groups.remove(group.getGroupName().toLowerCase());
            }
            if (channel.equals("PermDeletePermission")) {
                DataPermission perm = plugin.dataManager.getPermissionById(id);
                plugin.dataManager.permissions.remove(perm);
            }
            if (channel.equals("PermDeleteInheritance")) {
                DataInheritance inherit = plugin.dataManager.getInheritanceById(id);
                plugin.dataManager.inheritances.remove(inherit);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void onSubscribe(String channel, int subscribedChannels) {
    }

    public void onUnsubscribe(String channel, int subscribedChannels) {
    }

    public void onPSubscribe(String pattern, int subscribedChannels) {
    }

    public void onPUnsubscribe(String pattern, int subscribedChannels) {
    }

    public void onPMessage(String pattern, String channel, String message) {
    }

}
