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

    public static HashMap<Player, Location> expectingMarketName = new HashMap<>();

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;
            if (expectingMarketName.containsKey(player)) return false;

            Block clickedBlock = player.getTargetBlockExact(5);
            if (clickedBlock == null) return false;

            player.sendMessage(format("&ePlease give the name of the market:"));
            expectingMarketName.put(player, clickedBlock.getLocation());
            return true;
        }
        return false;
    }

    @EventHandler
    public void onChat(PlayerChatEvent e) {
        if (expectingMarketName.containsKey(e.getPlayer())) {
            int i = 0;
            while (true) {
                if (!markets.containsKey(i)) {
                    markets.put(i, new Market(e.getMessage(), expectingMarketName.get(e.getPlayer())));
                    e.getPlayer().sendMessage(format("&aMarket created"));
                    break;
                }
                else i++;
            }
            e.setCancelled(true);
        }
    }
}























