package aminecraftplugin.aminecraftplugin.market;

import aminecraftplugin.aminecraftplugin.drill.loot.Resource;
import aminecraftplugin.aminecraftplugin.drill.loot.resourceCategory;
import aminecraftplugin.aminecraftplugin.player.PlayerProfile;
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
import java.util.List;
import java.util.Random;

import static aminecraftplugin.aminecraftplugin.drill.loot.Resource.*;
import static aminecraftplugin.aminecraftplugin.player.PlayerProfile.getPlayerProfile;
import static aminecraftplugin.aminecraftplugin.player.PlayerProfile.playerProfiles;
import static aminecraftplugin.aminecraftplugin.utils.ChatUtils.format;

public class Market implements Listener {

    //ui/ux
    private static ItemStack backButton;
    private static ItemStack previousPageButton;
    private static ItemStack nextPageButton;
    private static ItemStack addItemButton;
    private static ItemStack removeItemButton;
    private static ItemStack darkDivider;
    private static ItemStack lightDivider;
    private static ItemStack metalsCategoryButton;
    private static ItemStack energyCategoryButton;
    private static ItemStack gemstonesCategoryButton;
    private static ItemStack otherCategoryButton;
    private static ItemStack sellAllButton;
    private static ItemStack changeOrderAmountButton;
    private static ItemStack resetOrderSizeButton;
    private static ItemStack orderAddHundered;
    private static ItemStack orderAddTen;
    private static ItemStack orderAddOne;
    private static ItemStack orderRemoveHundered;
    private static ItemStack orderRemoveTen;
    private static ItemStack orderRemoveOne;
    private static ItemStack addItemToMenu;

    public static Inventory marketCategoryGuiMenu;
    public static Inventory marketOrderSizeMenu;

    public static HashMap<Player, Market> latestMarketOpen = new HashMap<>();
    public static HashMap<Player, Integer> playerOrderSize = new HashMap<>();

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


    //market init
    public static void init() {
        initialiseGuiButtons();
        initialiseMarketCategoryGuiMenu();
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
    }

    //when loading from file
    public Market(String _name, Location _location, ArrayList<Integer> _tradeItemKeys, int _key) {
        name = _name;
        location = _location;
        //trades = generateTrades(_tradeItemKeys);
        key = _key;
        generateMarketMenus();
    }



    //method to generate all the trade items from a list of keys
    private ArrayList<Trade> generateTrades(ArrayList<Integer> tradeItemKeys) {
        ArrayList<Trade> trades = new ArrayList<>();
        for(int key : tradeItemKeys) {
            if (Resource.resources.containsKey(key)) {
                Trade trade = new Trade(key, strength);
                trades.add(trade);
            }
        }
        return trades;
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

        boolean buySellOrder = false;
        if (invName.equals(format("&eCategory Selector"))) {

            if (clickedItem.isSimilar(addItemToMenu)) {
                openAddItemMenu(p);
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

            //opened the change order menu
            if (clickedItem.isSimilar(changeOrderAmountButton)) {
                openOrderSizeEditorGui(p);
                e.setCancelled(true);
                return;
            }
        }
        else if (invName.equals(format("&eEnergy"))) {
            e.setCancelled(true);
            if (e.getCurrentItem().isSimilar(backButton)) p.openInventory(marketCategoryGuiMenu);

            if (e.getSlot() <= 35) buySellOrder = true;

            //opened the change order menu
            if (clickedItem.isSimilar(changeOrderAmountButton)) {
                openOrderSizeEditorGui(p);
                e.setCancelled(true);
                return;
            }
        }
        else if (invName.equals(format("&eGemstones"))) {
            e.setCancelled(true);
            if (e.getCurrentItem().isSimilar(backButton)) p.openInventory(marketCategoryGuiMenu);

            if (e.getSlot() <= 35) buySellOrder = true;

            //opened the change order menu
            if (clickedItem.isSimilar(changeOrderAmountButton)) {
                openOrderSizeEditorGui(p);
                e.setCancelled(true);
                return;
            }
        }
        else if (invName.equals(format("&eOther"))) {
            e.setCancelled(true);
            if (e.getCurrentItem().isSimilar(backButton)) p.openInventory(marketCategoryGuiMenu);

            if (e.getSlot() <= 35) buySellOrder = true;

            //opened the change order menu
            if (clickedItem.isSimilar(changeOrderAmountButton)) {
                openOrderSizeEditorGui(p);
                e.setCancelled(true);
                return;
            }
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
        else if (invName.equals(format("&eAdd item"))) {
            e.setCancelled(true);
            if (p.isOp()) {
                if (getKeyFromItemstack(clickedItem) == -1) return;
                latestMarketOpen.get(p).trades.put(getKeyFromItemstack(clickedItem), new Trade(getKeyFromItemstack(clickedItem), strength));
                p.openInventory(marketCategoryGuiMenu);
                latestMarketOpen.get(p).updateTrades();
            } else {
                p.sendMessage(format("&cOnly operators can edit markets"));
            }

        }

        //todo
        if (buySellOrder) {
            Market market = latestMarketOpen.get(p);
            int key = getKeyFromItemstack(clickedItem);
            int orderSize = playerOrderSize.get(p);
            double itemAmountInBackpack = getPlayerProfile(p).getBackPack().getItemAmountInBackpack(key);
            PlayerProfile playerProfile = playerProfiles.get(p.getUniqueId());
            double playerMoney = playerProfile.getMoney();
            double backpackEmptySpace = playerProfile.getBackPack().getEmptySpace();
            //buy
            if (e.getClick().isLeftClick()) {
                double stock = market.trades.get(key).getStock();
                double worth = market.trades.get(key).getBaseValue();

                double amountBought = orderSize;
                if (backpackEmptySpace < amountBought) amountBought = backpackEmptySpace;
                if (amountBought == 0.0) return;
                double price = 0;

                //calculates the price of the oder size
                double x1 = stock - amountBought;
                double x2 = stock;

                if (x1 >= 0) {
                    if (x1 == 0) x1 += Double.MIN_VALUE;
                    price = (worth * strength * Math.log(Math.abs(worth * x2 + worth * strength))) - (worth * strength * Math.log(worth * x1 + worth * strength));
                }
                else if (x2 <= 0) {
                    if (x2 == 0) x2 -= Double.MIN_VALUE;
                    price = (worth * (2 * x2 + strength * Math.log(Math.abs(x2 - strength)))) - (worth * (2 * x1 + strength * Math.log(Math.abs(x1 - strength))));
                }
                else if (x1 < 0 && x2 > 0) {
                    price = ((worth * strength * Math.log(Math.abs(worth * x2 + worth * strength))) - (worth * strength * Math.log(Math.abs(worth * strength))))
                            + (worth * (strength * Math.log(Math.abs(strength))) - (worth * (2 * x1 + strength * Math.log(Math.abs(x1 - strength)))));
                }

                price = price * marketBuyTax;
                if (!playerProfiles.containsKey(p.getUniqueId())) return;
                if (playerMoney < price) return;

                playerProfile.addMoney(price * -1);
                playerProfile.getBackPack().addResource(key, amountBought);
                stock -= amountBought;
                market.trades.get(key).setStock(stock);
                market.trades.get(key).tick(false);
                e.setCurrentItem(market.trades.get(key).generateTradeItem());

            }
            //sell
            else if (e.getClick().isRightClick()) {
                double stock = market.trades.get(key).getStock();
                double worth = market.trades.get(key).getBaseValue();

                double amountSold = orderSize;
                if (orderSize > itemAmountInBackpack) amountSold = itemAmountInBackpack;
                if (orderSize == 0.0) return;
                double value = 0;


                //calculates the value of the sold amount of items
                double x1 = stock;
                double x2 = stock + amountSold;

                if (x1 >= 0) {
                    if (x1 == 0) x1 += Double.MIN_VALUE;
                    value = (worth * strength * Math.log(Math.abs(worth * x2 + worth * strength))) - (worth * strength * Math.log(worth * x1 + worth * strength));
                }
                else if (x2 <= 0) {
                    if (x2 == 0) x2 -= Double.MIN_VALUE;
                    value = (worth * (2 * x2 + strength * Math.log(Math.abs(x2 - strength)))) - (worth * (2 * x1 + strength * Math.log(Math.abs(x1 - strength))));
                }
                else if (x1 < 0 && x2 > 0) {
                    value = ((worth * strength * Math.log(Math.abs(worth * x2 + worth * strength))) - (worth * strength * Math.log(Math.abs(worth * strength))))
                            + (worth * (strength * Math.log(Math.abs(strength))) - (worth * (2 * x1 + strength * Math.log(Math.abs(x1 - strength)))));
                }

                playerProfile.getBackPack().addResource(key, amountSold * -1);
                stock += amountSold;
                market.trades.get(key).setStock(stock);
                market.trades.get(key).tick(false);
                e.setCurrentItem(market.trades.get(key).generateTradeItem());
                playerProfile.addMoney(value);
            }
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

        ArrayList<String> otherCategoryButtonLore = new ArrayList<>();
        otherCategoryButtonLore.add(format("&7Open the &eother &7items tab"));
        otherCategoryButton = createGuiItem("&eOther", otherCategoryButtonLore, Material.SUNFLOWER);

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

        ArrayList<String> addItemToMenuLore = new ArrayList<>();
        addItemToMenuLore.add(format("&7Add a &eitem &7to this market"));
        addItemToMenu = createGuiItem("&eAdd item", addItemToMenuLore, Material.GREEN_GLAZED_TERRACOTTA);

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

    private void openAddItemMenu(Player p) {
        Inventory addItemInventory = Bukkit.createInventory(null, 54, format("&eAdd item"));

        for (Resource r : resources.values()) {
            boolean alreadyInMarket = false;
            for (Trade trade : latestMarketOpen.get(p).trades.values()) {
                if (trade.getItemKey() == r.getKey()) alreadyInMarket = true;
            }
            if (!alreadyInMarket) {
                ItemStack item = new ItemStack(r.getItemStack().getType());
                ItemMeta imeta = item.getItemMeta();
                imeta.setDisplayName(r.getName());
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
}
