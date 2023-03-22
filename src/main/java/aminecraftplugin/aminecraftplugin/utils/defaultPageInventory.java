package aminecraftplugin.aminecraftplugin.utils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

import static aminecraftplugin.aminecraftplugin.utils.ChatUtils.format;

public class defaultPageInventory {

    public static Inventory getDefaultScrollableInventory(String name, boolean backButton){

        Inventory inventory = Bukkit.createInventory(null, 54, name);

        ItemStack leftArrow = new ItemStack(Material.ARROW);
        ItemMeta meta = leftArrow.getItemMeta();
        meta.setDisplayName("Previous page");
        leftArrow.setItemMeta(meta);

        ItemStack rightArrow = new ItemStack(Material.ARROW);
        ItemMeta meta2 = rightArrow.getItemMeta();
        meta2.setDisplayName("Next page");
        rightArrow.setItemMeta(meta2);

        ItemStack grayGlass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta3 = grayGlass.getItemMeta();
        meta3.setDisplayName(format("&7"));
        grayGlass.setItemMeta(meta3);

        if (backButton) {
            ItemStack barrier = new ItemStack(Material.BARRIER);
            ItemMeta meta4 = barrier.getItemMeta();
            meta4.setDisplayName(format("&cGo back"));
            barrier.setItemMeta(meta4);
            inventory.setItem(49, barrier);
        }

        inventory.setItem(45, leftArrow);
        inventory.setItem(53, rightArrow);
        for (int i = 36; i < 45; i++){
            inventory.setItem(i, grayGlass);
        }

        return inventory;
    }


}
