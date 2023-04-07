package aminecraftplugin.aminecraftplugin.commands;

import aminecraftplugin.aminecraftplugin.shop.Shop;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static aminecraftplugin.aminecraftplugin.utils.ChatUtils.format;

public class createShopCommand implements CommandExecutor {


    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (commandSender instanceof Player){
            Player p = (Player) commandSender;
            String name = strings[0];
            Location location = p.getTargetBlockExact(10).getLocation();
            Shop shop = new Shop(name, location);
            p.sendMessage(format("&aSuccesfully created new shop with name " + shop.getName()));
            return true;
        }
        return false;
    }
}
