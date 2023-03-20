package aminecraftplugin.aminecraftplugin;

import aminecraftplugin.aminecraftplugin.market.Market;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

//runs the code in the run() method every 20 ticks
public final class Ticker extends JavaPlugin {
    public void onEnable() {
        BukkitScheduler scheduler = getServer().getScheduler();
        scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                for (Market m : Market.markets.values()) {
                    m.tick();
                }
            }
        }, 0L, 20L);
    }
}