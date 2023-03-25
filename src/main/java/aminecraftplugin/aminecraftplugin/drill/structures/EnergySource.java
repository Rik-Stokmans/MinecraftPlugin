package aminecraftplugin.aminecraftplugin.drill.structures;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

public class EnergySource implements Structure, Listener {


    @Override
    public void place(Player p) {

    }

    @Override
    public ItemStack destroy(boolean offline) {
        return null;
    }

    @Override
    public String getStructureName() {
        return null;
    }

    @Override
    public Location getLocation() {
        return null;
    }


    @EventHandler
    public void structurePlace(BlockPlaceEvent e) {

    }

}
