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

public class DataInheritance implements DataInterface{

    PermsPlugin plugin;
    
    public DataInheritance(PermsPlugin plugin, DataGroup group, DataGroup sub) {
        this.plugin = plugin;
        this.group = group;
        this.sub = sub;
        cGroup = true;
        cSub = true;
    }
    
    public DataInheritance(PermsPlugin plugin, ResultSet rs) {
        try {
            this.plugin = plugin;
            this.id = rs.getInt("inheritID");
            this.group = plugin.dataManager.getGroupById(rs.getInt("groupID"));
            this.sub = plugin.dataManager.getGroupById(rs.getInt("subID"));
        } catch (SQLException ex) {
            Logger.getLogger(DataInheritance.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private int id;
    
    public void setPrimary(int id){
        this.id = id;
    }
    
    public int getPrimary(){
        return id;
    }
    
    private boolean cGroup, cSub;
    private DataGroup group;
    private DataGroup sub;
    
    public void setGroup(DataGroup group){ this.group = group; cGroup = true; }
    public void setSub(DataGroup sub){ this.sub = sub; cSub = true; }
    
    public DataGroup getGroup(){ return group; }
    public DataGroup getSub(){ return sub; }
    
    public void insert() {
        plugin.dataManager.inheritances.add(this);
        plugin.mysqlManager.queries.add(new DataQuery(this, DataManager.QueryType.INSERT));
    }
    
    public void update(){
        plugin.mysqlManager.queries.add(new DataQuery(this, DataManager.QueryType.UPDATE));
    }
    
    public void delete() {
        plugin.dataManager.inheritances.remove(this);
        plugin.mysqlManager.queries.add(new DataQuery(this, DataManager.QueryType.DELETE));
    }
    
    public String prepareQuery(DataManager.QueryType type) {
        if(type.equals(DataManager.QueryType.DELETE)){
            return "DELETE FROM Inheritances WHERE inheritID=" + id;
        }
        String query = null;
        if(!cGroup && !cSub){
            return query;
        }
        List<String> columns = new ArrayList();
        List<String> values = new ArrayList();
        if(cGroup){
            cGroup = false;
            columns.add("groupID");
            values.add(String.valueOf(group.getPrimary()));
        }
        if(cSub){
            cSub = false;
            columns.add("subID");
            values.add(String.valueOf(sub.getPrimary()));
        }
        if(!values.isEmpty() && !columns.isEmpty()){
            if(type == DataManager.QueryType.UPDATE){
                query = "UPDATE Inheritances SET ";
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
                query += " WHERE inheritID=" + getPrimary();
            }
            else if(type == DataManager.QueryType.INSERT){
                query = "INSERT INTO Inheritances (";
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
            return "PermInsertInheritance";
        }
        if(type.equals(DataManager.QueryType.UPDATE)){
            return "PermUpdateInheritance";
        }
        if(type.equals(DataManager.QueryType.DELETE)){
            return "PermDeleteInheritance";
        }
        return null;
    }

    public void synchronize() {
        try {
            Connection conn = plugin.mysqlManager.getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM Inheritances WHERE inheritID=" + id);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                this.group = plugin.dataManager.getGroupById(rs.getInt("groupID"));
                this.sub = plugin.dataManager.getGroupById(rs.getInt("subID"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(DataUser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
