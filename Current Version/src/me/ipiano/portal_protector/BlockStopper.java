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
 * @author Andrew
 */
public class BlockStopper implements Listener {
    public static PortalProtector plugin;
    public BlockStopper(PortalProtector instance)
    {
        plugin = instance;
    }
    
    @EventHandler
    public void onBuild(BlockPlaceEvent event){
        Player plr = event.getPlayer();
        Location loc = event.getBlock().getLocation();
        if(plugin.inRangeOfPortal(loc, 1,1,1)){
            if(event.getBlock().getType() == Material.OBSIDIAN){
                event.setCancelled(true);
                plr.sendMessage(ChatColor.DARK_RED + "You can't put obsidan that close to a portal!");
            }else{
                double radius = 5;
                List<Entity> near = loc.getWorld().getEntities();
                for(Entity e : near) {
                    if(e.getLocation().distance(loc) <= radius && e instanceof Player){
                        if(plugin.inRangeOfPortal(e.getLocation(), 0, 0, 0)){
                            event.setCancelled(true);
                            plr.sendMessage(ChatColor.DARK_RED + "You can't put blocks near a portal someone is in");
                        }
                    } 
                        
                }
            }
        }
    }
}
