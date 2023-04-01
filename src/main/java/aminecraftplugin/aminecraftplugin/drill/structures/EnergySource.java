package aminecraftplugin.aminecraftplugin.drill.structures;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

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

    @Override
    public ArrayList<Location> getLocations() {
        return null;
    }

    @Override
    public void openStructureMenu(Player p, int page) {

    }

    @EventHandler
    public void structurePlace(BlockPlaceEvent e) {

    }

}
