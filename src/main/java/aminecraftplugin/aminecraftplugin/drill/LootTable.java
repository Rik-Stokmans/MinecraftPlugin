package aminecraftplugin.aminecraftplugin.drill;


import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static aminecraftplugin.aminecraftplugin.Main.loadFile;
import static aminecraftplugin.aminecraftplugin.Main.saveFile;
import static aminecraftplugin.aminecraftplugin.utils.ChatUtils.format;
import static aminecraftplugin.aminecraftplugin.utils.defaultPageInventory.getDefaultScrollableInventory;

public class LootTable implements Listener {


    //resource weight factor
    private static HashMap<Integer, LootTable> lootTableHashMap = new HashMap<>();
    private static YamlConfiguration loottableFile;
    private static HashMap<Player, LootTable> lootTableBrowsing = new HashMap<>();


    private HashMap<Resource, Float> table = new HashMap<>();
    private Location location;
    private String name;
    private int ID;

    public LootTable(){

    }

    public LootTable(String name, Location location, int ID){
        this.name = name;
        this.location = location;
        this.ID = ID;
        lootTableHashMap.put(this.ID, this);
    }

    public LootTable(String name, Location location){
        this.name = name;
        this.location = location;
        this.ID = findNewID();
        lootTableHashMap.put(this.ID, this);
    }

    public static void init(){
        try {
            lootTableHashMap = loadLoottables();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void save(){
        try {
            saveLoottables();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static int findNewID(){
        int index = 1;
        while(true){
            if (!lootTableHashMap.containsKey(index)){
                break;
            }
            index++;
        }
        return index;
    }

    public void openLoottableMenu(Player p){
        lootTableBrowsing.put(p, this);

        Inventory inventory = getDefaultScrollableInventory(this.getName(), true);


        p.openInventory(inventory);
    }

    public static void openSelectLoottableMenu(Player p){

        Inventory selectInventory = getDefaultScrollableInventory("Select Loot Table", false);;


        int index = 0;
        for (Integer i : lootTableHashMap.keySet()){
            LootTable lootTable = lootTableHashMap.get(i);
            ItemStack list = new ItemStack(Material.FILLED_MAP);
            ItemMeta listMeta = list.getItemMeta();
            listMeta.setDisplayName(lootTable.getName());
            ArrayList<String> lore = new ArrayList<>();

            Location loc = lootTable.getLocation();
            lore.add("Location: (" + "x: " + Math.round(loc.getX()) + ", y: " + Math.round(loc.getY()) + ", z: " + Math.round(loc.getZ()) + ")");

            for (Map.Entry<Resource, Float> entry : lootTable.getTable().entrySet()){
                Resource resource = entry.getKey();
                Float f = entry.getValue();
                lore.add(resource.getName() + ": " + f);
            }
            listMeta.setLore(lore);
            list.setItemMeta(listMeta);
            selectInventory.setItem(index, list);
            index++;
        }

        p.openInventory(selectInventory);
    }


    @EventHandler
    private void clickEvent(InventoryClickEvent e){
        if (e.getView() == null) return;
        String name = e.getView().getTitle();
        if (name.contains("Select Loot Table")) {
            e.setCancelled(true);
            int slot = e.getRawSlot();
            Player p = (Player) e.getWhoClicked();
            if (lootTableHashMap.containsKey(slot + 1)){
                LootTable lootTable = lootTableHashMap.get(slot + 1);
                lootTable.openLoottableMenu(p);
            }
        }

    }


    public HashMap<Resource, Float> getTable() {
        return table;
    }

    public Location getLocation() {
        return location;
    }

    public String getName() {
        return name;
    }

    public int getID() {
        return ID;
    }



    public static void saveLoottables() throws IOException {

        for (Map.Entry<Integer, LootTable> set : lootTableHashMap.entrySet()) {

            int id = set.getKey();
            LootTable lootTable = set.getValue();
            if (lootTable != null) {
                for (Map.Entry<Resource, Float> set2 : lootTable.getTable().entrySet()){
                    Resource resource = set2.getKey();
                    Float f = set2.getValue();
                    loottableFile.set("data." + id + ".items." + resource.getKey(), f);
                }
                loottableFile.set("data." + id + ".name", lootTable.getName());
                loottableFile.set("data." + id + ".location", lootTable.getLocation());
                loottableFile.set("data." + id + ".id", lootTable.getID());
            }
        }
        saveFile(loottableFile, "loottables.yml");

    }


    public static HashMap<Integer, LootTable> loadLoottables() throws IOException {

        loottableFile = loadFile("loottables.yml");
        if (loottableFile == null) return new HashMap<>();
        if (loottableFile.getConfigurationSection("data") == null) return new HashMap<>();

        HashMap<Integer, LootTable> lootTableHashMap = new HashMap<>();


        loottableFile.getConfigurationSection("data").getKeys(false).forEach(key -> {

            String name = loottableFile.getString("data." + key + ".name");
            Location location = loottableFile.getLocation("data." + key + ".location");
            int ID = loottableFile.getInt("data." + key + ".id");

            LootTable lootTable = new LootTable(name, location, ID);

            if ((loottableFile.getConfigurationSection("data." + key + ".items")) != null) {
                loottableFile.getConfigurationSection("data." + key + ".items").getKeys(false).forEach(key2 -> {
                    int resourceID = Integer.valueOf(key2);
                    Resource resource = Resource.getResourceFromKey(resourceID);
                    Float f = (Float) loottableFile.get("data." + key + ".items." + resourceID);
                    lootTable.getTable().put(resource, f);
                });
            }

            lootTableHashMap.put(ID, lootTable);
        });

        return lootTableHashMap;
    }


}
