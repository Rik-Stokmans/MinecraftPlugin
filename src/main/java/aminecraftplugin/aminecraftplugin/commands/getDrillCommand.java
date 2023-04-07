package aminecraftplugin.aminecraftplugin.commands;

import aminecraftplugin.aminecraftplugin.drilling.drill.DrillType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import static aminecraftplugin.aminecraftplugin.drilling.drill.Drill.getDrill;
import static aminecraftplugin.aminecraftplugin.drilling.drill.DrillType.getDrillTypeFromName;

public class getDrillCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (commandSender instanceof Player){
            Player p = (Player) commandSender;
            String name = "";
            for (int i = 1; i < strings.length; i++){
                name += strings[i];
            }
            ItemStack drill = getDrill(Integer.parseInt(strings[0]), getDrillTypeFromName(name));
            p.getInventory().addItem(drill);
            return true;
        }
        return false;
    }
}
