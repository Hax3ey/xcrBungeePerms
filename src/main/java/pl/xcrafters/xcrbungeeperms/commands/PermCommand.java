package pl.xcrafters.xcrbungeeperms.commands;

import java.util.Map.Entry;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Command;
import pl.xcrafters.xcrbungeeperms.PermsPlugin;
import pl.xcrafters.xcrbungeeperms.data.DataGroup;
import pl.xcrafters.xcrbungeeperms.data.DataInheritance;
import pl.xcrafters.xcrbungeeperms.data.DataManager.PermissionType;
import pl.xcrafters.xcrbungeeperms.data.DataPermission;
import pl.xcrafters.xcrbungeeperms.data.DataUser;

public class PermCommand extends Command {

    PermsPlugin plugin;

    public PermCommand(PermsPlugin plugin) {
        super("perm", "perms.manage", "pm");
        this.plugin = plugin;
        ProxyServer.getInstance().getPluginManager().registerCommand(plugin, this);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sendHelpMessage(sender);
            return;
        }
        try {
            if (args[0].equalsIgnoreCase("user")) {
                String nick = args[1];
                DataUser user = plugin.dataManager.getUserByNick(nick);
                if (user == null) {
                    user = plugin.dataManager.createUser();
                    user.setNickname(nick);
                    user.setGroup(plugin.dataManager.getGroupByName("default"));
                    user.insert();
                }
                if (args[2].equalsIgnoreCase("addperm") || args[2].equalsIgnoreCase("addpermission")) {
                    String perm = args[3];
                    if (user.getPermissions().contains(perm)) {
                        sender.sendMessage(plugin.color("&2Gracz &c" + user.getNickname() + " &2posiada juz uprawnienie &c" + perm + "&2."));
                        return;
                    }
                    DataPermission permission = new DataPermission(plugin, perm, PermissionType.USER, user);
                    permission.insert();
                    sender.sendMessage(plugin.color("&2Poprawnie nadano uprawnienie &c" + perm + " &2graczowi &c" + user.getNickname() + "&2."));
                    return;
                }
                if (args[2].equalsIgnoreCase("remperm") || args[2].equalsIgnoreCase("removeperm") || args[2].equalsIgnoreCase("removepermission")) {
                    String perm = args[3];
                    if (!user.getPermissions().contains(perm)) {
                        sender.sendMessage(plugin.color("&2Gracz &c" + user.getNickname() + " &2nie posiada uprawnienia &c" + perm + "&2."));
                        return;
                    }
                    DataPermission permission = plugin.dataManager.getPermission(perm, PermissionType.USER, user.getPrimary());
                    permission.delete();
                    sender.sendMessage(plugin.color("&2Poprawnie usunieto uprawnienie &c" + perm + " &2graczowi &c" + user.getNickname() + "&2."));
                    return;
                }
                if(args[2].equalsIgnoreCase("setperm") || args[2].equalsIgnoreCase("setpermission")){
                    String perm = args[3];
                    if (!user.getPermissions().contains(perm)) {
                        sender.sendMessage(plugin.color("&2Gracz &c" + user.getNickname() + " &2nie posiada uprawnienia &c" + perm + "&2."));
                        return;
                    }
                    boolean value = Boolean.valueOf(args[4]);
                    DataPermission permission = plugin.dataManager.getPermission(perm, PermissionType.USER, user.getPrimary());
                    permission.setValue(value);
                    permission.update();
                    sender.sendMessage(plugin.color("&2Poprawnie ustawiono uprawnienie &c" + perm + " &2graczowi &c" + user.getNickname() + "&2."));
                    return;
                }
                if(args[2].equalsIgnoreCase("setgroup")){
                    String name = args[3];
                    if(name.equalsIgnoreCase("default")){
                        user.delete();
                        sender.sendMessage(plugin.color("&2Ustawiono grupe &cdefault&2 dla gracza &c" + user.getNickname() + "!"));
                    } else {
                        DataGroup group = plugin.dataManager.getGroupByName(name);
                        if(group == null){
                            sender.sendMessage(plugin.color("&2Nie znaleziono grupy o nazwie &c" + name + "&2!"));
                            return;
                        }
                        user.setGroup(group);
                        user.update();
                        sender.sendMessage(plugin.color("&2Ustawiono grupe &c" + group.getGroupName() + "&2 dla gracza &c" + user.getNickname() + "!"));
                    }
                }
                if (args[2].equalsIgnoreCase("info")) {
                    sender.sendMessage(plugin.color("&2Nick: &c" + user.getNickname()));
                    sender.sendMessage(plugin.color("&2Grupa: &c" + (user.getGroup() != null ? user.getGroup().getGroupName() : "-")));
                    String perms = plugin.color("&2Uprawnienia: &c");
                    for (String perm : user.getPermissions()) {
                        perms += perm + ",";
                    }
                    sender.sendMessage(perms);
                }
            }
            if (args[0].equalsIgnoreCase("group")) {
                String name = args[1];
                DataGroup group = plugin.dataManager.getGroupByName(name);
                if(args[2].equalsIgnoreCase("create")){
                    if(group != null){
                        sender.sendMessage(plugin.color("&2Grupa o nazwie &c" + name + "&2 juz istnieje!"));
                        return;
                    }
                    group = plugin.dataManager.createGroup();
                    group.setGroupName(name);
                    group.insert();
                    sender.sendMessage(plugin.color("&2Stworzono grupe o nazwie &c" + name + "&2!"));
                } else if (group == null) {
                    sender.sendMessage(plugin.color("&2Nie znaleziono grupy o nazwie &c" + name + "&2!"));
                    return;
                }
                if (args[2].equalsIgnoreCase("addperm") || args[2].equalsIgnoreCase("addpermission")) {
                    String perm = args[3];
                    if (group.getGroupPermissions().get(perm) != null) {
                        sender.sendMessage(plugin.color("&2Grupa &c" + group.getGroupName() + " &2posiada juz uprawnienie &c" + perm + "&2."));
                        return;
                    }
                    DataPermission permission = new DataPermission(plugin, perm, PermissionType.GROUP, group);
                    permission.insert();
                    sender.sendMessage(plugin.color("&2Poprawnie nadano uprawnienie &c" + perm + " &2grupie &c" + group.getGroupName() + "&2."));
                    return;
                }
                if (args[2].equalsIgnoreCase("remperm") || args[2].equalsIgnoreCase("removeperm") || args[2].equalsIgnoreCase("removepermission")) {
                    String perm = args[3];
                    if (group.getGroupPermissions().get(perm) == null) {
                        sender.sendMessage(plugin.color("&2Grupa &c" + group.getGroupName() + " &2nie posiada uprawnienia &c" + perm + "&2."));
                        return;
                    }
                    DataPermission permission = plugin.dataManager.getPermission(perm, PermissionType.GROUP, group.getPrimary());
                    permission.delete();
                    sender.sendMessage(plugin.color("&2Poprawnie usunieto uprawnienie &c" + perm + " &2grupie &c" + group.getGroupName() + "&2."));
                    return;
                }
                if(args[2].equalsIgnoreCase("setperm") || args[2].equalsIgnoreCase("setpermission")){
                    String perm = args[3];
                    if (group.getGroupPermissions().get(perm) == null) {
                        sender.sendMessage(plugin.color("&2Grupa &c" + group.getGroupName() + " &2nie posiada uprawnienia &c" + perm + "&2."));
                        return;
                    }
                    boolean value = Boolean.valueOf(args[4]);
                    DataPermission permission = plugin.dataManager.getPermission(perm, PermissionType.GROUP, group.getPrimary());
                    permission.setValue(value);
                    permission.update();
                    sender.sendMessage(plugin.color("&2Poprawnie ustawiono uprawnienie &c" + perm + " &2grupie &c" + group.getGroupName() + "&2."));
                    return;
                }
                if(args[2].equalsIgnoreCase("addinherit") || args[2].equalsIgnoreCase("addinheritance")){
                    DataGroup sub = plugin.dataManager.getGroupByName(args[3]);
                    if(sub == null){
                        sender.sendMessage(plugin.color("&2Nie znaleziono grupy o nazwie &c" + args[3] + "&2!"));
                        return;
                    }
                    DataInheritance inherit = new DataInheritance(plugin, group, sub);
                    inherit.insert();
                    sender.sendMessage(plugin.color("&2Dodano podgrupe &c" + sub.getGroupName() + " &2dla grupy &c" + group.getGroupName() + "&2!"));
                }
                if(args[2].equalsIgnoreCase("reminherit") || args[2].equalsIgnoreCase("removeinherit") || args[2].equalsIgnoreCase("reminheritance") || args[2].equalsIgnoreCase("removeinheritance")){
                    DataGroup sub = plugin.dataManager.getGroupByName(args[3]);
                    if(sub == null){
                        sender.sendMessage(plugin.color("&2Nie znaleziono grupy o nazwie &c" + name + "&2!"));
                        return;
                    }
                    DataInheritance inherit = plugin.dataManager.getInheritance(group, sub);
                    if(inherit == null){
                        sender.sendMessage(plugin.color("&2Grupa &c" + sub.getGroupName() + " &2nie jest podgrupa grupy &c" + group.getGroupName() + "&2!"));
                        return;
                    }
                    inherit.delete();
                    sender.sendMessage(plugin.color("&2Usunieto podgrupe &c" + sub.getGroupName() + " &2dla grupy &c" + group.getGroupName() + "&2!"));
                }
                if (args[2].equalsIgnoreCase("info")) {
                    sender.sendMessage(plugin.color("&2Nazwa: &c" + group.getGroupName()));
                    String perms = plugin.color("&2Uprawnienia: &c");
                    for (Entry<String, Boolean> perm : group.getGroupPermissions().entrySet()) {
                        if(perm.getValue()){
                            perms += perm + ",";
                        }
                    }
                    sender.sendMessage(perms);
                }
                if(args[2].equalsIgnoreCase("delete") || args[2].equalsIgnoreCase("remove")) {
                    for(DataInheritance inheritance : plugin.dataManager.inheritances) {
                        if(inheritance.getGroup() == group || inheritance.getSub() == group) {
                            inheritance.delete();
                        }
                    }
                    for(DataUser user : plugin.dataManager.users.values()) {
                        if(user.getGroup() == group) {
                            user.setGroup(plugin.dataManager.getGroupByName("default"));
                            user.update();
                        }
                    }
                    group.delete();
                    sender.sendMessage(plugin.color("&2Usunieto grupe o nazwie &c" + name + "&2!"));
                }
                if(args[2].equalsIgnoreCase("rename")) {
                    String groupName = args[3];
                    sender.sendMessage(plugin.color("&2Poprawnie zmieniono nazwe grupy &c" + group.getGroupName() + " &2na &c" + groupName + "&2!"));
                    group.setGroupName(groupName);
                    group.update();
                }
            }
        } catch (ArrayIndexOutOfBoundsException ex) {
            sendHelpMessage(sender);
        }
    }

    public void sendHelpMessage(CommandSender sender) {
        sender.sendMessage(plugin.color("&c/perm user <nick> info &2- wyswietla informacje o danym graczu."));
        sender.sendMessage(plugin.color("&c/perm user <nick> addperm <uprawnienie> &2- nadaje graczowi podane uprawnienie."));
        sender.sendMessage(plugin.color("&c/perm user <nick> remperm <uprawnienie> &2- usuwa graczowi podane uprawnienie."));
        sender.sendMessage(plugin.color("&c/perm user <nick> setperm <uprawnienie> <true/false> &2- ustawia graczowi dane uprawnienie."));
        sender.sendMessage(plugin.color("&c/perm group <nazwa> info &2- wyswietla informacje o danej grupie."));
        sender.sendMessage(plugin.color("&c/perm group <nazwa> create &2- tworzy grupe o podanej nazwie."));
        sender.sendMessage(plugin.color("&c/perm group <nazwa> remove &2- usuwa grupe o podanej nazwie."));
        sender.sendMessage(plugin.color("&c/perm group <nazwa> addperm <uprawnienie> &2- nadaje grupie podane uprawnienie."));
        sender.sendMessage(plugin.color("&c/perm group <nazwa> remperm <uprawnienie> &2- usuwa grupie podane uprawnienie."));
        sender.sendMessage(plugin.color("&c/perm group <nazwa> setperm <uprawnienie> <true/false> &2- ustawia grupie dane uprawnienie."));
        sender.sendMessage(plugin.color("&c/perm group <nazwa> addinherit <grupa> &2- dodaje podgrupe dla danej grupy."));
        sender.sendMessage(plugin.color("&c/perm group <nazwa> reminherit <grupa> &2- usuwa podgrupe dla danej grupy."));
    }

}
