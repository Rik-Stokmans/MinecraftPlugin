package aminecraftplugin.aminecraftplugin;

import aminecraftplugin.aminecraftplugin.drill.Resource;
import aminecraftplugin.aminecraftplugin.market.Market;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

public final class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic. test

        ArrayList<Listener> events = new ArrayList<>();
        //list of events
        events.add(new Market());

        for (Listener l : events) {
            getServer().getPluginManager().registerEvents(l, this);
        }

        //resource init
        Resource.init();

        Main main = this;

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
