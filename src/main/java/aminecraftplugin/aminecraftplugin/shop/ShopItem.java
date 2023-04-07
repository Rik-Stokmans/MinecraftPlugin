package aminecraftplugin.aminecraftplugin.shop;

import org.bukkit.inventory.ItemStack;

public class ShopItem {

    private ItemStack item;
    private double price;

    public ShopItem(ItemStack item, double price) {
        this.item = item;
        this.price = price;
    }

    public ItemStack getItem() {
        return item;
    }

    public void setItem(ItemStack item) {
        this.item = item;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
