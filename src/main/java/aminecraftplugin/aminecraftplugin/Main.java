package aminecraftplugin.aminecraftplugin;

import aminecraftplugin.aminecraftplugin.commands.*;
import aminecraftplugin.aminecraftplugin.commands.tabcompleters.*;
import aminecraftplugin.aminecraftplugin.drilling.Backpack;
import aminecraftplugin.aminecraftplugin.drilling.drill.Drill;
import aminecraftplugin.aminecraftplugin.drilling.energy.EnergySource;
import aminecraftplugin.aminecraftplugin.drilling.loot.LootTable;
import aminecraftplugin.aminecraftplugin.drilling.resource.Resource;
import aminecraftplugin.aminecraftplugin.drilling.structures.Structure;
import aminecraftplugin.aminecraftplugin.drilling.structures.StructureEvent;
import aminecraftplugin.aminecraftplugin.market.Market;
import aminecraftplugin.aminecraftplugin.player.PlayerProfile;
import aminecraftplugin.aminecraftplugin.shop.Shop;
import aminecraftplugin.aminecraftplugin.sideSkills.mining.MiningSkill;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import me.filoghost.holographicdisplays.api.HolographicDisplaysAPI;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import static aminecraftplugin.aminecraftplugin.utils.ChatUtils.format;

public final class Main extends JavaPlugin {

    private static String path;
    public static Plugin plugin;
    public static boolean useHolographicDisplays;
    public static HolographicDisplaysAPI api;
    public static ProtocolManager protocolManager;
    public static World mainWorld;


    @Override
    public void onEnable() {

        plugin = this;

        //get main world
        File f = new File("server.properties");
        String world = getString("level-name", f);
        mainWorld = Bukkit.getWorld(world);


        //holographicdisplays
        useHolographicDisplays = Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays");
        if (useHolographicDisplays) {
            api = HolographicDisplaysAPI.get(this);
        }

        //protocollib
        protocolManager = ProtocolLibrary.getProtocolManager();

        //file path
        path = getDataFolder().getAbsoluteFile().toString();

        //events
        getServer().getPluginManager().registerEvents(new events(new Drill(), new EnergySource()), this);

        //resource loading data
        Resource.init();

        //playerprofiles loading data
        PlayerProfile.init();

        //loottable loading data
        LootTable.init();

        //market things
        Market.init();

        //skills things
        MiningSkill.init();

        //load spawn locations
        addSpawnCommand.init();

        //load shops
        Shop.init();

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
        new Command("getdrill", new getDrillCommand(), new getDrillTabCompleter());
        new Command("addmaterial", new addMaterialCommand(), new materialTabCompleter());
        new Command("addmoney", new addMoneyCommand(), new numTabCompleter());
        new Command("backpack", new openBackPackCommand(), new nullTabCompleter());
        new Command("miningResourceMenu", new miningResourceMenu(), new nullTabCompleter());
        new Command("setspawn", new addSpawnCommand(), new spawnTabCompleter());
        new Command("removespawn", new removeSpawnCommand(), new spawnTabCompleter());
        new Command("createshop", new createShopCommand(), new shopTabCompleter());
        new Command("removeshop", new removeShopCommand(), new shopTabCompleter());

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

        //market save
        Market.saveMarketsToFile();
        Market.closePlayerInventories();

        //spawn locations save
        addSpawnCommand.save();

        //save shops
        Shop.save();

    }


    //save and load files

    public static void saveFile(YamlConfiguration file, String s) throws IOException {
        file.save(new File(path,s));
    }

    public static YamlConfiguration loadFile(String s) throws IOException {
        YamlConfiguration file = YamlConfiguration.loadConfiguration(new File(path, s));
        return file;
    }

    private static String getString(String s, File f)
    {
        Properties pr = new Properties();
        try
        {
            FileInputStream in = new FileInputStream(f);
            pr.load(in);
            String string = pr.getProperty(s);
            return string;
        }
        catch (IOException e)
        {

        }
        return "";
    }

}
