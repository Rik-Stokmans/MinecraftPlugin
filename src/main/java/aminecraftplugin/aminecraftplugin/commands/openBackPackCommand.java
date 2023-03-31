package aminecraftplugin.aminecraftplugin.commands;

import aminecraftplugin.aminecraftplugin.player.PlayerProfile;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static aminecraftplugin.aminecraftplugin.player.PlayerProfile.getPlayerProfile;

public class openBackPackCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (commandSender instanceof Player){
            Player p = (Player) commandSender;
            PlayerProfile playerProfile = getPlayerProfile(p);
            playerProfile.getBackPack().open(p, p.getUniqueId());
            return true;
        }
        return false;
    }
}
