package aminecraftplugin.aminecraftplugin.drill;

import net.minecraft.nbt.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_19_R2.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_19_R2.inventory.util.CraftInventoryCreator;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static aminecraftplugin.aminecraftplugin.Main.loadFile;
import static aminecraftplugin.aminecraftplugin.Main.saveFile;
import static aminecraftplugin.aminecraftplugin.drill.resourceCategory.getCategory;
import static aminecraftplugin.aminecraftplugin.utils.ChatUtils.format;

public class Resource implements Listener {


    //todo: make GUI for adding new resources



    public static HashMap<Integer, Resource> resources = new HashMap<>();

    public static HashMap<resourceCategory, ArrayList<Integer>> categories = new HashMap<>();

    private static HashMap<Player, resourceCategory> browsingCategory = new HashMap<>();


    private static YamlConfiguration resourceFile;
    private static YamlConfiguration categoryFile;

    private ItemStack itemStack;
    private String name;
    private Double value;
    private int key;



    public Resource(){

    }

    public Resource(ItemStack item, String name, Double value, int key){
        this.itemStack = item;
        this.name = name;
        this.value = value;
        this.key = key;
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


    public static void openResourceGUI(Player p) {

        Inventory inventory = Bukkit.createInventory(null, InventoryType.HOPPER, "choose category");
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

    private static int getMaxAmountOfPages(){
        int amount = resources.keySet().size();
        int pages = (int) Math.ceil(amount / 45);
        return pages;
    }

    private static ArrayList<Inventory> getAllPages(resourceCategory resourceCategory){

        ItemStack leftArrow = new ItemStack(Material.ARROW);
        ItemMeta meta = leftArrow.getItemMeta();
        meta.setDisplayName("Previous Page");
        leftArrow.setItemMeta(meta);

        ItemStack rightArrow = new ItemStack(Material.ARROW);
        ItemMeta meta2 = rightArrow.getItemMeta();
        meta2.setDisplayName("Next Page");
        rightArrow.setItemMeta(meta2);

        ItemStack grayGlass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta3 = grayGlass.getItemMeta();
        meta3.setDisplayName(format("&7"));
        grayGlass.setItemMeta(meta3);

        ArrayList<Inventory> allPages = new ArrayList<>();
        int totalIndex = 0;
        int currentPage = 1;
        Inventory currentlyEditing = Bukkit.createInventory(null, 54, format("page " + currentPage));
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
                    if (totalIndex % 45 == 0 && totalIndex != 0) {

                        Inventory addedInventory = Bukkit.createInventory(null, 54, format("page " + currentPage));
                        addedInventory.setContents(currentlyEditing.getContents().clone());
                        allPages.add(addedInventory);

                        currentlyEditing = Bukkit.createInventory(null, 54);
                        currentlyEditing.setItem(45, leftArrow);
                        currentlyEditing.setItem(53, rightArrow);
                        for (int i = 46; i < 53; i++) {
                            currentlyEditing.setItem(i, grayGlass);
                        }
                    }
                    ItemStack item = resource.getItemStack().clone();
                    ItemMeta metaItem = item.getItemMeta();
                    if (metaItem != null) {
                        metaItem.setDisplayName(resource.getName());
                        ArrayList<String> lore = new ArrayList<>();
                        lore.add("ID: " + integer);
                        lore.add("value: " + resource.getValue());
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
        Inventory addedInventory = Bukkit.createInventory(null, 54, format("page " + currentPage));
        addedInventory.setContents(currentlyEditing.getContents().clone());
        addedInventory.setItem(45, leftArrow);
        addedInventory.setItem(53, rightArrow);
        for (int i = 46; i < 53; i++){
            addedInventory.setItem(i, grayGlass);
        }
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

    private static Inventory getPage(int page, resourceCategory resourceCategory){
        return getAllPages(resourceCategory).get(page - 1);
    }

    @EventHandler
    private void clickEvent(InventoryClickEvent e){
        if (e.getView() == null) return;
        String name = e.getView().getTitle();
        if (name.contains("choose category")){
            e.setCancelled(true);
            int slot = e.getRawSlot();
            Player p = (Player) e.getWhoClicked();
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

            Inventory inventory = Bukkit.createInventory(null, 54, "page 1");
            inventory.setContents(getPage(1, browsingCategory.get(p)).getContents());
            p.openInventory(inventory);
        }
        if (name.contains(format("page"))) {
            e.setCancelled(true);
            int currentPage = Integer.parseInt(name.split(" ")[1]);
            Player p = (Player) e.getWhoClicked();
            int slot = e.getRawSlot();


            if (e.getClick().equals(ClickType.LEFT) && !e.getCurrentItem().getType().equals(Material.AIR)) {
                if (slot == 45 && currentPage > 1) {
                    p.openInventory(getPage(currentPage - 1, browsingCategory.get(p)));
                } else if (slot == 53 && currentPage < getMaxAmountOfPages()) {
                    p.openInventory(getPage(currentPage + 1, browsingCategory.get(p)));
                } else if (slot > 53) {
                    net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(e.getCurrentItem());
                    NBTTagCompound nbt = nmsItem.u();
                    int ID = findEmptyID();
                    Resource resource = new Resource(e.getCurrentItem(), e.getCurrentItem().getItemMeta().getDisplayName(), nbt.k("value"), ID);
                    resources.put(ID, resource);
                    if (categories.get(browsingCategory.get(p)) == null){
                        categories.put(browsingCategory.get(p), new ArrayList<>());
                    }
                    categories.get(browsingCategory.get(p)).add(ID);
                    p.openInventory(getPage(currentPage, browsingCategory.get(p)));
                }
            }


            if (e.getClick().equals(ClickType.RIGHT) && slot < 45){
                int num = slot + (currentPage - 1) * 45;
                int ID = categories.get(browsingCategory.get(p)).get(num);
                resources.remove(ID);
                categories.remove(ID);
                p.openInventory(getPage(currentPage, browsingCategory.get(p)));
            }
        }
    }

    public static void saveResources() throws IOException {

        for (Map.Entry<Integer, Resource> set : resources.entrySet()) {

            int id = set.getKey();
            Resource resource = set.getValue();
            if (resource != null && resource.getItemStack() != null) {
                resourceFile.set("data." + "." + id + ".itemstack", resource.getItemStack());
                resourceFile.set("data." + "." + id + ".name", resource.getName());
                resourceFile.set("data." + "." + id + ".value", resource.getValue());
                resourceFile.set("data." + "." + id + ".id", resource.getKey());
            }
        }
        saveFile(resourceFile, "resources.yml");

    }

    public static HashMap<Integer, Resource> loadResources() throws IOException{

        resourceFile = loadFile("resources.yml");
        if (resourceFile == null) return new HashMap<>();
        if (resourceFile.getConfigurationSection("data") == null) return new HashMap<>();

        HashMap<Integer, Resource> resourceHashMap = new HashMap<>();


        resourceFile.getConfigurationSection("data").getKeys(false).forEach(key -> {
            ItemStack item = resourceFile.getItemStack("data." + key + ".itemstack");
            String name = resourceFile.getString("data." + key + ".name");
            Double value = resourceFile.getDouble("data." + key + ".value");
            int ID = resourceFile.getInt("data." + key + ".id");
            resourceHashMap.put(Integer.valueOf(key), new Resource(item, name, value, ID));
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
            }
            else if (categories.get(resourceCategory.OTHER).contains(resourceKey)) {
                return resourceCategory.OTHER;
            }
        }
        return resourceCategory.NULL;
    }

    public static int getKeyFromItemstack(ItemStack item) {
        int key = -1;

        for (Resource r : resources.values()) {
            if (r.itemStack.isSimilar(item)) key = r.getKey();
        }

        return key;
    }

}
