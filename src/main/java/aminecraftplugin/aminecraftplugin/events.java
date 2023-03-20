package aminecraftplugin.aminecraftplugin;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFadeEvent;

import java.util.Locale;

public class events implements Listener {

    @EventHandler
    private void coralEvent(BlockFadeEvent e){
        if (e.getBlock().getType().name().toLowerCase(Locale.ROOT).contains("coral")){
            e.setCancelled(true);
        }
    }

}
