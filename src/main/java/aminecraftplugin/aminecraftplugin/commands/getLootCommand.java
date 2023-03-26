package aminecraftplugin.aminecraftplugin.commands;

import aminecraftplugin.aminecraftplugin.drill.structures.Drill;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class getLootCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (commandSender instanceof Player){
            int amount = 1;
            if (strings.length > 0){
                amount = Integer.parseInt(strings[0]);
            }
            Player p = (Player) commandSender;
            Drill drill = new Drill(p.getLocation(), p, new ItemStack(Material.HOPPER));
            for (int i = 0; i < amount; i++) {
                drill.drill(p);
            }
            return true;
        }
        return false;
    }
}
