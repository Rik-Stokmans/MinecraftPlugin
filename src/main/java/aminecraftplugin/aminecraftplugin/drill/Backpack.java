package aminecraftplugin.aminecraftplugin.drill;

import it.unimi.dsi.fastutil.Hash;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class Backpack {

    public static HashMap<Player, Backpack> backpacks = new HashMap<>();

    //key int is item key/ID
    //value is the amount of the item the player has in Kg
    private HashMap<Integer, Double> backpack;
    public Backpack() {

    }

}
