package aminecraftplugin.aminecraftplugin.drill.loot;

import net.minecraft.nbt.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_19_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.IOException;
import java.util.*;

import static aminecraftplugin.aminecraftplugin.Main.loadFile;
import static aminecraftplugin.aminecraftplugin.Main.saveFile;
import static aminecraftplugin.aminecraftplugin.drill.loot.LootTable.lootTableAdding;
import static aminecraftplugin.aminecraftplugin.drill.loot.resourceCategory.getCategory;
import static aminecraftplugin.aminecraftplugin.utils.ChatUtils.format;
import static aminecraftplugin.aminecraftplugin.utils.defaultPageInventory.getDefaultScrollableInventory;

public class Resource implements Listener {

    public static HashMap<Integer, Resource> resources = new HashMap<>();

    public static HashMap<resourceCategory, ArrayList<Integer>> categories = new HashMap<>();

    private static HashMap<Player, resourceCategory> browsingCategory = new HashMap<>();


    private static YamlConfiguration resourceFile;
    private static YamlConfiguration categoryFile;

    private ItemStack itemStack;
    private String name;
    private Double value;
    private int key;
    private Material block;



    public Resource(){

    }

    public Resource(ItemStack item, String name, Double value, int key, Material block){
        this.itemStack = item;
        this.name = name;
        this.value = value;
        this.key = key;
        this.block = block;
    }


    public static void init(){
        try {
            resources = loadResources();
            categories = loadCategories();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void save(){
        try {
            saveResources();
            saveCategories();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    private void clickEvent(InventoryClickEvent e){
        if (e.getView() == null) return;
        String name = e.getView().getTitle();
        if (name.contains("Choose category")){
            e.setCancelled(true);
            Player p = (Player) e.getWhoClicked();
            int slot = e.getRawSlot();
            switch (slot){
                case(0):
                    browsingCategory.put(p, resourceCategory.METALS);
                    break;
                case(1):
                    browsingCategory.put(p, resourceCategory.ENERGY);
                    break;
                case(2):
                    browsingCategory.put(p, resourceCategory.GEMSTONES);
                    break;
                case(3):
                    browsingCategory.put(p, resourceCategory.ARCHEOLOGY);
                    break;
                case(4):
                    browsingCategory.put(p, resourceCategory.OTHER);
                    break;
            }

            openCategoryMenu(p, browsingCategory.get(p), 1);
        }
        if (name.contains("resource category")) {
            e.setCancelled(true);
            int currentPage = Integer.parseInt(name.split("category Page ")[1]);
            int slot = e.getRawSlot();
            Player p = (Player) e.getWhoClicked();
            boolean adding = false;
            if (lootTableAdding.containsKey(p)){
                adding = true;
            }
            if (e.getCurrentItem() != null &&
                    e.getClick().equals(ClickType.LEFT)) {
                if (slot < 36){
                    int num = slot + (currentPage - 1) * 36;
                    int ID = categories.get(browsingCategory.get(p)).get(num);
                    Resource resource = resources.get(ID);
                    if (adding){
                        LootTable lootTable = lootTableAdding.get(p);
                        int newID = lootTable.findNewID();
                        lootTable.getTable().put(newID, 1.0f);
                        lootTable.getResources().put(newID, ID);

                        //insert new ID into correct position (sorted array list)
                        int index = 0;
                        for (Integer i : lootTable.getIDs()){
                            if (i > newID){
                                break;
                            }
                            index++;
                        }
                        lootTable.getIDs().add(index, newID);

                        lootTable.openLoottableMenu(p, 1);
                    } else {
                        p.getInventory().addItem(resource.getItemStack());
                    }
                }
                else if (slot == 45 && currentPage > 1) {
                    openCategoryMenu(p, browsingCategory.get(p), currentPage - 1);
                } else if (slot == 49){
                    openResourceGUI(p);
                } else if (slot == 53 && currentPage < getMaxAmountOfPages(browsingCategory.get(p))) {
                    openCategoryMenu(p, browsingCategory.get(p), currentPage + 1);
                } else if (slot > 53) {
                    net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(e.getCurrentItem());
                    NBTTagCompound nbt = nmsItem.u();
                    int ID = findEmptyID();
                    Resource resource = new Resource(e.getCurrentItem(), e.getCurrentItem().getItemMeta().getDisplayName(), nbt.k("value"), ID, Material.getMaterial(nbt.l("material")));
                    resources.put(ID, resource);
                    if (categories.get(browsingCategory.get(p)) == null){
                        categories.put(browsingCategory.get(p), new ArrayList<>());
                    }
                    categories.get(browsingCategory.get(p)).add(ID);
                    openCategoryMenu(p, browsingCategory.get(p), currentPage);
                }
            }


            if (e.getClick().equals(ClickType.RIGHT) && slot < 36 && !adding){
                int num = slot + (currentPage - 1) * 36;
                int ID = categories.get(browsingCategory.get(p)).get(num);
                Resource resource = resources.get(ID);
                p.getInventory().addItem(resource.getItemStack());
                resources.remove(ID);
                categories.get(browsingCategory.get(p)).remove((Integer) ID);
                sortCategory(browsingCategory.get(p));
                p.openInventory(getPage(p, currentPage, browsingCategory.get(p)));
            }
        }
    }


    public static void openResourceGUI(Player p) {

        Inventory inventory = Bukkit.createInventory(null, InventoryType.HOPPER, "Choose category");
        ItemStack metals = new ItemStack(Material.IRON_INGOT);
        ItemStack energy = new ItemStack(Material.COAL);
        ItemStack gemstones = new ItemStack(Material.EMERALD);
        ItemStack archeology = new ItemStack(Material.GOLDEN_SHOVEL);
        ItemStack other = new ItemStack(Material.SUNFLOWER);

        ItemMeta metaMetals = metals.getItemMeta();
        ItemMeta metaEnergy = energy.getItemMeta();
        ItemMeta metaGemstones = gemstones.getItemMeta();
        ItemMeta metaArcheology = archeology.getItemMeta();
        ItemMeta metaOther = other.getItemMeta();

        metaMetals.setDisplayName(format("&fMetals"));
        metaEnergy.setDisplayName(format("&fEnergy"));
        metaGemstones.setDisplayName(format("&fGemstones"));
        metaArcheology.setDisplayName(format("&fArcheology"));
        metaOther.setDisplayName(format("&fOther"));

        metals.setItemMeta(metaMetals);
        energy.setItemMeta(metaEnergy);
        gemstones.setItemMeta(metaGemstones);
        archeology.setItemMeta(metaArcheology);
        other.setItemMeta(metaOther);

        inventory.setItem(0, metals);
        inventory.setItem(1, energy);
        inventory.setItem(2, gemstones);
        inventory.setItem(3, archeology);
        inventory.setItem(4, other);
        p.openInventory(inventory);


    }

    private static int getMaxAmountOfPages(resourceCategory resourceCategory){
        if (!categories.containsKey(resourceCategory)) return 0;
        int amount = categories.get(resourceCategory).size();
        int pages = (int) Math.ceil(amount / 36);
        return pages;
    }

    private static ArrayList<Inventory> getAllPages(Player p, resourceCategory resourceCategory){

        ArrayList<Inventory> allPages = new ArrayList<>();
        int totalIndex = 0;
        int currentPage = 1;
        String name = resourceCategory + " resource category " + "Page " + currentPage;
        if (lootTableAdding.containsKey(p)){
            name = "Select resource from resource category Page " + currentPage;
        }
        Inventory currentlyEditing = getDefaultScrollableInventory(name, true);
        for (Map.Entry<resourceCategory, ArrayList<Integer>> categories : categories.entrySet()) {
            resourceCategory resourceCategory1 = categories.getKey();
            if (resourceCategory1.equals(resourceCategory)) {
                ArrayList<Integer> bugged = new ArrayList<>();
                for (Integer integer : categories.getValue()) {
                    if (!resources.containsKey(integer)){
                        bugged.add(integer);
                        continue;
                    }
                    Resource resource = resources.get(integer);
                    if (totalIndex % 35 == 0 && totalIndex != 0) {

                        Inventory addedInventory = Bukkit.createInventory(null, 54, resourceCategory + " resource category " + "Page " + currentPage);
                        addedInventory.setContents(currentlyEditing.getContents().clone());
                        allPages.add(addedInventory);

                        currentlyEditing = getDefaultScrollableInventory(resourceCategory + " resource category " + "Page " + currentPage, true);
                    }
                    ItemStack item = resource.getItemStack().clone();
                    ItemMeta metaItem = item.getItemMeta();
                    if (metaItem != null) {
                        metaItem.setDisplayName(resource.getName());
                        ArrayList<String> lore = new ArrayList<>();
                        lore.add(format("&e-----Resource info-----"));
                        lore.add(format("&7ID: &f" + integer));
                        lore.add(format("&7value: &f" + resource.getValue()));
                        lore.add("");
                        lore.add(format("&aLeft click to copy to inventory"));
                        lore.add(format("&cRight click to delete"));
                        metaItem.setLore(lore);
                        item.setItemMeta(metaItem);
                    }
                    currentlyEditing.setItem(totalIndex, item);
                    totalIndex += 1;
                }
                for (Integer i : bugged){
                    categories.getValue().remove(i);
                }
            }
        }
        Inventory addedInventory = Bukkit.createInventory(null, 54, resourceCategory + " resource category " + "Page " + currentPage);
        addedInventory.setContents(currentlyEditing.getContents().clone());
        allPages.add(addedInventory);
        return allPages;
    }

    private static int findEmptyID(){
        int index = 1;
        while(resources.containsKey(index)){
            index++;
        }
        return index;
    }

    private static Inventory getPage(Player p, int page, resourceCategory resourceCategory){
        return getAllPages(p, resourceCategory).get(page - 1);
    }

    private static void openCategoryMenu(Player p, resourceCategory resourceCategory, int page){
        Inventory inventory = Bukkit.createInventory(null, 54, resourceCategory + " resource category " + "Page " + page);
        sortCategory(resourceCategory);
        inventory.setContents(getPage(p, page, resourceCategory).getContents());
        p.openInventory(inventory);
    }


    private static void sortCategory(resourceCategory resourceCategory){
        if (!categories.isEmpty() && !categories.get(resourceCategory).isEmpty())  Collections.sort(categories.get(resourceCategory));
    }

    private static void saveResources() throws IOException {

        for (Map.Entry<Integer, Resource> set : resources.entrySet()) {

            int id = set.getKey();
            Resource resource = set.getValue();
            if (resource != null && resource.getItemStack() != null) {
                resourceFile.set("data." + "." + id + ".itemstack", resource.getItemStack());
                resourceFile.set("data." + "." + id + ".name", resource.getName());
                resourceFile.set("data." + "." + id + ".value", resource.getValue());
                resourceFile.set("data." + "." + id + ".id", resource.getKey());
                resourceFile.set("data." + "." + id + ".material", resource.getBlock().name());
            }
        }
        saveFile(resourceFile, "resources.yml");

    }

    private static HashMap<Integer, Resource> loadResources() throws IOException{

        resourceFile = loadFile("resources.yml");
        if (resourceFile == null) return new HashMap<>();
        if (resourceFile.getConfigurationSection("data") == null) return new HashMap<>();

        HashMap<Integer, Resource> resourceHashMap = new HashMap<>();


        resourceFile.getConfigurationSection("data").getKeys(false).forEach(key -> {
            ItemStack item = resourceFile.getItemStack("data." + key + ".itemstack");
            String name = resourceFile.getString("data." + key + ".name");
            Double value = resourceFile.getDouble("data." + key + ".value");
            int ID = resourceFile.getInt("data." + key + ".id");
            String materialName = resourceFile.getString("data." + key + ".material");
            if (materialName != null) {
                resourceHashMap.put(Integer.valueOf(key), new Resource(item, name, value, ID, Material.getMaterial(materialName)));
            }
        });

        return resourceHashMap;
    }

    private static void saveCategories() throws IOException {
        for (Map.Entry<resourceCategory, ArrayList<Integer>> set : categories.entrySet()) {

            resourceCategory resourceCategory = set.getKey();
            ArrayList<Integer> IDs = set.getValue();
            ArrayList<Integer> removals = new ArrayList<>();
            for (Integer i : IDs){
                if (!resources.containsKey(i)){
                    removals.add(i);
                }
            }
            for (Integer i : removals){
                resources.remove(i);
            }
            categoryFile.set("data." + resourceCategory.toString(), IDs);
        }
        saveFile(categoryFile, "categories.yml");
    }

    private static HashMap<resourceCategory, ArrayList<Integer>> loadCategories() throws IOException {

        categoryFile = loadFile("categories.yml");
        if (categoryFile == null) return new HashMap<>();
        if (categoryFile.getConfigurationSection("data") == null) return new HashMap<>();

        HashMap<resourceCategory, ArrayList<Integer>> categories = new HashMap<>();

        categoryFile.getConfigurationSection("data").getKeys(false).forEach(key -> {
            resourceCategory resourceCategory = getCategory(key);
            ArrayList<Integer> intList = (ArrayList<Integer>) categoryFile.getIntegerList("data." + key);
            categories.put(resourceCategory, intList);
        });

        resourceCategory[] resourceCategories = {resourceCategory.METALS, resourceCategory.ENERGY, resourceCategory.GEMSTONES
        , resourceCategory.ARCHEOLOGY, resourceCategory.OTHER};
        for (resourceCategory resourceCategory : resourceCategories){
            if (!categories.containsKey(resourceCategory)){
                categories.put(resourceCategory, new ArrayList<>());
            }
        }

        return categories;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public String getName() {
        return name;
    }

    public Double getValue() {
        return value;
    }

    public int getKey() {
        return key;
    }

    public Material getBlock() {
        return block;
    }

    @Override
    public String toString() {
        return name;
    }

    public static Resource getResourceFromKey(int ID){
        for (Resource resource : resources.values()){
            if (resource.getKey() == ID){
                return resource;
            }
        }
        return null;
    }

    public static resourceCategory getCategoryFromResourceKey(int resourceKey){
        if (resources.containsKey(resourceKey)) {
            if (categories.get(resourceCategory.METALS).contains(resourceKey)) {
                return resourceCategory.METALS;
            } else if (categories.get(resourceCategory.ENERGY).contains(resourceKey)) {
                return resourceCategory.ENERGY;
            } else if (categories.get(resourceCategory.GEMSTONES).contains(resourceKey)) {
                return resourceCategory.GEMSTONES;
            } else if (categories.get(resourceCategory.ARCHEOLOGY).contains(resourceKey)) {
                return resourceCategory.ARCHEOLOGY;
            } else if (categories.get(resourceCategory.OTHER).contains(resourceKey)) {
                return resourceCategory.OTHER;
            }
        }
        return resourceCategory.NULL;
    }

    public static int getKeyFromItemstack(ItemStack item) {
        int key = -1;

        for (Resource r : resources.values()) {
            if (r.getName().equals(item.getItemMeta().getDisplayName())) key = r.getKey();
            Bukkit.broadcastMessage(r.getName() + "," + item.getItemMeta().getDisplayName());
        }

        return key;
    }

}
