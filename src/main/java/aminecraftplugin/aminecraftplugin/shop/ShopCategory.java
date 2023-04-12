package aminecraftplugin.aminecraftplugin.shop;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import static aminecraftplugin.aminecraftplugin.utils.ChatUtils.format;

public enum ShopCategory {

    DRILL,
    ENERGY,
    WEAPONS,
    ARMOR,
    NULL;


    public static ShopCategory getShopCategoryFromString(String s){
        switch (s){
            case "DRILL":
                return ShopCategory.DRILL;
            case "ENERGY":
                return ShopCategory.ENERGY;
            case "WEAPONS":
                return ShopCategory.WEAPONS;
            case "ARMOR":
                return ShopCategory.ARMOR;
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
            case WEAPONS:
                icon = new ItemStack(Material.GOLDEN_SWORD);
                iconMeta = icon.getItemMeta();
                iconMeta.setDisplayName(format("&7Weapons"));
                icon.setItemMeta(iconMeta);
                break;
            case ARMOR:
                icon = new ItemStack(Material.IRON_HELMET);
                iconMeta = icon.getItemMeta();
                iconMeta.setDisplayName(format("&7Armor"));
                icon.setItemMeta(iconMeta);
                break;
        }
        icon = new ItemStack(Material.BARRIER);
        iconMeta = icon.getItemMeta();
        iconMeta.setDisplayName(format("&cERROR"));
        icon.setItemMeta(iconMeta);
        return icon;
    }
}
