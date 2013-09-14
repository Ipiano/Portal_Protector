package me.ipiano.portal_protector;

import java.util.List;
import java.util.logging.Logger;
import static me.ipiano.portal_protector.BlockStopper.plugin;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
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
    private final int RADIUS = 2;
    public static final Logger m_log = Logger.getLogger("Minecraft");
    private static final String notifyKey = "portalprotector.notify";
    
    @Override
    public void onEnable(){
        m_log.info("PortalProtector is active");
        PluginManager manager = getServer().getPluginManager();
        getDataFolder().mkdir();
        saveDefaultConfig();
        
        //Registers the listener classes
        manager.registerEvents(new DamageListener(this), this);
        manager.registerEvents(new PotionCanceller(this), this);  
        manager.registerEvents(new FireCanceller(this), this);    
        manager.registerEvents(new LavaCanceller(this), this); 
        manager.registerEvents(new LavaStopper(this), this);   
        manager.registerEvents(new BlockStopper(this), this); 
        manager.registerEvents(new TNTCanceller(this), this);  

        
    }
    
    @Override
    public void onDisable(){
        getLogger().info("PortalProtector is inactive");        
    }
    public boolean isPlayerInPortal(Location loc){
        return isPlayerInPortal(loc, 0,0,0);
    }
    public boolean isPlayerInPortal(Location loc, int x, int y, int z){
        List<Entity> near = loc.getWorld().getEntities();
        for(Entity e : near) {
            if(e.getLocation().distance(loc) <= 2 && e instanceof Player){
                if(inRangeOfPortal(e.getLocation(), x, y, z)){
                    return true;
                }
            }
        }
        return false;
    }
    //Method to check if a location is in range of a portal, default distance is the RADIUS constant
    public boolean inRangeOfPortal(Location location){
        return inRangeOfPortal(location, RADIUS, RADIUS, RADIUS);
    }
    public boolean inRangeOfPortal(Location location, int xVar, int yVar, int zVar){
        for (int x = -(xVar); x <= xVar; x++){
            for (int y = -(yVar); y <= yVar; y++){
                for (int z = -(zVar); z <= zVar; z++){
                    Location loc = location.getBlock().getRelative(x, y, z).getLocation();
                    if (loc.getBlock().getType() == Material.PORTAL){
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
	if (cmd.getName().equalsIgnoreCase("PortalProtector")){
            if(args.length == 1){
                if(args[0].equalsIgnoreCase("notify")){
                    if(sender instanceof Player){
                        Player thisPlr = (Player)sender;
                        boolean curVal = true;
                        try{
                            curVal = getMetaboolean(thisPlr, notifyKey, this);
                        }catch (Exception ex){m_log.info(ex.getMessage());}
                            setMetadata(thisPlr, notifyKey, !curVal, this);
                            String result = ChatColor.GREEN + "on";
                            if(curVal){
                                result = ChatColor.RED + "off";
                            }
                            sender.sendMessage(ChatColor.DARK_PURPLE +"PortalProtector "+ ChatColor.WHITE +"notifications are now " + result);


                    }else{
                        sender.sendMessage(ChatColor.RED +"Only players can do this");
                    }
                    return true;
                }else if(args[0].equalsIgnoreCase("status")){
                    sender.sendMessage(ChatColor.BLACK + "-----" +ChatColor.DARK_PURPLE +"PortalProtector STATUS" + ChatColor.BLACK + "-----");
                    sender.sendMessage("These events are only affected near portals");
                    sender.sendMessage(ChatColor.DARK_PURPLE + "Melee" + ChatColor.WHITE + " protection is " + ChatColor.BOLD + getConfig().getBoolean(DamageListener.meleePath));
                    sender.sendMessage(ChatColor.DARK_PURPLE + "Projectile"+ ChatColor.WHITE+ " protection is " + ChatColor.BOLD+ getConfig().getBoolean(DamageListener.projectilePath));
                    sender.sendMessage(ChatColor.DARK_PURPLE +"Potion Splash"+ ChatColor.WHITE+" protection is " + ChatColor.BOLD+ getConfig().getBoolean(PotionCanceller.path));
                    sender.sendMessage(ChatColor.DARK_PURPLE +"Block placement(When a player is in the portal)"+ ChatColor.WHITE+" protection is " + ChatColor.BOLD+ getConfig().getBoolean(BlockStopper.blockpath));
                    sender.sendMessage(ChatColor.DARK_PURPLE +"Obsidian placement(Adjecent to portals)"+ ChatColor.WHITE+" protection is " + ChatColor.BOLD+ getConfig().getBoolean(BlockStopper.obsidianpath));
                    sender.sendMessage(ChatColor.DARK_PURPLE +"Lava placement"+ ChatColor.WHITE+" protection is " + ChatColor.BOLD+ getConfig().getBoolean(LavaCanceller.path));
                    sender.sendMessage(ChatColor.DARK_PURPLE +"Lava flowing"+ ChatColor.WHITE+" protection is " + ChatColor.BOLD+ getConfig().getBoolean(LavaStopper.path));
                    sender.sendMessage(ChatColor.DARK_PURPLE +"TNT exploding"+ ChatColor.WHITE+" protection is " + ChatColor.BOLD+ getConfig().getBoolean(TNTCanceller.path));
                    sender.sendMessage(ChatColor.DARK_PURPLE +"Starting fires"+ ChatColor.WHITE+" protection is " + ChatColor.BOLD+ getConfig().getBoolean(FireCanceller.path));
                    return true;
                }else if(args[0].equalsIgnoreCase("refresh")){
                    if(sender.hasPermission("portalprotector.manager") || sender instanceof ConsoleCommandSender){
                        reloadConfig();
                        sender.sendMessage("PortalProtector refreshed");
                    }else{
                        sender.sendMessage(ChatColor.DARK_RED +"You don't have permission to do that");
                    }
                    return true;
                }else if(args[0].equalsIgnoreCase("help")){
                    sender.sendMessage(ChatColor.BLACK + "-----" +ChatColor.DARK_PURPLE +"PortalProtector HELP" + ChatColor.BLACK + "-----");
                    sender.sendMessage(ChatColor.DARK_PURPLE +"/PortalProtector notify"+ChatColor.WHITE+" - Toggle notification when disallowed actions are attempted");
                    sender.sendMessage(ChatColor.DARK_PURPLE +"/PortalProtector status"+ChatColor.WHITE+" - See what is currently protected by the plugin");
                    if(sender.hasPermission("portalprotector.manager") || sender instanceof ConsoleCommandSender){
                        sender.sendMessage(ChatColor.DARK_PURPLE +"/PortalProtector refresh"+ChatColor.WHITE+" - Refresh the plugin to load changes to config.yml");
                        sender.sendMessage(ChatColor.DARK_PURPLE +"/PortalProtector <event><setting>"+ChatColor.WHITE+" - Set whether the plugin protects an event");
                        sender.sendMessage(ChatColor.BLUE+"Example: "+ChatColor.DARK_PURPLE +"/PortalProtector melee false" + ChatColor.WHITE +" would disable melee protection");
                        sender.sendMessage("For a list of events that can be changed type " + ChatColor.DARK_PURPLE + "/PortalProtector events");
                        
                    }
                    return true;
                }else if(args[0].equalsIgnoreCase("events")){
                    if(sender.hasPermission("portalprotector.manager") || sender instanceof ConsoleCommandSender){
                        sender.sendMessage("The PortalProtector events are as follows:\n" + ChatColor.DARK_PURPLE +  
                                                                                        "melee\n" +
                                                                                        "projectile\n" +
                                                                                        "potionsplash\n" +
                                                                                        "blockplace\n" +
                                                                                        "obsidianplace\n" +
                                                                                        "lavaplace\n" +
                                                                                        "lavaflow\n" +
                                                                                        "tntexplode\n" +
                                                                                        "firestart");
                        
                    }else{
                        sender.sendMessage(ChatColor.DARK_RED +"You don't have permission to do that");
                    }
                    return true;
                }
            }else if(args.length == 2){
                if(sender.hasPermission("portalprotector.manager")|| sender instanceof ConsoleCommandSender){
                    if(getConfig().contains("protection." + args[0])){
                        try{
                            boolean var = parseBoolean(args[1]);
                            getConfig().set("protection." + args[0], var);
                            saveConfig();
                            sender.sendMessage(ChatColor.DARK_PURPLE + args[0] + ChatColor.WHITE + " protection set to " + ChatColor.BOLD + var);
                            return true;
                        }catch(Exception ex){
                            sender.sendMessage("Enter"+ ChatColor.GREEN + " true " + ChatColor.WHITE +"or" + ChatColor.RED+" false " + ChatColor.WHITE+"after an effect name, to see effect names use" + ChatColor.DARK_PURPLE + "/PortalProtector events");
                            return true;
                        }
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
    
    public void notifyPlayer(Player player, String message){
        try{
            if(getMetaboolean(player, notifyKey, this)){
                player.sendMessage(message);
            }
        }catch(Exception ex){player.sendMessage(message);}
    }
    
    public void setMetadata(Player player, String key, Object value, Plugin plugin){
        player.setMetadata(key,new FixedMetadataValue(plugin,value));
    }
    public boolean getMetaboolean(Player player, String key, Plugin plugin) throws Exception{
        Player[] online = getServer().getOnlinePlayers();
        for(int i =0; i<online.length; i++){
            if (player == online[i]){
                List<MetadataValue> values = player.getMetadata(key);  
                for(MetadataValue value : values){
                    if(value.getOwningPlugin().getDescription().getName().equals(plugin.getDescription().getName())){
                        return value.asBoolean();
                    }
                }
            }
        }
        throw new Exception("That value or player doesn't exist");
    } 
    
    public boolean parseBoolean(String val) throws Exception  {
        if(val.equalsIgnoreCase("true") || val.equalsIgnoreCase("enable")){
            return true;
        }else if(val.equalsIgnoreCase("false") || val.equalsIgnoreCase("disable")){
            return false;
        }
        throw new Exception();
    }
}
