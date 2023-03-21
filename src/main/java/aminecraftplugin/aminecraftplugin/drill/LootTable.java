package aminecraftplugin.aminecraftplugin.drill;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;

import static aminecraftplugin.aminecraftplugin.utils.ChatUtils.format;

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
        Inventory selectInventory = Bukkit.createInventory(null, 54, "Select Loot Table");


        ItemStack leftArrow = new ItemStack(Material.ARROW);
        ItemMeta meta = leftArrow.getItemMeta();
        meta.setDisplayName("Previous page");
        leftArrow.setItemMeta(meta);

        ItemStack rightArrow = new ItemStack(Material.ARROW);
        ItemMeta meta2 = rightArrow.getItemMeta();
        meta2.setDisplayName("Next page");
        rightArrow.setItemMeta(meta2);

        ItemStack grayGlass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta3 = grayGlass.getItemMeta();
        meta3.setDisplayName(format("&7"));
        grayGlass.setItemMeta(meta3);


        selectInventory.setItem(45, leftArrow);
    }


}
