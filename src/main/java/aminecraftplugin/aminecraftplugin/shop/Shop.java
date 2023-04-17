package aminecraftplugin.aminecraftplugin.shop;

import aminecraftplugin.aminecraftplugin.market.Market;
import com.comphenix.protocol.events.PacketContainer;
import me.filoghost.holographicdisplays.api.hologram.Hologram;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static aminecraftplugin.aminecraftplugin.Main.*;
import static aminecraftplugin.aminecraftplugin.shop.ShopCategory.getShopCategoryFromString;
import static aminecraftplugin.aminecraftplugin.utils.ChatUtils.format;

public class Shop {

    public static ArrayList<Shop> shops = new ArrayList<>();
    private Location location;
    private String name;
    private HashMap<ShopCategory, ArrayList<Integer>> categories = new HashMap<>();
    private HashMap<Integer, ShopItem> shopItems = new HashMap<>();
    private Hologram hologram;
    private static HashMap<Player, Shop> lastOpenedShop = new HashMap<>();

    public Shop(){

    }

    public Shop(String name, Location location) {
        this.name = name;
        this.location = location;
        this.generateHologram();
        shops.add(this);
    }

    public Shop(String name, Location location, HashMap<ShopCategory, ArrayList<Integer>> categories, HashMap<Integer, ShopItem> shopItems) {
        this.name = name;
        this.location = location;
        this.categories = categories;
        this.shopItems = shopItems;
        this.generateHologram();
        shops.add(this);
    }

    public void generateHologram() {
        this.hologram = api.createHologram(this.getLocation().clone().add(0.5, 3.1875, 0.5));
        hologram.getLines().appendText(format(name + " shop"));
    }

    public static void rightClickShopEvent(NPCRightClickEvent e) {
        Player p = e.getClicker();
        Location location = e.getNPC().getStoredLocation();
        for (Shop shop : shops) {
            if (shop.getLocation().distance(location) < 1.5) {
                shop.openShop(p);
            }
        }
    }

    public void openShop(Player p){
        p.openInventory(getCategorySelectingInventory());
        lastOpenedShop.put(p, this);
    }

    public void openShop(Player p, ShopCategory shopCategory){
        p.openInventory(this.getShop(shopCategory));
    }

    public Inventory getCategorySelectingInventory(){
        Inventory inventory = Bukkit.createInventory(null, 45, this.getName()  + " shop");
        int index = 10;
        for (ShopCategory shopCategory : this.getCategories().keySet()){
            ItemStack item = shopCategory.getIcon();
            inventory.setItem(index, item);
            index += 2;
            if ((index + 1) % 9 == 0){
                index += 11;
                if (index > 44) break;
            }
        }
        return inventory;
    }

    public Inventory getShop(ShopCategory shopCategory){
        Inventory inventory = Bukkit.createInventory(null, 45, this.getName()  + " shop ► " + shopCategory.getStringFromShopCategory());
        return inventory;
    }

    public static void shopCategoryInventoryClickEvent(InventoryClickEvent e){
        if (e.getView() == null) return;
        String name = e.getView().getTitle();
        Player p = (Player) e.getWhoClicked();
        if (name.contains("shop") && lastOpenedShop.containsKey(p)) {
            Shop shop = lastOpenedShop.get(p);
            ShopCategory selectedCategory = ShopCategory.NULL;
            int slot = e.getRawSlot();
            int index = 10;
            for (ShopCategory shopCategory : shop.getCategories().keySet()){
                if (slot == index){
                    selectedCategory = shopCategory;
                    break;
                }
                index += 2;
                if ((index + 1) % 9 == 0){
                    index += 11;
                    if (index > 44) break;
                }
            }
            shop.openShop(p, selectedCategory);
        }
    }

    public void delete(){
        this.getHologram().delete();
        this.shopItems = new HashMap<>();
        this.categories = new HashMap<>();
    }

    public static void init(){
        try {
            loadShops();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void save(){
        try {
            saveShops();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadShops() throws IOException {

        File[] allShopFiles = new File(plugin.getDataFolder() + "/shops").listFiles();
        if (allShopFiles == null) return;

        for (File file : allShopFiles) {
            YamlConfiguration shopFile = loadFile("shops/" + file.getName());
            if (shopFile == null) continue;

            String name = file.getName().substring(0, file.getName().length() - 4);
            Location location = shopFile.getLocation("location");
            HashMap<ShopCategory, ArrayList<Integer>> categories = new HashMap<>();
            if (shopFile.contains("categories")){
                shopFile.getConfigurationSection("categories").getKeys(false).forEach(key -> {
                    ShopCategory shopCategory = getShopCategoryFromString(key);
                    ArrayList<Integer> intList = (ArrayList<Integer>) shopFile.getIntegerList("categories." + key);
                    categories.put(shopCategory, intList);
                });
            }
            HashMap<Integer, ShopItem> shopItems = new HashMap<>();
            if (shopFile.contains("shopitems")){
                shopFile.getConfigurationSection("shopitems").getKeys(false).forEach(key -> {
                    int ID = Integer.parseInt(key);
                    ItemStack shopItemItemStack = shopFile.getItemStack("shopitems." + key + ".item");
                    double price = shopFile.getDouble("shopitems." + key + ".price");
                    ShopItem shopItem = new ShopItem(shopItemItemStack, price);
                    shopItems.put(ID, shopItem);
                });
            }
            new Shop(name, location, categories, shopItems);
        }
    }

    public static void saveShops() throws IOException {
        for (Shop shop : shops){
            YamlConfiguration shopFile = new YamlConfiguration();
            shopFile.set("location", shop.getLocation());
            for (Map.Entry<ShopCategory, ArrayList<Integer>> category : shop.getCategories().entrySet()){
                shopFile.set(category.getKey().toString(), category.getValue());
            }
            for (Map.Entry<Integer, ShopItem> shopItem : shop.getShopItems().entrySet()){
                shopFile.set("shopitems." + shopItem.getKey() + ".item", shopItem.getValue().getItem());
                shopFile.set("shopitems." + shopItem.getKey() + ".price", shopItem.getValue().getPrice());
            }
            saveFile(shopFile, "shops/" + shop.getName() + ".yml");
        }
    }

    public Location getLocation() {
        return location;
    }

    public String getName() {
        return name;
    }

    public HashMap<ShopCategory, ArrayList<Integer>> getCategories() {
        return categories;
    }

    public HashMap<Integer, ShopItem> getShopItems() {
        return shopItems;
    }

    public Hologram getHologram() {
        return hologram;
    }
}
