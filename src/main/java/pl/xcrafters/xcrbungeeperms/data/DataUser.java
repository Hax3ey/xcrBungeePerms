package pl.xcrafters.xcrbungeeperms.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import pl.xcrafters.xcrbungeeperms.mysql.DataQuery;
import pl.xcrafters.xcrbungeeperms.PermsPlugin;

public class DataUser implements DataInterface{

    PermsPlugin plugin;
    
    public DataUser(PermsPlugin plugin){
        this.plugin = plugin;
    }
    
    public DataUser(PermsPlugin plugin, ResultSet rs) {
        try {
            this.plugin = plugin;
            this.id = rs.getInt("userID");
            this.nickname = rs.getString("nickname");
            this.uuid = rs.getString("uuid") != null ? UUID.fromString(rs.getString("uuid")) : null;
            this.group = plugin.dataManager.getGroupById(rs.getInt("groupID"));
        } catch (SQLException ex) {
            Logger.getLogger(DataUser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private int id;
    
    public void setPrimary(int id){
        this.id = id;
    }
    
    public int getPrimary(){
        return id;
    }
    
    private boolean cNickname, cUUID, cGroup;
    
    private String nickname;
    private UUID uuid;
    private DataGroup group;
    
    public void setNickname(String nickname){ this.nickname = nickname; cNickname = true; }
    public void setUUID(UUID uuid){ this.uuid = uuid; cUUID = true; }
    public void setGroup(DataGroup group){ this.group = group; cGroup = true; }

    
    public String getNickname(){ return nickname; }
    public UUID getUUID(){ return uuid; }
    public final List<String> getPermissions(){
        List<String> perms = new ArrayList();
        for(DataPermission perm : plugin.dataManager.permissions){
            if(perm.getObject() != null && perm.getType().equals(DataManager.PermissionType.USER) && perm.getObject().getPrimary() == id){
                perms.add(perm.getPermission());
            }
        }
        return perms;
    }
    public DataGroup getGroup(){ return group; }
    
    public void insert() {
        plugin.dataManager.users.put(nickname.toLowerCase(), this);
        if(uuid != null) {
            plugin.dataManager.usersByUUID.put(uuid, this);
        }
        plugin.mysqlManager.queries.add(new DataQuery(this, DataManager.QueryType.INSERT));
    }
    
    public void update() {
        if(cNickname){
            for(DataUser user : plugin.dataManager.users.values()){
                if(user.equals(this)){
                    plugin.dataManager.users.remove(user.getNickname().toLowerCase());
                }
            }
            plugin.dataManager.users.put(nickname.toLowerCase(), this);
        }
        plugin.mysqlManager.queries.add(new DataQuery(this, DataManager.QueryType.UPDATE));
    }
    
    public void delete() {
        plugin.dataManager.users.remove(nickname.toLowerCase(), this);
        if(uuid != null) {
            plugin.dataManager.usersByUUID.remove(uuid, this);
        }
        plugin.mysqlManager.queries.add(new DataQuery(this, DataManager.QueryType.DELETE));
    }
    
    public String prepareQuery(DataManager.QueryType type) {
        if(type.equals(DataManager.QueryType.DELETE)){
            return "DELETE FROM Users WHERE userID=" + id;
        }
        String query = null;

        List<String> columns = new ArrayList();
        List<String> values = new ArrayList();
        if(cNickname){
            cNickname = false;
            columns.add("nickname");
            values.add(nickname);
        }
        if(cUUID){
            cUUID = false;
            columns.add("uuid");
            values.add(uuid.toString());
        }
        if(cGroup){
            cGroup = false;
            columns.add("groupID");
            values.add(group != null ? String.valueOf(group.getPrimary()) : "0");
        }
        if(!values.isEmpty() && !columns.isEmpty()){
            if(type == DataManager.QueryType.UPDATE){
                query = "UPDATE Users SET ";
                for(String column : columns){
                    int index = columns.indexOf(column);
                    String value = values.get(index);
                    String comma = "";
                    if(index > 0){
                        comma = ",";
                    }
                    if(value != null){
                        query += comma + column + "='" + value + "'";
                    } else {
                        query += comma + column + "=NULL";
                    }
                }
                query += " WHERE userID=" + getPrimary();
            }
            else if(type == DataManager.QueryType.INSERT){
                query = "INSERT INTO Users (";
                for(String column : columns){
                    int index = columns.indexOf(column);
                    String comma = "";
                    if(index > 0){
                        comma = ",";
                    }
                    query += comma + column;
                }
                query += ") VALUES (";
                for(String value : values){
                    int index = values.indexOf(value);
                    String comma = "";
                    if(index > 0){
                        comma = ",";
                    }
                    if (value != null){
                        query += comma + "'" + value + "'";
                    } else {
                        query += comma + "NULL";
                    }
                }
                query += ")";
            }
        }
        return query;
    }
    
    public String getUpdateChannel(DataManager.QueryType type) {
        if(type.equals(DataManager.QueryType.INSERT)){
            return "PermInsertUser";
        }
        if(type.equals(DataManager.QueryType.UPDATE)){
            return "PermUpdateUser";
        }
        if(type.equals(DataManager.QueryType.DELETE)){
            return "PermDeleteUser";
        }
        return null;
    }

    public void synchronize() {
        try {
            Connection conn = plugin.mysqlManager.getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM Users WHERE userID=" + id);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                this.nickname = rs.getString("nickname");
                this.uuid = rs.getString("uuid") != null ? UUID.fromString(rs.getString("uuid")) : null;
                this.group = plugin.dataManager.getGroupById(rs.getInt("groupID"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(DataUser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
