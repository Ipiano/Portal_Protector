/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ipiano.portal_protector;

import java.util.ArrayList;
import java.util.Iterator;
import static me.ipiano.portal_protector.EventsHandler.anyBlockPath;
import static me.ipiano.portal_protector.EventsHandler.obsidianPath;
import static me.ipiano.portal_protector.EventsHandler.plugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author Andrew
 */
public class PortalClearRun extends BukkitRunnable {
    
    PlayerPortalEvent m_event;
    PortalProtector m_plugin;
    
    public PortalClearRun(PlayerPortalEvent event, PortalProtector plugin){
        m_event = event;
        m_plugin = plugin;
    }

    @Override
    public void run() {
        Location result = m_event.getPlayer().getLocation();
        try{
            Object[] portal = EventsHandler.getPortal(result.getBlock());
            ArrayList<Block> frameBlocks = (ArrayList<Block>)portal[1];
            ArrayList<Block> portalBlocks = (ArrayList<Block>)portal[0];
            boolean zAxis = (Boolean)portal[2];
            if(zAxis){
                plugin.debug("Portal is on Z axis at x of " + portalBlocks.get(1).getLocation().getX());
            }else{
                plugin.debug("Portal is on X axis at z of " + portalBlocks.get(1).getLocation().getZ());
            }
            for (Block thisPortal : portalBlocks) {
                Block[] suspicious = new Block[2];
                if(zAxis){
                    suspicious[0] = thisPortal.getRelative(1,0,0);
                    suspicious[1] = thisPortal.getRelative(-1, 0, 0);
                }else{
                    suspicious[0] = thisPortal.getRelative(0,0,1);
                    suspicious[1] = thisPortal.getRelative(0, 0, -1);
                }
                for(int i=0; i < suspicious.length; i++){
                    if(plugin.getProperties().getBoolean(anyBlockPath)){                  
                        suspicious[i].setType(Material.AIR);
                        plugin.debug("Cleared at " + suspicious[i].getLocation() + " to air");
                    }else if(plugin.getProperties().getBoolean(obsidianPath)){
                        if(suspicious[i].getType() == Material.OBSIDIAN){
                            suspicious[i].setType(Material.AIR);  
                            plugin.debug("Cleared at " + suspicious[i].getLocation() + " to air"); 
                        }
                    }
                }
            }
        }catch(Exception ex){
            m_plugin.debug(ex.getMessage());
        }
    }
}
