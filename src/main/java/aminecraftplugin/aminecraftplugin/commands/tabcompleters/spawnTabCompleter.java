package aminecraftplugin.aminecraftplugin.commands.tabcompleters;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static aminecraftplugin.aminecraftplugin.commands.addSpawnCommand.spawnLocations;

public class spawnTabCompleter implements TabCompleter {
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        ArrayList<String> spawns = new ArrayList<>();
        if (strings.length == 1){
            for (String locName : spawnLocations.keySet()){
                spawns.add(locName);
            }
        }
        return spawns;
    }
}
