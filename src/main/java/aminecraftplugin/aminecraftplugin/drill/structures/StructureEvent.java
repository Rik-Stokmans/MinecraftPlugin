package aminecraftplugin.aminecraftplugin.drill.structures;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.UUID;

import static aminecraftplugin.aminecraftplugin.Main.plugin;
import static aminecraftplugin.aminecraftplugin.drill.structures.Structure.scheduleRemoveStructures;
import static aminecraftplugin.aminecraftplugin.drill.structures.Structure.structures;
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
                    ItemStack structureRemoved = structure.destroy(false);
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

}
