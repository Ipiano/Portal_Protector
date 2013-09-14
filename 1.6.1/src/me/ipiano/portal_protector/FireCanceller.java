/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ipiano.portal_protector;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockIgniteEvent;

/**
 *
 * @author Ipiano
 */

//Class cancels fire near portals
public class FireCanceller implements Listener {
    public static PortalProtector plugin;
    public static String path = "protection.firestart";
    public FireCanceller(PortalProtector instance)
    {
        plugin = instance;
    }

    //If a player tries to light a fire next to a portal, it's canceled
    @EventHandler
    public void onBurn(BlockIgniteEvent event){
        if(plugin.getProperties().getBoolean(path)){       
            Entity entity = event.getPlayer();
            if(null != entity){
                Player plr = (Player)entity;
                Location loc = event.getBlock().getLocation();
                if(plugin.inRangeOfPortal(loc, 1,1,1)){
                        event.setCancelled(true);
                        plugin.notifyPlayer(plr,ChatColor.DARK_RED + "You can't start a fire that close to a portal.");
                        plr.updateInventory();
                }
            }
        }
    }
}
