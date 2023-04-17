package aminecraftplugin.aminecraftplugin.drilling.energy;

import aminecraftplugin.aminecraftplugin.drilling.structures.Structure;
import me.filoghost.holographicdisplays.api.hologram.Hologram;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
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

    public ItemStack getEnergySource(int tier, EnergySourceType energySourceType){
        return new ItemStack(Material.AIR);
    }

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

    @Override
    public void placeEvent(BlockPlaceEvent e) {

    }

}
