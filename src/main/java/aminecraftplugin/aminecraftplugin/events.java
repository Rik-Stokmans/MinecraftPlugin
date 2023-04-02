package aminecraftplugin.aminecraftplugin;

import org.bukkit.GameMode;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntitySpawnEvent;

import java.util.Locale;

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
    private void placeEvent(BlockPlaceEvent e){
        Player p = e.getPlayer();
        if (!p.getGameMode().equals(GameMode.CREATIVE)){
            e.setCancelled(true);
        }
    }

}
