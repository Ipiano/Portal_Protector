/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ipiano.portal_protector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.scheduler.BukkitTask;

/**
 *
 * @author Andrew
 */
public class EventsHandler implements Listener{
    public static PortalProtector plugin;

    public final static String meleePath = "protection.melee";
    public final static String projectilePath = "protection.projectile";
    public final static String anyBlockPath = "protection.anyblockplace";
    public final static String partialBlockPath = "protection.partialblockplace";
    public final static String obsidianPath = "protection.obsidianplace";
    public final static String firePath = "protection.firestart";
    public final static String lavaPlacePath = "protection.lavaplace";
    public final static String lavaFlowPath = "protection.lavaflow";
    public final static String potionPath = "protection.potionsplash";
    public final static String tntPath = "protection.tntexplode";
    public final static String autoClearPath = "autoclear";

   

    
    public EventsHandler(PortalProtector instance)
    {
        plugin = instance;
    }
    
    //BEGIN EVENT HANDLERS

    @EventHandler
    public void portalTeleport(PlayerPortalEvent event){
            plugin.startPlayerTimeout(event.getPlayer());
            if(plugin.getProperties().getInt("timeout") >= 0){
                event.getPlayer().sendMessage(ChatColor.BLUE + "You are protected from melee, projectile, and potion effects for " + plugin.getProperties().getInt("timeout") + " seconds");
            }
            //Possibly remove existing blocks near portals?
            //PortalProtector.m_log.info("Autoclearing set to "  + plugin.getProperties().getBoolean(autoClearPath));
            if(plugin.getProperties().getBoolean(autoClearPath)){
                BukkitTask task = new PortalClearRun(event, plugin).runTaskLater(plugin, 10);
            }

   
    }
    
    //Handler for melee and projectile damage
    @EventHandler
    public void onTakeDamage(EntityDamageByEntityEvent event){
        Entity entity = event.getEntity();
        boolean validEvent = false;
        Player player = null;
        Player damager = null;
        String path = meleePath;
        if(plugin.getProperties().getBoolean(meleePath)||plugin.getProperties().getBoolean(projectilePath)){
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
        }
        if(plugin.getProperties().getBoolean(path)){        
            if(validEvent){

                //If either player is in range of a portal, and the damager and damagee were players, cancel the damage

                if(inRangeOfPortal(player.getLocation()) && plugin.isPlayerTimedOut(player) || inRangeOfPortal(damager.getLocation()) && plugin.isPlayerTimedOut(damager)){
                        //PortalProtector.m_log.info("Player [" + player.getDisplayName() + "] is near a portal; negating " + event.getDamage() + " damage");
                        notifyPlayer(damager, ChatColor.DARK_RED + "You cannot attack this player right now, one of you is too close to a portal.");
                        event.setCancelled(true);
                }
            }
            
        }
    }    
    
    //Handler for placement of blocks
    @EventHandler
    public void onBuild(BlockPlaceEvent event){
        Player plr = event.getPlayer();
        Location loc = event.getBlock().getLocation();
        if(inRangeOfPortal(loc, 1,0,0) || inRangeOfPortal(loc, 0,0,1)){
            if(plugin.getProperties().getBoolean(anyBlockPath) && event.getBlock().getType() != Material.PORTAL){
                event.setCancelled(true);
                notifyPlayer(plr, ChatColor.DARK_RED + "You can't put blocks near a portal");
                plr.updateInventory();
            }else if(event.getBlock().getType() == Material.OBSIDIAN && plugin.getProperties().getBoolean(obsidianPath)){
                
                //Prevent any obsidian being placed 1 away from a portal block
                
                event.setCancelled(true);
                notifyPlayer(plr, ChatColor.DARK_RED + "You can't put obsidan that close to a portal!");
                plr.updateInventory();
            }else if(plugin.getProperties().getBoolean(partialBlockPath)){
                if(isPlayerInPortal(loc)){

                    //Prevent any block being placed by a portal block if a nearby player is standing in a portal
                    //Could potentially prevent unecessary blocks when portals are excessively close(1-3 blocks away)

                    event.setCancelled(true);
                    notifyPlayer(plr, ChatColor.DARK_RED + "You can't put blocks near a portal someone is in");
                    plr.updateInventory();

                }      
            }
        }
    }
    
    //Handler for lighting fire near portals
    @EventHandler
    public void onBurn(BlockIgniteEvent event){
        if(plugin.getProperties().getBoolean(firePath)){       
            Entity entity = event.getPlayer();
            if(null != entity){
                Player plr = (Player)entity;
                Location loc = event.getBlock().getLocation();
                if(inRangeOfPortal(loc, 1,1,0) || inRangeOfPortal(loc, 0,1,1)){
                        event.setCancelled(true);
                        notifyPlayer(plr,ChatColor.DARK_RED + "You can't start a fire that close to a portal.");
                        plr.updateInventory();
                }
            }
        }
    }
    
    //Handler for placement of lava
    @EventHandler
    public void onDrop(PlayerBucketEmptyEvent event){
        if(plugin.getProperties().getBoolean(lavaPlacePath)){
            Entity entity = event.getPlayer();
            if(event.getBucket() == Material.LAVA_BUCKET){
                if(null != entity){
                    Player plr = (Player)entity;
                    Location loc = event.getBlockClicked().getLocation().add(event.getBlockFace().getModX(), event.getBlockFace().getModY(),event.getBlockFace().getModZ());
                    if(loc.getBlock().getType() == Material.PORTAL && !isPlayerInPortal(loc, 1,1,1)){
                        //Allows breaking of portals with lava when players are not in them
                    }else if(inRangeOfPortal(loc) || event.getBlockClicked().getType() == Material.PORTAL){
                            event.setCancelled(true);
                            plr.sendBlockChange(loc, Material.AIR, loc.getBlock().getData());
                            notifyPlayer(plr,ChatColor.DARK_RED + "You can't put lava there, it's too close to a portal!");
                            plr.updateInventory();
                    }
                }
            }
        }
    }
    
    //Handler for flowing lava near portals
    @EventHandler
    public void onFlow(BlockFromToEvent event){
    //If any lava attempts to flow near or into a space near a portal, the block that caused the event is removed and the lava does not flow
    //Will remove pre-existing lava from near portals over time, but only if the lava is disturbed, causing a reason for it to flow
        if(plugin.getProperties().getBoolean(lavaFlowPath)){
            Material startType = event.getBlock().getType();
            Material lava = Material.STATIONARY_LAVA;
            Material lavaFlow = Material.LAVA;

            if((startType == lava || startType == lavaFlow)){
                if (inRangeOfPortal(event.getBlock().getLocation())){
                    event.getBlock().setType(Material.AIR);
                    event.setCancelled(true);
                }
            }
        }
    }  
    
    //Handler for potion splashes
    @EventHandler
    public void onMakeSplash(PotionSplashEvent event){
        if(plugin.getProperties().getBoolean(potionPath)){
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

                //If any players are affected, checks if any are in range of a portal
                //If the thrower is in range of a portal, all effects on players are nullified
                //If it would affects player(s) near a portal, only that player(s) will have the effect nullified
                if(validEvent){
                    Player[] finalPlrs = plrsAffected.toArray(new Player[plrsAffected.size()]);
                    if(inRangeOfPortal(damager.getLocation())&& plugin.isPlayerTimedOut(damager)){
                        //PortalProtector.m_log.info("Player [" + player.getDisplayName() + "] is near a portal; negating " + event.getDamage() + " damage");
                        notifyPlayer(damager, ChatColor.DARK_RED + "You cannot attack this player right now, you are too close to a portal.");
                        for (int i = 0; i < finalPlrs.length; i++){
                            event.setIntensity(finalPlrs[i], 0);
                        }
                    }else{
                        boolean message = false;
                        for(int i = 0; i < finalPlrs.length; i++){
                            if(inRangeOfPortal(finalPlrs[i].getLocation())&&plugin.isPlayerTimedOut(finalPlrs[i])){
                                event.setIntensity(finalPlrs[i], 0);
                                message = true;
                            }
                        }
                        if (message){
                            notifyPlayer(damager, ChatColor.DARK_RED + "Some players were not affected by the potion because they were near a portal.");
                        }
                    }

                }
            }
        }
    }
    
    //Handler for TNT explosions
    @EventHandler
    public void onPrime(EntityExplodeEvent event){
        if(plugin.getProperties().getBoolean(tntPath)){
          Entity thisPrime = event.getEntity();
            if(thisPrime.getType() == EntityType.PRIMED_TNT && inRangeOfPortal(thisPrime.getLocation(), 7,7,7)){
                event.setCancelled(true);
                TNTPrimed tnt = (TNTPrimed)thisPrime;
                tnt.remove();
                tnt.setYield(0);
            }
        }

    }
    
    
    //END EVENT HANDLERS
    //BEGIN HELPER METHODS
    public static boolean isPlayerInPortal(Location loc){
        return isPlayerInPortal(loc, 0,0,0);
    }
    public static boolean isPlayerInPortal(Location loc, int x, int y, int z){
        List<Entity> near = loc.getWorld().getEntities();
        for(Entity e : near) {
            if(e.getLocation().distance(loc) <= 2 && e instanceof Player){
                if(inRangeOfPortal(e.getLocation(), x, y, z)){
                    return true;
                }
            }
        }
        return false;
    }
    //Method to check if a location is in range of a portal, default distance is the RADIUS constant
    public static boolean inRangeOfPortal(Location location){
        int radius = plugin.getProperties().getInt(PortalProtector.m_sizeKey);
        return inRangeOfPortal(location, radius, radius, radius);
    }
    public static boolean inRangeOfPortal(Location location, int xVar, int yVar, int zVar){
        for (int x = -(xVar); x <= xVar; x++){
            for (int y = -(yVar); y <= yVar; y++){
                for (int z = -(zVar); z <= zVar; z++){
                    Location loc = location.getBlock().getRelative(x, y, z).getLocation();
                    if (loc.getBlock().getType() == Material.PORTAL){
                        return true;
                    }
                }
            }
        }
        return false;
    }
    public static Location[] getNearbyBlocks(Location location, int radius){
        Block block = location.getBlock();
        ArrayList<Location> blockLocs = new ArrayList<Location>();
        blockLocs.add(location);
        for(int x = block.getX() - radius; x <= block.getX() + radius; x++){
            for(int z = block.getZ() - radius; z <= block.getZ() + radius; z++){
                for (int y = block.getY() - radius; y <= block.getY() + radius; y++){
                    Location loc = new Location(block.getWorld(), x, y, z);
                    if(loc.getBlock() != location.getBlock())
                        blockLocs.add(loc);
                }
            }
        }
        return blockLocs.toArray(new Location[blockLocs.size()]);
    }
    
    
    public static void notifyPlayer(Player player, String message){
        message = (ChatColor.GRAY +"[[" + ChatColor.DARK_PURPLE + "PortalProtector" + ChatColor.GRAY + "]] "+ ChatColor.WHITE +  message);
        try{
            if(plugin.getMetavalue(player, PortalProtector.m_notifyKey, plugin).asBoolean()){
                player.sendMessage(message);
            }
        }catch(Exception ex){player.sendMessage(message);}
    }
    
    public static Object[] getPortal(Block portal) throws Exception{
        int backupRange = 1;
        
        if(portal.getType() != Material.PORTAL){
            boolean valid = false;
            for(int x=-backupRange; x<=backupRange && valid == false; x++){
                for(int z=-backupRange; z<=backupRange && valid == false; z++){
                    for(int y=-backupRange; y<=backupRange && valid == false; y++){
                        if(portal.getRelative(x, y, z).getType() == Material.PORTAL){
                            portal = portal.getRelative(x, y, z);
                            valid = true;
                        }
                    }
                }
            }
            if(!valid){
                throw new Exception("Attempted to find a frame, no portal found");
            }
        }
        
        //COMMENCE EXTREMELY UGLY CODE; IT SHOULD PROBABLY BE REWRITTEN, BUT I DON'T FEEL LIKE IT
        plugin.debug("Portal found at " + portal.getLocation());
        ArrayList<Block> portalBlocks = new ArrayList<Block>();
        ArrayList<Block> frameList = new ArrayList<Block>();
        portalBlocks.add(portal);
        boolean zAxis = false;
        if(portal.getRelative(1,0,0).getType() == Material.PORTAL || portal.getRelative(-1,0,0).getType() == Material.PORTAL){
            int x = 0;
            boolean xEdge = false;
            while (! xEdge){
                if(portal.getRelative(x+1, 0, 0).getType() != Material.PORTAL){
                    if(portal.getRelative(x+1, 0, 0).getType() == Material.OBSIDIAN){
                        frameList.add(portal.getRelative(x+1, 0, 0));
                    }
                    xEdge = true;
                }
                ArrayList[] allBlocks = checkPortalHeight(portal, x, portalBlocks, 0, frameList, xEdge, true);
                portalBlocks = allBlocks[0];
                frameList = allBlocks[1];
                x++;
            }
            x = 0;
            xEdge = false;
            while (! xEdge){
                if(portal.getRelative(x-1, 0, 0).getType() != Material.PORTAL){
                    if(portal.getRelative(x-1, 0, 0).getType() == Material.OBSIDIAN){
                        frameList.add(portal.getRelative(x-1, 0, 0));
                    }
                    xEdge = true;
                }
                ArrayList[] allBlocks = checkPortalHeight(portal, x, portalBlocks, 0, frameList, xEdge, true);
                portalBlocks = allBlocks[0];
                frameList = allBlocks[1];
                x--;
            }
        }else{
            zAxis = true;
            int z = 0;
            boolean zEdge = false;
            while (! zEdge){
                if(portal.getRelative(0, 0, z+1).getType() != Material.PORTAL){
                    if(portal.getRelative(0, 0, z+1).getType() == Material.OBSIDIAN){
                        frameList.add(portal.getRelative(0, 0, z+1));
                    }      
                    zEdge = true;
                }                
                ArrayList[] allBlocks = checkPortalHeight(portal, 0, portalBlocks, z, frameList, zEdge, false);
                portalBlocks = allBlocks[0];
                frameList = allBlocks[1]; 
                z++;
            }
            z = 0;
            zEdge = false;
            while (! zEdge){
                if(portal.getRelative(0, 0, z-1).getType() != Material.PORTAL){
                    if(portal.getRelative(0, 0, z-1).getType() == Material.OBSIDIAN){
                        frameList.add(portal.getRelative(0, 0, z-1));
                    }   
                    zEdge = true;
                }
                ArrayList[] allBlocks = checkPortalHeight(portal, 0, portalBlocks, z, frameList, zEdge, false);
                portalBlocks = allBlocks[0];
                frameList = allBlocks[1]; 
                z--;
            }            
        }
        
        
        /*   V2
        boolean newBlock = true;
        while (newBlock == true){
            newBlock = false;
            Iterator<Block> iter = newBlocks.iterator();
            while (iter.hasNext()){
                Block thisBlock = iter.next();
                for(int x=-1; x<=1; x++){
                    for(int z=-1; z<=1; z++){
                        for(int y=-1; y<=1; y++){
                            Block nearBlock = thisBlock.getRelative(x, y, z);
                            if(thisBlock.getType() == Material.PORTAL){
                                if (!portalBlocks.contains(thisBlock)){
                                    portalBlocks.add(thisBlock);
                                    newBlocks.add(nearBlock);
                                    newBlock = true;
                                }
                            }
                        }
                    }
                }
                iter.remove();
            }
        }
        ArrayList<Block> frameList = new ArrayList<Block>();
        Iterator<Block> iter = newBlocks.iterator();
        
        boolean onXPlane = false;
        while(iter.hasNext()){
            Block thisBlock = iter.next();
            for(int x=-1; x<=1; x++){
                if (thisBlock.getRelative(x,0,0).getType() == Material.OBSIDIAN){
                    Block possible = thisBlock.getRelative(x,0,0);
                    if(thisBlock.getRelative(-x,0,0).getType() == Material.PORTAL){
                        if(!frameList.contains(possible)){
                            frameList.add(possible);
                            onXPlane = true;
                        }
                    }
                }
            }
            if(!onXPlane){
                for(int z=-1;z<=1; z++){
                    if (thisBlock.getRelative(0,0,z).getType() == Material.OBSIDIAN){
                        Block possible = thisBlock.getRelative(0,0,z);
                        if(thisBlock.getRelative(0,0,-z).getType() == Material.PORTAL){
                            frameList.add(thisBlock.getRelative(0,0,z));
                            if(!frameList.contains(possible)){
                                frameList.add(possible);
                            }
                        }
                    }
                } 
            }
            for(int y=-1; y<=1; y++){
                if (thisBlock.getRelative(0,y,0).getType() == Material.OBSIDIAN){
                    Block possible = thisBlock.getRelative(0,y,0);                    
                    if(thisBlock.getRelative(0,-y,0).getType() == Material.PORTAL){
                        frameList.add(thisBlock.getRelative(0,y,0));
                        if(!frameList.contains(possible)){
                            frameList.add(possible);
                        }                     
                    }
                }
            }
        }*/
        
        
        /*   V1
        while(newBlock == true){
            newBlock = false;
            Block[] currentBlocks = portalBlocks.toArray(new Block[portalBlocks.size()]);
            for(int i=0; i<currentBlocks.length; i++){
                for(int x=-1; x<=1; x++){
                    for(int z=-1; z<=1; z++){
                        for(int y=-1; y<=1; y++){
                            Block thisBlock = currentBlocks[i].getRelative(x, y, z);
                            if(thisBlock.getType() == Material.PORTAL){
                                if (!portalBlocks.contains(thisBlock)){
                                    portalBlocks.add(thisBlock);
                                    newBlock = true;
                                }
                            }
                        }
                    }
                }
            }
        }
        Block[] allPortals = portalBlocks.toArray(new Block[portalBlocks.size()]);
        ArrayList<Block> frameList = new ArrayList<Block>();
        for(int i=0; i<allPortals.length; i++){
            for(int x=-1; x<=1; x++){
                if (allPortals[i].getRelative(x,0,0).getType() == Material.OBSIDIAN){
                    Block possible = allPortals[i].getRelative(x,0,0);
                    if(allPortals[i].getRelative(-x,0,0).getType() == Material.PORTAL){
                        if(!frameList.contains(possible)){
                            frameList.add(possible);
                        }
                    }
                }
            }
            for(int z=-1;z<=1; z++){
                if (allPortals[i].getRelative(0,0,z).getType() == Material.OBSIDIAN){
                    Block possible = allPortals[i].getRelative(0,0,z);
                    if(allPortals[i].getRelative(0,0,-z).getType() == Material.PORTAL){
                        frameList.add(allPortals[i].getRelative(0,0,z));
                        if(!frameList.contains(possible)){
                            frameList.add(possible);
                        }
                    }
                }
            } 
            for(int y=-1; y<=1; y++){
                if (allPortals[i].getRelative(0,y,0).getType() == Material.OBSIDIAN){
                    Block possible = allPortals[i].getRelative(0,y,0);                    
                    if(allPortals[i].getRelative(0,-y,0).getType() == Material.PORTAL){
                        frameList.add(allPortals[i].getRelative(0,y,0));
                        if(!frameList.contains(possible)){
                            frameList.add(possible);
                        }                     
                    }
                }
            }
             */
        Object[] myReturn = {portalBlocks, frameList, zAxis};
        return myReturn;
    }
    
    
    private static ArrayList[] checkPortalHeight(Block portal, int x, ArrayList<Block> portalBlocks, int z, ArrayList<Block> frameBlocks, boolean onEdge, boolean xDim){
        int y = 0;
        boolean yEdge = false;
        while(! yEdge){
            Block currBlock = portal.getRelative(x, y, z);
            if(!portalBlocks.contains(currBlock)){
                portalBlocks.add(currBlock);
            }
            if(onEdge){
                if(xDim){
                    if (currBlock.getRelative(1, 0, 0).getType() == Material.OBSIDIAN && currBlock.getRelative(-1, 0, 0).getType() == Material.PORTAL){
                        frameBlocks.add(currBlock.getRelative(1, 0, 0));
                    }else if(currBlock.getRelative(-1, 0, 0).getType() == Material.OBSIDIAN && currBlock.getRelative(1, 0, 0).getType() == Material.PORTAL){
                        frameBlocks.add(currBlock.getRelative(-1, 0, 0));
                    }
                }else{
                    if (currBlock.getRelative(0, 0, 1).getType() == Material.OBSIDIAN && currBlock.getRelative(0, 0, -1).getType() == Material.PORTAL){
                        frameBlocks.add(currBlock.getRelative(0, 0, 1));
                    }else if(currBlock.getRelative(0, 0, -1).getType() == Material.OBSIDIAN && currBlock.getRelative(0, 0, 1).getType() == Material.PORTAL){
                        frameBlocks.add(currBlock.getRelative(0, 0, -1));
                    }
                }
            }
            
            
            if(portal.getRelative(x, y+1, z).getType() == Material.PORTAL){
                y++;
            }else{
                if(portal.getRelative(x, y+1, z).getType() == Material.OBSIDIAN){
                    frameBlocks.add(portal.getRelative(x, y+1, z));
                }
                yEdge = true;
            }
        }
        y = 0;
        yEdge = false;
        while(! yEdge){
            Block currBlock = portal.getRelative(x, y, z);
            if(!portalBlocks.contains(currBlock)){
                portalBlocks.add(currBlock);
            }
            if(onEdge){
                if(xDim){
                    if (currBlock.getRelative(1, 0, 0).getType() == Material.OBSIDIAN && currBlock.getRelative(-1, 0, 0).getType() == Material.PORTAL){
                        frameBlocks.add(currBlock.getRelative(1, 0, 0));
                    }else if(currBlock.getRelative(-1, 0, 0).getType() == Material.OBSIDIAN && currBlock.getRelative(1, 0, 0).getType() == Material.PORTAL){
                        frameBlocks.add(currBlock.getRelative(-1, 0, 0));
                    }
                }else{
                    if (currBlock.getRelative(0, 0, 1).getType() == Material.OBSIDIAN && currBlock.getRelative(0, 0, -1).getType() == Material.PORTAL){
                        frameBlocks.add(currBlock.getRelative(0, 0, 1));
                    }else if(currBlock.getRelative(0, 0, -1).getType() == Material.OBSIDIAN && currBlock.getRelative(0, 0, 1).getType() == Material.PORTAL){
                        frameBlocks.add(currBlock.getRelative(0, 0, -1));
                    }
                }
            }
            if(portal.getRelative(x, y-1, z).getType() == Material.PORTAL){
                y--;
            }else{
                if(portal.getRelative(x, y-1, z).getType() == Material.OBSIDIAN){
                    frameBlocks.add(portal.getRelative(x, y-1, z));
                }
                yEdge = true;
            }
        }
        ArrayList[] myReturn = {portalBlocks, frameBlocks};
        return myReturn;
    }

    
}
