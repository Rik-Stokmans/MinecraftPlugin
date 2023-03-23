package aminecraftplugin.aminecraftplugin.drill;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static aminecraftplugin.aminecraftplugin.drill.LootTable.lootTableHashMap;

public class LootFinder {


    private HashMap<LootTable, Double> lootTables;

    public LootFinder(Location location){
        this.lootTables = calculateLootTable(location);
    }

    public void findLoot(){

    }


    private HashMap<LootTable, Double> calculateLootTable(Location location){
        ArrayList<LootTable> closestLootTables = calculateClosestLootTables(location);
        Double totalDistanceCombined = 0.0;
        for (LootTable lootTable : closestLootTables){
            totalDistanceCombined += location.distance(lootTable.getLocation());
        }
        HashMap<LootTable, Double> factors = new HashMap<>();
        for (LootTable lootTable : closestLootTables){
            Double distance = location.distance(lootTable.getLocation());
            Double factor = distance / totalDistanceCombined;
            factors.put(lootTable, factor);
        }
        return factors;
    }

    private ArrayList<LootTable> calculateClosestLootTables(Location location){

        ArrayList<Location> closestLocations = new ArrayList<>();

        for (Map.Entry<Integer, LootTable> entry : lootTableHashMap.entrySet()){

        }

        return new ArrayList<>();
    }


    public HashMap<LootTable, Double> getLootTables() {
        return lootTables;
    }

}
