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
 * @author Andrew
 */
public class FireCanceller implements Listener {
    public static PortalProtector plugin;
    public FireCanceller(PortalProtector instance)
    {
        plugin = instance;
    }
    
    @EventHandler
    public void onBurn(BlockIgniteEvent event){
        Entity entity = event.getPlayer();
        if(null != entity){
            Player plr = (Player)entity;
            Location loc = event.getBlock().getLocation();
            if(plugin.inRangeOfPortal(loc)){
                event.setCancelled(true);
                plr.sendMessage(ChatColor.DARK_RED + "You can't start a fire that close to a portal.");
            }
        }
    }
}
