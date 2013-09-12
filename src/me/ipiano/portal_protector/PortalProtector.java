package me.ipiano.portal_protector;

import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.permissions.Permission;
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
                    sender.sendMessage("PortalProtector notifications are now...");
                }else if(args[0].equalsIgnoreCase("status")){
                    sender.sendMessage(ChatColor.DARK_PURPLE + "Melee" + ChatColor.WHITE + " is protected: " + ChatColor.BOLD + getConfig().getBoolean(DamageListener.meleePath));
                    sender.sendMessage(ChatColor.DARK_PURPLE + "Projectile"+ ChatColor.WHITE+ " is protected: " + ChatColor.BOLD+ getConfig().getBoolean(DamageListener.projectilePath));
                    sender.sendMessage(ChatColor.DARK_PURPLE +"Potionsplash"+ ChatColor.WHITE+" is protected: " + ChatColor.BOLD+ getConfig().getBoolean(PotionCanceller.path));
                    sender.sendMessage(ChatColor.DARK_PURPLE +"Blockplace is"+ ChatColor.WHITE+" protected: " + ChatColor.BOLD+ getConfig().getBoolean(BlockStopper.blockpath));
                    sender.sendMessage(ChatColor.DARK_PURPLE +"Obsidianplace"+ ChatColor.WHITE+" is protected: " + ChatColor.BOLD+ getConfig().getBoolean(BlockStopper.obsidianpath));
                    sender.sendMessage(ChatColor.DARK_PURPLE +"Lavaplace is"+ ChatColor.WHITE+" protected: " + ChatColor.BOLD+ getConfig().getBoolean(LavaCanceller.path));
                    sender.sendMessage(ChatColor.DARK_PURPLE +"Lavaflow"+ ChatColor.WHITE+" is protected: " + ChatColor.BOLD+ getConfig().getBoolean(LavaStopper.path));
                    sender.sendMessage(ChatColor.DARK_PURPLE +"Tntexplode"+ ChatColor.WHITE+" is protected: " + ChatColor.BOLD+ getConfig().getBoolean(TNTCanceller.path));
                    sender.sendMessage(ChatColor.DARK_PURPLE +"Firestart"+ ChatColor.WHITE+" is protected: " + ChatColor.BOLD+ getConfig().getBoolean(FireCanceller.path));
                    
                }else if(args[0].equalsIgnoreCase("refresh")){
                    if(sender.hasPermission("portalprotector.manager")){
                        reloadConfig();
                        sender.sendMessage("PortalProtector refreshed");
                    }else{
                        sender.sendMessage("You don't have permission to do that");
                    }
                }
            }else if(args.length == 2){
                if(sender.hasPermission("portalprotector.manager")){
                    if(getConfig().contains("protection." + args[0])){
                        try{
                            boolean var = Boolean.parseBoolean(args[1]);
                            getConfig().set("protection." + args[0], var);
                            saveConfig();
                            sender.sendMessage(args[1] + " set to " + var);
                            return true;
                        }catch(Exception ex){
                            sender.sendMessage("Enter true or false after an effect");
                        }
                    }
                }else{
                    sender.sendMessage("You don't have permission to do that");
                    return true;                    
                }
                    
            }
        }
	return false;
    }
    
    public FileConfiguration getProperties(){
        return getConfig();
    }

    
}
