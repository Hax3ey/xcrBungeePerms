package pl.xcrafters.xcrbungeeperms;

import com.google.common.io.ByteStreams;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.*;

public class ConfigManager {

    PermsPlugin plugin;
    Configuration config;
    
    public ConfigManager(PermsPlugin plugin){
        this.plugin = plugin;
        load();
    }
    
    public String mysqlHost;
    public String mysqlBase;
    public String mysqlUser;
    public String mysqlPass;

    public String redisHost;

    public void load(){
        saveDefaultConfig();
        try {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(plugin.getDataFolder(), "config.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        mysqlHost = config.getString("config.mysql.host");
        mysqlBase = config.getString("config.mysql.base");
        mysqlUser = config.getString("config.mysql.user");
        mysqlPass = config.getString("config.mysql.pass");

        redisHost = config.getString("config.redis.host");
    }

    public void saveDefaultConfig() {
        if (!plugin.getDataFolder().exists() ) {
            plugin.getDataFolder().mkdir();
        }
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
                InputStream is = plugin.getResourceAsStream("config.yml");
                OutputStream os = new FileOutputStream(configFile);
                ByteStreams.copy(is, os);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    
}
