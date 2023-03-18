package aminecraftplugin.aminecraftplugin;

import aminecraftplugin.aminecraftplugin.drill.Resource;
import aminecraftplugin.aminecraftplugin.market.Market;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public final class Main extends JavaPlugin {

    private static String path;

    @Override
    public void onEnable() {

        //file path
        path = getDataFolder().getAbsoluteFile().toString();

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


    public static void saveFile(YamlConfiguration file, String s) throws IOException {
        file.save(new File(path,s));
    }
}
