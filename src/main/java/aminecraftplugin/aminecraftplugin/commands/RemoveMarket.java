package aminecraftplugin.aminecraftplugin.commands;

import aminecraftplugin.aminecraftplugin.market.Market;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static aminecraftplugin.aminecraftplugin.market.Market.markets;
import static aminecraftplugin.aminecraftplugin.utils.ChatUtils.format;

public class RemoveMarket implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (commandSender instanceof Player) {
            Player p = (Player) commandSender;

            Block targetBlock = p.getTargetBlockExact(5);
            if (targetBlock == null) return false;

            int marketToRemove = -1;
            for (Market m : markets.values()) {
                if (m.getLocation().equals(targetBlock)) {
                    marketToRemove = m.getKey();
                    m.getLocation().getBlock().setType(Material.AIR);
                }
            }
            markets.remove(marketToRemove);
            p.sendMessage(format("&aMarket removed successfully"));

            return true;
        }
        return false;
    }
}











