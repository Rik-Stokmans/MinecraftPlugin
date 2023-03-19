package aminecraftplugin.aminecraftplugin.drill;

import net.kyori.adventure.text.Component;
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

import java.io.File;
import java.io.IOException;
import java.util.*;

import static aminecraftplugin.aminecraftplugin.Main.loadFile;
import static aminecraftplugin.aminecraftplugin.Main.saveFile;
import static aminecraftplugin.aminecraftplugin.utils.ChatUtils.format;

public class Resource implements Listener {


    //todo: make GUI for adding new resources



    public static HashMap<String, HashMap<Integer, Resource>> resources;
    private static HashMap<Player, String> browsingCategory = new HashMap<>();


    public static YamlConfiguration resourceFile;

    private ItemStack itemStack;
    private String name;
    private Double value;


    public Resource(){

    }

    public Resource(ItemStack item, String name, Double value){
        this.itemStack = item;
        this.name = name;
        this.value = value;
    }


    public static void init(){
        try {
            resources = loadResources();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void save(){
        try {
            saveResources();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void openResourceGUI(Player p) {

        Inventory inventory = Bukkit.createInventory(null, InventoryType.HOPPER, "choose category");
        inventory.setItem(0, new ItemStack(Material.IRON_INGOT));
        inventory.setItem(1, new ItemStack(Material.COAL));
        inventory.setItem(2,new ItemStack(Material.EMERALD));
        inventory.setItem(3, new ItemStack(Material.GOLDEN_SHOVEL));
        p.openInventory(inventory);


    }

    private static int getMaxAmountOfPages(){
        int amount = resources.keySet().size();
        int pages = (int) Math.ceil(amount / 45);
        return pages;
    }

    private static ArrayList<Inventory> getAllPages(){

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
        for (HashMap<Integer, Resource> resourceEntry : resources.values()){
            for (Map.Entry<Integer, Resource> resourceEntry1 : resourceEntry.entrySet()) {
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
                Resource resource = resourceEntry1.getValue();
                ItemStack item = resource.getItemStack().clone();
                ItemMeta metaItem = item.getItemMeta();
                if (metaItem != null) {
                    metaItem.setDisplayName(resource.getName());
                    ArrayList<String> lore = new ArrayList<>();
                    lore.add("ID: " + resourceEntry1.getKey());
                    lore.add("value: " + resource.getValue());
                    metaItem.setLore(lore);
                    item.setItemMeta(metaItem);
                }
                currentlyEditing.setItem(totalIndex, item);
                totalIndex += 1;
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

    private static Inventory getPage(int page){
        return getAllPages().get(page - 1);
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
                    browsingCategory.put(p, "metals");
                    break;
                case(1):
                    browsingCategory.put(p, "energy");
                    break;
                case(2):
                    browsingCategory.put(p, "gemstones");
                    break;
                case(3):
                    browsingCategory.put(p, "archeology");
                    break;
            }
            Inventory inventory = Bukkit.createInventory(null, 54, "page 1");
            inventory.setContents(getPage(1).getContents());
            p.openInventory(inventory);
        }
        if (name.contains(format("page"))) {
            e.setCancelled(true);
            int currentPage = Integer.parseInt(name.split(" ")[1]);
            Player p = (Player) e.getWhoClicked();

            if (e.getClick().equals(ClickType.LEFT) && !e.getCurrentItem().getType().equals(Material.AIR)) {
                if (e.getRawSlot() == 45 && currentPage > 1) {
                    p.openInventory(getPage(currentPage - 1));
                } else if (e.getRawSlot() == 53 && currentPage < getMaxAmountOfPages()) {
                    p.openInventory(getPage(currentPage + 1));
                } else if (e.getRawSlot() > 53) {
                    net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(e.getCurrentItem());
                    NBTTagCompound nbt = nmsItem.u();
                    Resource resource = new Resource(e.getCurrentItem(), e.getCurrentItem().getItemMeta().getDisplayName(), nbt.k("value"));

                    resources.get(browsingCategory.get(p)).put(findEmptyID(), resource);
                    p.openInventory(getPage(currentPage));
                }
            }
            if (e.getClick().equals(ClickType.RIGHT) && e.getRawSlot() < 45){
                ItemStack clickedItem = e.getCurrentItem();
                ItemMeta meta = clickedItem.getItemMeta();
                ArrayList<String> lore = (ArrayList<String>) meta.getLore();
                int ID = Integer.valueOf(lore.get(0).split(" ")[1]);
                resources.remove(ID);
                p.openInventory(getPage(currentPage));
            }
        }
    }

    public static void saveResources() throws IOException {

        for (Map.Entry<String ,HashMap<Integer, Resource>> set : resources.entrySet()) {
            String category = set.getKey();

            for (Map.Entry<Integer, Resource> set2: set.getValue().entrySet()) {
                int id = set2.getKey();
                Resource resource = set2.getValue();
                resourceFile.set("data." + category + "." + id + ".itemstack", resource.getItemStack());
                resourceFile.set("data." + category + "." + id + ".name", resource.getName());
                resourceFile.set("data." + category + "." + id + ".value", resource.getValue());
            }
        }
        saveFile(resourceFile, "resources.yml");

    }

    public static HashMap<String ,HashMap<Integer, Resource>> loadResources() throws IOException{

        resourceFile = loadFile("resources.yml");
        if (resourceFile == null) return new HashMap<>();
        if (resourceFile.getConfigurationSection("data") == null) return new HashMap<>();

        HashMap<String, HashMap<Integer, Resource>> resourceHashMap = new HashMap<>();


        resourceFile.getConfigurationSection("data").getKeys(false).forEach(key -> {
            HashMap<Integer, Resource> resourcesInCategory = new HashMap<>();
            resourceFile.getConfigurationSection("data." + key).getKeys(false).forEach(key2 ->{
                ItemStack item = resourceFile.getItemStack("data." + key + "." + key2 +  ".itemstack");
                String name = resourceFile.getString("data." + key + "." + key2 + ".name");
                Double value = resourceFile.getDouble("data." + key + "." + key2 + ".value");
                resourcesInCategory.put(Integer.valueOf(key2), new Resource(item, name, value));
            });
            resourceHashMap.put(key, resourcesInCategory);
        });

        return resourceHashMap;
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
}
