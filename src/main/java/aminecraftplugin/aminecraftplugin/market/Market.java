package aminecraftplugin.aminecraftplugin.market;

import aminecraftplugin.aminecraftplugin.drill.Resource;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
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

    //market init
    public static void init() {
        initialiseGuiButtons();
        initialiseMarketCategoryGuiMenu();
    }



    //market constructors
    public Market() {

    }

    public Market(String _name, Location _location) {
        name = _name;
        location = _location;
        trades = new ArrayList<>();
        generateMarketMenus();
    }

    public Market(String _name, Location _location, ArrayList<Integer> _tradeItemKeys) {
        name = _name;
        location = _location;
        trades = generateTrades(_tradeItemKeys);
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
        if (e.getClickedBlock() == null) return;
        Location clickLocation = e.getClickedBlock().getLocation();

        for(Market m : markets.values()) {
            if (m.location.equals(clickLocation)) {
                latestMarketOpen.put(player, m);
                openMarket(player);
            }
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
        Inventory inv = Bukkit.createInventory(null, 26, format("&eCategory Selector"));

        inv.setItem(11, metalsCategoryButton);
        inv.setItem(13, energyCategoryButton);
        inv.setItem(15, gemstonesCategoryButton);

        marketCategoryGuiMenu = inv;
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

}
