package aminecraftplugin.aminecraftplugin.market;

import aminecraftplugin.aminecraftplugin.drill.Backpack;
import aminecraftplugin.aminecraftplugin.drill.Resource;
import aminecraftplugin.aminecraftplugin.drill.resourceCategory;
import aminecraftplugin.aminecraftplugin.utils.Function;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import static aminecraftplugin.aminecraftplugin.drill.Backpack.backpacks;
import static aminecraftplugin.aminecraftplugin.drill.Resource.getCategoryFromResourceKey;
import static aminecraftplugin.aminecraftplugin.drill.Resource.getKeyFromItemstack;
import static aminecraftplugin.aminecraftplugin.utils.ChatUtils.format;
import static aminecraftplugin.aminecraftplugin.utils.Integral.integral;

public class Market implements Listener {

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
    public static ItemStack sellAllButton;
    public static ItemStack changeOrderAmountButton;
    public static ItemStack resetOrderSizeButton;
    public static ItemStack orderAddHundered;
    public static ItemStack orderAddTen;
    public static ItemStack orderAddOne;
    public static ItemStack orderRemoveHundered;
    public static ItemStack orderRemoveTen;
    public static ItemStack orderRemoveOne;

    public static Inventory marketCategoryGuiMenu;
    public static Inventory marketOrderSizeMenu;

    public static HashMap<Player, Market> latestMarketOpen = new HashMap<>();
    public static HashMap<Player, Integer> playerOrderSize = new HashMap<>();

    //Hashmap loaded from file containing all markets
    public static HashMap<Integer, Market> markets = new HashMap<>();

    //all market attributes
    String name;
    Location location;
    ArrayList<Trade> trades;
    Inventory metalsGuiMenu;
    Inventory energyGuiMenu;
    Inventory gemstonesGuiMenu;
    double stock = -5;
    int strength = 1000;
    int key;


    //market init
    public static void init() {
        initialiseGuiButtons();
        initialiseMarketCategoryGuiMenu();
    }



    //market constructors
    public Market() {

    }

    public Market(String _name, Location _location, int _key, int _strength) {
        name = _name;
        location = _location;
        trades = new ArrayList<>();
        key = _key;
        strength = _strength;
        //temp
        trades.add(new Trade(1));
        generateMarketMenus();
    }

    //when loading from file
    public Market(String _name, Location _location, ArrayList<Integer> _tradeItemKeys, int _key) {
        name = _name;
        location = _location;
        trades = generateTrades(_tradeItemKeys);
        key = _key;
        generateMarketMenus();
    }



    //method to generate all the trade items from a list of keys
    private ArrayList<Trade> generateTrades(ArrayList<Integer> tradeItemKeys) {
        ArrayList<Trade> trades = new ArrayList<>();
        for(int key : tradeItemKeys) {
            if (Resource.resources.containsKey(key)) {
                Trade trade = new Trade(key);
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

        //trade items
        updateTrades();

        //separator
        for(int i = 36; i <= 44; i++) {
            metalsGuiMenu.setItem(i, darkDivider);
            energyGuiMenu.setItem(i, darkDivider);
            gemstonesGuiMenu.setItem(i, darkDivider);
        }

        //back button
        metalsGuiMenu.setItem(45, backButton);
        energyGuiMenu.setItem(45, backButton);
        gemstonesGuiMenu.setItem(45, backButton);

        //sell all button
        metalsGuiMenu.setItem(49, sellAllButton);
        energyGuiMenu.setItem(49, sellAllButton);
        gemstonesGuiMenu.setItem(49, sellAllButton);

        //order size editor
        metalsGuiMenu.setItem(53, changeOrderAmountButton);
        energyGuiMenu.setItem(53, changeOrderAmountButton);
        gemstonesGuiMenu.setItem(53, changeOrderAmountButton);
    }

    private void updateTrades() {
        int metalsSlot = 0;
        int energySlot = 0;
        int gemstonesSlot = 0;
        for (Trade t : trades) {
            //metals
            if (getCategoryFromResourceKey(t.getItemKey()).equals(resourceCategory.METALS)) {
                ItemStack tradeItem = t.generateTradeItem();
                metalsGuiMenu.setItem(metalsSlot, tradeItem);
                metalsSlot++;
            }
            //energy
            else if (getCategoryFromResourceKey(t.getItemKey()).equals(resourceCategory.ENERGY)) {
                ItemStack tradeItem = t.generateTradeItem();
                energyGuiMenu.setItem(energySlot, tradeItem);
                energySlot++;
            }
            //gemstones
            else if (getCategoryFromResourceKey(t.getItemKey()).equals(resourceCategory.GEMSTONES)) {
                ItemStack tradeItem = t.generateTradeItem();
                gemstonesGuiMenu.setItem(gemstonesSlot, tradeItem);
                gemstonesSlot++;
            }
        }
    }




    //gui methods
    private void openMarket(Player p) {
        p.openInventory(marketCategoryGuiMenu);
        if (!playerOrderSize.containsKey(p)) playerOrderSize.put(p, 1);
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
        ItemStack clickedItem = e.getCurrentItem();
        if (clickedItem == null) return;

        Player p = (Player) e.getWhoClicked();
        String invName = e.getView().getTitle();
        //makes items protected
        if (clickedItem.isSimilar(changeOrderAmountButton)) {
            openOrderSizeEditorGui(p);
            e.setCancelled(true);
            return;
        }
        boolean buySellOrder = false;
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
            if (clickedItem.isSimilar(backButton)) p.openInventory(marketCategoryGuiMenu);

            if (e.getSlot() <= 35) buySellOrder = true;
        }
        else if (invName.equals(format("&eEnergy"))) {
            e.setCancelled(true);
            if (e.getCurrentItem().isSimilar(backButton)) p.openInventory(marketCategoryGuiMenu);

            if (e.getSlot() <= 35) buySellOrder = true;
        }
        else if (invName.equals(format("&eGemstones"))) {
            e.setCancelled(true);
            if (e.getCurrentItem().isSimilar(backButton)) p.openInventory(marketCategoryGuiMenu);

            if (e.getSlot() <= 35) buySellOrder = true;
        }
        else if (invName.equals(format("&eChange Order Size"))) {
            e.setCancelled(true);
            int orderSize = playerOrderSize.get(p);
            if (clickedItem.isSimilar(backButton)) {
                p.openInventory(marketCategoryGuiMenu);
                return;
            }
            if (clickedItem.isSimilar(orderAddHundered) && orderSize <= 9900) orderSize += 100;
            else if (clickedItem.isSimilar(orderAddTen) && orderSize <= 9990) orderSize += 10;
            else if (clickedItem.isSimilar(orderAddOne) && orderSize <= 9999) orderSize += 1;
            else if (clickedItem.isSimilar(orderRemoveOne) && orderSize > 1) orderSize -= 1;
            else if (clickedItem.isSimilar(orderRemoveTen) && orderSize > 10) orderSize -= 10;
            else if (clickedItem.isSimilar(orderRemoveHundered) && orderSize > 100) orderSize -= 100;
            playerOrderSize.put(p, orderSize);
            p.getOpenInventory().setItem(22, generateOrderInfoItem(orderSize));
        }

        //todo
        if (buySellOrder) {
            int key = getKeyFromItemstack(clickedItem);
            int orderSize = playerOrderSize.get(p);
            if (!backpacks.containsKey(p)) {
                backpacks.put(p, new Backpack());
            }
            double itemAmountInBackpack = backpacks.get(p).getItemAmountInBackpack(key);
            //buy
            if (e.getClick().isLeftClick()) {
                double amountBought = orderSize;
                double price = 0;

                double x1 = stock - amountBought; // -1
                double x2 = stock; // 0

                p.sendMessage(strength + ", " + stock + ", " + x1 + ", " + x2);

                if ((x1) >= 0) {
                    if (x1 == 0) x1 += Double.MIN_VALUE;
                    price = (strength * stock * Math.log(Math.abs(10 * x2 + strength * stock))) - (strength * stock * Math.log(10 * x1 + strength * stock));
                }
                else if (x2 <= 0) {
                    if (x2 == 0) x2 -= Double.MIN_VALUE;
                    price = (strength * (2 * x2 + stock * Math.log(Math.abs(x2 - stock)))) - (strength * (2 * x1 + stock * Math.log(Math.abs(x1 - stock))));
                }
                else if (x1 < 0 && x2 > 0) {
                    price = ((strength * stock * Math.log(Math.abs(10 * x2 + strength * stock))) - (strength * stock * Math.log(Math.abs(10 * Double.MIN_VALUE + strength * stock))))
                            + (strength * (2 * Double.MIN_VALUE + stock * Math.log(Math.abs(Double.MIN_VALUE - stock))) - strength * (2 * x1 + stock * Math.log(Math.abs(x1 - stock))));
                }

                p.sendMessage(String.valueOf("price: " + price));
            }
            //sell
            else if (e.getClick().isRightClick()) {


            }
        }
    }



    //function that slowly shifts the prices to base price
    public void tick() {
        Random rand = new Random();
        for (Trade trade : trades) {
            if (rand.nextDouble() > 0.1) trade.tick(true);
        }
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
        //new
        ArrayList<String> sellAllButtonLore = new ArrayList<>();
        sellAllButtonLore.add(format("&7Sell all items of this category"));
        sellAllButton = createGuiItem("&6Sellall", sellAllButtonLore, Material.GOLD_INGOT);

        ArrayList<String> changeOrderSizeLore = new ArrayList<>();
        changeOrderSizeLore.add(format("&7Change your &eorder &7size"));
        changeOrderAmountButton = createGuiItem("&eOrder Size", changeOrderSizeLore, Material.COMPARATOR);

        ArrayList<String> resetOrderSizeLore = new ArrayList<>();
        resetOrderSizeLore.add(format("&7Reset your &eorder &7size"));
        resetOrderSizeButton = createGuiItem("&eReset order size", resetOrderSizeLore, Material.BLACK_CONCRETE);

        ArrayList<String> orderAddHunderedLore = new ArrayList<>();
        orderAddHunderedLore.add(format("&7Add &e100Kg &7to your order size"));
        orderAddHundered = createGuiItem("&eAdd 100Kg", orderAddHunderedLore, Material.GREEN_GLAZED_TERRACOTTA);

        ArrayList<String> orderAddTenLore = new ArrayList<>();
        orderAddTenLore.add(format("&7Add &e10Kg &7to your order size"));
        orderAddTen = createGuiItem("&eAdd 10Kg", orderAddTenLore, Material.GREEN_CONCRETE);

        ArrayList<String> orderAddOneLore = new ArrayList<>();
        orderAddOneLore.add(format("&7Add &e100Kg &7to your order size"));
        orderAddOne = createGuiItem("&eAdd 1Kg", orderAddOneLore, Material.GREEN_TERRACOTTA);

        ArrayList<String> orderRemoveHunderedLore = new ArrayList<>();
        orderRemoveHunderedLore.add(format("&7Remove &e100Kg &7from your order size"));
        orderRemoveHundered = createGuiItem("&eRemove 100Kg", orderRemoveHunderedLore, Material.RED_GLAZED_TERRACOTTA);

        ArrayList<String> orderRemoveTenLore = new ArrayList<>();
        orderRemoveTenLore.add(format("&7Remove &e10Kg &7from your order size"));
        orderRemoveTen = createGuiItem("&eRemove 10Kg", orderRemoveTenLore, Material.RED_CONCRETE);

        ArrayList<String> orderRemoveOneLore = new ArrayList<>();
        orderRemoveOneLore.add(format("&7Remove &e1Kg &7from your order size"));
        orderRemoveOne = createGuiItem("&eRemove 1Kg", orderRemoveOneLore, Material.RED_TERRACOTTA);

        darkDivider = createGuiItem(" ", new ArrayList<>(), Material.BLACK_STAINED_GLASS_PANE);
        lightDivider = createGuiItem(" ", new ArrayList<>(), Material.GRAY_STAINED_GLASS_PANE);
    }



    //method to make the order size editor
    private void openOrderSizeEditorGui(Player p) {
        int orderSize = playerOrderSize.get(p);
        marketOrderSizeMenu = Bukkit.createInventory(null, 27, format("&eChange Order Size"));

        marketOrderSizeMenu.setItem(10, orderAddHundered);
        marketOrderSizeMenu.setItem(11, orderAddTen);
        marketOrderSizeMenu.setItem(12, orderAddOne);
        marketOrderSizeMenu.setItem(13, resetOrderSizeButton);
        marketOrderSizeMenu.setItem(14, orderRemoveOne);
        marketOrderSizeMenu.setItem(15, orderRemoveTen);
        marketOrderSizeMenu.setItem(16, orderRemoveHundered);

        marketOrderSizeMenu.setItem(18, backButton);
        marketOrderSizeMenu.setItem(22, generateOrderInfoItem(orderSize));

        p.openInventory(marketOrderSizeMenu);
    }
    private ItemStack generateOrderInfoItem(int orderSize) {
        ArrayList<String> orderInfoItemLore = new ArrayList<>();
        orderInfoItemLore.add(format("&b" + orderSize + "&7Kg"));
        return createGuiItem("&eOrder Size", orderInfoItemLore, Material.BOOK);
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
