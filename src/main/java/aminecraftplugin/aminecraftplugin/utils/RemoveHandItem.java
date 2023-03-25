package aminecraftplugin.aminecraftplugin.utils;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class RemoveHandItem {

    public static void removeHandItem(Player p, EquipmentSlot equipmentSlot){
        if (equipmentSlot.equals(EquipmentSlot.HAND)) {
            //removes item from hand
            if (p.getInventory().getItemInMainHand().getAmount() == 1) {
                p.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
            } else {
                p.getInventory().getItemInMainHand().setAmount(p.getInventory().getItemInMainHand().getAmount() - 1);
            }
        } else if (equipmentSlot.equals(EquipmentSlot.OFF_HAND)){
            if (p.getInventory().getItemInOffHand().getAmount() == 1) {
                p.getInventory().setItemInOffHand(new ItemStack(Material.AIR));
            } else {
                p.getInventory().getItemInOffHand().setAmount(p.getInventory().getItemInOffHand().getAmount() - 1);
            }
        }
    }
}
