package aminecraftplugin.aminecraftplugin.market;

import aminecraftplugin.aminecraftplugin.drill.loot.Resource;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

import static aminecraftplugin.aminecraftplugin.utils.ChatUtils.format;

public class Trade {

    ItemStack item;
    String name;
    double baseValue;
    double buyPrice;
    double sellPrice;
    int itemKey;
    int strength;

    //trade logic
    double stock;

    public Trade(int _itemKey, int _strength) {
        stock = 0;
        Resource resource = Resource.resources.get(_itemKey);
        strength = _strength;
        item = resource.getItemStack();
        name = resource.getName();
        baseValue = resource.getValue();
        itemKey = _itemKey;
        tick();
    }



    //tick method to update this trade
    public void tick() {
        if (stock > 0) sellPrice = (strength * baseValue)/(stock + strength);
        else sellPrice = (2 * baseValue + ((strength * baseValue)/(stock - strength)));
        buyPrice = sellPrice * 1.05;
    }

    public ItemStack generateTradeItem() {
        tick();
        ItemStack tradeItem = item;
        ItemMeta meta = tradeItem.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        lore.add(" ");
        lore.add(format("&aBuy: &f" + String.format("%.2f",buyPrice) + " &e$/Kg"));
        lore.add(format("&cSell: &f" + String.format("%.2f",sellPrice) + " &e$/Kg"));
        lore.add(" ");
        lore.add(format("&8Estimated price"));
        lore.add(format("&8Market may change"));
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
    public double getStock() {
        return stock;
    }
    public void setStock(double stock) {
        this.stock = stock;
    }

}
