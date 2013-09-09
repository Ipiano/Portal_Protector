/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ipiano.portal_protector;

import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;

/**
 *
 * @author Andrew
 */
public class LavaStopper implements Listener {
    public static PortalProtector plugin;
    public LavaStopper(PortalProtector instance)
    {
        plugin = instance;
    }
    
    @EventHandler
    public void onFlow(BlockFromToEvent event){
        Material startType = event.getBlock().getType();
        Material lava = Material.STATIONARY_LAVA;
        Material lavaFlow = Material.LAVA;
        
        if((startType == lava || startType == lavaFlow) && plugin.inRangeOfPortal(event.getBlock().getLocation())){
            event.getBlock().setType(Material.AIR);
            event.setCancelled(true);
        }
    }  
}
