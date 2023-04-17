package aminecraftplugin.aminecraftplugin.commands.tabcompleters;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class getDrillTabCompleter implements TabCompleter {
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        ArrayList<String> tabComplete = new ArrayList<>();
        if (strings.length == 1){
            tabComplete.add("speedtier");
        }
        else if (strings.length == 2){
            tabComplete.add("depthtier");
        }
        else if (strings.length == 3){
            tabComplete.add("starterdrill");
        }
        return tabComplete;
    }
}
