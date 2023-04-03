package aminecraftplugin.aminecraftplugin.commands;

import aminecraftplugin.aminecraftplugin.market.Market;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.CitizensNPC;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static aminecraftplugin.aminecraftplugin.market.Market.markets;
import static aminecraftplugin.aminecraftplugin.utils.ChatUtils.format;

public class removeMarketCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (commandSender instanceof Player) {
            Player p = (Player) commandSender;

            Entity targetEntity = p.getTargetEntity(5);
            Location marketLocation = targetEntity.getLocation();

            if (targetEntity == null || !targetEntity.hasMetadata("NPC")) {
                p.sendMessage(format("&cNo Npc found"));
                return true;
            }

            int marketToRemove = -1;
            for (Market m : markets.values()) {
                if (m.getLocation().equals(marketLocation)) {
                    marketToRemove = m.getKey();
                    marketLocation = m.getLocation();
                }
            }
            if (marketToRemove != -1) {
                markets.remove(marketToRemove);
                p.sendMessage(format(" "));
                p.sendMessage(format("&8 >> &7Market removed"));
                p.sendMessage(format(" &8 ( " + marketLocation.getX() + ", " + marketLocation.getY() + ", " + marketLocation.getZ() + " )"));
                CitizensAPI.getNPCRegistry().getNPC(targetEntity).destroy();
                return true;
            }
        }
        return false;
    }
}











