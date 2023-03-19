package aminecraftplugin.aminecraftplugin.drill;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static aminecraftplugin.aminecraftplugin.Main.loadFile;
import static aminecraftplugin.aminecraftplugin.Main.saveFile;

public class Resource {


    //todo: make GUI for adding new resources



    public static HashMap<Integer, Resource> resources;


    public static YamlConfiguration resourceFile;

    private ItemStack itemStack;
    private String name;
    private Double value;


    public Resource(ItemStack item, String name, Double value){
        this.itemStack = item;
        this.name = name;
        this.value = value;
    }


    public static void init(){
        try {
            resources = loadResources();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void save(){
        try {
            saveResources();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void openResourceGUI(Player p){

    }

    public static void saveResources() throws IOException {

        for (Map.Entry<Integer, Resource> set : resources.entrySet()) {

            int id = set.getKey();
            Resource resource = set.getValue();
            resourceFile.set(id + ".itemstack", resource.getItemStack());
            resourceFile.set(id + ".name", resource.getName());
            resourceFile.set(id + ".value", resource.getValue());

        }
        saveFile(resourceFile, "resources.yml");

    }

    public static HashMap<Integer, Resource> loadResources() throws IOException{

        resourceFile = loadFile("resources.yml");

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
