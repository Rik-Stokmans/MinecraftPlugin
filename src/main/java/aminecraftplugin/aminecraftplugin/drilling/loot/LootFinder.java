package aminecraftplugin.aminecraftplugin.drilling.loot;

import aminecraftplugin.aminecraftplugin.drilling.resource.Resource;
import aminecraftplugin.aminecraftplugin.player.PlayerProfile;
import aminecraftplugin.aminecraftplugin.sideSkills.Skill;
import aminecraftplugin.aminecraftplugin.sideSkills.SkillType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

import static aminecraftplugin.aminecraftplugin.drilling.loot.LootTable.lootTableHashMap;
import static aminecraftplugin.aminecraftplugin.drilling.resource.Resource.getResourceFromKey;
import static aminecraftplugin.aminecraftplugin.player.PlayerProfile.playerProfiles;
import static aminecraftplugin.aminecraftplugin.utils.log.logBase;

public class LootFinder {


    private HashMap<LootTable, Double> lootTables;

    public LootFinder(Location location){
        this.lootTables = calculateLootTable(location);
    }

    public HashMap<Resource, Double> findLoot(int veinTier, OfflinePlayer p, long drillDepth){

        HashMap<Resource, Double> foundResources = new HashMap<>();

        PlayerProfile playerProfile = playerProfiles.get(p.getUniqueId());
        Skill miningSkill = playerProfile.getSkills().get(SkillType.miningskill);

        //exponent (higher prospecting skill -> lower exponent -> more rare resources)
        double exponent = 6 - logBase(1 + (drillDepth / 50) + veinTier, 6);

        double totalWeight = 0.0;
        for (Map.Entry<LootTable, Double> entry : this.getLootTables().entrySet()) {

            LootTable lootTable = entry.getKey();
            double factor = entry.getValue();
            //calculate total weight
            for (int i : lootTable.getIDs()) {
                int resourceID = lootTable.getResources().get(i);
                Resource resource = getResourceFromKey(resourceID);
                totalWeight += (1 / (Math.pow(resource.getValue(), exponent))) * factor;
            }

        }

        for (Map.Entry<LootTable, Double> entry : this.getLootTables().entrySet()){
            LootTable lootTable = entry.getKey();

            //factor based on distance to loot table
            double factor = entry.getValue();
            //calculate percentage and roll chances
            for (int i : lootTable.getIDs()){
                int resourceID = lootTable.getResources().get(i);
                //factor in loot table
                float factor2 = lootTable.getTable().get(i);
                Resource resource = getResourceFromKey(resourceID);
                double weight = (1/(Math.pow(resource.getValue(), exponent))) * factor * factor2;
                double percentage = ((weight * 100) / totalWeight);
                double rollChance = new Random().nextDouble() * 100;
                if (rollChance <= percentage){
                    double amountOfKG = (0.1715 * Math.log(miningSkill.getTier() + 1) + 0.1475) * veinTier;
                    //random multiplier of 0.5 to 1.5
                    amountOfKG *= 0.5 + new Random().nextDouble();
                    foundResources.put(resource, amountOfKG);
                }
            }
        }
        return foundResources;
    }


    private HashMap<LootTable, Double> calculateLootTable(Location location){
        ArrayList<LootTable> closestLootTables = calculateClosestLootTables(location, 3);
        double totalDistanceCombined = 0.0;
        for (LootTable lootTable : closestLootTables){
            totalDistanceCombined += 1/(location.distance(lootTable.getLocation()));
        }
        HashMap<LootTable, Double> factors = new HashMap<>();
        for (LootTable lootTable : closestLootTables){
            double distance = location.distance(lootTable.getLocation());
            double factor = (1/distance) / totalDistanceCombined;
            factors.put(lootTable, factor);
        }
        return factors;
    }

    private ArrayList<LootTable> calculateClosestLootTables(Location location, int amount){

        ArrayList<LootTable> closestLootTables = new ArrayList<>();

        for (Map.Entry<Integer, LootTable> entry : lootTableHashMap.entrySet()){
            closestLootTables.add(entry.getValue());
        }

        closestLootTables = (ArrayList<LootTable>) closestLootTables.stream().sorted(Comparator.comparingDouble(lootTable -> lootTable.getLocation().distance(location))).limit(amount).collect(Collectors.toList());

        return closestLootTables;
    }


    public HashMap<LootTable, Double> getLootTables() {
        return lootTables;
    }

}
