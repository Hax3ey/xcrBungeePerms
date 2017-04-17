package pl.xcrafters.xcrbungeeperms.listeners;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import pl.xcrafters.xcrbungeeperms.PermsPlugin;
import pl.xcrafters.xcrbungeeperms.data.DataUser;

import java.util.UUID;

public class LoginListener implements Listener {

    PermsPlugin plugin;

    public LoginListener(PermsPlugin plugin) {
        this.plugin = plugin;
        ProxyServer.getInstance().getPluginManager().registerListener(plugin, this);
    }

    @EventHandler
    public void onLogin(LoginEvent event) {
        String nick = event.getConnection().getName();
        UUID uuid = event.getConnection().getUniqueId();
        DataUser user = plugin.dataManager.getUserByNick(nick);
        if(user == null) {
            user = plugin.dataManager.getUserByUUID(uuid);
        }
        if(user != null && !user.getNickname().equals(nick)) {
            user.setNickname(nick);
            user.update();
        }
        if(user != null && user.getUUID() == null) {
            user.setUUID(uuid);
            user.update();
            plugin.dataManager.usersByUUID.put(uuid, user);
        }
    }

}
