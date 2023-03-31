package aminecraftplugin.aminecraftplugin.drill;

import aminecraftplugin.aminecraftplugin.drill.loot.Resource;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.awt.geom.RectangularShape;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static aminecraftplugin.aminecraftplugin.drill.loot.Resource.getResourceFromKey;
import static aminecraftplugin.aminecraftplugin.utils.ChatUtils.format;

public class Backpack {


    //key int is item key/ID
    //value is the amount of the item the player has in Kg
    private double space;
    private HashMap<Integer, Double> backpack;

    public Backpack() {
        this.space = 5.0;
        this.backpack = new HashMap<>();
    }

    public Backpack(HashMap<Integer, Double> backpack, double space){
        this.backpack = backpack;
        this.space = space;
    }

    public double getItemAmountInBackpack(int key) {
        if (!backpack.containsKey(key)) return 0;
        else {
            return backpack.get(key);
        }
    }

    public void removeItemFromBackpack(int key, double removeAmount) {
        if (backpack.containsKey(key)) {
            double oldAmount = backpack.get(key);
            double newAmount = oldAmount - removeAmount;
            if (newAmount <= 0) backpack.remove(key);
            else backpack.put(key, newAmount);

        }
    }

    public void open(Player p, UUID owner){
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(owner);
        Inventory inventory = Bukkit.createInventory(null, 54, offlinePlayer.getName() + "'s backpack");
        int index = 0;
        for (Map.Entry<Integer, Double> entry : this.getBackpack().entrySet()){
            int key = entry.getKey();
            double kg = entry.getValue();
            Resource resource = getResourceFromKey(key);
            ItemStack item = resource.getItemStack();
            ItemMeta meta = item.getItemMeta();
            ArrayList<String> lore = new ArrayList<>();
            lore.add(format("&7amount: &f" + kg));
            meta.setLore(lore);
            inventory.setItem(index, item);
            index++;
        }
        p.openInventory(inventory)
    }

    public double addResource(int key, double amount) {
        double leftOver = 0.0;

        double emptySpace = getEmptySpace();

        //not enough room in backpack
        if (emptySpace < amount) {
            leftOver = amount - emptySpace;
            if (backpack.containsKey(key)) backpack.put(key, backpack.get(key) + emptySpace);
            else backpack.put(key, emptySpace);
        } //has enough room
        else {
            if (backpack.containsKey(key)) backpack.put(key, backpack.get(key) + amount);
            else backpack.put(key, amount);
        }
        //returns the amount of items it was unable to put in the backpack
        return leftOver;
    }

    public double getEmptySpace() {
        double emptySpace = space;
        for (double itemAmount : backpack.values()) {
            emptySpace -= itemAmount;
        }
        return emptySpace;
    }


    public HashMap<Integer, Double> getBackpack() {
        return backpack;
    }
}
