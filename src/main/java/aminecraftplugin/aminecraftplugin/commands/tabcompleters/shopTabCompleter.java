package aminecraftplugin.aminecraftplugin.commands.tabcompleters;

import aminecraftplugin.aminecraftplugin.shop.Shop;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static aminecraftplugin.aminecraftplugin.shop.Shop.shops;


public class shopTabCompleter implements TabCompleter {
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        ArrayList<String> shopNames = new ArrayList<>();
        if (strings.length == 1){
            for (Shop shop : shops){
                shopNames.add(shop.getName());
            }
        }
        return shopNames;
    }
}
