package aminecraftplugin.aminecraftplugin.drill.structures;


import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;

import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static aminecraftplugin.aminecraftplugin.Main.loadFile;
import static aminecraftplugin.aminecraftplugin.Main.saveFile;
import static aminecraftplugin.aminecraftplugin.player.PlayerProfile.playerProfiles;


public interface Structure {

    public static HashMap<UUID, ArrayList<Structure>> structures = new HashMap<>();
    public static HashMap<UUID, Integer> scheduleRemoveStructures = new HashMap<>();

    public static YamlConfiguration savedBlocksFile = new YamlConfiguration();
    public static HashMap<Location, ItemStack> savedBlocks = new HashMap<>();

    static Material[] doubleBlocks = new Material[]{Material.LARGE_FERN, Material.TALL_GRASS, Material.LILAC, Material.PEONY, Material.ROSE_BUSH, Material.SUNFLOWER};

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
                    playerProfiles.get(uuid).addOfflineItem(structure.destroy(true));
                } else {
                    Bukkit.getPlayer(uuid).getInventory().addItem(structure.destroy(true));
                }
            }
        }
    }

    static void saveLongGrass(){
        int id = 1;
        for (Map.Entry<Location, ItemStack> entry : savedBlocks.entrySet()){
            savedBlocksFile.set("data." + id + ".location", entry.getKey());
            savedBlocksFile.set("data." + id + ".material", new ItemStack(entry.getValue()));
            id++;
        }
        try {
            saveFile(savedBlocksFile, "savedblocks.yml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    static void loadAndPlaceLongGrass(){
        YamlConfiguration savedBlocksFile = null;
        try {
            savedBlocksFile = loadFile("savedblocks.yml");
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (savedBlocksFile != null && savedBlocksFile.contains("data")) {
            YamlConfiguration finalSavedBlocksFile = savedBlocksFile;
            savedBlocksFile.getConfigurationSection("data").getKeys(false).forEach(key -> {
                Location loc = finalSavedBlocksFile.getLocation("data." + key + ".location");
                Material material = finalSavedBlocksFile.getItemStack("data." + key + ".material").getType();

                loc.getBlock().setType(material);
                Location locUp = loc.clone().add(0, 1, 0);
                BlockData blockData2 = Bukkit.createBlockData(material, "[half=upper]");
                locUp.getBlock().setBlockData(blockData2);
            });
        }
    }

    public void place(Player p);
    public ItemStack destroy(boolean offline);

    @EventHandler public void structurePlace(BlockPlaceEvent e);


}
