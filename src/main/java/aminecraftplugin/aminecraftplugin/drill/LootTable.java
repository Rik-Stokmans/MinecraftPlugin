package aminecraftplugin.aminecraftplugin.drill;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class LootTable {


    //todo: make GUI to create new loottables, save and loading

    //resource weight factor
    public static HashMap<Integer, LootTable> IDs = new HashMap<>();

    private HashMap<Resource, Float> table = new HashMap<>();
    private Location location;
    private String name;
    private int ID;


    public LootTable(String name, Location location){
        this.name = name;
        this.location = location;
        this.ID = findNewID();
        IDs.put(this.ID, this);
    }

    private static int findNewID(){
        int index = 1;
        while(true){
            if (IDs.containsKey(index)){
                index++;
                break;
            }
            index++;
        }
        return index;
    }

    public void openLoottableMenu(Player p){

    }

    public static void openSelectLoottableMenu(Player p){

    }


}
