package aminecraftplugin.aminecraftplugin.sideSkills.adventuring;

import aminecraftplugin.aminecraftplugin.sideSkills.SideSkill;
import it.unimi.dsi.fastutil.Hash;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.HashMap;

public class AdventureSkill extends SideSkill implements Listener {

    //global variables
    public static HashMap<Integer, AdventureObject> adventureObjects = new HashMap<>();

    private ArrayList<Integer> discoveredAdventureObjects;

    public AdventureSkill() {
        discoveredAdventureObjects = new ArrayList<>();
    }
    //when loaded from file
    public AdventureSkill(ArrayList<Integer> discoveredAdventureObjects) {
        this.discoveredAdventureObjects = discoveredAdventureObjects;
    }

    @EventHandler
    public void adventureObjectClick(PlayerInteractEvent e) {

    }




    public static void loadAdventureObjects() {

    }
    public static void saveAdventureObjects() {

    }

}
