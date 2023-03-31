package aminecraftplugin.aminecraftplugin.commands.tabcompleters;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class materialTabCompleter implements TabCompleter {
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        ArrayList<String> tabComplete = new ArrayList<>();
        if (strings.length == 1){
            String current = strings[0];
            for (Material material : Material.values()){
                if (current != null) {
                    if (material.name().contains(current)) {
                        tabComplete.add(material.name());
                    }
                } else {
                    tabComplete.add(material.name());
                }
            }
        }
        return tabComplete;
    }
}
