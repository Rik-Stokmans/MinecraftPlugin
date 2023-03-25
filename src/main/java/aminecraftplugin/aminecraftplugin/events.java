package aminecraftplugin.aminecraftplugin;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.entity.EntitySpawnEvent;

import java.util.Locale;

public class events implements Listener {


    @EventHandler
    private void entitySpawn(EntitySpawnEvent e){
        if (e.getEntityType().equals(EntityType.BAT)){
            e.setCancelled(true);
        }
    }

}
