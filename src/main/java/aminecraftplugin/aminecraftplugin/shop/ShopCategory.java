package aminecraftplugin.aminecraftplugin.shop;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import static aminecraftplugin.aminecraftplugin.utils.ChatUtils.format;

public enum ShopCategory {

    DRILL,
    ENERGY,
    NULL;


    public static ShopCategory getShopCategoryFromString(String s){
        switch (s){
            case "DRILL":
                return ShopCategory.DRILL;
            case "ENERGY":
                return ShopCategory.ENERGY;
        }
        return ShopCategory.NULL;
    }

    public String getStringFromShopCategory(){
        return this.toString();
    }

    public ItemStack getIcon(){
        ItemStack icon;
        ItemMeta iconMeta;
        switch (this){
            case DRILL:
                icon = new ItemStack(Material.HOPPER);
                iconMeta = icon.getItemMeta();
                iconMeta.setDisplayName(format("&7Drill"));
                icon.setItemMeta(iconMeta);
                return icon;
            case ENERGY:
                icon = new ItemStack(Material.COAL);
                iconMeta = icon.getItemMeta();
                iconMeta.setDisplayName(format("&7Energy"));
                icon.setItemMeta(iconMeta);
                return icon;
        }
        icon = new ItemStack(Material.BARRIER);
        iconMeta = icon.getItemMeta();
        iconMeta.setDisplayName(format("&cERROR"));
        icon.setItemMeta(iconMeta);
        return icon;
    }
}
