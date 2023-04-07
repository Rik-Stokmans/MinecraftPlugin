package aminecraftplugin.aminecraftplugin.drilling.structures;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.UUID;

import static aminecraftplugin.aminecraftplugin.Main.plugin;
import static aminecraftplugin.aminecraftplugin.drilling.structures.Structure.scheduleRemoveStructures;
import static aminecraftplugin.aminecraftplugin.drilling.structures.Structure.structures;
import static aminecraftplugin.aminecraftplugin.player.PlayerProfile.playerProfiles;

public class StructureEvent implements Listener {

    @EventHandler
    private void quitEvent(PlayerQuitEvent e){
        Player p = e.getPlayer();
        UUID uuid = p.getUniqueId();
        BukkitTask removeStructure = new BukkitRunnable() {
            @Override
            public void run() {
                for (Structure structure : structures.get(uuid)) {
                    ItemStack structureRemoved = structure.destroy(uuid, false);
                    playerProfiles.get(uuid).addOfflineItem(structureRemoved);
                }
            }
        }.runTaskLater(plugin, 300 * 20l);

        scheduleRemoveStructures.put(uuid,removeStructure.getTaskId());

    }

    @EventHandler
    private void joinEvent(PlayerJoinEvent e){
        Player p = e.getPlayer();
        UUID uuid = p.getUniqueId();
        if (scheduleRemoveStructures.containsKey(uuid)){
            Bukkit.getScheduler().cancelTask(scheduleRemoveStructures.get(uuid));
            scheduleRemoveStructures.remove(uuid);
        }
        ArrayList<ItemStack> offlineItems = playerProfiles.get(uuid).getOfflineItems();
        if (!offlineItems.isEmpty()){
            for (ItemStack item : offlineItems){
                p.getInventory().addItem(item);
            }
            playerProfiles.get(uuid).setOfflineItems(new ArrayList<>());
        }
    }


    @EventHandler
    public void structureRightClick(PlayerInteractEvent e){
        if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
            Player p = e.getPlayer();
            Location loc = e.getClickedBlock().getLocation();
            if (structures.containsKey(p.getUniqueId())) {
                for (aminecraftplugin.aminecraftplugin.drilling.structures.Structure structure : structures.get(p.getUniqueId())) {
                    for (Location loc1 : structure.getLocations()) {
                        if (loc.equals(loc1)) {
                            e.setCancelled(true);
                            structure.openStructureMenu(p, 1);
                        }
                    }
                }
            }
        }
    }

}
