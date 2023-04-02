package aminecraftplugin.aminecraftplugin.drill;

import aminecraftplugin.aminecraftplugin.drill.loot.Resource;
import aminecraftplugin.aminecraftplugin.drill.loot.resourceCategory;
import aminecraftplugin.aminecraftplugin.player.PlayerProfile;
import net.minecraft.nbt.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_19_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BundleMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.stream.Collectors;

import static aminecraftplugin.aminecraftplugin.drill.ResourceSorters.resourceComparators;
import static aminecraftplugin.aminecraftplugin.drill.ResourceSorters.returnSortedList;
import static aminecraftplugin.aminecraftplugin.drill.loot.Resource.categories;
import static aminecraftplugin.aminecraftplugin.drill.loot.Resource.getResourceFromKey;
import static aminecraftplugin.aminecraftplugin.player.PlayerProfile.getPlayerProfile;
import static aminecraftplugin.aminecraftplugin.utils.ChatUtils.format;
import static aminecraftplugin.aminecraftplugin.utils.Compress.returnCompressed;

public class Backpack implements Listener {


    //key int is item key/ID
    //value is the amount of the item the player has in Kg
    private final String bundleName = format("&fBackpack");
    private double space;
    private HashMap<Integer, Double> backpack;
    private UUID owner;


    public Backpack() {
        this.space = 5.0;
        this.backpack = new HashMap<>();
    }



    public Backpack(HashMap<Integer, Double> backpack, double space, UUID owner){
        this.backpack = backpack;
        this.space = space;
        this.owner = owner;
    }

    public double getItemAmountInBackpack(int key) {
        if (!backpack.containsKey(key)) return 0;
        else {
            return backpack.get(key);
        }
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

        this.updateBackpackInPlayerInventory();
        //returns the amount of items it was unable to put in the backpack
        return leftOver;
    }

    public void removeResource(int key, double removeAmount) {
        if (backpack.containsKey(key)) {
            double oldAmount = backpack.get(key);
            double newAmount = oldAmount - removeAmount;
            if (newAmount <= 0) backpack.remove(key);
            else backpack.put(key, newAmount);
            this.updateBackpackInPlayerInventory();
        }
    }

    public void open(Player p, UUID owner){
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(owner);
        Inventory inventory = Bukkit.createInventory(null, 54, offlinePlayer.getName() + "'s backpack");
        PlayerProfile playerProfile = getPlayerProfile(p);
        int sortingIndex = playerProfile.getSortingIndex();
        resourceCategory filterCategory = playerProfile.getFilterCategory();

        List<Map.Entry<Integer, Double>> resourceList = this.getBackpack().entrySet().stream().filter(entry -> categories.get(filterCategory).contains(entry.getKey())).collect(Collectors.toList());
        Collections.sort(resourceList, resourceComparators[sortingIndex]);

        int index = 0;
        for (Map.Entry<Integer, Double> entry : resourceList){
            int key = entry.getKey();
            double kg = entry.getValue();
            Resource resource = getResourceFromKey(key);
            ItemStack item = resource.getItemStack();
            ItemMeta meta = item.getItemMeta();
            ArrayList<String> lore = new ArrayList<>();
            lore.add(format("&7Weight: &f" + returnCompressed(kg, 2) + "kg"));
            double totalValue = kg * resource.getValue();
            lore.add(format("&7Estimated price: &f" + returnCompressed(totalValue, 2)));
            meta.setLore(lore);
            item.setItemMeta(meta);
            inventory.setItem(index, item);
            index++;
        }
        p.openInventory(inventory);
    }

    private ArrayList<ItemStack> getBackPackItems(){
        ArrayList<ItemStack> items = new ArrayList<>();
        for (int resourceID : this.getBackpack().keySet()){
            Resource resource = getResourceFromKey(resourceID);
            if (resource == null) continue;
            ItemStack item = resource.getItemStack();
            items.add(item);
        }
        return items;
    }

    public void updateBackpackInPlayerInventory(){
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(this.getOwner());
        if (!offlinePlayer.isOnline()) return;
        Player p = offlinePlayer.getPlayer();
        Inventory inventory = p.getInventory();

        ItemStack bundle = inventory.getItem(8);
        if (bundle == null || !bundle.getType().equals(Material.BUNDLE)){
            bundle = new ItemStack(Material.BUNDLE);
            ItemMeta meta = bundle.getItemMeta();;
            meta.setDisplayName(bundleName);
            bundle.setItemMeta(meta);
        }

        net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(bundle);
        NBTTagCompound nbt = nmsItem.u();
        if (nbt == null) nbt = new NBTTagCompound();
        nbt.a("CustomModelData", 1);
        nmsItem.c(nbt);
        bundle = CraftItemStack.asBukkitCopy(nmsItem);

        ItemMeta itemMeta = bundle.getItemMeta();

        itemMeta.setDisplayName(bundleName);

        //removes the ?/64 from the bundle lore
        itemMeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);

        ArrayList<String> lore = new ArrayList<>();
        double amount = this.getSpace() - this.getEmptySpace();
        lore.add(format("&7" + returnCompressed(amount, 2) + " / " + this.getSpace() + " kg"));
        itemMeta.setLore(lore);

        BundleMeta bundleMeta = (BundleMeta) itemMeta;
        bundleMeta.setItems(this.getBackPackItems());
        bundle.setItemMeta(bundleMeta);

        inventory.setItem(8, bundle);
    }

    @EventHandler
    private void inventoryClickEvent(InventoryClickEvent e){
        if (e.getClickedInventory() == null) return;
        Player p = (Player) e.getWhoClicked();
        if (e.getClickedInventory().equals(p.getInventory())){
            if (e.getSlot() == 8 && e.getCurrentItem().getType().equals(Material.BUNDLE) && e.getCurrentItem().getItemMeta().getDisplayName().equals(bundleName)){
                e.setCancelled(true);
                PlayerProfile playerProfile = getPlayerProfile(p);
                playerProfile.getBackPack().open(p, p.getUniqueId());
            }
        }
    }

    @EventHandler
    private void rightClick(PlayerInteractEvent e){
        Player p = e.getPlayer();
        if (p.getInventory().getItemInMainHand().getType().equals(Material.BUNDLE)){
            ItemStack item = p.getInventory().getItemInMainHand();
            ItemMeta meta = item.getItemMeta();
            if (meta.getDisplayName().equals(bundleName)){
                e.setCancelled(true);
                PlayerProfile playerProfile = getPlayerProfile(p);
                playerProfile.getBackPack().open(p, p.getUniqueId());
            }
        }
    }


    public double getEmptySpace() {
        double emptySpace = space;
        for (double itemAmount : backpack.values()) {
            emptySpace -= itemAmount;
        }
        return emptySpace;
    }


    public double getSpace() {
        return space;
    }

    public HashMap<Integer, Double> getBackpack() {
        return backpack;
    }

    public UUID getOwner() {
        return owner;
    }
}
