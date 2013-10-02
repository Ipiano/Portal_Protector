package me.ipiano.portal_protector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Ipiano
 */

//Main class, registers all events which must be modified
public class PortalProtector extends JavaPlugin {
    //Distance away from portal for most effects
    public static final Logger m_log = Logger.getLogger("Minecraft");
    public static final String m_notifyKey = "portalprotector.notify";
    public static final String m_timeoutMetaKey = "portalprotector.timeout";
    public static final HashMap<String, String> m_pathMap = new HashMap<String, String>();

    
    //BEGIN SERVER METHODS
    @Override
    public void onEnable(){
        m_log.info("PortalProtector is active");
        PluginManager manager = getServer().getPluginManager();
        getDataFolder().mkdir();
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();
        
        m_pathMap.put("melee","Melee");
        m_pathMap.put("projectile","Projectile");
        m_pathMap.put("anyblockplace","Block placement next to portals");
        m_pathMap.put("partialblockplace","Block placement next to portals with players in them");        
        m_pathMap.put("obsidianplace","Obsidian placement next to portals");
        m_pathMap.put("firestart","Starting fires");
        m_pathMap.put("lavaplace","Lava placement");
        m_pathMap.put("lavaflow","Lava flowing");
        m_pathMap.put("potionsplash","Potion Splash");
        m_pathMap.put("tntexplode","TNT exploding");
        
        //Registers the listener classes
        manager.registerEvents(new EventsHandler(this), this);

        
    }
    
    @Override
    public void onDisable(){
        getLogger().info("PortalProtector is inactive");        
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
	if (cmd.getName().equalsIgnoreCase("PortalProtector")){
            if(args.length == 1){
                if(args[0].equalsIgnoreCase("notify")){
                    
                    if(sender instanceof Player){
                        
                        //Notify command switches the notification status for a player and tells them what it is. Does not save over server reboot, defaults to notifications on
                        
                        Player thisPlr = (Player)sender;
                        boolean curVal = true;
                        try{
                            curVal = getMetavalue(thisPlr, m_notifyKey, this).asBoolean();
                        }catch (Exception ex){m_log.info(ex.getMessage());}
                            setMetadata(thisPlr, m_notifyKey, !curVal, this);
                            String result = ChatColor.GREEN + "on";
                            if(curVal){
                                result = ChatColor.RED + "off";
                            }
                            sender.sendMessage(ChatColor.DARK_PURPLE +"PortalProtector "+ ChatColor.WHITE +"notifications are now " + result);


                    }else{
                        
                        //Doesn't allow the CommandWindow to use this command
                        
                        sender.sendMessage(ChatColor.RED +"Only players can do this");
                    }
                    return true;
                }else if(args[0].equalsIgnoreCase("status")){
                    
                    //Status command displays the status of all plugin effects
                    
                    sender.sendMessage(ChatColor.BLACK + "-----" +ChatColor.DARK_PURPLE +"PortalProtector STATUS" + ChatColor.BLACK + "-----");
                    sender.sendMessage("These effects are only in place near portals");
                    for(Entry<String, String> entry : m_pathMap.entrySet()){
                        boolean setting = getConfig().getBoolean("protection." + entry.getKey());
                        boolean valid = true;
                        if (entry.getKey().equalsIgnoreCase("anyblockplace") && setting == false){
                            valid = false;
                        }else if((entry.getKey().equalsIgnoreCase("partialblockplace") || entry.getKey().equalsIgnoreCase("obsidianplace")) && getConfig().getBoolean("protection.anyblockplace")){
                            valid = false;
                        }
                        if(valid){
                            String output = (ChatColor.GREEN + "protected");
                            if(!setting){
                                output = (ChatColor.RED + "not protected");
                            }
                            sender.sendMessage(ChatColor.DARK_PURPLE + entry.getValue() + ChatColor.WHITE + " is " + output);
                        }
                    }
                    return true;
                }else if(args[0].equalsIgnoreCase("refresh")){
                    
                    //Refresh command reloads the config.yml file, op players only
                    
                    if(sender.hasPermission("portalprotector.manager") || sender instanceof ConsoleCommandSender){
                        reloadConfig();
                        sender.sendMessage("PortalProtector refreshed");
                    }else{
                        sender.sendMessage(ChatColor.DARK_RED +"You don't have permission to do that");
                    }
                    return true;
                }else if(args[0].equalsIgnoreCase("help")){
                    
                    //The help command lists what plugin commands the user can use
                            
                    sender.sendMessage(ChatColor.BLACK + "-----" +ChatColor.DARK_PURPLE +"PortalProtector HELP" + ChatColor.BLACK + "-----");
                    if(!(sender instanceof ConsoleCommandSender)){
                        sender.sendMessage(ChatColor.DARK_PURPLE +"/PortalProtector notify"+ChatColor.WHITE+" - Toggle notification when disallowed actions are attempted");
                    }
                    sender.sendMessage(ChatColor.DARK_PURPLE +"/PortalProtector status"+ChatColor.WHITE+" - See what is currently protected by the plugin");
                    if(sender.hasPermission("portalprotector.manager") || sender instanceof ConsoleCommandSender){
                        sender.sendMessage(ChatColor.DARK_PURPLE +"/PortalProtector refresh"+ChatColor.WHITE+" - Refresh the plugin to load changes to config.yml");
                        sender.sendMessage(ChatColor.DARK_PURPLE +"/PortalProtector <effect><setting>"+ChatColor.WHITE+" - Set whether the plugin protects an event");
                        sender.sendMessage(ChatColor.BLUE+"Example: "+ChatColor.DARK_PURPLE +"/PortalProtector melee unprotect" + ChatColor.WHITE +" would disable melee protection");
                        sender.sendMessage("You can use either " + ChatColor.GREEN + "protect" + ChatColor.WHITE + " or " + ChatColor.RED + "unprotect" + ChatColor.WHITE + " as the setting");
                        sender.sendMessage("For a list of effects that can be changed type " + ChatColor.DARK_PURPLE + "/PortalProtector effects");  
                        sender.sendMessage(ChatColor.DARK_PURPLE +"/PortalProtector timeout <number> "+ChatColor.WHITE+" - Set how long you are protected from attacks after using a portal; if you set it to a negative number, players are always protected when they are near portals");
                        
                    }
                    sender.sendMessage(ChatColor.DARK_PURPLE +"/PortalProtector timeout"+ChatColor.WHITE+" - See how long you are protected from attacks after using a portal");

                    return true;
                }else if(args[0].equalsIgnoreCase("effects")){
                    
                    //Effects lists the name of each affect for the purpose of the <affect><setting> command
                    
                    if(sender.hasPermission("portalprotector.manager") || sender instanceof ConsoleCommandSender){
                        String finalOutput = "The PortalProtector effects are as follows:\n";
                        for(Entry<String, String> entry : m_pathMap.entrySet()){
                            finalOutput += ChatColor.DARK_PURPLE + entry.getKey() + "\n";
                        }
                        sender.sendMessage(finalOutput);
                    }else{
                        sender.sendMessage(ChatColor.DARK_RED +"You don't have permission to do that");
                    }
                    return true;
                }else if(args[0].equalsIgnoreCase("timeout")){
                    String output = ("" + ChatColor.BLUE + getConfig().getInt("timeout"));
                    if(getConfig().getInt("timeout") < 0){
                        output = "" + ChatColor.BLUE + "never";
                    }
                    sender.sendMessage(ChatColor.DARK_PURPLE + "PortalProtector" + ChatColor.WHITE+ " timeout is set to "+output);
                    return true;
                }
            }else if(args.length == 2){
                if(sender.hasPermission("portalprotector.manager")|| sender instanceof ConsoleCommandSender){
                    
                    //<effect><setting> command allows op players to change the config.yml from in-game
                    
                    if(getConfig().contains("protection." + args[0])){
                        try{
                            boolean var = parseBoolean(args[1]);
                            getConfig().set("protection." + args[0], var);
                            saveConfig();
                            String output = ChatColor.GREEN + "protected";
                            if(!var){
                                output = ChatColor.RED + "unprotected";
                            }
                            sender.sendMessage(ChatColor.DARK_PURPLE + args[0] + ChatColor.WHITE + " is "  + output);
                            return true;
                        }catch(Exception ex){
                            sender.sendMessage("Enter"+ ChatColor.GREEN + " protect " + ChatColor.WHITE +"or" + ChatColor.RED+" unprotect " + ChatColor.WHITE+"after an effect name, to see effect names use" + ChatColor.DARK_PURPLE + "/PortalProtector effects");
                            return true;
                        }
                        
                    //Command to set the timeout
                    }else if(args[0].equalsIgnoreCase("timeout")){
                        try{
                            getConfig().set("timeout", Integer.parseInt(args[1]));
                            String output = ("" + ChatColor.BLUE + Integer.parseInt(args[1]));
                            if(Integer.parseInt(args[1]) < 0){
                                output = "" + ChatColor.BLUE + "never";
                            }
                            sender.sendMessage(ChatColor.DARK_PURPLE + "PortalProtector" + ChatColor.WHITE+" timeout set to " +output);
                            saveConfig();
                        }catch(Exception ex){
                            sender.sendMessage("Enter a number after " + ChatColor.DARK_PURPLE + "/timeout" + ChatColor.WHITE + ", if you enter a negative number players will always be protected");
                        }
                        return true;
                    }
                }else{
                    sender.sendMessage(ChatColor.DARK_RED +"You don't have permission to do that");
                    return true;                    
                }
                    
            }
        }
        return false;

    }
    
    public FileConfiguration getProperties(){
        return getConfig();
    }

    //END SERVER METHODS
    //BEGIN HELPER METHODS
    
    public void setMetadata(Player player, String key, Object value, Plugin plugin){
        player.setMetadata(key,new FixedMetadataValue(plugin,value));
    }
    public MetadataValue getMetavalue(Player player, String key, Plugin plugin) throws Exception{
        Player[] online = getServer().getOnlinePlayers();
        for(int i =0; i<online.length; i++){
            if (player == online[i]){
                List<MetadataValue> values = player.getMetadata(key);  
                for(MetadataValue value : values){
                    if(value.getOwningPlugin().getDescription().getName().equals(plugin.getDescription().getName())){
                        return value;
                    }
                }
            }
        }
        throw new Exception("That value or player doesn't exist");
    } 
    
    public boolean parseBoolean(String val) throws Exception  {
        if(val.equalsIgnoreCase("true") || val.equalsIgnoreCase("protect")){
            return true;
        }else if(val.equalsIgnoreCase("false") || val.equalsIgnoreCase("unprotect")){
            return false;
        }
        throw new Exception();
    }

    public void startPlayerTimeout(Player plr){
        setMetadata(plr, m_timeoutMetaKey, System.currentTimeMillis(), this);
    }
    public boolean isPlayerTimedOut(Player plr){
        if(getConfig().getInt("timeout") < 0){
            return true;
        }
        try{
            return (System.currentTimeMillis() - getMetavalue(plr, m_timeoutMetaKey, this).asLong())/1000 < getConfig().getInt("timeout");
        }catch(Exception ex){
            return false;
        }
    }

}
