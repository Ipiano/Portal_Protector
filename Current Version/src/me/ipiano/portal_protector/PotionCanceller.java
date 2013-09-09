/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ipiano.portal_protector;

import java.util.ArrayList;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PotionSplashEvent;

/**
 *
 * @author Andrew
 */
public class PotionCanceller implements Listener {
    public static PortalProtector plugin;
    public PotionCanceller(PortalProtector instance)
    {
        plugin = instance;
    }
    
    @EventHandler
    public void onMakeSplash(PotionSplashEvent event){
        Entity entity = event.getEntity();
        if (entity instanceof ThrownPotion){
            ArrayList<Player> plrsAffected = new ArrayList();
            ThrownPotion potion = (ThrownPotion)entity;
            boolean validEvent = false;
            Player damager = null;
            if(potion.getShooter() instanceof Player){
                LivingEntity[] affected = event.getAffectedEntities().toArray(new LivingEntity[event.getAffectedEntities().size()]);
                damager = (Player)potion.getShooter();
                for(int i=0; i<affected.length;i++){
                    if(affected[i] instanceof Player){
                        plrsAffected.add((Player)affected[i]);
                    }
                }
                validEvent = true;
            }
            if(validEvent){
                Player[] finalPlrs = plrsAffected.toArray(new Player[plrsAffected.size()]);
                if(plugin.inRangeOfPortal(damager.getLocation())){
                    //PortalProtector.m_log.info("Player [" + player.getDisplayName() + "] is near a portal; negating " + event.getDamage() + " damage");
                    damager.sendMessage(ChatColor.DARK_RED + "You cannot attack this player right now, you are too close to a portal.");
                    for (int i = 0; i < finalPlrs.length; i++){
                        event.setIntensity(finalPlrs[i], 0);
                    }
                    event.setCancelled(true);
                }else{
                    boolean message = false;
                    for(int i = 0; i < finalPlrs.length; i++){
                        if(plugin.inRangeOfPortal(finalPlrs[i].getLocation())){
                            event.setIntensity(finalPlrs[i], 0);
                            message = true;
                        }
                    }
                    if (message){
                        damager.sendMessage(ChatColor.DARK_RED + "Some players were not affected by the potion because they were near a portal.");
                    }
                }

            }
        }
    }
}
