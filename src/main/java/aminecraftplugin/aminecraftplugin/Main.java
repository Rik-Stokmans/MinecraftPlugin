package aminecraftplugin.aminecraftplugin;

import aminecraftplugin.aminecraftplugin.commands.*;
import aminecraftplugin.aminecraftplugin.commands.tabcompleters.getDrillTabCompleter;
import aminecraftplugin.aminecraftplugin.commands.tabcompleters.materialTabCompleter;
import aminecraftplugin.aminecraftplugin.commands.tabcompleters.numTabCompleter;
import aminecraftplugin.aminecraftplugin.drill.structures.Drill;
import aminecraftplugin.aminecraftplugin.drill.loot.LootTable;
import aminecraftplugin.aminecraftplugin.drill.loot.Resource;
import aminecraftplugin.aminecraftplugin.drill.structures.Structure;
import aminecraftplugin.aminecraftplugin.drill.structures.StructureEvent;
import aminecraftplugin.aminecraftplugin.market.Market;
import aminecraftplugin.aminecraftplugin.player.PlayerProfile;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import me.filoghost.holographicdisplays.api.HolographicDisplaysAPI;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static aminecraftplugin.aminecraftplugin.utils.ChatUtils.format;

public final class Main extends JavaPlugin {

    private static String path;
    public static Plugin plugin;
    public static boolean useHolographicDisplays;
    public static HolographicDisplaysAPI api;
    public static ProtocolManager protocolManager;

    @Override
    public void onEnable() {

        plugin = this;

        //holographicdisplays
        useHolographicDisplays = Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays");
        if (useHolographicDisplays) {
            api = HolographicDisplaysAPI.get(this);
        }

        //protocollib
        protocolManager = ProtocolLibrary.getProtocolManager();

        //file path
        path = getDataFolder().getAbsoluteFile().toString();

        // Plugin startup logic. test

        ArrayList<Listener> events = new ArrayList<>();
        //list of events
        events.add(new Market()); events.add(new Resource()); events.add(new events()); events.add(new LootTable());
        events.add(new PlayerProfile()); events.add(new Drill()); events.add(new StructureEvent());

        for (Listener l : events) {
            getServer().getPluginManager().registerEvents(l, this);
        }

        //playerprofiles loading data
        PlayerProfile.init();

        //loottable loading data
        LootTable.init();

        //resource loading data
        Resource.init();

        //market things
        Market.init();

        new BukkitRunnable() {
            @Override
            public void run() {
                Structure.loadAndPlaceLongGrass();
            }
        }.runTaskLater(plugin, 5l);
        //load long grass blocks

        //commands
        new Command("addresource", new addResourceCommand());
        new Command("addvalue", new addValueCommand(), new numTabCompleter());
        new Command("createmarket", new createMarketCommand());
        new Command("removemarket", new removeMarketCommand());
        new Command("createloottable", new createLootTableCommand());
        new Command("checkloottables", new checkLootTableCommand());
        new Command("getloot", new getLootCommand());
        new Command("getdrill", new getDrillCommand(), new getDrillTabCompleter());
        new Command("addmaterial", new addMaterialCommand(), new materialTabCompleter());

        //ticker all
        BukkitScheduler scheduler = getServer().getScheduler();
        scheduler.scheduleSyncRepeatingTask(this, () -> {
            Market.tickTrades();
        }, 0L, 20L);
        for (Player p : Bukkit.getOnlinePlayers()){
            p.sendMessage(format("&aUpdating complete"));
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        for (Player p : Bukkit.getOnlinePlayers()){
            p.sendMessage(format("&aUpdating server..."));
            p.sendMessage(format("&aAll drills, energy sources and other structures need to be replaced"));
        }

        api.deleteHolograms();

        //destroy all drills in the world
        Structure.destroyAll();

        //save long grass blocks
        Structure.saveLongGrass();

        //playerprofiles save
        PlayerProfile.save();

        //lootable save
        LootTable.save();

        //resource save
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
