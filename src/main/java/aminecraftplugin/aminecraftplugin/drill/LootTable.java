package aminecraftplugin.aminecraftplugin.drill;


import net.minecraft.nbt.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;

import org.bukkit.craftbukkit.v1_19_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
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
import static aminecraftplugin.aminecraftplugin.drill.Resource.getResourceFromKey;
import static aminecraftplugin.aminecraftplugin.drill.Resource.openResourceGUI;
import static aminecraftplugin.aminecraftplugin.utils.ChatUtils.format;
import static aminecraftplugin.aminecraftplugin.utils.defaultPageInventory.getDefaultScrollableInventory;

public class LootTable implements Listener {


    //resource weight factor
    private static HashMap<Integer, LootTable> lootTableHashMap = new HashMap<>();
    private static YamlConfiguration loottableFile;
    private static HashMap<Player, LootTable> lootTableBrowsing = new HashMap<>();
    public static HashMap<Player, LootTable> lootTableAdding = new HashMap<>();


    //keys of resources sorted by key ascending
    private HashMap<Integer, Integer> resources = new HashMap<>();

    //float is factor
    private HashMap<Integer, Float> table = new HashMap<>();

    //sorted array list
    private ArrayList<Integer> IDs = new ArrayList<>();

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


    public int findNewID(){
        int index = 1;
        while(true){
            if (!this.getIDs().contains(index)){
                break;
            }
            index++;
        }
        return index;
    }

    private Resource IDinTableToResource(Integer ID){
        int resourceID = this.getResources().get(ID);
        Resource resource = getResourceFromKey(resourceID);
        return resource;
    }

    @EventHandler
    private void clickEvent(InventoryClickEvent e){
        if (e.getView() == null) return;
        String name = e.getView().getTitle();
        if (name.contains("Select Loot Table")) {
            e.setCancelled(true);
            Player p = (Player) e.getWhoClicked();
            int slot = e.getRawSlot();
            if (lootTableHashMap.containsKey(slot + 1)){
                LootTable lootTable = lootTableHashMap.get(slot + 1);
                lootTable.openLoottableMenu(p, 1);
            }
        } else if (name.contains("Loot Table Page ")){
            e.setCancelled(true);
            int currentPage = Integer.parseInt(name.split("Loot Table Page ")[1]);
            int slot = e.getRawSlot();
            Player p = (Player) e.getWhoClicked();
            LootTable lootTable = lootTableBrowsing.get(p);
            if (e.getCurrentItem() != null && e.getCurrentItem().getType().equals(Material.LIME_STAINED_GLASS_PANE)){
                lootTableAdding.put(p, lootTable);
                openResourceGUI(p);
            }
            if (e.getCurrentItem() != null &&
                    e.getClick().equals(ClickType.LEFT)) {
                if (slot < 36){
                    int num = slot + (currentPage - 1) * 36;
                    int ID = lootTable.getIDs().get(num);
                    Float f = lootTable.getTable().get(ID) + 0.1f;
                    lootTable.getTable().put(ID, f);
                    lootTable.openLoottableMenu(p, currentPage);
                }
                else if (slot == 45 && currentPage > 1) {
                    lootTable.openLoottableMenu(p, currentPage - 1);
                } else if (slot == 49){
                    openSelectLoottableMenu(p);
                } else if (slot == 53 && currentPage < getMaxAmountOfPages(lootTable)) {
                    lootTable.openLoottableMenu(p, currentPage + 1);
                }
            }
            else if (e.getCurrentItem() != null && e.getClick().equals(ClickType.RIGHT) && slot < 36){
                int num = slot + (currentPage - 1) * 36;
                int IDinTable = num + 1;
                Float f = lootTable.getTable().get(IDinTable) - 0.1f;
                lootTable.getTable().put(IDinTable, f);
                lootTable.openLoottableMenu(p, currentPage);
            }
            else if (e.getCurrentItem() != null && e.getClick().equals(ClickType.SHIFT_RIGHT) && slot < 36){
                int num = slot + (currentPage - 1) * 36;
                int ID = lootTable.getIDs().get(num);
                lootTable.getTable().remove(ID);
                resources.remove(ID);
                lootTable.getIDs().remove(Integer.valueOf(ID));
                lootTable.openLoottableMenu(p, currentPage);
            }
        }

    }

    private static int getMaxAmountOfPages(LootTable lootTable){
        int amount = lootTable.getTable().keySet().size();
        int pages = (int) Math.ceil(amount / 36);
        return pages;
    }

    private static Inventory getPage(int page, LootTable lootTable){
        return getAllPages(lootTable).get(page - 1);
    }

    private static ArrayList<Inventory> getAllPages(LootTable lootTable){

        ArrayList<Inventory> allPages = new ArrayList<>();
        int totalIndex = 0;
        int currentPage = 1;
        Inventory currentlyEditing = getDefaultScrollableInventory( lootTable.getName() + " Loot Table Page " + currentPage, true);
        for (Integer ID : lootTable.getIDs()) {
            Resource resource = lootTable.IDinTableToResource(ID);
            Float factor = lootTable.getTable().get(resource);
            if (totalIndex % 35 == 0 && totalIndex != 0) {

                Inventory addedInventory = Bukkit.createInventory(null, 54, lootTable.getName() + " Loot Table Page " + currentPage);
                addedInventory.setContents(currentlyEditing.getContents().clone());
                allPages.add(addedInventory);
                currentlyEditing = getDefaultScrollableInventory(lootTable.getName() + " Loot Table Page " + currentPage, true);
                totalIndex = 0;
            }
            ItemStack item = resource.getItemStack().clone();
            ItemMeta metaItem = item.getItemMeta();
            if (metaItem != null) {
                metaItem.setDisplayName(resource.getName());
                ArrayList<String> lore = new ArrayList<>();
                lore.add(format("&e-----Table info-----"));
                lore.add(format("&7ID: &f" + ID));
                lore.add(format("&7Spawn factor: &f" + factor));
                lore.add(format("&aLeft click to increase factor"));
                lore.add(format("&cRight click to decrease factor"));
                lore.add(format("&e-----Resource info-----"));
                lore.add(format("&7ID: &f" + resource.getValue()));
                lore.add(format("&7value: &f" + resource.getValue()));
                metaItem.setLore(lore);
                item.setItemMeta(metaItem);
            }
            currentlyEditing.setItem(totalIndex, item);
            totalIndex += 1;

        }
        ItemStack greenGlass = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta glassMeta = greenGlass.getItemMeta();
        glassMeta.setDisplayName(format("&aClick to add resource"));
        greenGlass.setItemMeta(glassMeta);

        currentlyEditing.setItem(totalIndex, greenGlass);
        Inventory addedInventory = Bukkit.createInventory(null, 54, lootTable.getName() + " Loot Table Page " + currentPage);
        addedInventory.setContents(currentlyEditing.getContents().clone());
        allPages.add(addedInventory);
        return allPages;
    }

    public void openLoottableMenu(Player p, int page){

        if (lootTableAdding.containsKey(p)){
            lootTableAdding.remove(p);
        }

        lootTableBrowsing.put(p, this);

        Inventory inventory = getPage(page, this);
        p.openInventory(inventory);
    }

    public static void openSelectLoottableMenu(Player p){

        if (lootTableAdding.containsKey(p)){
            lootTableAdding.remove(p);
        }

        Inventory selectInventory = getDefaultScrollableInventory("Select Loot Table", false);;

        int index = 0;
        for (Integer i : lootTableHashMap.keySet()){
            LootTable lootTable = lootTableHashMap.get(i);
            ItemStack list = new ItemStack(Material.FILLED_MAP);
            ItemMeta listMeta = list.getItemMeta();
            listMeta.setDisplayName(lootTable.getName());
            ArrayList<String> lore = new ArrayList<>();

            Location loc = lootTable.getLocation();

            lore.add(format("&7Location: &f(" + "x: " + Math.round(loc.getX()) + ", y: " + Math.round(loc.getY()) + ", z: " + Math.round(loc.getZ()) + ")"));
            lore.add(format("&7ID: &f" + lootTable.getID()));
            for (Integer i2 : lootTable.getIDs()){
                Resource resource = lootTable.IDinTableToResource(i2);
                Float f = lootTable.getTable().get(i2);
                lore.add(resource.getName() + ": " + f);
            }
            listMeta.setLore(lore);
            list.setItemMeta(listMeta);
            selectInventory.setItem(index, list);
            index++;
        }

        p.openInventory(selectInventory);
    }




    public HashMap<Integer, Integer> getResources() {
        return resources;
    }

    public ArrayList<Integer> getIDs() {
        return IDs;
    }

    public void setIDs(ArrayList<Integer> IDs) {
        this.IDs = IDs;
    }

    public HashMap<Integer, Float> getTable() {
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

                loottableFile.set("data." + id + ".IDs", lootTable.getIDs());
                for (Map.Entry<Integer, Integer> set2 : lootTable.getResources().entrySet()) {
                    Integer lootID = set2.getKey();
                    Integer resourceID = set2.getValue();
                    loottableFile.set("data." + id + ".resourceIDs." + lootID, resourceID);
                }
                for (Map.Entry<Integer, Float> set2 : lootTable.getTable().entrySet()){
                    Integer lootID = set2.getKey();
                    Float f = set2.getValue();
                    loottableFile.set("data." + id + ".factors." + lootID, f);
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

            ArrayList<Integer> IDs = (ArrayList<Integer>) loottableFile.getIntegerList("data." + key + "IDs");
            lootTable.setIDs(IDs);

            if ((loottableFile.getConfigurationSection("data." + key + ".resourceIDs")) != null) {
                loottableFile.getConfigurationSection("data." + key + ".resourceIDs").getKeys(false).forEach(key2 -> {
                    int lootID = Integer.valueOf(key2);
                    Integer resourceID = loottableFile.getInt("data." + key + ".resourceIDs." + lootID);
                    lootTable.getResources().put(lootID, resourceID);
                });
            }

            if ((loottableFile.getConfigurationSection("data." + key + ".factors")) != null) {
                loottableFile.getConfigurationSection("data." + key + ".factors").getKeys(false).forEach(key2 -> {
                    int lootID = Integer.valueOf(key2);
                    Float f = Float.valueOf(String.valueOf(loottableFile.get("data." + key + ".factors." + lootID)));
                    lootTable.getTable().put(lootID, f);
                });
            }

            lootTableHashMap.put(ID, lootTable);
        });

        return lootTableHashMap;
    }


}
