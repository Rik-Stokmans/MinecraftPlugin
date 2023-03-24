package aminecraftplugin.aminecraftplugin.commands;

import aminecraftplugin.aminecraftplugin.drill.Drill;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class getLootCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (commandSender instanceof Player){
            Player p = (Player) commandSender;
            Drill drill = new Drill(p.getLocation());
            drill.drill(p);
            return true;
        }
        return false;
    }
}
