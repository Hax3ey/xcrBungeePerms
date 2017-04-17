package pl.xcrafters.xcrbungeeperms.data;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import pl.xcrafters.xcrbungeeperms.PermsPlugin;

public class DataManager {

    PermsPlugin plugin;
    
    public DataManager(PermsPlugin plugin){
        this.plugin = plugin;
    }
    
    public ConcurrentHashMap<String, DataGroup> groups = new ConcurrentHashMap();
    public ConcurrentHashMap<String, DataUser> users = new ConcurrentHashMap();
    public ConcurrentHashMap<UUID, DataUser> usersByUUID = new ConcurrentHashMap();
    public List<DataPermission> permissions = new CopyOnWriteArrayList();
    public List<DataInheritance> inheritances = new CopyOnWriteArrayList();
    
    public enum QueryType {INSERT, UPDATE, DELETE};
    public enum PermissionType {USER, GROUP};
    
    public DataUser createUser(){
        return new DataUser(plugin);
    }
    
    public DataUser getUserByPlayer(ProxiedPlayer player){
        return getUserByNick(player.getName());
    }
    
    public DataUser getUserByNick(String nick){
        return users.get(nick.toLowerCase());
    }

    public DataUser getUserByUUID(UUID uuid) { return usersByUUID.get(uuid); }
    
    public DataUser getUserById(int id){
        for(DataUser user : users.values()){
            if(user.getPrimary() == id){
                return user;
            }
        }
        return null;
    }
    
    public DataGroup createGroup(){
        return new DataGroup(plugin);
    }
    
    public DataGroup getGroupByName(String groupName){
        return groups.get(groupName.toLowerCase());
    }
    
    public DataGroup getGroupById(int id){
        for(DataGroup group : groups.values()){
            if(group.getPrimary() == id){
                return group;
            }
        }
        return null;
    }

    public DataPermission getPermissionById(int id){
        for(DataPermission perm : permissions){
            if(perm.getPrimary() == id){
                return perm;
            }
        }
        return null;
    }

    public DataInheritance getInheritanceById(int id){
        for(DataInheritance inherit : inheritances){
            if(inherit.getPrimary() == id){
                return inherit;
            }
        }
        return null;
    }
    
    public void addPermission(String permission, PermissionType type, DataInterface object){
        DataPermission perm = new DataPermission(plugin, permission, type, object);
        permissions.add(perm);
    }
    
    public DataPermission getPermission(String permission, PermissionType type, int typeID){
        for(DataPermission perm : permissions){
            if(perm.getPermission().equals(permission) && perm.getType().equals(type) && perm.getObject().getPrimary() == typeID){
                return perm;
            }
        }
        return null;
    }
    
    public DataInheritance getInheritance(DataGroup group, DataGroup sub){
        for(DataInheritance inherit : inheritances){
            if(inherit.getGroup().equals(group) && inherit.getSub().equals(sub)){
                return inherit;
            }
        }
        return null;
    }
    
}
