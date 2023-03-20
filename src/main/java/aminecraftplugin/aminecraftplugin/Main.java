package aminecraftplugin.aminecraftplugin;

import aminecraftplugin.aminecraftplugin.commands.CreateMarket;
import aminecraftplugin.aminecraftplugin.commands.RemoveMarket;
import aminecraftplugin.aminecraftplugin.commands.addResourceCommand;
import aminecraftplugin.aminecraftplugin.commands.addValueCommand;
import aminecraftplugin.aminecraftplugin.commands.tabcompleters.nullTabCompleter;
import aminecraftplugin.aminecraftplugin.commands.tabcompleters.numTabCompleter;
import aminecraftplugin.aminecraftplugin.drill.Resource;
import aminecraftplugin.aminecraftplugin.market.Market;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

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
        events.add(new Market()); events.add(new Resource());

        for (Listener l : events) {
            getServer().getPluginManager().registerEvents(l, this);
        }

        //resource init
        Resource.init();

        //market things
        Market.init();

        Main main = this;


        //todo: make this better
        //commands
        getServer().getPluginCommand("addresource").setExecutor(new addResourceCommand());
        getServer().getPluginCommand("addresource").setTabCompleter(new nullTabCompleter());
        getServer().getPluginCommand("addvalue").setExecutor(new addValueCommand());
        getServer().getPluginCommand("addvalue").setTabCompleter(new numTabCompleter());
        getServer().getPluginCommand("createmarket").setExecutor(new CreateMarket());
        getServer().getPluginCommand("createmarket").setTabCompleter(new nullTabCompleter());
        getServer().getPluginCommand("removemarket").setExecutor(new RemoveMarket());
        getServer().getPluginCommand("removemarket").setTabCompleter(new nullTabCompleter());

        //ticker all
        BukkitScheduler scheduler = getServer().getScheduler();
        scheduler.scheduleSyncRepeatingTask(this, () -> {
            for (Market m : Market.markets.values()) {
                m.tick();
            }
        }, 0L, 20L);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic

        Resource.save();
    }


    //save and load files

    public static void saveFile(YamlConfiguration file, String s) throws IOException {
        file.save(new File(path,s));
    }

    public static YamlConfiguration loadFile(String s) throws IOException {
        YamlConfiguration file = YamlConfiguration.loadConfiguration(new File(path, s));
        return file;
    }
}
