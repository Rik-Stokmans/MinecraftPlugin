package aminecraftplugin.aminecraftplugin.utils;

import org.bukkit.ChatColor;

public class ChatUtils {

    public static String format(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static String deFormat(String text) {
        return ChatColor.stripColor(text);
    }

}
