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

import static aminecraftplugin.aminecraftplugin.drill.structures.Drill.getDrill;

public class getDrillCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (commandSender instanceof Player){
            Player p = (Player) commandSender;
            String name = "";
            for (int i = 1; i < strings.length; i++){
                name += strings[i];
            }
            ItemStack drill = getDrill(Integer.parseInt(strings[0]), name);
            p.getInventory().addItem(drill);
            return true;
        }
        return false;
    }
}
