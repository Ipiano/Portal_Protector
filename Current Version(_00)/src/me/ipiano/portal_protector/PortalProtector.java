/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ipiano.portal_protector;

import java.util.logging.Logger;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Andrew
 */
public class PortalProtector extends JavaPlugin {
    private final int RADIUS = 2;

    public static final Logger m_log = Logger.getLogger("Minecraft");
    
    @Override
    public void onEnable(){
        m_log.info("PortalProtector is active");
        PluginManager manager = getServer().getPluginManager();
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
    
    public boolean inRangeOfPortal(Location location){
        return inRangeOfPortal(location, RADIUS, RADIUS, RADIUS);
    }
    public boolean inRangeOfPortal(Location location, int xVar, int yVar, int zVar){
        //Location locationAhead = location.getBlock().getRelative(getPlayerFacing(player), 20).getLocation();
        /*for (int x = -(RADIUS); x <= RADIUS; x++){
            for (int y = -(RADIUS); y <= RADIUS; y++){
                for (int z = -(RADIUS); z <= RADIUS; z++){
                    Location loc = locationAhead.getBlock().getRelative(x, y, z).getLocation();
                    if (!getTransparentMaterials().contains(loc.getBlock().getType())){
                            player.sendBlockChange(loc, Material.SNOW_BLOCK.getId(), (byte) 0);
                    }
                }
            }
        }*/
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

    
}
