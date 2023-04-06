package aminecraftplugin.aminecraftplugin.commands;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static aminecraftplugin.aminecraftplugin.Main.loadFile;
import static aminecraftplugin.aminecraftplugin.Main.saveFile;

public class addSpawnCommand implements CommandExecutor {

    public static HashMap<String, Location> spawnLocations = new HashMap<>();

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (commandSender instanceof Player){
            Player p = (Player) commandSender;
            Location loc = p.getLocation();
            String name = strings[0];
            spawnLocations.put(name, loc);
            return true;
        }
        return false;
    }

    public static void init(){
        try {
            spawnLocations = loadSpawns();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void save(){
        try {
            saveSpawns();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static HashMap<String, Location> loadSpawns() throws IOException {
        HashMap<String, Location> locationHashMap = new HashMap<>();
        YamlConfiguration spawnFile = loadFile("spawnlocations.yml");
        if (!spawnFile.contains("data")) return new HashMap<>();
        spawnFile.getConfigurationSection("data").getKeys(false).forEach(key -> {
            Location location = spawnFile.getLocation("data." + key);
            locationHashMap.put(key, location);
        });
        return locationHashMap;
    }

    public static void saveSpawns() throws IOException {
        YamlConfiguration spawnFile = new YamlConfiguration();
        for (Map.Entry<String, Location> entry : spawnLocations.entrySet()){
            String name = entry.getKey();
            Location spawnLoc = entry.getValue();
            spawnFile.set("data." + name, spawnLoc);
        }
        saveFile(spawnFile, "spawnlocations.yml");
    }

}
