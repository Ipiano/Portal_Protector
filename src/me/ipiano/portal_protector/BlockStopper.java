/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ipiano.portal_protector;

import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

/**
 *
 * @author Ipiano
 */

//Class prevents players from placing blocks next to a portal at times
public class BlockStopper implements Listener {
    public static PortalProtector plugin;
    public static String blockpath = "protection.blockplace";
    public static String obsidianpath = "protection.obsidianplace";
    

    public BlockStopper(PortalProtector instance)
    {
        plugin = instance;
    }
    
    @EventHandler
    public void onBuild(BlockPlaceEvent event){
        Player plr = event.getPlayer();
        Location loc = event.getBlock().getLocation();
        if(plugin.inRangeOfPortal(loc, 1,0,0) || plugin.inRangeOfPortal(loc, 0,0,1)){
            if(event.getBlock().getType() == Material.OBSIDIAN && plugin.getProperties().getBoolean(obsidianpath)){
                
                //Prevent any obsidian being placed 1 away from a portal block
                
                event.setCancelled(true);
                plugin.notifyPlayer(plr, ChatColor.DARK_RED + "You can't put obsidan that close to a portal!");
                plr.updateInventory();
            }else{
                if(plugin.getProperties().getBoolean(blockpath)){
                    if(plugin.isPlayerInPortal(loc)){

                        //Prevent any block being placed by a portal block if a nearby player is standing in a portal
                        //Could potentially prevent unecessary blocks when portals are excessively close(1-3 blocks away)

                        event.setCancelled(true);
                        plugin.notifyPlayer(plr, ChatColor.DARK_RED + "You can't put blocks near a portal someone is in");
                        plr.updateInventory();
 
                    }
                        
                }
            }
        }
    }
}
