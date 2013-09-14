/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ipiano.portal_protector;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBucketEmptyEvent;

/**
 *
 * @author Ipiano
 */

//Stops placeing of lava near portals
public class LavaCanceller implements Listener {
    public static PortalProtector plugin;
    public static String path = "protection.lavaplace";
    
    public LavaCanceller(PortalProtector instance)
    {
        plugin = instance;
    }
    
    //If the player tries to put lava near a portal, it is cancelled, and the player does not get lava back
    @EventHandler
    public void onDrop(PlayerBucketEmptyEvent event){
        if(plugin.getProperties().getBoolean(path)){
            Entity entity = event.getPlayer();
            if(event.getBucket() == Material.LAVA_BUCKET){
                if(null != entity){
                    Player plr = (Player)entity;
                    Location loc = event.getBlockClicked().getLocation().add(event.getBlockFace().getModX(), event.getBlockFace().getModY(),event.getBlockFace().getModZ());
                    if(loc.getBlock().getType() == Material.PORTAL && !plugin.isPlayerInPortal(loc, 1,1,1)){
                        //Allows breaking of portals with lava when players are not in them
                    }else if(plugin.inRangeOfPortal(loc) || event.getBlockClicked().getType() == Material.PORTAL){
                            event.setCancelled(true);
                            plr.sendBlockChange(loc, Material.AIR, loc.getBlock().getData());
                            plugin.notifyPlayer(plr,ChatColor.DARK_RED + "You can't put lava there, it's too close to a portal!");
                            plr.updateInventory();
                    }
                }
            }
        }
    }

}
