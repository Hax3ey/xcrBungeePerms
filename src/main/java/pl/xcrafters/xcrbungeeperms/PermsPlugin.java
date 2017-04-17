package pl.xcrafters.xcrbungeeperms;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.plugin.Plugin;
import pl.xcrafters.xcrbungeeperms.commands.PermCommand;
import pl.xcrafters.xcrbungeeperms.data.DataManager;
import pl.xcrafters.xcrbungeeperms.listeners.LoginListener;
import pl.xcrafters.xcrbungeeperms.listeners.PermissionCheckListener;
import pl.xcrafters.xcrbungeeperms.listeners.RedisListener;
import pl.xcrafters.xcrbungeeperms.redis.RedisManager;
import pl.xcrafters.xcrbungeeperms.mysql.MySQLManager;

import java.util.Random;

public class PermsPlugin extends Plugin {

    public ConfigManager configManager;
    public DataManager dataManager;
    public MySQLManager mysqlManager;
    public RedisManager redisManager;

    PermCommand permCommand;

    PermissionCheckListener permissionCheckListener;
    LoginListener loginListener;
    RedisListener redisListener;

    private static PermsPlugin instance;

    @Override
    public void onEnable() {
        this.configManager = new ConfigManager(this);
        this.dataManager = new DataManager(this);
        this.mysqlManager = new MySQLManager(this);
        this.redisManager = new RedisManager(this);

        this.permCommand = new PermCommand(this);

        this.permissionCheckListener = new PermissionCheckListener(this);
        this.loginListener = new LoginListener(this);
        this.redisListener = new RedisListener(this);

        instance = this;
    }

    @Override
    public void onDisable() {
        mysqlManager.closeConnection();
    }

    public static PermsPlugin getInstance() {
        return instance;
    }

    public String color(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    private static final String CHAR_LIST = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";

    public String generateRandomString(){
        StringBuffer randStr = new StringBuffer();
        for(int i=0; i<8; i++){
            int number = getRandomNumber();
            char ch = CHAR_LIST.charAt(number);
            randStr.append(ch);
        }
        return randStr.toString();
    }

    private int getRandomNumber() {
        int randomInt = 0;
        Random randomGenerator = new Random();
        randomInt = randomGenerator.nextInt(CHAR_LIST.length());
        if (randomInt - 1 == -1) {
            return randomInt;
        } else {
            return randomInt - 1;
        }
    }

}
