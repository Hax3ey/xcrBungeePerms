package pl.xcrafters.xcrbungeeperms.listeners;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PermissionCheckEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import pl.xcrafters.xcrbungeeperms.PermsPlugin;
import pl.xcrafters.xcrbungeeperms.data.DataUser;

public class PermissionCheckListener implements Listener {

    PermsPlugin plugin;

    public PermissionCheckListener(PermsPlugin plugin) {
        this.plugin = plugin;
        ProxyServer.getInstance().getPluginManager().registerListener(plugin, this);
    }

    @EventHandler
    public void onPermissionCheck(PermissionCheckEvent event) {
        if (event.getSender() instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) event.getSender();
            String perm = event.getPermission();
            DataUser user = plugin.dataManager.getUserByPlayer(player);
            if (user == null) {
                if (plugin.dataManager.getGroupByName("default") == null) {
                    return;
                }
                event.setHasPermission(plugin.dataManager.getGroupByName("default").getPermissions().get(perm) != null && plugin.dataManager.getGroupByName("default").getPermissions().get(perm));
                return;
            }
            if (user.getPermissions().contains(perm)) {
                event.setHasPermission(true);
                return;
            }
            if (user.getGroup() != null) {
                if (user.getGroup().getPermissions().get(perm) != null && user.getGroup().getPermissions().get(perm)) {
                    event.setHasPermission(true);
                    return;
                }
            } else {
                if (plugin.dataManager.getGroupByName("default") == null) {
                    return;
                }
                event.setHasPermission(plugin.dataManager.getGroupByName("default").getPermissions().get(perm) != null && plugin.dataManager.getGroupByName("default").getPermissions().get(perm));
                return;
            }
            event.setHasPermission(false);
        }
    }

}
