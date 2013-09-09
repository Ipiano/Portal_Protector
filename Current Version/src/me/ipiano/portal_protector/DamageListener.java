/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ipiano.portal_protector;

import java.util.Collection;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;

/**
 *
 * @author Andrew
 */
public class DamageListener implements Listener {
    public static PortalProtector plugin;
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
        if(entity instanceof Player && event.getDamager() instanceof Player){
            player = (Player)entity;
            damager = (Player)event.getDamager();
            validEvent = true;
        }else if(event.getDamager() instanceof Projectile){
            Projectile project = (Projectile) event.getDamager();
            if(entity instanceof Player && project.getShooter() instanceof Player){
                player = (Player)entity;
                damager = (Player)project.getShooter();
                project.remove();
                validEvent = true;
            }
        }
        if(validEvent){
            if(plugin.inRangeOfPortal(player.getLocation()) || plugin.inRangeOfPortal(damager.getLocation())){
                //PortalProtector.m_log.info("Player [" + player.getDisplayName() + "] is near a portal; negating " + event.getDamage() + " damage");
                damager.sendMessage(ChatColor.DARK_RED + "You cannot attack this player right now, one of you is too close to a portal.");
                event.setCancelled(true);
            }
            
        }
    }
    

    

}