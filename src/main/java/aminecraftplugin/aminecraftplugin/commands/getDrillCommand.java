package aminecraftplugin.aminecraftplugin.commands;

import net.minecraft.nbt.NBTTagCompound;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_19_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class getDrillCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (commandSender instanceof Player){
            Player p = (Player) commandSender;
            ItemStack drill = new ItemStack(Material.HOPPER);
            net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(drill);
            NBTTagCompound nbt = nmsItem.u();
            if (nbt == null) nbt = new NBTTagCompound();
            nbt.a("drilltier", Integer.parseInt(strings[0]));

            String name = "";
            for (int i = 1; i < strings.length; i++){
                name += strings[i];
            }

            nbt.a("drilltype", name);
            nmsItem.c(nbt);
            p.getInventory().addItem(CraftItemStack.asBukkitCopy(nmsItem));
            return true;
        }
        return false;
    }
}
