package aminecraftplugin.aminecraftplugin.drill;

import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class Resource {


    public static HashMap<Integer, Resource> resources;

    private ItemStack itemStack;
    private String name;
    private Float value;


    public static void init(){
        resources = loadResources();
    }

    public Resource(){

    }


    public void saveResources(){

    }

    public static HashMap<Integer, Resource> loadResources(){
        return new HashMap<>();
    }
}
