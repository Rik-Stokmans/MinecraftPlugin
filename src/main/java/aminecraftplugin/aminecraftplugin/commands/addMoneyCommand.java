package aminecraftplugin.aminecraftplugin.commands;

import aminecraftplugin.aminecraftplugin.player.PlayerProfile;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static aminecraftplugin.aminecraftplugin.player.PlayerProfile.getPlayerProfile;
import static aminecraftplugin.aminecraftplugin.utils.Compress.returnCompressed;

public class addMoneyCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (commandSender instanceof Player){
            Player p = (Player) commandSender;
            PlayerProfile profile = getPlayerProfile(p);
            profile.addMoney(Double.parseDouble(strings[0]));
            p.sendMessage("New balance: " + returnCompressed(profile.getMoney(), 2));
            return true;
        }
        return false;
    }
}
