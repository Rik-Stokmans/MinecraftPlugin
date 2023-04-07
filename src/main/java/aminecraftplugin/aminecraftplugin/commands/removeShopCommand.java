package aminecraftplugin.aminecraftplugin.commands;

import aminecraftplugin.aminecraftplugin.shop.Shop;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import static aminecraftplugin.aminecraftplugin.shop.Shop.shops;
import static aminecraftplugin.aminecraftplugin.utils.ChatUtils.format;

public class removeShopCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (commandSender instanceof Player){
            Player p = (Player) commandSender;
            ArrayList<Shop> shopsToBeRemoved = new ArrayList<>();
            if (strings.length == 0){
                for (Shop shop : shops){
                    Location targetLoc = p.getTargetBlockExact(10).getLocation();
                    if (shop.getLocation().equals(targetLoc)){
                        shop.delete();
                        shopsToBeRemoved.add(shop);
                        p.sendMessage(format("&cDeleted shop with the name " + shop.getName()));
                    }
                }
            } else if (strings.length == 1){
                String shopName = strings[0];
                for (Shop shop : shops){
                    if (shop.getName().equals(shopName)){
                        shop.delete();
                        shopsToBeRemoved.remove(shop);
                        p.sendMessage(format("&cDeleted shop with the name " + shop.getName()));
                    }
                }
            }
            for (Shop shop : shopsToBeRemoved){
                shops.remove(shop);
            }
            return true;
        }
        return false;
    }
}
