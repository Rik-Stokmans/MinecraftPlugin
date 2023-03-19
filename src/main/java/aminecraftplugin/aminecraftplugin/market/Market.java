package aminecraftplugin.aminecraftplugin.market;

import aminecraftplugin.aminecraftplugin.drill.Resource;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;

import static aminecraftplugin.aminecraftplugin.utils.ChatUtils.format;

public class Market implements Listener {

    //todo: a gui for every market

    //ui/ux
    public static ItemStack backButton;
    public static ItemStack previousPageButton;
    public static ItemStack nextPageButton;
    public static ItemStack addItemButton;
    public static ItemStack removeItemButton;
    public static ItemStack darkDivider;
    public static ItemStack lightDivider;
    public static ItemStack metalsCategoryButton;
    public static ItemStack energyCategoryButton;
    public static ItemStack gemstonesCategoryButton;

    public static Inventory marketCategoryGuiMenu;

    public static HashMap<Player, Market> latestMarketOpen = new HashMap<>();

    //Hashmap loaded from file containing all markets
    public static HashMap<Integer, Market> markets = new HashMap<>();

    //all market attributes
    String name;
    Location location;
    ArrayList<Trade> trades;
    Inventory metalsGuiMenu;
    Inventory energyGuiMenu;
    Inventory gemstonesGuiMenu;
    int key;

    //market init
    public static void init() {
        initialiseGuiButtons();
        initialiseMarketCategoryGuiMenu();
    }



    //market constructors
    public Market() {

    }

    public Market(String _name, Location _location, int _key) {
        name = _name;
        location = _location;
        trades = new ArrayList<>();
        key = _key;
        generateMarketMenus();
    }

    public Market(String _name, Location _location, ArrayList<Integer> _tradeItemKeys, int _key) {
        name = _name;
        location = _location;
        trades = generateTrades(_tradeItemKeys);
        key = _key;
    }



    //method to generate all the trade items from a list of keys
    private ArrayList<Trade> generateTrades(ArrayList<Integer> tradeItemKeys) {
        ArrayList<Trade> trades = new ArrayList<>();
        for(int key : tradeItemKeys) {
            if (Resource.resources.containsKey(key)) {
                Resource r = Resource.resources.get(key);
                Trade trade = new Trade(r.getItemStack(), r.getName(), r.getValue(), key);
                trades.add(trade);
            }
        }
        return trades;
    }



    //method to generate the inventory of the market
    private void generateMarketMenus() {
        boolean hasTrades = this.trades.size() > 0;

        metalsGuiMenu = Bukkit.createInventory(null, 54, format("&eMetals"));
        energyGuiMenu = Bukkit.createInventory(null, 54, format("&eEnergy"));
        gemstonesGuiMenu = Bukkit.createInventory(null, 54, format("&eGemstones"));

        //separator
        for(int i = 36; i <= 44; i++) {
            metalsGuiMenu.setItem(i, darkDivider);
            energyGuiMenu.setItem(i, darkDivider);
            gemstonesGuiMenu.setItem(i, darkDivider);
        }

        //back button
        metalsGuiMenu.setItem(49, backButton);
        energyGuiMenu.setItem(49, backButton);
        gemstonesGuiMenu.setItem(49, backButton);

    }



    //gui methods
    private void openMarket(Player p) {
        p.openInventory(marketCategoryGuiMenu);
    }



    //function that detects if a player clicks on a market block
    @EventHandler
    public void marketClick(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        if (!e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;
        Location clickLocation = e.getClickedBlock().getLocation();

        for (Market m : markets.values()) {
            if (m.location.equals(clickLocation)) {
                latestMarketOpen.put(player, m);
                openMarket(player);
            }
        }
    }

    @EventHandler
    public void marketDestroy(BlockBreakEvent e) {
        Player p = e.getPlayer();

        Location breakLocation = e.getBlock().getLocation();

        for (Market m : markets.values()) {
            if (m.location.equals(breakLocation)) {
                e.setCancelled(true);
                p.sendMessage(format("&cYou can't break a market, remove it with &e/removeMarket"));
            }
        }
    }

    @EventHandler
    public void inventoryClick(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        String invName = e.getView().getTitle();
        if (invName.equals(format("&eCategory Selector"))) {
            e.setCancelled(true);
            if (!e.isLeftClick()) return;
            if (e.getCurrentItem().isSimilar(metalsCategoryButton)) {
                    p.openInventory(latestMarketOpen.get(p).metalsGuiMenu);
            }
            else if (e.getCurrentItem().isSimilar(energyCategoryButton)) {
                p.openInventory(latestMarketOpen.get(p).energyGuiMenu);
            }
            else if (e.getCurrentItem().isSimilar(gemstonesCategoryButton)) {
                p.openInventory(latestMarketOpen.get(p).gemstonesGuiMenu);
            }
        }
        else if (invName.equals(format("&eMetals"))) {
            e.setCancelled(true);
            if (e.getCurrentItem().isSimilar(backButton)) p.openInventory(marketCategoryGuiMenu);
        }
        else if (invName.equals(format("&eEnergy"))) {
            e.setCancelled(true);
            if (e.getCurrentItem().isSimilar(backButton)) p.openInventory(marketCategoryGuiMenu);
        }
        else if (invName.equals(format("&eGemstones"))) {
            e.setCancelled(true);
            if (e.getCurrentItem().isSimilar(backButton)) p.openInventory(marketCategoryGuiMenu);
        }
    }



    //function that slowly shifts the prices to base price
    public void tick() {

    }



    //methods to load and save the markets
    public void saveMarketsToFile() {

    }

    public void loadMarketsFromFile() {

    }



    //method to initialise all the gui items
    private static void initialiseGuiButtons() {
        ArrayList<String> backButtonLore = new ArrayList<>();
        backButtonLore.add(format("&7Go back to the previous menu"));
        backButton = createGuiItem("&cBack", backButtonLore, Material.BARRIER);

        ArrayList<String> previousPageButtonLore = new ArrayList<>();
        previousPageButtonLore.add(format("&7Go to the previous page"));
        previousPageButton = createGuiItem("&ePrevious Page", previousPageButtonLore, Material.PAPER);

        ArrayList<String> nextPageButtonLore = new ArrayList<>();
        nextPageButtonLore.add(format("&7Go to the next page"));
        nextPageButton = createGuiItem("&eNext Page", backButtonLore, Material.PAPER);

        ArrayList<String> addItemButtonLore = new ArrayList<>();
        addItemButtonLore.add(format("&7Add a custom item"));
        addItemButton = createGuiItem("&aAdd Item", addItemButtonLore, Material.LIME_SHULKER_BOX);

        ArrayList<String> removeItemButtonLore = new ArrayList<>();
        removeItemButtonLore.add(format("&7Remove a custom item"));
        removeItemButton = createGuiItem("&cRemove Item", removeItemButtonLore, Material.RED_SHULKER_BOX);

        ArrayList<String> metalsCategoryButtonLore = new ArrayList<>();
        metalsCategoryButtonLore.add(format("&7Open the &emetal &7tab"));
        metalsCategoryButton = createGuiItem("&eMetals", metalsCategoryButtonLore, Material.IRON_INGOT);

        ArrayList<String> energyCategoryButtonLore = new ArrayList<>();
        energyCategoryButtonLore.add(format("&7Open the &eenergy &7tab"));
        energyCategoryButton = createGuiItem("&eEnergy", energyCategoryButtonLore, Material.COAL);

        ArrayList<String> gemstonesCategoryButtonLore = new ArrayList<>();
        gemstonesCategoryButtonLore.add(format("&7Open the &egemstones &7tab"));
        gemstonesCategoryButton = createGuiItem("&eGemstones", gemstonesCategoryButtonLore, Material.EMERALD);

        darkDivider = createGuiItem(" ", new ArrayList<>(), Material.BLACK_STAINED_GLASS_PANE);
        lightDivider = createGuiItem(" ", new ArrayList<>(), Material.GRAY_STAINED_GLASS_PANE);
    }



    //method to make the main menu for all markets
    private static void initialiseMarketCategoryGuiMenu() {
        marketCategoryGuiMenu = Bukkit.createInventory(null, 27, format("&eCategory Selector"));

        marketCategoryGuiMenu.setItem(11, metalsCategoryButton);
        marketCategoryGuiMenu.setItem(13, energyCategoryButton);
        marketCategoryGuiMenu.setItem(15, gemstonesCategoryButton);
    }



    //constructs the item and returns it with lore
    protected static ItemStack createGuiItem(String name, ArrayList<String> lore, Material itemType) {
        final ItemStack item = new ItemStack(itemType, 1);
        final ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(format(name));
        meta.setLore(lore);

        item.setItemMeta(meta);

        return item;
    }



    //getters and setters
    public static ItemStack getBackButton() {
        return backButton;
    }
    public static void setBackButton(ItemStack backButton) {
        Market.backButton = backButton;
    }
    public static ItemStack getPreviousPageButton() {
        return previousPageButton;
    }
    public static void setPreviousPageButton(ItemStack previousPageButton) {
        Market.previousPageButton = previousPageButton;
    }
    public static ItemStack getNextPageButton() {
        return nextPageButton;
    }
    public static void setNextPageButton(ItemStack nextPageButton) {
        Market.nextPageButton = nextPageButton;
    }
    public static ItemStack getAddItemButton() {
        return addItemButton;
    }
    public static void setAddItemButton(ItemStack addItemButton) {
        Market.addItemButton = addItemButton;
    }
    public static ItemStack getRemoveItemButton() {
        return removeItemButton;
    }
    public static void setRemoveItemButton(ItemStack removeItemButton) {
        Market.removeItemButton = removeItemButton;
    }
    public static ItemStack getDarkDivider() {
        return darkDivider;
    }
    public static void setDarkDivider(ItemStack darkDivider) {
        Market.darkDivider = darkDivider;
    }
    public static ItemStack getLightDivider() {
        return lightDivider;
    }
    public static void setLightDivider(ItemStack lightDivider) {
        Market.lightDivider = lightDivider;
    }
    public static ItemStack getMetalsCategoryButton() {
        return metalsCategoryButton;
    }
    public static void setMetalsCategoryButton(ItemStack metalsCategoryButton) {
        Market.metalsCategoryButton = metalsCategoryButton;
    }
    public static ItemStack getEnergyCategoryButton() {
        return energyCategoryButton;
    }
    public static void setEnergyCategoryButton(ItemStack energyCategoryButton) {
        Market.energyCategoryButton = energyCategoryButton;
    }
    public static ItemStack getGemstonesCategoryButton() {
        return gemstonesCategoryButton;
    }
    public static void setGemstonesCategoryButton(ItemStack gemstonesCategoryButton) {
        Market.gemstonesCategoryButton = gemstonesCategoryButton;
    }
    public static Inventory getMarketCategoryGuiMenu() {
        return marketCategoryGuiMenu;
    }
    public static void setMarketCategoryGuiMenu(Inventory marketCategoryGuiMenu) {
        Market.marketCategoryGuiMenu = marketCategoryGuiMenu;
    }
    public static HashMap<Player, Market> getLatestMarketOpen() {
        return latestMarketOpen;
    }
    public static void setLatestMarketOpen(HashMap<Player, Market> latestMarketOpen) {
        Market.latestMarketOpen = latestMarketOpen;
    }
    public static HashMap<Integer, Market> getMarkets() {
        return markets;
    }
    public static void setMarkets(HashMap<Integer, Market> markets) {
        Market.markets = markets;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Location getLocation() {
        return location;
    }
    public void setLocation(Location location) {
        this.location = location;
    }
    public ArrayList<Trade> getTrades() {
        return trades;
    }
    public void setTrades(ArrayList<Trade> trades) {
        this.trades = trades;
    }
    public Inventory getMetalsGuiMenu() {
        return metalsGuiMenu;
    }
    public void setMetalsGuiMenu(Inventory metalsGuiMenu) {
        this.metalsGuiMenu = metalsGuiMenu;
    }
    public Inventory getEnergyGuiMenu() {
        return energyGuiMenu;
    }
    public void setEnergyGuiMenu(Inventory energyGuiMenu) {
        this.energyGuiMenu = energyGuiMenu;
    }
    public Inventory getGemstonesGuiMenu() {
        return gemstonesGuiMenu;
    }
    public void setGemstonesGuiMenu(Inventory gemstonesGuiMenu) {
        this.gemstonesGuiMenu = gemstonesGuiMenu;
    }
    public int getKey() {
        return key;
    }
    public void setKey(int key) {
        this.key = key;
    }
}
