package aminecraftplugin.aminecraftplugin.commands;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static aminecraftplugin.aminecraftplugin.commands.addSpawnCommand.spawnLocations;

public class removeSpawnCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        for (String locName : spawnLocations.keySet()){
            if (locName.equals(strings[0])){
                spawnLocations.remove(locName);
                return true;
            }
        }
        return false;
    }
}
