package aminecraftplugin.aminecraftplugin.market;

import aminecraftplugin.aminecraftplugin.drill.loot.Resource;
import aminecraftplugin.aminecraftplugin.player.PlayerProfile;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Random;

import static aminecraftplugin.aminecraftplugin.drill.loot.Resource.resources;
import static aminecraftplugin.aminecraftplugin.market.Market.*;
import static aminecraftplugin.aminecraftplugin.player.PlayerProfile.getPlayerProfile;
import static aminecraftplugin.aminecraftplugin.player.PlayerProfile.playerProfiles;
import static aminecraftplugin.aminecraftplugin.utils.ChatUtils.format;

public class Trade {

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
        strength = _strength;
        itemKey = _itemKey;
        Resource resource = resources.get(itemKey);
        baseValue = resource.getValue();
        tick(false);
    }


    //method that gets called when loading from file
    public Trade(int _itemKey, int _strength, double _stock) {
        stock = _stock;
        itemKey = _itemKey;
        strength = _strength;
        Resource resource = resources.get(itemKey);
        baseValue = resource.getValue();
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



    public void executeBuyOrder(double orderSize, Player p, InventoryClickEvent e) {

        double amountBought = orderSize;

        PlayerProfile playerProfile = playerProfiles.get(p.getUniqueId());
        double playerMoney = playerProfile.getMoney();
        double backpackEmptySpace = playerProfile.getBackPack().getEmptySpace();


        if (backpackEmptySpace < amountBought) amountBought = backpackEmptySpace;
        if (amountBought == 0.0) {
            p.sendMessage(format("&cInsufficient space in your &ebackpack"));
            return;
        }
        double price = 0;

        //calculates the price of the oder size
        double x1 = stock - amountBought;
        double x2 = stock;

        if (x1 >= 0) {
            if (x1 == 0) x1 += Double.MIN_VALUE;
            price = (baseValue * strength * Math.log(Math.abs(baseValue * x2 + baseValue * strength))) - (baseValue * strength * Math.log(baseValue * x1 + baseValue * strength));
        }
        else if (x2 <= 0) {
            if (x2 == 0) x2 -= Double.MIN_VALUE;
            price = (baseValue * (2 * x2 + strength * Math.log(Math.abs(x2 - strength)))) - (baseValue * (2 * x1 + strength * Math.log(Math.abs(x1 - strength))));
        }
        else if (x1 < 0 && x2 > 0) {
            price = ((baseValue * strength * Math.log(Math.abs(baseValue * x2 + baseValue * strength))) - (baseValue * strength * Math.log(Math.abs(baseValue * strength))))
                    + (baseValue * (strength * Math.log(Math.abs(strength))) - (baseValue * (2 * x1 + strength * Math.log(Math.abs(x1 - strength)))));
        }

        price = price * marketBuyTax;

        if (playerMoney < price) {
            p.sendMessage(format("&cInsufficient &efunds"));
            return;
        }

        playerProfile.addMoney(price * -1);
        playerProfile.getBackPack().addResource(itemKey, amountBought);
        stock -= amountBought;
        tick(false);
        e.setCurrentItem(generateTradeItem());
    }

    public void executeSellOrder(double orderSize, Player p, InventoryClickEvent e) {

        double amountSold = orderSize;

        PlayerProfile playerProfile = playerProfiles.get(p.getUniqueId());
        double itemAmountInBackpack = playerProfile.getBackPack().getItemAmountInBackpack(itemKey);
        double backpackEmptySpace = playerProfile.getBackPack().getEmptySpace();



        if (amountSold > itemAmountInBackpack) amountSold = itemAmountInBackpack;
        if (amountSold == 0.0) return;
        double value = 0;

        //calculates the value of the sold amount of items
        double x1 = stock;
        double x2 = stock + amountSold;

        if (x1 >= 0) {
            if (x1 == 0) x1 += Double.MIN_VALUE;
            value = (baseValue * strength * Math.log(Math.abs(baseValue * x2 + baseValue * strength))) - (baseValue * strength * Math.log(baseValue * x1 + baseValue * strength));
        }
        else if (x2 <= 0) {
            if (x2 == 0) x2 -= Double.MIN_VALUE;
            value = (baseValue * (2 * x2 + strength * Math.log(Math.abs(x2 - strength)))) - (baseValue * (2 * x1 + strength * Math.log(Math.abs(x1 - strength))));
        }
        else if (x1 < 0 && x2 > 0) {
            value = ((baseValue * strength * Math.log(Math.abs(baseValue * x2 + baseValue * strength))) - (baseValue * strength * Math.log(Math.abs(baseValue * strength))))
                    + (baseValue * (strength * Math.log(Math.abs(strength))) - (baseValue * (2 * x1 + strength * Math.log(Math.abs(x1 - strength)))));
        }

        playerProfile.getBackPack().removeResource(itemKey, amountSold);
        stock += amountSold;
        tick(false);
        e.setCurrentItem(generateTradeItem());
        playerProfile.addMoney(value);
    }


    public ItemStack generateTradeItem() {
        tick(false);
        ItemStack tradeItem = resources.get(itemKey).getItemStack().clone();
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
    public int getStrength() {
        return strength;
    }
    public void setStrength(int strength) {
        this.strength = strength;
    }
}
