package aminecraftplugin.aminecraftplugin.drill.structures;


import org.apache.commons.lang3.tuple.ImmutablePair;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;

import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.structure.StructureManager;
import org.bukkit.util.BlockVector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static aminecraftplugin.aminecraftplugin.Main.loadFile;
import static aminecraftplugin.aminecraftplugin.Main.saveFile;
import static aminecraftplugin.aminecraftplugin.player.PlayerProfile.playerProfiles;
import static aminecraftplugin.aminecraftplugin.utils.ChatUtils.format;


public interface Structure {

    public void place(Player p);
    public ItemStack destroy(UUID uuid, boolean offline);
    public String getStructureName();
    public Location getLocation();
    public ArrayList<Location> getLocations();
    public void openStructureMenu(Player p, int page);

    @EventHandler public void structurePlace(BlockPlaceEvent e);

    public static final String[] destroyableMaterials = {"BANNER", "BUTTON", "SIGN", "DRIPLEAF", "CACTUS", "BAMBOO", "VINES", "LICHEN", "LILY_PAD",
    "REDSTONE", "RAIL", "STRING", "LANTERN", "DRIPSTONE", "FROGSPAWN", "AMETHYST", "TRIPWIRE_HOOK", "LEVER", "SPORE_BLOSSOM", "NETHER_SPROUTS", "CRIMSON_ROOTS",
    "CRIMSON_FUNGUS", "WARPED_ROOTS", "WARPED_FUNGUS", "HANGING_ROOTS", "SEA_PICKLE", "AZALEA", "FARMLAND", "SCULK VEIN"};

    HashMap<UUID, ArrayList<Structure>> structures = new HashMap<>();
    HashMap<UUID, Integer> scheduleRemoveStructures = new HashMap<>();

    YamlConfiguration savedBlocksFile = new YamlConfiguration();
    HashMap<Location, BlockData> savedBlocks = new HashMap<>();
    Material[] doubleBlocks = new Material[]{Material.LARGE_FERN, Material.TALL_GRASS, Material.LILAC, Material.PEONY, Material.ROSE_BUSH, Material.SUNFLOWER};



    static void addStructure(UUID uuid, Structure structure){
        if (!structures.containsKey(uuid)){
            structures.put(uuid, new ArrayList<>());
        }
        structures.get(uuid).add(structure);
    }

    static void destroyAll(){
        HashMap<UUID, ArrayList<Structure>> offline = new HashMap<>();
        HashMap<UUID, ArrayList<Structure>> online = new HashMap<>();

        for (Map.Entry<UUID, ArrayList<Structure>> entry : structures.entrySet()){
            UUID uuid = entry.getKey();
            offline.put(uuid, new ArrayList<>());
            online.put(uuid, new ArrayList<>());
            for (Structure structure : entry.getValue()) {
                if (scheduleRemoveStructures.containsKey(uuid)) {
                    offline.get(uuid).add(structure);
                } else {
                    online.get(uuid).add(structure);
                }
            }
        }
        for (Map.Entry<UUID, ArrayList<Structure>> entry : offline.entrySet()){
            UUID uuid = entry.getKey();
            for (Structure structure : entry.getValue()) {
                playerProfiles.get(uuid).addOfflineItem(structure.destroy(uuid, true));
            }
        }
        for (Map.Entry<UUID, ArrayList<Structure>> entry : online.entrySet()){
            UUID uuid = entry.getKey();
            for (Structure structure : entry.getValue()) {
                Bukkit.getPlayer(uuid).getInventory().addItem(structure.destroy(uuid, true));
            }
        }

    }

    static void saveLongGrass(){
        int id = 1;
        for (Map.Entry<Location, BlockData> entry : savedBlocks.entrySet()){

            boolean upper = true;
            if (entry.getValue().getAsString().contains("lower")){
                upper = false;
            }
            savedBlocksFile.set("data." + id + ".location", entry.getKey());
            savedBlocksFile.set("data." + id + ".material", new ItemStack(entry.getValue().getMaterial()));
            savedBlocksFile.set("data." + id + ".upper", upper);
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
                boolean upper = finalSavedBlocksFile.getBoolean("data." + key + ".upper");

                Location locUp = loc.clone().add(0, 1, 0);
                Location locDown = loc.clone().add(0, -1, 0);
                BlockData blockData2 = Bukkit.createBlockData(material, "[half=upper]");

                if (!upper) {
                    loc.getBlock().setType(material);
                    locUp.getBlock().setBlockData(blockData2);
                } else {
                    locDown.getBlock().setType(material);
                    loc.getBlock().setBlockData(blockData2);
                }
            });
        }
    }

    static org.bukkit.structure.Structure getStructure(String s){
        StructureManager structureManager = Bukkit.getStructureManager();
        Map<NamespacedKey, org.bukkit.structure.Structure> structureMap = structureManager.getStructures();
        org.bukkit.structure.Structure structure = null;
        for (NamespacedKey namespacedKey : structureMap.keySet()){
            if (namespacedKey.asString().equals("minecraft:" + s)){
                structure = structureMap.get(namespacedKey);
            }
        }
        return structure;

    }

    static double biggestFromVector(BlockVector blockVector){
        double biggest1 = 0.0;
        if (blockVector.getX() > biggest1){
            biggest1 = blockVector.getX() + 2;
        }
        if (blockVector.getY() > biggest1){
            biggest1 = blockVector.getY() + 1;
        }
        if (blockVector.getZ() > biggest1){
            biggest1 = blockVector.getZ() + 2;
        }
        return biggest1;
    }

    static boolean canBePlaced(String structureName, Location placedLoc, Player p){
        double distance = biggestFromVector(getStructure(structureName).getSize());
        if (placedLoc.getWorld().getNearbyEntities(placedLoc, distance, distance, distance).stream().filter(entity -> !(entity instanceof LivingEntity)).collect(Collectors.toList()).size() != 0){
            p.sendMessage(format("&cCan not place while entities are nearby"));
            return false;
        }
        for (ArrayList<aminecraftplugin.aminecraftplugin.drill.structures.Structure> structures : structures.values()){
            for (aminecraftplugin.aminecraftplugin.drill.structures.Structure structure : structures) {
                org.bukkit.structure.Structure structureComparing = aminecraftplugin.aminecraftplugin.drill.structures.Structure.getStructure(structure.getStructureName());
                org.bukkit.structure.Structure structurePlacing = aminecraftplugin.aminecraftplugin.drill.structures.Structure.getStructure(structureName);

                double biggest1 = biggestFromVector(structureComparing.getSize());
                double biggest2 = biggestFromVector(structurePlacing.getSize());

                double minDistance = (biggest1 + biggest2) / 2;
                if (placedLoc.distance(structure.getLocation()) < minDistance) {
                    p.sendMessage(format("&cThis structure is too close to another one"));
                    return false;
                }
            }
        }
        return true;
    }

    static ArrayList<Location> getStructureSpace(Location location, org.bukkit.structure.Structure structure, ImmutablePair<Integer, Integer> pair){

        ArrayList<Location> locations = new ArrayList<>();
        ArrayList<Location> ignoredLocations = new ArrayList<>();
        //add block below drill
        locations.add(location.clone().add(0,-1,0));
        int length = (int) structure.getSize().getZ();
        int width = (int) structure.getSize().getX();
        int height = (int) structure.getSize().getY() + 1;

        int lengthFloor = (int) -Math.ceil(((length + 2) - 1) / 2);
        int lengthCeiling = (int) Math.ceil((length + 2) / 2);
        for (int l = lengthFloor; l <= lengthCeiling; l++){
            int widthFloor = (int) -Math.ceil(((width + 2) - 1) / 2);
            int widthCeiling = (int) Math.ceil(((width + 2) - 1) / 2);
            for (int w = widthFloor; w <= widthCeiling; w++){

                for (int h = 1; h <= height + 1; h++){
                    Location loc = location.clone().add(
                            (l * pair.getKey() + w * pair.getValue()),
                            h - 1,
                            (w * pair.getKey() + l * pair.getValue()));

                    Material type = loc.getBlock().getType();
                    Location locUp = loc.clone().add(0,1,0);
                    Location locDown = loc.clone().add(0,-1,0);

                    //exception to not place tall grass twice
                    for (Material material : doubleBlocks){
                        if (type.equals(material)){
                            if (loc.getBlock().getBlockData().getAsString().contains("[half=upper]")){
                                ignoredLocations.add(locDown);
                            }
                            if (loc.getBlock().getBlockData().getAsString().contains("[half=lower]")){
                                ignoredLocations.add(locUp);
                            }
                        }
                    }

                    //exception to not break surroundings
                    while ((locUp.getBlock().isPassable() || locUp.getBlock().getType().hasGravity())
                            && !locUp.getBlock().getType().equals(Material.AIR)){
                        if (!ignoredLocations.contains(locUp)) {
                            locations.add(locUp);
                        }
                        locUp = locUp.clone().add(0,1,0);
                    }

                    ArrayList<Location> allLocationsAround = new ArrayList<>();
                    ArrayList<Location> locationsAround = new ArrayList<>();
                    locationsAround.add(loc.clone());
                    while (true) {
                        ArrayList<Location> locationsNotToBeRemoved = new ArrayList<>();
                        ArrayList<Location> locationsToBeAddedToLocationsAround = new ArrayList<>();
                        for (Location loc1 : locationsAround) {
                            allLocationsAround.add(loc1);
                            for (double x = loc1.getX() - 1; x < loc1.getX() + 1.01; x++) {
                                for (double y = loc1.getY() - 1; y < loc1.getY() + 1.01; y++) {
                                    for (double z = loc1.getZ() - 1; z < loc1.getZ() + 1.01; z++) {
                                        Location location1 = new Location(loc.getWorld(), x, y, z);
                                        boolean con = true;
                                        for (Location location2 : allLocationsAround){
                                            if (location2.equals(location1)){
                                                con = false;
                                            }
                                        }
                                        if (!con) continue;
                                        locationsToBeAddedToLocationsAround.add(location1);
                                        for (String s : destroyableMaterials) {
                                            if (location1.getBlock().getType().toString().contains(s)) {
                                                locationsNotToBeRemoved.add(location1);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        for (Location loc1 : locationsToBeAddedToLocationsAround){
                            locationsAround.add(loc1);
                        }
                        ArrayList<Location> locationsToBeRemoved = new ArrayList<>();
                        for (Location loc1 : locationsAround){
                            if (!locationsNotToBeRemoved.contains(loc1)){
                                locationsToBeRemoved.add(loc1);
                            }
                        }
                        for (Location loc1 : locationsToBeRemoved){
                            locationsAround.remove(loc1);
                        }
                        if (locationsNotToBeRemoved.isEmpty()){
                            for (Location loc1 : locationsAround) {
                                allLocationsAround.add(loc1);
                            }
                            break;
                        }
                    }
                    for (Location loc1 : allLocationsAround){
                        if (!locations.contains(loc1) && !ignoredLocations.contains(loc1)){
                            locations.add(loc1);
                        }
                    }

                    if (!locations.contains(loc) && !ignoredLocations.contains(loc)) {
                        locations.add(loc);
                    }
                }
            }
        }
        return locations;
    }



}
