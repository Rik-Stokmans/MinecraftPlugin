package aminecraftplugin.aminecraftplugin.drill;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Drill {

    private Location location;


    public Drill(){

    }


    //todo: GUI to display loot
    //oven/hopper GUI for collecting
    public void drill(Player p){
        Loot loot = new Loot(this.getLocation());
        for (ItemStack itemStack : loot.getLoot()){
            p.getInventory().addItem(itemStack);
        }
    }

    public Location getLocation() {
        return location;
    }
}
