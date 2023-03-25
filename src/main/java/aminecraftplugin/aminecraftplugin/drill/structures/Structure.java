package aminecraftplugin.aminecraftplugin.drill.structures;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.bukkit.Bukkit;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static aminecraftplugin.aminecraftplugin.player.PlayerProfile.playerProfiles;
import static aminecraftplugin.aminecraftplugin.utils.Direction.getCardinalDirection;
import static aminecraftplugin.aminecraftplugin.utils.Direction.getXandZ;

public interface Structure {

    public static HashMap<UUID, ArrayList<Structure>> structures = new HashMap<>();
    public static HashMap<UUID, Integer> scheduleRemoveStructures = new HashMap<>();

    static void addStructure(UUID uuid, Structure structure){
        if (!structures.containsKey(uuid)){
            structures.put(uuid, new ArrayList<>());
        }
        structures.get(uuid).add(structure);
    }

    static void destroyAll(){
        for (Map.Entry<UUID, ArrayList<Structure>> entry : structures.entrySet()){
            UUID uuid = entry.getKey();
            for (Structure structure : entry.getValue()) {
                if (scheduleRemoveStructures.containsKey(uuid)) {
                    playerProfiles.get(uuid).addOfflineItem(structure.destroy());
                } else {
                    Bukkit.getPlayer(uuid).getInventory().addItem(structure.destroy());
                }
            }
        }
    }

    public void place(Player p);
    public ItemStack destroy();
    @EventHandler public void structurePlace(BlockPlaceEvent e);


}
