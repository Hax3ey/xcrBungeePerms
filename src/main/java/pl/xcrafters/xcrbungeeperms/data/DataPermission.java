package pl.xcrafters.xcrbungeeperms.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import pl.xcrafters.xcrbungeeperms.mysql.DataQuery;
import pl.xcrafters.xcrbungeeperms.PermsPlugin;
import pl.xcrafters.xcrbungeeperms.data.DataManager.PermissionType;
import pl.xcrafters.xcrbungeeperms.data.DataManager.QueryType;

public class DataPermission implements DataInterface{

    PermsPlugin plugin;
    
    public DataPermission(PermsPlugin plugin, String permission, PermissionType type, DataInterface object) {
        this.plugin = plugin;
        this.permission = permission;
        this.type = type;
        this.object = object;
        this.value = true;
        cPermission = true;
        cType = true;
        cObject = true;
        cValue = true;
    }
    
    public DataPermission(PermsPlugin plugin, ResultSet rs) {
        try {
            this.plugin = plugin;
            this.id = rs.getInt("permID");
            this.permission = rs.getString("permission");
            this.type = PermissionType.valueOf(rs.getString("type").toUpperCase());
            this.object = this.type == PermissionType.GROUP ? plugin.dataManager.getGroupById(rs.getInt("typeID")) : plugin.dataManager.getUserById(rs.getInt("typeID"));
            this.value = rs.getInt("value") == 1;
        } catch (SQLException ex) {
            Logger.getLogger(DataPermission.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private int id;
    
    public void setPrimary(int id){
        this.id = id;
    }
    
    public int getPrimary(){
        return id;
    }
    
    private boolean cPermission, cType, cObject, cValue;
    
    private String permission;
    private PermissionType type;
    private DataInterface object;
    private boolean value;
    
    public void setPermission(String permission){ this.permission = permission; cPermission = true; }
    public void setType(PermissionType type){ this.type = type; cType = true; }
    public void setObject(DataInterface object){ this.object = object; cObject =  true; }
    public void setValue(boolean value){ this.value = value; cValue = true; }
    
    public String getPermission(){ return permission; }
    public PermissionType getType(){ return type; }
    public DataInterface getObject(){ return object; }
    public boolean getValue(){ return value; }
    
    public void insert() {
        plugin.dataManager.permissions.add(this);
        plugin.mysqlManager.queries.add(new DataQuery(this, QueryType.INSERT));
    }
    
    public void update(){
        plugin.mysqlManager.queries.add(new DataQuery(this, QueryType.UPDATE));
    }
    
    public void delete() {
        plugin.dataManager.permissions.remove(this);
        plugin.mysqlManager.queries.add(new DataQuery(this, QueryType.DELETE));
    }
    
    public String prepareQuery(QueryType type) {
        if(type.equals(QueryType.DELETE)){
            return "DELETE FROM Permissions WHERE permID=" + id;
        }
        String query = null;
        if(!cPermission && !cType && !cObject && !cValue){
            return query;
        }
        List<String> columns = new ArrayList();
        List<String> values = new ArrayList();
        if(cPermission){
            cPermission = false;
            columns.add("permission");
            values.add(permission);
        }
        if(cType){
            cType = false;
            columns.add("type");
            values.add(this.type.name());
        }
        if(cObject){
            cObject = false;
            columns.add("typeID");
            values.add(String.valueOf(object.getPrimary()));
        }
        if(cValue){
            cValue = false;
            columns.add("value");
            values.add(String.valueOf(value ? 1 : 0));
        }
        if(!values.isEmpty() && !columns.isEmpty()){
            if(type == QueryType.UPDATE){
                query = "UPDATE Permissions SET ";
                for(String column : columns){
                    int index = columns.indexOf(column);
                    String val = values.get(index);
                    String comma = "";
                    if(index > 0){
                        comma = ",";
                    }
                    if(val != null){
                        query += comma + column + "='" + val + "'";
                    } else {
                        query += comma + column + "=NULL";
                    }
                }
                query += " WHERE permID=" + getPrimary();
            }
            else if(type == QueryType.INSERT){
                query = "INSERT INTO Permissions (";
                for(String column : columns){
                    int index = columns.indexOf(column);
                    String comma = "";
                    if(index > 0){
                        comma = ",";
                    }
                    query += comma + column;
                }
                query += ") VALUES (";
                for(String val : values){
                    int index = values.indexOf(val);
                    String comma = "";
                    if(index > 0){
                        comma = ",";
                    }
                    if (val != null){
                        query += comma + "'" + val + "'";
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
            return "PermInsertPermission";
        }
        if(type.equals(QueryType.UPDATE)){
            return "PermUpdatePermission";
        }
        if(type.equals(QueryType.DELETE)){
            return "PermDeletePermission";
        }
        return null;
    }

    public void synchronize() {
        try {
            Connection conn = plugin.mysqlManager.getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM Permissions WHERE permID=" + id);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                this.permission = rs.getString("permission");
                this.type = PermissionType.valueOf(rs.getString("type").toUpperCase());
                this.object = this.type == PermissionType.GROUP ? plugin.dataManager.getGroupById(rs.getInt("typeID")) : plugin.dataManager.getUserById(rs.getInt("typeID"));
                this.value = rs.getInt("value") == 1;
            }
        } catch (SQLException ex) {
            Logger.getLogger(DataUser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
