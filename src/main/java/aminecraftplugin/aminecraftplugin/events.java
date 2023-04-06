package aminecraftplugin.aminecraftplugin;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;


import static aminecraftplugin.aminecraftplugin.commands.addSpawnCommand.spawnLocations;

public class events implements Listener {


    @EventHandler
    private void entitySpawn(EntitySpawnEvent e){
        if (e.getEntityType().equals(EntityType.BAT)){
            e.setCancelled(true);
        }
    }

    @EventHandler
    private void dropBlockEvent(BlockDropItemEvent e){
        e.setCancelled(true);
    }

    @EventHandler
    private void breakEvent(BlockBreakEvent e){
        Player p = e.getPlayer();
        if (!p.getGameMode().equals(GameMode.CREATIVE)){
            e.setCancelled(true);
        }
    }

    @EventHandler
    private void breakEvent(PlayerInteractEvent e){
        Player p = e.getPlayer();
        if (!p.getGameMode().equals(GameMode.CREATIVE)){
            e.setCancelled(true);
        }
    }

    @EventHandler
    private void placeEvent(BlockPlaceEvent e){
        Player p = e.getPlayer();
        if (!p.getGameMode().equals(GameMode.CREATIVE)){
            e.setCancelled(true);
        }
    }

    @EventHandler
    private void damageEvent(EntityDamageByEntityEvent e){
        if (e.getDamager() instanceof Player) {
            Player p = (Player) e.getDamager();
            if (!p.getGameMode().equals(GameMode.CREATIVE)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    private void rightClickArmorStand(PlayerArmorStandManipulateEvent e){
        Player p = e.getPlayer();
        if (!p.getGameMode().equals(GameMode.CREATIVE)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    private void onDeath(PlayerRespawnEvent e){
        Location deathLoc = e.getPlayer().getLastDeathLocation();
        Location spawnLoc = getSpawnLocation(deathLoc);
        e.setRespawnLocation(spawnLoc);
    }


    public Location getSpawnLocation(Location deathLoc){
        double shortestDistance = Double.MAX_VALUE;
        Location spawnLoc = new Location(Bukkit.getWorld("Map"), 468.5, 71, -46.5);
        spawnLoc.setYaw(-90.0f);
        for (Location loc : spawnLocations.values()){
            double distance = loc.distance(deathLoc);
            if (distance < shortestDistance){
                shortestDistance = distance;
                spawnLoc = loc;
            }
        }
        return spawnLoc;
    }



}
