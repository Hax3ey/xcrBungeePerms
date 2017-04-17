package pl.xcrafters.xcrbungeeperms;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import pl.xcrafters.xcrbungeeperms.data.DataUser;

import java.util.UUID;

public class PermissionAPI {

    public static String getGroup(String nickname) {
        DataUser user = PermsPlugin.getInstance().dataManager.getUserByNick(nickname);
        if (user != null && user.getGroup() != null) {
            return user.getGroup().getGroupName();
        }
        return "default";
    }

    public static String getGroup(UUID uuid) {
        DataUser user = PermsPlugin.getInstance().dataManager.getUserByUUID(uuid);
        if (user != null && user.getGroup() != null) {
            return user.getGroup().getGroupName();
        }
        return "default";
    }

    public static void setGroup(String nickname, String group) {
        DataUser user = PermsPlugin.getInstance().dataManager.getUserByNick(nickname);
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(nickname);
        if (user == null) {
            user = PermsPlugin.getInstance().dataManager.createUser();
            user.setNickname(nickname);
            if(player != null) {
                user.setUUID(player.getUniqueId());
            }
            user.setGroup(PermsPlugin.getInstance().dataManager.getGroupByName(group));
            user.insert();
        } else {
            user.setGroup(PermsPlugin.getInstance().dataManager.getGroupByName(group));
            if(player != null && user.getUUID() == null) {
                user.setUUID(player.getUniqueId());
            }
            user.update();
        }
    }

    public static boolean hasPermission(UUID uuid, String permission) {
        DataUser user = PermsPlugin.getInstance().dataManager.getUserByUUID(uuid);

        if (user == null) {
            if (PermsPlugin.getInstance().dataManager.getGroupByName("default") == null) {
                return false;
            }
            return PermsPlugin.getInstance().dataManager.getGroupByName("default").getPermissions().get(permission) != null && PermsPlugin.getInstance().dataManager.getGroupByName("default").getPermissions().get(permission);
        }

        if (user.getPermissions().contains(permission)) {
            return true;
        }
        if (user.getGroup() != null) {
            if (user.getGroup().getPermissions().get(permission) != null && user.getGroup().getPermissions().get(permission)) {
                return true;
            }
        } else {
            if (PermsPlugin.getInstance().dataManager.getGroupByName("default") == null) {
                return false;
            }
            return PermsPlugin.getInstance().dataManager.getGroupByName("default").getPermissions().get(permission) != null && PermsPlugin.getInstance().dataManager.getGroupByName("default").getPermissions().get(permission);
        }

        return false;
    }

}
