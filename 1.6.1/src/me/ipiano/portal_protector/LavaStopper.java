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
 * @author Ipiano
 */

//Class stops and removes lava near portals
public class LavaStopper implements Listener {
    public static PortalProtector plugin;
    public static String path = "protection.lavaflow";
    
    public LavaStopper(PortalProtector instance)
    {
        plugin = instance;
    }
    
    
    //If any lava attempts to flow near or into a space near a portal, the block that caused the event is removed and the lava does not flow
    //Will remove pre-existing lava from near portals over time, but only if the lava is disturbed, causing a reason for it to flow
    @EventHandler
    public void onFlow(BlockFromToEvent event){
        if(plugin.getProperties().getBoolean(path)){
            Material startType = event.getBlock().getType();
            Material lava = Material.STATIONARY_LAVA;
            Material lavaFlow = Material.LAVA;

            if((startType == lava || startType == lavaFlow) && plugin.inRangeOfPortal(event.getBlock().getLocation())){
                    event.getBlock().setType(Material.AIR);
                    event.setCancelled(true);
            }
        }
    }  
}
