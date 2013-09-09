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
 * @author Andrew
 */
public class LavaCanceller implements Listener {
    public static PortalProtector plugin;
    public LavaCanceller(PortalProtector instance)
    {
        plugin = instance;
    }
    
    @EventHandler
    public void onDrop(PlayerBucketEmptyEvent event){
        Entity entity = event.getPlayer();
        if(event.getBucket() == Material.LAVA_BUCKET){
            if(null != entity){
                Player plr = (Player)entity;
                Location loc = event.getBlockClicked().getLocation().subtract(event.getBlockFace().getModX(), event.getBlockFace().getModY(),event.getBlockFace().getModZ());
                if(placedAbovePortal(loc)){
                    event.setCancelled(true);
                    plr.sendMessage(ChatColor.DARK_RED + "You can't put lava there, it's too close to a portal!");
                }
            }
        }
    }
    
    private boolean placedAbovePortal(Location loc){
        if(plugin.inRangeOfPortal(loc)){
            return true;
        }
        return false;
    }
}
