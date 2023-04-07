package aminecraftplugin.aminecraftplugin.drilling;

import aminecraftplugin.aminecraftplugin.drilling.resource.Resource;
import aminecraftplugin.aminecraftplugin.drilling.resource.resourceCategory;
import aminecraftplugin.aminecraftplugin.player.PlayerProfile;
import net.minecraft.nbt.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_19_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BundleMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.stream.Collectors;

import static aminecraftplugin.aminecraftplugin.drilling.resource.ResourceSorters.*;
import static aminecraftplugin.aminecraftplugin.drilling.resource.ResourceSorters.getFilterItem;
import static aminecraftplugin.aminecraftplugin.drilling.resource.Resource.categories;
import static aminecraftplugin.aminecraftplugin.drilling.resource.Resource.getResourceFromKey;
import static aminecraftplugin.aminecraftplugin.player.PlayerProfile.getPlayerProfile;
import static aminecraftplugin.aminecraftplugin.utils.ChatUtils.format;
import static aminecraftplugin.aminecraftplugin.utils.Compress.returnCompressed;
import static aminecraftplugin.aminecraftplugin.utils.defaultPageInventory.getDefaultScrollableInventory;

public class Backpack implements Listener {


    //key int is item key/ID
    //value is the amount of the item the player has in Kg
    private final String bundleName = format("&fBackpack");
    private double space;
    private HashMap<Integer, Double> backpack;
    private UUID owner;
    private ArrayList<Inventory> pages = new ArrayList<>();


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
        this.updateAllPlages(this.getOwner());
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
            this.updateAllPlages(this.getOwner());
        }
    }

    public Inventory createNewInventory(int page, int maxPages){
        PlayerProfile owner = getPlayerProfile(this.getOwner());
        String inventoryName = Bukkit.getOfflinePlayer(this.getOwner()).getName() + "'s backpack page " + page + "/" + maxPages;
        Inventory inventory = getDefaultScrollableInventory(inventoryName, false);
        inventory.setItem(51, getSortItem(owner.getSortingIndex()));
        inventory.setItem(52, getFilterItem(owner.getFilterCategory()));
        return inventory;
    }

    public int getMaxAmountOfPages(resourceCategory filterCategory){
        List<Map.Entry<Integer, Double>> resourceList = this.getBackpack().entrySet().stream().filter(entry -> categories.get(filterCategory).contains(entry.getKey())).collect(Collectors.toList());
        if (resourceList.isEmpty()) return 1;
        return (int) Math.ceil(Double.valueOf(Integer.valueOf(resourceList.size())) / 36);
    }

    public void updateAllPlages(UUID owner) {
        PlayerProfile playerProfile = getPlayerProfile(owner);
        resourceCategory filterCategory = playerProfile.getFilterCategory();
        int sortingIndex = playerProfile.getSortingIndex();
        int maxAmountOfPages = getMaxAmountOfPages(filterCategory);
        List<Map.Entry<Integer, Double>> resourceList = playerProfile.getBackPack().getBackpack().entrySet().stream().filter(entry -> categories.get(filterCategory).contains(entry.getKey())).collect(Collectors.toList());
        Collections.sort(resourceList, resourceComparators[sortingIndex]);


        ItemStack sorter = getSortItem(sortingIndex);
        ItemStack filter = getFilterItem(filterCategory);

        ArrayList<Inventory> inventories = this.getPages();
        int amountOfPages = inventories.size();

        for (int i = 0; i < maxAmountOfPages - amountOfPages; i++) {
            inventories.add(createNewInventory(amountOfPages + i + 1, maxAmountOfPages));
        }
        for (int i = amountOfPages - maxAmountOfPages; i > 0; i--) {
            inventories.remove(amountOfPages - i + 1);
            amountOfPages--;
        }

        int inventoryIndex = 0;
        int slotIndex = 0;

        for (Inventory inventory : inventories) {
            for (int i = 0; i < 36; i++) {
                inventory.setItem(i, null);
            }
            inventory.setItem(51, sorter);
            inventory.setItem(52, filter);
        }

        for (Map.Entry<Integer, Double> entry : resourceList) {
            if (slotIndex >= 36) {
                slotIndex = 0;
                inventoryIndex++;
            }
            Resource resource = getResourceFromKey(entry.getKey());
            double weight = entry.getValue();
            ItemStack item = resource.getItemStack();
            ItemMeta itemMeta = item.getItemMeta();
            itemMeta.setDisplayName(format("&f" + itemMeta.getDisplayName()));
            ArrayList<String> lore = new ArrayList<>();
            lore.add(format("&7Weight: &f" + returnCompressed(weight, 2) + "kg"));
            lore.add(format("&7Estimated value: &f" + returnCompressed(weight * resource.getValue(), 2)));
            itemMeta.setLore(lore);
            item.setItemMeta(itemMeta);
            inventories.get(inventoryIndex).setItem(slotIndex, item);
            slotIndex++;
        }


    }

    public void open(Player p, int page){
        Inventory inventory = this.getPages().get(page - 1);
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
        if (e.getView() == null) return;
        String name = e.getView().getTitle();
        Player p = (Player) e.getWhoClicked();
        if (e.getClickedInventory().equals(p.getInventory())){
            if (!p.getGameMode().equals(GameMode.CREATIVE) && e.getSlot() == 8 && e.getCurrentItem().getType().equals(Material.BUNDLE) && e.getCurrentItem().getItemMeta().getDisplayName().equals(bundleName)){
                int page = Integer.parseInt(name.split("page ")[1].split("/")[0]);
                e.setCancelled(true);
                PlayerProfile playerProfile = getPlayerProfile(p);
                playerProfile.getBackPack().open(p, page);
            }
            return;
        }
        if (name.contains("backpack")) {
            e.setCancelled(true);
            String playerName = name.split(" backpack")[0].split("'")[0];
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
            PlayerProfile playerProfile = getPlayerProfile(offlinePlayer.getUniqueId());
            int sortingIndex = playerProfile.getSortingIndex();
            resourceCategory filterCategory = playerProfile.getFilterCategory();

            Backpack backpack = playerProfile.getBackPack();
            int maxAmountOfPages = backpack.getMaxAmountOfPages(filterCategory);
            int currentPage = Integer.parseInt(name.split("page ")[1].split("/")[0]);
            switch (e.getRawSlot()) {
                case 45:
                    if (currentPage > 1) {
                        backpack.open(p, currentPage - 1);
                    }
                    break;
                case 53:
                    if (currentPage + 1 <= maxAmountOfPages) {
                        backpack.open(p, currentPage + 1);
                    }
                    break;
                case 51:
                    int sortingIndex1 = sortingIndex;
                    if (e.getClick().equals(ClickType.LEFT)) {
                        if (sortingIndex1 == resourceComparators.length - 1) {
                            sortingIndex1 = 0;
                        } else {
                            sortingIndex1++;
                        }
                    } else if (e.getClick().equals(ClickType.RIGHT)) {
                        if (sortingIndex1 == 0) {
                            sortingIndex1 = resourceComparators.length - 1;
                        } else {
                            sortingIndex1--;
                        }
                    }
                    playerProfile.setSortingIndex(sortingIndex1);
                    backpack.updateAllPlages(offlinePlayer.getUniqueId());
                    backpack.open(p, 1);
                    break;
                case 52:
                    int listIndex = 0;
                    int index = 0;
                    for (resourceCategory resourceCategory : resourceCategory.values()) {
                        if (filterCategory.equals(resourceCategory)) {
                            listIndex = index;
                        }
                        index++;
                    }
                    if (e.getClick().equals(ClickType.LEFT)) {
                        if (listIndex == resourceCategory.values().length - 1) {
                            listIndex = 0;
                        } else {
                            listIndex++;
                        }
                    } else if (e.getClick().equals(ClickType.RIGHT)) {
                        if (listIndex == 0) {
                            listIndex = resourceCategory.values().length - 1;
                        } else {
                            listIndex--;
                        }
                    }
                    playerProfile.setFilterCategory(resourceCategory.values()[listIndex]);
                    backpack.updateAllPlages(offlinePlayer.getUniqueId());
                    backpack.open(p, 1);
                    break;
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
                playerProfile.getBackPack().open(p, 1);
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



    public ArrayList<Inventory> getPages() {
        return pages;
    }

    public void setPages(ArrayList<Inventory> pages) {
        this.pages = pages;
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
