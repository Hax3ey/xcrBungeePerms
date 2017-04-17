package pl.xcrafters.xcrbungeeperms.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import pl.xcrafters.xcrbungeeperms.mysql.DataQuery;
import pl.xcrafters.xcrbungeeperms.PermsPlugin;
import pl.xcrafters.xcrbungeeperms.data.DataManager.PermissionType;
import pl.xcrafters.xcrbungeeperms.data.DataManager.QueryType;

public class DataGroup implements DataInterface {

    PermsPlugin plugin;

    public DataGroup(PermsPlugin plugin) {
        this.plugin = plugin;
    }
    
    public DataGroup(PermsPlugin plugin, ResultSet rs) {
        try {
            this.plugin = plugin;
            this.id = rs.getInt("groupID");
            this.groupName = rs.getString("groupName");
        } catch (SQLException ex) {
            Logger.getLogger(DataGroup.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private int id;

    public void setPrimary(int id) {
        this.id = id;
    }

    public int getPrimary() {
        return id;
    }

    private boolean cGroupName;

    private String groupName;

    public void setGroupName(String groupName){ this.groupName = groupName; cGroupName = true; }

    public String getGroupName(){ return groupName; }
    public final Map<String, Boolean> getGroupPermissions(){
        Map<String, Boolean> perms = new HashMap();
        for(DataPermission perm : plugin.dataManager.permissions){
            if(perm.getType().equals(PermissionType.GROUP) && perm.getObject().getPrimary() == id){
                perms.put(perm.getPermission(), perm.getValue());
            }
        }
        return perms;
    }
    public final List<DataGroup> getInheritGroups(){
        List<DataGroup> groups = new ArrayList();
        for(DataInheritance inherit : plugin.dataManager.inheritances){
            if(inherit.getGroup().equals(this)){
                groups.add(inherit.getSub());
            }
        }
        return groups;
    }
    public final Map<String, Boolean> getPermissions() {
        Map<String, Boolean> perms = new HashMap();
        for(Entry<String, Boolean> perm : getGroupPermissions().entrySet()){
            perms.put(perm.getKey(), perm.getValue());
        }
        for (DataGroup inherit : getInheritGroups()) {
            for (Entry<String, Boolean> perm : inherit.getPermissions().entrySet()) {
                perms.put(perm.getKey(), perm.getValue());
            }
        }
        return perms;
    }

    public void insert() {
        plugin.dataManager.groups.put(groupName.toLowerCase(), this);
        plugin.mysqlManager.queries.add(new DataQuery(this, QueryType.INSERT));
    }

    public void update() {
        if (cGroupName) {
            for (DataGroup group : plugin.dataManager.groups.values()) {
                if (group.equals(this)) {
                    plugin.dataManager.groups.remove(group.getGroupName().toLowerCase());
                }
            }
            plugin.dataManager.groups.put(groupName.toLowerCase(), this);
        }
        plugin.mysqlManager.queries.add(new DataQuery(this, QueryType.UPDATE));
    }

    public void delete() {
        plugin.dataManager.groups.remove(groupName.toLowerCase());
        plugin.mysqlManager.queries.add(new DataQuery(this, QueryType.DELETE));
    }

    public String prepareQuery(QueryType type) {
        if(type.equals(QueryType.DELETE)){
            return "DELETE FROM Groups WHERE groupID=" + id;
        }
        String query = null;
        if(!cGroupName){
            return query;
        }
        List<String> columns = new ArrayList();
        List<String> values = new ArrayList();
        if(cGroupName){
            cGroupName = false;
            columns.add("groupName");
            values.add(groupName);
        }
        if(!values.isEmpty() && !columns.isEmpty()){
            if(type == QueryType.UPDATE){
                query = "UPDATE Groups SET ";
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
                query += " WHERE groupID=" + getPrimary();
            }
            else if(type == QueryType.INSERT){
                query = "INSERT INTO Groups (";
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
    
    public String getUpdateChannel(QueryType type) {
        if(type.equals(QueryType.INSERT)){
            return "PermInsertGroup";
        }
        if(type.equals(QueryType.UPDATE)){
            return "PermUpdateGroup";
        }
        if(type.equals(QueryType.DELETE)){
            return "PermDeleteGroup";
        }
        return null;
    }

    public void synchronize() {
        try {
            Connection conn = plugin.mysqlManager.getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM Groups WHERE groupID=" + id);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                this.groupName = rs.getString("groupName");
            }
        } catch (SQLException ex) {
            Logger.getLogger(DataUser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
