/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ipiano.portal_protector;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

/**
 *
 * @author Andrew
 */
public class TNTCanceller implements Listener {
    public static PortalProtector plugin;
    public TNTCanceller(PortalProtector instance)
    {
        plugin = instance;
    }
    
    @EventHandler
    public void onPrime(EntityExplodeEvent event){
        Entity thisPrime = event.getEntity();
        if(thisPrime.getType() == EntityType.PRIMED_TNT && plugin.inRangeOfPortal(thisPrime.getLocation(), 7,7,7)){
            event.setCancelled(true);
        }

    }      
}