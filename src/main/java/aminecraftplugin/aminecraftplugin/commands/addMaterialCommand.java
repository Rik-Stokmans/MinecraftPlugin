package aminecraftplugin.aminecraftplugin.commands;

import net.minecraft.nbt.NBTTagCompound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_19_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class addMaterialCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        if (commandSender instanceof Player && strings.length == 1){
            Player p = (Player) commandSender;
            if (p.getInventory().getItemInMainHand() == null) return true;
            ItemStack item = p.getInventory().getItemInMainHand();
            net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
            NBTTagCompound nbt = nmsItem.u();
            if (nbt == null) nbt = new NBTTagCompound();
            nbt.a("material", strings[0]);
            nmsItem.c(nbt);
            p.getInventory().setItemInMainHand(CraftItemStack.asBukkitCopy(nmsItem));
            return true;
        }
        return false;
    }
}
