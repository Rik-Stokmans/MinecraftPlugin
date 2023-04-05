package aminecraftplugin.aminecraftplugin.drill.structures;

import aminecraftplugin.aminecraftplugin.drill.loot.LootFinder;
import me.filoghost.holographicdisplays.api.hologram.Hologram;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class EnergySource implements Structure, Listener {

    private OfflinePlayer owner;
    private Location location;
    private ArrayList<Inventory> pages = new ArrayList<>();
    private Hologram hologram;
    private org.bukkit.structure.Structure structure;
    private ArrayList<Integer> tasks = new ArrayList<>();
    private double energyGain;
    private double energyCap;
    private int range;

    @Override
    public void place(Player p) {

    }

    @Override
    public ItemStack destroy(UUID uuid, boolean offline) {
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
