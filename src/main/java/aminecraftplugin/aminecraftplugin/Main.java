package aminecraftplugin.aminecraftplugin;

import aminecraftplugin.aminecraftplugin.commands.*;
import aminecraftplugin.aminecraftplugin.commands.tabcompleters.nullTabCompleter;
import aminecraftplugin.aminecraftplugin.commands.tabcompleters.numTabCompleter;
import aminecraftplugin.aminecraftplugin.drill.LootTable;
import aminecraftplugin.aminecraftplugin.drill.Resource;
import aminecraftplugin.aminecraftplugin.market.Market;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public final class Main extends JavaPlugin {

    private static String path;
    public static Plugin plugin;


    @Override
    public void onEnable() {

        plugin = this;


        //file path
        path = getDataFolder().getAbsoluteFile().toString();

        // Plugin startup logic. test

        ArrayList<Listener> events = new ArrayList<>();
        //list of events
        events.add(new Market()); events.add(new Resource()); events.add(new events()); events.add(new LootTable());

        for (Listener l : events) {
            getServer().getPluginManager().registerEvents(l, this);
        }

        //loottable init
        LootTable.init();

        //resource init
        Resource.init();

        //market things
        Market.init();


        //commands
        new Command("addresource", new addResourceCommand());
        new Command("addvalue", new addValueCommand(), new numTabCompleter());
        new Command("createmarket", new createMarketCommand());
        new Command("removemarket", new removeMarketCommand());
        new Command("createloottable", new createLootTableCommand());
        new Command("checkloottables", new checkLootTableCommand());


        //ticker all
        /*
        BukkitScheduler scheduler = getServer().getScheduler();
        scheduler.scheduleSyncRepeatingTask(this, () -> {
            for (Market m : Market.markets.values()) {
                m.tick();
            }
        }, 0L, 20L);

        */
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic

        //resource save
        Resource.save();

        //lootable save
        LootTable.save();

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
