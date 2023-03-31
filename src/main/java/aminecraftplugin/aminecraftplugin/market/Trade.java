package aminecraftplugin.aminecraftplugin.market;

import aminecraftplugin.aminecraftplugin.drill.loot.Resource;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Random;

import static aminecraftplugin.aminecraftplugin.drill.loot.Resource.resources;
import static aminecraftplugin.aminecraftplugin.utils.ChatUtils.format;

public class Trade {

    Material material;
    String name;
    int itemKey;

    //trade logic
    double stock;
    int strength;
    double baseValue;
    double buyPrice;
    double sellPrice;

    public Trade(int _itemKey, int _strength) {
        stock = 0;
        Resource resource = resources.get(_itemKey);
        strength = _strength;
        material = resource.getItemStack().getType();
        name = resource.getName();
        baseValue = resource.getValue();
        itemKey = _itemKey;
        tick(false);
    }



    //tick method to update this trade
    public void tick(boolean updateStock) {
        Random rand = new Random();
        double chance = (strength * Math.pow(Math.abs(stock), 1.6)) / 1000000;

        if (updateStock && chance > rand.nextDouble() * 100) {
            if ((stock < 0 && stock > -1) || (stock > 0 && stock < 1)) {
                stock = 0;
            }
            else if (stock > 0) {
                stock--;
            }
            else if (stock < 0) {
                stock++;
            }
        }
        if (stock > 0) sellPrice = (strength * baseValue)/(stock + strength);
        else sellPrice = (2 * baseValue + ((strength * baseValue)/(stock - strength)));
        buyPrice = sellPrice * 1.05;
    }

    public ItemStack generateTradeItem() {
        tick(false);
        ItemStack tradeItem = new ItemStack(material);
        ItemMeta meta = tradeItem.getItemMeta();
        meta.setDisplayName(resources.get(itemKey).getName());
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
