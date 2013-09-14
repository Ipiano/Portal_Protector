/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ipiano.portal_protector;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 *
 * @author Ipiano
 */

//Class prevents player-caused melee and projectile damage
public class DamageListener implements Listener {
    public static PortalProtector plugin;
    public static String meleePath = "protection.melee";
    public static String projectilePath = "protection.projectile";
    public DamageListener(PortalProtector instance)
    {
        plugin = instance;
    }
    
    @EventHandler
    public void onTakeDamage(EntityDamageByEntityEvent event){
        Entity entity = event.getEntity();
        boolean validEvent = false;
        Player player = null;
        Player damager = null;
        String path = meleePath;

        //If it was melee damage, flag the player and damager
        if(entity instanceof Player && event.getDamager() instanceof Player){
            player = (Player)entity;
            damager = (Player)event.getDamager();
            validEvent = true;
            path = meleePath;

        //If it was projectile damage, flag the player and the shooter of the projectile
        }else if(event.getDamager() instanceof Projectile){
            Projectile project = (Projectile) event.getDamager();
            if(entity instanceof Player && project.getShooter() instanceof Player){
                player = (Player)entity;
                damager = (Player)project.getShooter();
                project.remove();
                validEvent = true;
                path = projectilePath;
            }
        }
        if(plugin.getProperties().getBoolean(path)){        
            if(validEvent){

                //If either player is in range of a portal, and the damager and damagee were players, cancel the damage

                if(plugin.inRangeOfPortal(player.getLocation()) || plugin.inRangeOfPortal(damager.getLocation())){
                        //PortalProtector.m_log.info("Player [" + player.getDisplayName() + "] is near a portal; negating " + event.getDamage() + " damage");
                        plugin.notifyPlayer(damager, ChatColor.DARK_RED + "You cannot attack this player right now, one of you is too close to a portal.");
                        event.setCancelled(true);
                }
            }
            
        }
    }
    

    

}
