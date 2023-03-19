package aminecraftplugin.aminecraftplugin.commands;

import aminecraftplugin.aminecraftplugin.market.Market;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerChatEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

import static aminecraftplugin.aminecraftplugin.market.Market.markets;
import static aminecraftplugin.aminecraftplugin.utils.ChatUtils.format;

public class CreateMarket implements CommandExecutor {


    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (commandSender instanceof Player) {
            Player p = (Player) commandSender;
            String marketName = "";
            for(String namePart : args) marketName += namePart + " ";

            Block clickedBlock = p.getTargetBlockExact(5);
            if (clickedBlock == null) return false;

            int i = 0;
            while (true) {
                if (!markets.containsKey(i)) {
                    markets.put(i, new Market(marketName, clickedBlock.getLocation()));
                    p.sendMessage(format("&aMarket created"));
                    break;
                }
                else i++;
            }
            return true;
        }
        return false;
    }
}























