package aminecraftplugin.aminecraftplugin.sideSkills.mining;

import aminecraftplugin.aminecraftplugin.sideSkills.SideSkill;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;

import static aminecraftplugin.aminecraftplugin.Main.mainWorld;
import static aminecraftplugin.aminecraftplugin.Main.plugin;

public class MiningSkill extends SideSkill {

    public static void init() {
        ores.put(1, new Ore(new ItemStack(Material.DIAMOND), Material.DIAMOND_ORE, Material.STONE, 1, 1)); //TEMP
        ores.put(2, new Ore(new ItemStack(Material.EMERALD), Material.EMERALD_ORE, Material.STONE, 3, 2)); //TEMP
        Location diamondLocation = new Location(mainWorld, 13.0, 135.0, -44.0); //TEMP
        oreLocations.put(diamondLocation, new OrePlacement(diamondLocation, 1, 50)); //TEMP
        Location emeraldLocation = new Location(mainWorld, 13.0, 136.0, -44.0); //TEMP
        oreLocations.put(emeraldLocation, new OrePlacement(emeraldLocation, 2, 50)); //TEMP
    }

    //global hashmaps
    public static HashMap<Integer, Ore> ores = new HashMap<>();
    public static HashMap<Location, OrePlacement> oreLocations = new HashMap<>();
    public static ArrayList<Player> playerCantMine = new ArrayList<>();
    //global variables
    private static final int miningDelay = 20;


    public static void miningSkillPlayerInteractEvent(PlayerInteractEvent e) {
        if (!(e.getAction().isLeftClick() && e.getClickedBlock() != null)) return;
        if (!oreLocations.containsKey(e.getClickedBlock().getLocation())) return;
        if (playerCantMine.contains(e.getPlayer())) return;
        Block minedBlock = e.getClickedBlock();
        OrePlacement orePlacement = oreLocations.get(minedBlock.getLocation());
        if (!orePlacement.isActive()) return;

        Player p = e.getPlayer();

        p.getInventory().addItem(orePlacement.removeOrePlacement());

        if (!miningSkills.containsKey(p.getUniqueId())) miningSkills.put(p.getUniqueId(), new MiningSkill());
        miningSkills.get(p.getUniqueId()).addXp(getOreFromID(orePlacement.getID()).getXpReward(), p);

        playerCantMine.add(p);
        BukkitTask delayEnforcer = new BukkitRunnable() {
            @Override
            public void run() {
                if (playerCantMine.contains(p)) playerCantMine.remove(p);
            }
        }.runTaskLater(plugin, miningDelay);

        BukkitTask blockSpawner = new BukkitRunnable() {
            @Override
            public void run() {
                orePlacement.createOrePlacement();
            }
        }.runTaskLater(plugin, orePlacement.getRespawnTime());
    }


    //method to add an ore to the ores list
    public static void addOre(Ore ore) {
        ores.put(ore.getID(), ore);
    }
    //method to remove an ore from the ores list
    public void removeOre(int ID) {
        if (!ores.containsKey(ID)) return;

        ores.remove(ID);

        ArrayList<Location> placementsToBeRemoved = new ArrayList<>();
        for (Location loc : oreLocations.keySet()) if (oreLocations.get(loc).getID() == ID) placementsToBeRemoved.add(loc);
        for (Location loc : placementsToBeRemoved) if (oreLocations.containsValue(loc)) oreLocations.remove(loc);
    }
    //method to get the Ore from an id
    @Nullable
    public static Ore getOreFromID(int ID) {
        if (ores.containsKey(ID)) return ores.get(ID);
        else return null;
    }


}
