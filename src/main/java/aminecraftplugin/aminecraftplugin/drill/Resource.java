package aminecraftplugin.aminecraftplugin.drill;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Resource {


    public static HashMap<Integer, Resource> resources;


    YamlConfiguration resourceFile;

    private ItemStack itemStack;
    private String name;
    private Double value;


    public static void init(){
        resources = loadResources();
    }

    public Resource(){

    }


    public void saveResources(){

        for (Map.Entry<Integer, Resource> set : resources.entrySet()) {

            int id = set.getKey();
            Resource resource = set.getValue();
            resourceFile.set(id + ".itemstack", resource.getItemStack());
            resourceFile.set(id + ".name", resource.getName());
            resourceFile.set(id + ".value", resource.getValue());

        }
        //resourceFile.save(new File(), "");

    }

    public static HashMap<Integer, Resource> loadResources(){
        return new HashMap<>();
    }


    public ItemStack getItemStack() {
        return itemStack;
    }

    public String getName() {
        return name;
    }

    public Double getValue() {
        return value;
    }
}
