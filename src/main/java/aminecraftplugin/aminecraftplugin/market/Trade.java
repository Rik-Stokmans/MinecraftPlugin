package aminecraftplugin.aminecraftplugin.market;

import aminecraftplugin.aminecraftplugin.drill.Resource;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Random;

import static aminecraftplugin.aminecraftplugin.utils.ChatUtils.format;

public class Trade {

    ItemStack item;
    String name;
    double baseValue;
    double buyPrice;
    double sellPrice;
    int itemKey;

    //trade logic
    int stockBaseline = 1000;
    int stock = 1000;

    public Trade(int _itemKey) {
        Resource resource = Resource.resources.get(_itemKey);
        item = resource.getItemStack();
        name = resource.getName();
        baseValue = resource.getValue();
        sellPrice = baseValue;
        buyPrice = sellPrice + sellPrice * 0.05;
        itemKey = _itemKey;
    }



    //tick method to update this trade
    public void tick(boolean includeStock) {
        Random rand = new Random();
        double oneDevBaseMultiple = 1 / (stock / stockBaseline);
        if (stock > stockBaseline) {
            sellPrice = baseValue * oneDevBaseMultiple;
            if (includeStock) {
                if (rand.nextDouble() < 1 - oneDevBaseMultiple) stock--;
            }
        }
        else sellPrice = baseValue * ((-1 / stockBaseline) * stock + 2);

        buyPrice = sellPrice * 1.05;
    }

    public ItemStack generateTradeItem() {
        ItemStack tradeItem = item;
        ItemMeta meta = tradeItem.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        lore.add(" ");
        lore.add(format("&aBuy: " + buyPrice + "$/Kg"));
        lore.add(format("&cSell: " + sellPrice + "$/Kg"));
        lore.add(" ");
        lore.add(format("&7 - left click to buy"));
        lore.add(format("&7 - right click to sell"));
        meta.setLore(lore);
        tradeItem.setItemMeta(meta);
        return tradeItem;
    }

    //gettters and setters
    public ItemStack getItem() {
        return item;
    }
    public void setItem(ItemStack item) {
        this.item = item;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public double getBaseValue() {
        return baseValue;
    }
    public void setBaseValue(double baseValue) {
        this.baseValue = baseValue;
    }
    public double getBuyPrice() {
        return buyPrice;
    }
    public void setBuyPrice(double buyPrice) {
        this.buyPrice = buyPrice;
    }
    public double getSellPrice() {
        return sellPrice;
    }
    public void setSellPrice(double sellPrice) {
        this.sellPrice = sellPrice;
    }
    public int getItemKey() {
        return itemKey;
    }
    public void setItemKey(int itemKey) {
        this.itemKey = itemKey;
    }
    public int getStockBaseline() {
        return stockBaseline;
    }
    public void setStockBaseline(int stockBaseline) {
        this.stockBaseline = stockBaseline;
    }
    public int getStock() {
        return stock;
    }
    public void setStock(int stock) {
        this.stock = stock;
    }

}
