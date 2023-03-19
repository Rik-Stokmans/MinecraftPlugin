package aminecraftplugin.aminecraftplugin;

import aminecraftplugin.aminecraftplugin.commands.addResourceCommand;
import aminecraftplugin.aminecraftplugin.commands.addValueCommand;
import aminecraftplugin.aminecraftplugin.commands.tabcompleters.nullTabCompleter;
import aminecraftplugin.aminecraftplugin.commands.tabcompleters.numTabCompleter;
import aminecraftplugin.aminecraftplugin.drill.Resource;
import aminecraftplugin.aminecraftplugin.market.Market;
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
        events.add(new Market()); events.add(new Resource());

        for (Listener l : events) {
            getServer().getPluginManager().registerEvents(l, this);
        }

        //resource init
        Resource.init();

        //market init
        Market.init();

        Main main = this;


        //todo: make this better
        //commands
        getServer().getPluginCommand("addresource").setExecutor(new addResourceCommand());
        getServer().getPluginCommand("addresource").setTabCompleter(new nullTabCompleter());
        getServer().getPluginCommand("addvalue").setExecutor(new addValueCommand());
        getServer().getPluginCommand("addvalue").setTabCompleter(new numTabCompleter());
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
