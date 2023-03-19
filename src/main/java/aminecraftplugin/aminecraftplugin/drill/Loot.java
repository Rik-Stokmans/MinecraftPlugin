package aminecraftplugin.aminecraftplugin.drill;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;

public class Loot {

    private ArrayList<ItemStack> loot = new ArrayList<>();


    //Loottable with factor of influence
    private static HashMap<LootTable, Double> allLootTables = new HashMap<>();


    public Loot(Location location){
        this.loot = calculateLoot(location);
    }


    //todo: use all loottables and distance to calculate loot

    private ArrayList<ItemStack> calculateLoot(Location location){
        return new ArrayList<>();
    }

    public ArrayList<ItemStack> getLoot() {
        return loot;
    }
}
