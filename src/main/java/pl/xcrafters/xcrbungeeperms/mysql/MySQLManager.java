package pl.xcrafters.xcrbungeeperms.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.md_5.bungee.api.ProxyServer;
import pl.xcrafters.xcrbungeeperms.PermsPlugin;
import pl.xcrafters.xcrbungeeperms.data.DataGroup;
import pl.xcrafters.xcrbungeeperms.data.DataInheritance;
import pl.xcrafters.xcrbungeeperms.data.DataPermission;
import pl.xcrafters.xcrbungeeperms.data.DataUser;
import pl.xcrafters.xcrbungeeperms.data.DataManager;

public class MySQLManager {

    PermsPlugin plugin;

    Connection conn;

    private final Object lock = new Object();

    public List<DataQuery> queries = new ArrayList();

    public MySQLManager(PermsPlugin plugin) {
        this.plugin = plugin;
        conn = prepareConnection();
        loadAll();
        ProxyServer.getInstance().getScheduler().schedule(plugin, new Runnable(){
            public void run(){
                saveAll();
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    Gson gson = new Gson();

    private Connection prepareConnection() {
        for (int i = 0; i < 5; i++) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                String url = "jdbc:mysql://" + plugin.configManager.mysqlHost + "/" + plugin.configManager.mysqlBase;
                return DriverManager.getConnection(url, plugin.configManager.mysqlUser, plugin.configManager.mysqlPass);
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, "CANNOT CONNECT TO DATABASE!", ex);
            } catch (ClassNotFoundException ex) {
                plugin.getLogger().log(Level.SEVERE, "JDBC IS NOT FOUND - CANNOT CONNECT TO DATABASE!", ex);
            }
        }
        return null;
    }

    public Connection getConnection() throws SQLException {
        if (conn == null || conn.isClosed()) {
            conn = prepareConnection();
        }
        return conn;
    }

    public void closeConnection() {
        try {
            conn.close();
        } catch (SQLException ex) {
            Logger.getLogger(MySQLManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void loadAll() {
        try {
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM Groups");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                DataGroup group = new DataGroup(plugin, rs);
                plugin.dataManager.groups.put(group.getGroupName().toLowerCase(), group);
            }
            ps = conn.prepareStatement("SELECT * FROM Users");
            rs = ps.executeQuery();
            while (rs.next()) {
                DataUser user = new DataUser(plugin, rs);
                plugin.dataManager.users.put(user.getNickname().toLowerCase(), user);
                if(user.getUUID() != null) {
                    plugin.dataManager.usersByUUID.put(user.getUUID(), user);
                }
            }
            ps = conn.prepareStatement("SELECT * FROM Permissions");
            rs = ps.executeQuery();
            while (rs.next()) {
                DataPermission perm = new DataPermission(plugin, rs);
                plugin.dataManager.permissions.add(perm);
            }
            ps = conn.prepareStatement("SELECT * FROM Inheritances");
            rs = ps.executeQuery();
            while (rs.next()) {
                DataInheritance inherit = new DataInheritance(plugin, rs);
                plugin.dataManager.inheritances.add(inherit);
            }
        } catch (SQLException ex) {
            Logger.getLogger(MySQLManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void saveAll() {
        DataQuery[] toSend;
        synchronized (lock) {
            toSend = new DataQuery[queries.size()];
            toSend = queries.toArray(toSend);
            queries.clear();
        }
        for (DataQuery data : toSend) {
            try {
                if (getConnection() != null) {
                    DataManager.QueryType type = data.type;
                    String query = data.data.prepareQuery(type);
                    if (query != null) {
                        try {
                            if (type == DataManager.QueryType.INSERT && data.data != null) {
                                Statement statement = getConnection().prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
                                statement.execute(query, Statement.RETURN_GENERATED_KEYS);
                                ResultSet rs = statement.getGeneratedKeys();
                                while (rs.next()) {
                                    data.data.setPrimary(rs.getInt(1));
                                }
                                rs.close();
                                statement.close();
                            } else {
                                PreparedStatement statement = getConnection().prepareStatement(query);
                                statement.executeUpdate();
                                statement.close();
                            }
                        } catch (SQLException ex) {
                            plugin.getLogger().log(Level.WARNING, "Wystapil blad podczas zapisu query: " + query);
                            ex.printStackTrace();
                        }

                        JsonObject object = new JsonObject();
                        object.addProperty("id", data.data.getPrimary());
                        object.addProperty("instance", plugin.redisManager.getInstance());

                        plugin.redisManager.sendMessage(data.data.getUpdateChannel(type), gson.toJson(object));
                    }
                } else {
                    queries.add(data);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    public DataUser loadUser(int id){
        try {
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM Users WHERE userID=" + id);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                DataUser user = new DataUser(plugin, rs);
                return user;
            }
        } catch (SQLException ex) {
            Logger.getLogger(MySQLManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public DataGroup loadGroup(int id){
        try {
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM Groups WHERE groupID=" + id);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                DataGroup group = new DataGroup(plugin, rs);
                return group;
            }
        } catch(SQLException ex) {
            Logger.getLogger(MySQLManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public DataPermission loadPermission(int id){
        try {
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM Permissions WHERE permID=" + id);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                DataPermission perm = new DataPermission(plugin, rs);
                return perm;
            }
        } catch(SQLException ex) {
            Logger.getLogger(MySQLManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public DataInheritance loadInheritance(int id){
        try {
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM Inheritances WHERE inheritID=" + id);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                DataInheritance inherit = new DataInheritance(plugin, rs);
                return inherit;
            }
        } catch(SQLException ex) {
            Logger.getLogger(MySQLManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

}
