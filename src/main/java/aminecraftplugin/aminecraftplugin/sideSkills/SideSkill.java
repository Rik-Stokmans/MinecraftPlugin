package aminecraftplugin.aminecraftplugin.sideSkills;

import aminecraftplugin.aminecraftplugin.sideSkills.mining.MiningSkill;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

import static aminecraftplugin.aminecraftplugin.utils.ChatUtils.format;
import static aminecraftplugin.aminecraftplugin.utils.Compress.roundAvoid;

public class SideSkill {

    public static HashMap<UUID, MiningSkill> miningSkills = new HashMap<>();

    double xp = 0;
    int level = 0;
    double xpNeeded = 10;

    public boolean addXp(double addedAmount, Player p) {
        xp += addedAmount;


        if (xp >= xpNeeded) {
            level++;
            xp -= xpNeeded;
            xpNeeded = 10 * Math.pow(Math.E, Math.pow(level, 0.4));
            Bukkit.broadcastMessage("you leveled up");
            sendMiningActionbar(p, addedAmount);
            return true;
        } else  {
            sendMiningActionbar(p, addedAmount);
            return false;
        }
    }

    private void sendMiningActionbar(Player p, double addedAmount) {
        p.sendActionBar(format("&7" + getPercentageBar(xp/xpNeeded * 100) + " &8(&a+" + addedAmount + "&8)"));
    }

    private String getPercentageBar(double percentage) {
        if (percentage < 5.0) return format("&7[||||||||||||||||||]");
        if (percentage < 10.0) return format("&a[&7||||||||||||||||||]");
        if (percentage < 15.0) return format("&a[|&7|||||||||||||||||]");
        if (percentage < 20.0) return format("&a[||&7||||||||||||||||]");
        if (percentage < 25.0) return format("&a[|||&7|||||||||||||||]");
        if (percentage < 30.0) return format("&a[||||&7||||||||||||||]");
        if (percentage < 35.0) return format("&a[|||||&7|||||||||||||]");
        if (percentage < 40.0) return format("&a[||||||&7||||||||||||]");
        if (percentage < 45.0) return format("&a[|||||||&7|||||||||||]");
        if (percentage < 50.0) return format("&a[||||||||&7||||||||||]");
        if (percentage < 55.0) return format("&a[|||||||||&7|||||||||]");
        if (percentage < 60.0) return format("&a[||||||||||&7||||||||]");
        if (percentage < 65.0) return format("&a[|||||||||||&7|||||||]");
        if (percentage < 70.0) return format("&a[||||||||||||&7||||||]");
        if (percentage < 75.0) return format("&a[|||||||||||||&7|||||]");
        if (percentage < 80.0) return format("&a[||||||||||||||&7||||]");
        if (percentage < 85.0) return format("&a[|||||||||||||||&7|||]");
        if (percentage < 90.0) return format("&a[||||||||||||||||&7||]");
        if (percentage < 95.0) return format("&a[|||||||||||||||||&7|]");
        else return format("&a[|||||||||||||||||&7]");
    }
}
