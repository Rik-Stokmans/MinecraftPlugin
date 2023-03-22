package aminecraftplugin.aminecraftplugin.commands;

import aminecraftplugin.aminecraftplugin.market.Market;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static aminecraftplugin.aminecraftplugin.market.Market.markets;
import static aminecraftplugin.aminecraftplugin.utils.ChatUtils.format;

public class removeMarketCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (commandSender instanceof Player) {
            Player p = (Player) commandSender;

            Block targetBlock = p.getTargetBlock(null, 5);
            if (targetBlock == null) return false;

            Location marketLocation = targetBlock.getLocation();
            int marketToRemove = -1;
            for (Market m : markets.values()) {
                if (m.getLocation().equals(targetBlock.getLocation())) {
                    marketToRemove = m.getKey();
                    marketLocation = m.getLocation();
                }
            }
            if (marketToRemove != -1) {
                markets.remove(marketToRemove);
                p.sendMessage(format(" "));
                p.sendMessage(format("&8 >> &7Market removed"));
                p.sendMessage(format(" &8 ( " + marketLocation.getX() + ", " + marketLocation.getY() + ", " + marketLocation.getZ() + " )"));
                return true;
            }
        }
        return false;
    }
}











