package aminecraftplugin.aminecraftplugin.commands;

import aminecraftplugin.aminecraftplugin.drill.LootTable;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class addLootTableCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        if (commandSender instanceof Player){
            Player p = (Player) commandSender;
            String name = "";
            for (String string : strings){
                name = string + " ";
            }
            Location location = p.getLocation();

            LootTable lootTable = new LootTable(name, location);
            lootTable.openLoottableMenu(p);
        }

        return false;
    }
}
