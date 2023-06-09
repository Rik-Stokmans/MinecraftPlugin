package aminecraftplugin.aminecraftplugin.market;

import aminecraftplugin.aminecraftplugin.drilling.resource.Resource;
import aminecraftplugin.aminecraftplugin.drilling.resource.resourceCategory;
import me.filoghost.holographicdisplays.api.hologram.Hologram;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static aminecraftplugin.aminecraftplugin.Main.*;
import static aminecraftplugin.aminecraftplugin.drilling.resource.Resource.*;
import static aminecraftplugin.aminecraftplugin.utils.ChatUtils.format;
import static aminecraftplugin.aminecraftplugin.utils.Compress.roundAvoid;

public class Market {

    //ui/ux
    public  static ItemStack backButton;
    private static ItemStack previousPageButton;
    private static ItemStack nextPageButton;
    private static ItemStack addItemButton;
    private static ItemStack removeItemButton;
    public  static ItemStack darkDivider;
    public  static ItemStack lightDivider;
    private static ItemStack metalsCategoryButton;
    private static ItemStack energyCategoryButton;
    private static ItemStack gemstonesCategoryButton;
    private static ItemStack otherCategoryButton;
    private static ItemStack sellAllButton;
    private static ItemStack changeOrderAmountButton;
    private static ItemStack resetOrderSizeButton;
    private static ItemStack orderAddHundred;
    private static ItemStack orderAddTen;
    private static ItemStack orderAddOne;
    private static ItemStack orderRemoveHundred;
    private static ItemStack orderRemoveTen;
    private static ItemStack orderRemoveOne;
    private static ItemStack addItemToMenu;
    public  static ItemStack addMiningResource;
    public  static ItemStack removeMiningResource;

    public static Inventory marketCategoryGuiMenu;
    public static Inventory marketOrderSizeMenu;

    public static HashMap<Player, Market> latestMarketOpen = new HashMap<>();
    public static HashMap<Player, Double> playerOrderSize = new HashMap<>();

    //Hashmap loaded from file containing all markets
    public static HashMap<Integer, Market> markets = new HashMap<>();

    public static double marketBuyTax = 1.05;

    //all market attributes
    private String name;
    private Location location;
    private HashMap<Integer, Trade> trades;
    private Inventory metalsGuiMenu;
    private Inventory energyGuiMenu;
    private Inventory gemstonesGuiMenu;
    private Inventory otherItemsGuiMenu;
    private int strength = 1000;
    private int key;
    private Hologram hologram;


    //market init
    public static void init() {
        initialiseGuiButtons();
        initialiseMarketCategoryGuiMenu();
        try {
            loadMarketsFromFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



    //market constructors
    public Market() {

    }

    //when creating a new market
    public Market(String _name, Location _location, int _key, int _strength) {
        name = _name;
        location = _location;
        key = _key;
        strength = _strength;
        trades = new HashMap<>();
        generateMarketMenus();
        generateHologram();
    }

    //when loading from file
    public Market(String _name, Location _location, int _strength, int _key, HashMap<Integer, Trade> _trades) {
        name = _name;
        location = _location;
        key = _key;
        trades = _trades;
        strength = _strength;
        generateMarketMenus();
        generateHologram();
    }

    public void generateHologram() {
        this.hologram = api.createHologram(this.getLocation().clone().add(0.5, 3.1875, 0.5));
        hologram.getLines().appendText(format(name));
    }

    //method to generate the inventory of the market
    private void generateMarketMenus() {
        metalsGuiMenu = Bukkit.createInventory(null, 54, format("&eMetals"));
        energyGuiMenu = Bukkit.createInventory(null, 54, format("&eEnergy"));
        gemstonesGuiMenu = Bukkit.createInventory(null, 54, format("&eGemstones"));
        otherItemsGuiMenu = Bukkit.createInventory(null, 54, format("&eOther"));

        //trade items
        updateTrades();

        //separator
        for(int i = 36; i <= 44; i++) {
            metalsGuiMenu.setItem(i, darkDivider);
            energyGuiMenu.setItem(i, darkDivider);
            gemstonesGuiMenu.setItem(i, darkDivider);
            otherItemsGuiMenu.setItem(i, darkDivider);
        }

        //back button
        metalsGuiMenu.setItem(45, backButton);
        energyGuiMenu.setItem(45, backButton);
        gemstonesGuiMenu.setItem(45, backButton);
        otherItemsGuiMenu.setItem(45, backButton);

        //sell all button
        metalsGuiMenu.setItem(49, sellAllButton);
        energyGuiMenu.setItem(49, sellAllButton);
        gemstonesGuiMenu.setItem(49, sellAllButton);
        otherItemsGuiMenu.setItem(49, sellAllButton);

        //order size editor
        metalsGuiMenu.setItem(53, changeOrderAmountButton);
        energyGuiMenu.setItem(53, changeOrderAmountButton);
        gemstonesGuiMenu.setItem(53, changeOrderAmountButton);
        otherItemsGuiMenu.setItem(53, changeOrderAmountButton);
    }

    private void updateTrades() {
        int metalsSlot = 0;
        int energySlot = 0;
        int gemstonesSlot = 0;
        int otherItemsSlot = 0;
        ArrayList<Integer> tradesToRemove = new ArrayList<>();
        for (Trade t : trades.values()) {
            if (!resources.containsKey(t.itemKey)) tradesToRemove.add(t.itemKey);
            //metals
            else if (getCategoryFromResourceKey(t.getItemKey()).equals(resourceCategory.METALS)) {
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
            //other items
            else if (getCategoryFromResourceKey(t.getItemKey()).equals(resourceCategory.OTHER)) {
                ItemStack tradeItem = t.generateTradeItem();
                otherItemsGuiMenu.setItem(otherItemsSlot, tradeItem);
                otherItemsSlot++;
            }
        }
        if (!tradesToRemove.isEmpty()) for (int tradeToRemove : tradesToRemove) {
            trades.remove(tradeToRemove);
        }
    }



    public void executeMarketEvent(MarketEvent marketEvent) {
        double stockChange = marketEvent.stockChange;


    }

    //gui methods
    private static void openMarket(Player p) {
        p.openInventory(marketCategoryGuiMenu);
        if (!playerOrderSize.containsKey(p)) playerOrderSize.put(p, 1.0);
    }

    public static void rightClickMarketEvent(NPCRightClickEvent e){
        Player p = e.getClicker();
        Location location = e.getNPC().getStoredLocation();
        for (Market m : markets.values()) {
            if (m.location.distance(location) < 1.5) {
                latestMarketOpen.put(p, m);
                openMarket(p);
            }
        }
    }

    public static void marketInventoryClickEvent(InventoryClickEvent e){
        ItemStack clickedItem = e.getCurrentItem();
        if (clickedItem == null) return;
        Player p = (Player) e.getWhoClicked();
        String invName = e.getView().getTitle();

        //opened the change order menu
        if (clickedItem.isSimilar(changeOrderAmountButton)) {
            openOrderSizeEditorGui(p);
            e.setCancelled(true);
            return;
        }

        boolean buySellOrder = false;
        if (invName.equals(format("&eCategory Selector"))) {

            if (clickedItem.isSimilar(addItemToMenu)) {
                if (p.isOp()) {
                    openAddItemMenu(p);
                } else {
                    p.sendMessage(format("&cOnly operators can edit markets"));
                }
            }

            e.setCancelled(true);
            if (!e.isLeftClick()) return;
            if (e.getCurrentItem().isSimilar(metalsCategoryButton)) {
                p.openInventory(latestMarketOpen.get(p).metalsGuiMenu);
                latestMarketOpen.get(p).updateTrades();
            }
            else if (e.getCurrentItem().isSimilar(energyCategoryButton)) {
                p.openInventory(latestMarketOpen.get(p).energyGuiMenu);
                latestMarketOpen.get(p).updateTrades();
            }
            else if (e.getCurrentItem().isSimilar(gemstonesCategoryButton)) {
                p.openInventory(latestMarketOpen.get(p).gemstonesGuiMenu);
                latestMarketOpen.get(p).updateTrades();
            }
            else if (e.getCurrentItem().isSimilar(otherCategoryButton)) {
                p.openInventory(latestMarketOpen.get(p).otherItemsGuiMenu);
                latestMarketOpen.get(p).updateTrades();
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
        else if (invName.equals(format("&eOther"))) {
            e.setCancelled(true);
            if (e.getCurrentItem().isSimilar(backButton)) p.openInventory(marketCategoryGuiMenu);

            if (e.getSlot() <= 35) buySellOrder = true;
        }
        else if (invName.equals(format("&eChange Order Size"))) {
            e.setCancelled(true);
            double orderSize = playerOrderSize.get(p);
            if (clickedItem.isSimilar(backButton)) {
                p.openInventory(marketCategoryGuiMenu);
                return;
            }
            if (clickedItem.isSimilar(orderAddHundred) && orderSize <= 999.9) orderSize += 0.1;
            else if (clickedItem.isSimilar(orderAddTen) && orderSize <= 990) orderSize += 10.0;
            else if (clickedItem.isSimilar(orderAddOne) && orderSize <= 999) orderSize += 1.0;
            else if (clickedItem.isSimilar(orderRemoveOne) && orderSize > 1) orderSize -= 1.0;
            else if (clickedItem.isSimilar(orderRemoveTen) && orderSize > 10) orderSize -= 10.0;
            else if (clickedItem.isSimilar(orderRemoveHundred) && orderSize > 0.1) orderSize -= 0.1;
            else if (clickedItem.isSimilar(resetOrderSizeButton)) orderSize = 0.1;
            orderSize = roundAvoid(orderSize, 1);
            playerOrderSize.put(p, orderSize);
            p.getOpenInventory().setItem(22, generateOrderInfoItem(orderSize));
        }
        else if (invName.equals(format("&eAdd item"))) {
            e.setCancelled(true);
            if (getKeyFromItemstack(clickedItem) == -1) return;

            latestMarketOpen.get(p).trades.put(getKeyFromItemstack(clickedItem), new Trade(getKeyFromItemstack(clickedItem), latestMarketOpen.get(p).getStrength()));
            p.openInventory(marketCategoryGuiMenu);
            latestMarketOpen.get(p).updateTrades();
        }
        //when a player wants to buy or sell a item
        if (buySellOrder) {

            if (!latestMarketOpen.containsKey(p)) return;
            Market market = latestMarketOpen.get(p);

            if (e.getClick().isLeftClick()) {
                int key = getKeyFromItemstack(clickedItem);
                double orderSize = playerOrderSize.get(p);

                if (market.getTrades().containsKey(key)) market.getTrades().get(key).executeBuyOrder(orderSize, p, e);
            }


            //sell
            else if (e.getClick().isRightClick()) {

                int key = getKeyFromItemstack(clickedItem);
                double orderSize = playerOrderSize.get(p);

                if (market.getTrades().containsKey(key)) market.getTrades().get(key).executeSellOrder(orderSize, p, e);

            }
        }
    }


    //methods to load and save the markets
    public static void saveMarketsToFile() {
        YamlConfiguration marketsFile = new YamlConfiguration();

        for (Map.Entry<Integer, Market> market : markets.entrySet()) {
            marketsFile.set("markets." + market.getKey() + ".name", market.getValue().getName());
            marketsFile.set("markets." + market.getKey() + ".strength", market.getValue().getStrength());
            marketsFile.set("markets." + market.getKey() + ".location", market.getValue().getLocation());

            for (Map.Entry<Integer, Trade> trade : market.getValue().getTrades().entrySet()) {
                marketsFile.set("markets." + market.getKey() + ".trades." + trade.getKey() + ".stock", trade.getValue().getStock());
                marketsFile.set("markets." + market.getKey() + ".trades." + trade.getKey() + ".strength", trade.getValue().getStrength());
            }

        }
        try { saveFile(marketsFile,"markets.yml"); } catch (IOException e) {throw new RuntimeException(e);}
    }

    public static void loadMarketsFromFile() throws IOException {
        YamlConfiguration marketsFile = loadFile("markets.yml");

        if (!marketsFile.contains("markets")) return;

        marketsFile.getConfigurationSection("markets").getKeys(false).forEach(marketKey -> {
            String name = marketsFile.getString("markets." + marketKey + ".name");
            Location location = marketsFile.getLocation("markets." + marketKey + ".location");
            int strength = marketsFile.getInt("markets." + marketKey + ".strength");

            HashMap<Integer, Trade> trades = new HashMap<>();
            if (marketsFile.getConfigurationSection("markets." + marketKey + ".trades") != null) {
                marketsFile.getConfigurationSection("markets." + marketKey + ".trades").getKeys(false).forEach(tradeKey -> {
                    double stock = marketsFile.getDouble("markets." + marketKey + ".trades." + tradeKey + ".stock");
                    int tradeStrength = marketsFile.getInt("markets." + marketKey + ".trades." + tradeKey + ".strength");
                    trades.put(Integer.parseInt(tradeKey), new Trade(Integer.parseInt(tradeKey), tradeStrength, stock));
                });
            }
            //adds the market to the global var.
            markets.put(Integer.parseInt(marketKey), new Market(name, location, strength, Integer.parseInt(marketKey), trades));
        });
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

        ArrayList<String> otherCategoryButtonLore = new ArrayList<>();
        otherCategoryButtonLore.add(format("&7Open the &eother &7items tab"));
        otherCategoryButton = createGuiItem("&eOther", otherCategoryButtonLore, Material.SUNFLOWER);

        ArrayList<String> sellAllButtonLore = new ArrayList<>();
        sellAllButtonLore.add(format("&7Sell all items of this category"));
        sellAllButton = createGuiItem("&6Sell all", sellAllButtonLore, Material.GOLD_INGOT);

        ArrayList<String> changeOrderSizeLore = new ArrayList<>();
        changeOrderSizeLore.add(format("&7Change your &eorder &7size"));
        changeOrderAmountButton = createGuiItem("&eOrder Size", changeOrderSizeLore, Material.COMPARATOR);

        ArrayList<String> resetOrderSizeLore = new ArrayList<>();
        resetOrderSizeLore.add(format("&7Reset your &eorder &7size"));
        resetOrderSizeButton = createGuiItem("&eReset order size", resetOrderSizeLore, Material.BLACK_CONCRETE);

        ArrayList<String> orderAddHunderedLore = new ArrayList<>();
        orderAddHunderedLore.add(format("&7Add &e100g &7to your order size"));
        orderAddHundred = createGuiItem("&eAdd 100g", orderAddHunderedLore, Material.GREEN_TERRACOTTA);

        ArrayList<String> orderAddTenLore = new ArrayList<>();
        orderAddTenLore.add(format("&7Add &e10Kg &7to your order size"));
        orderAddTen = createGuiItem("&eAdd 10Kg", orderAddTenLore, Material.GREEN_GLAZED_TERRACOTTA);

        ArrayList<String> orderAddOneLore = new ArrayList<>();
        orderAddOneLore.add(format("&7Add &e1Kg &7to your order size"));
        orderAddOne = createGuiItem("&eAdd 1Kg", orderAddOneLore, Material.GREEN_CONCRETE);

        ArrayList<String> orderRemoveHunderedLore = new ArrayList<>();
        orderRemoveHunderedLore.add(format("&7Remove &e100g &7from your order size"));
        orderRemoveHundred = createGuiItem("&eRemove 100g", orderRemoveHunderedLore, Material.RED_TERRACOTTA);

        ArrayList<String> orderRemoveTenLore = new ArrayList<>();
        orderRemoveTenLore.add(format("&7Remove &e10Kg &7from your order size"));
        orderRemoveTen = createGuiItem("&eRemove 10Kg", orderRemoveTenLore, Material.RED_GLAZED_TERRACOTTA);

        ArrayList<String> orderRemoveOneLore = new ArrayList<>();
        orderRemoveOneLore.add(format("&7Remove &e1Kg &7from your order size"));
        orderRemoveOne = createGuiItem("&eRemove 1Kg", orderRemoveOneLore, Material.RED_CONCRETE);

        ArrayList<String> addItemToMenuLore = new ArrayList<>();
        addItemToMenuLore.add(format("&7Add a &eitem &7to this market"));
        addItemToMenu = createGuiItem("&eAdd item", addItemToMenuLore, Material.GREEN_GLAZED_TERRACOTTA);

        ArrayList<String> addMiningResourceLore = new ArrayList<>();
        addMiningResourceLore.add(format("&7Add a &eminable resource"));
        addMiningResource = createGuiItem("&eAdd a mining resource", addItemToMenuLore, Material.GREEN_GLAZED_TERRACOTTA);

        ArrayList<String> removeMiningResourceLore = new ArrayList<>();
        removeMiningResourceLore.add(format("&7Remove a &eminable resource"));
        removeMiningResource = createGuiItem("&eRemove a mining resource", addItemToMenuLore, Material.RED_GLAZED_TERRACOTTA);

        darkDivider = createGuiItem(" ", new ArrayList<>(), Material.BLACK_STAINED_GLASS_PANE);
        lightDivider = createGuiItem(" ", new ArrayList<>(), Material.GRAY_STAINED_GLASS_PANE);
    }



    //method to make the order size editor
    private static void openOrderSizeEditorGui(Player p) {
        double orderSize = playerOrderSize.get(p);
        marketOrderSizeMenu = Bukkit.createInventory(null, 27, format("&eChange Order Size"));

        marketOrderSizeMenu.setItem(10, orderAddTen);
        marketOrderSizeMenu.setItem(11, orderAddOne);
        marketOrderSizeMenu.setItem(12, orderAddHundred);
        marketOrderSizeMenu.setItem(13, resetOrderSizeButton);
        marketOrderSizeMenu.setItem(14, orderRemoveHundred);
        marketOrderSizeMenu.setItem(15, orderRemoveOne);
        marketOrderSizeMenu.setItem(16, orderRemoveTen);

        marketOrderSizeMenu.setItem(18, backButton);
        marketOrderSizeMenu.setItem(22, generateOrderInfoItem(orderSize));

        p.openInventory(marketOrderSizeMenu);
    }

    private static ItemStack generateOrderInfoItem(double orderSize) {
        ArrayList<String> orderInfoItemLore = new ArrayList<>();
        orderInfoItemLore.add(format("&b" + orderSize + "&7Kg"));
        return createGuiItem("&eOrder Size", orderInfoItemLore, Material.BOOK);
    }

    private static void openAddItemMenu(Player p) {
        Inventory addItemInventory = Bukkit.createInventory(null, 54, format("&eAdd item"));

        for (Resource r : resources.values()) {
            boolean alreadyInMarket = false;
            for (Trade trade : latestMarketOpen.get(p).trades.values()) {
                if (trade.getItemKey() == r.getKey()) alreadyInMarket = true;
            }
            if (!alreadyInMarket) {
                ItemStack item = r.getItemStack().clone();
                ItemMeta imeta = item.getItemMeta();
                List<String> lore = new ArrayList<>();
                lore.add("");
                resourceCategory category = getCategoryFromResourceKey(r.getKey());
                if (category.equals(resourceCategory.METALS)) lore.add(format("&9&lMetal"));
                else if (category.equals(resourceCategory.ENERGY)) lore.add(format("&9&lEnergy"));
                else if (category.equals(resourceCategory.GEMSTONES)) lore.add(format("&9&lGemstones"));
                else if (category.equals(resourceCategory.OTHER)) lore.add(format("&9&lOther"));
                imeta.setLore(lore);
                item.setItemMeta(imeta);
                addItemInventory.addItem(item);
            }
        }

        p.openInventory(addItemInventory);
    }



    public static void closePlayerInventories() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            String invName = p.getOpenInventory().getTitle();

            if (invName.equals(format("&eCategory Selector")) ||
                    invName.equals(format("&eMetals")) ||
                    invName.equals(format("&eEnergy")) ||
                    invName.equals(format("&eGemstones")) ||
                    invName.equals(format("&eOther")) ||
                    invName.equals(format("&eChange Order Size")) ||
                    invName.equals(format("&eAdd item")))
            {
                p.closeInventory();
            }
        }
    }



    //method to make the main menu for all markets
    private static void initialiseMarketCategoryGuiMenu() {
        marketCategoryGuiMenu = Bukkit.createInventory(null, 27, format("&eCategory Selector"));

        marketCategoryGuiMenu.setItem(10, metalsCategoryButton);
        marketCategoryGuiMenu.setItem(12, energyCategoryButton);
        marketCategoryGuiMenu.setItem(14, gemstonesCategoryButton);
        marketCategoryGuiMenu.setItem(16, otherCategoryButton);
        marketCategoryGuiMenu.setItem(22, addItemToMenu);
    }



    public static void tickTrades() {
        for (Market m : markets.values()) {
            for (Trade t : m.trades.values()) {
                t.tick(true);
            }
            m.updateTrades();
        }
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
    public HashMap<Integer, Trade> getTrades() {
        return trades;
    }
    public void setTrades(HashMap<Integer, Trade> trades) {
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
    public int getStrength() {
        return strength;
    }
    public void setStrength(int strength) {
        this.strength = strength;
    }
}
