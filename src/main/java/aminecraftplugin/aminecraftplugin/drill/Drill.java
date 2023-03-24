package aminecraftplugin.aminecraftplugin.drill;

import aminecraftplugin.aminecraftplugin.drill.loot.LootFinder;
import aminecraftplugin.aminecraftplugin.drill.loot.Resource;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Map;

public class Drill {

    private Location location;


    public Drill(Location location){
        this.location = location;
    }


    //todo: GUI to display loot
    //oven/hopper GUI for collecting
    public void drill(Player p){
        LootFinder loot = new LootFinder(this.getLocation());
        for (Map.Entry<Resource, Double> resource : loot.findLoot(p).entrySet()){
            //todo: add to backpack
            p.sendMessage(resource.getKey().getName() + ": " + resource.getValue() + "kg");
        }
    }

    public Location getLocation() {
        return location;
    }
}
