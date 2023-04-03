package aminecraftplugin.aminecraftplugin.commands;

import aminecraftplugin.aminecraftplugin.market.Market;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static aminecraftplugin.aminecraftplugin.market.Market.markets;
import static aminecraftplugin.aminecraftplugin.utils.ChatUtils.format;

public class createMarketCommand implements CommandExecutor {

    //todo: tabcompleter toont alle markets

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (commandSender instanceof Player) {
            Player p = (Player) commandSender;
            String marketName = "";
            for(String namePart : args) marketName += namePart + " ";

            Location marketLocation = p.getLocation();
            marketLocation.set(Math.floor(marketLocation.getX()) + 0.5,Math.floor(marketLocation.getY()),Math.floor(marketLocation.getZ()) + 0.5);
            marketLocation.setPitch(0);
            marketLocation.setYaw(0);

            //adds the npc for the market
            NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, marketName);

            npc.spawn(marketLocation);

            int i = 0;
            while (true) {
                if (!markets.containsKey(i)) {
                    markets.put(i, new Market(marketName, marketLocation, i, 1000));
                    p.sendMessage(format(" "));
                    p.sendMessage(format("&8 >> &7Market created"));
                    p.sendMessage(format(" &8 ( " + marketLocation.getX() + ", " + marketLocation.getY() + ", " + marketLocation.getZ() + " )"));
                    break;
                }
                else i++;
            }
            return true;
        }
        return false;
    }
}























