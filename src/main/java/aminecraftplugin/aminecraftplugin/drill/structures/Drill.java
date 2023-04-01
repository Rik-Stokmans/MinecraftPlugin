package aminecraftplugin.aminecraftplugin.drill.structures;

import aminecraftplugin.aminecraftplugin.drill.loot.LootFinder;
import aminecraftplugin.aminecraftplugin.drill.loot.Resource;
import aminecraftplugin.aminecraftplugin.drill.loot.resourceCategory;
import aminecraftplugin.aminecraftplugin.player.PlayerProfile;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import me.filoghost.holographicdisplays.api.hologram.Hologram;
import me.filoghost.holographicdisplays.api.hologram.line.HologramLine;
import me.filoghost.holographicdisplays.api.hologram.line.TextHologramLine;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.structure.Mirror;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.craftbukkit.v1_19_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.structure.Structure;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static aminecraftplugin.aminecraftplugin.Main.*;
import static aminecraftplugin.aminecraftplugin.drill.ResourceSorters.*;
import static aminecraftplugin.aminecraftplugin.drill.loot.Resource.categories;
import static aminecraftplugin.aminecraftplugin.drill.loot.Resource.getResourceFromKey;
import static aminecraftplugin.aminecraftplugin.drill.structures.DrillType.getDrillTypeFromName;
import static aminecraftplugin.aminecraftplugin.player.PlayerProfile.getPlayerProfile;
import static aminecraftplugin.aminecraftplugin.utils.ChatUtils.format;
import static aminecraftplugin.aminecraftplugin.utils.Direction.getCardinalDirection;
import static aminecraftplugin.aminecraftplugin.utils.Direction.getXandZ;
import static aminecraftplugin.aminecraftplugin.utils.RemoveHandItem.removeHandItem;
import static aminecraftplugin.aminecraftplugin.utils.defaultPageInventory.getDefaultScrollableInventory;

public class Drill implements Listener, aminecraftplugin.aminecraftplugin.drill.structures.Structure {

    public static HashMap<Player, Drill> openedDrillInventory = new HashMap<>();


    private OfflinePlayer owner;
    private Location location;
    private LootFinder lootFinder;
    private DrillType drillType;
    private ArrayList<Inventory> pages = new ArrayList<>();
    private int drillTier;
    private Hologram hologram;
    private Structure structure;
    private HashMap<Integer, Double> resources = new HashMap<>();
    private ArrayList<BlockState> destroyedBlocks = new ArrayList<>();
    private int packetKey;
    private boolean muted;


    private static DecimalFormat df = new DecimalFormat("#.##");


    public Drill(){
    }

    public Drill(Location location, OfflinePlayer owner, ItemStack drill){

        this.muted = false;
        this.location = location;
        this.owner = owner;

        net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(drill);
        NBTTagCompound nbt = nmsItem.u();
        String drillType = nbt.l("drilltype");
        int drillTier = nbt.h("drilltier");

        this.drillType = getDrillTypeFromName(drillType);
        this.drillTier = drillTier;

        aminecraftplugin.aminecraftplugin.drill.structures.Structure.addStructure(owner.getUniqueId(), this);

        //loot
        this.structure = aminecraftplugin.aminecraftplugin.drill.structures.Structure.getStructure(this.getDrillType().getNameFromDrillType());
        this.hologram = api.createHologram(this.getLocation().clone().add(0.5, this.getThisStructure().getSize().getY() + 0.5, 0.5));
        this.correctHologramPosition();
        this.lootFinder = new LootFinder(this.getLocation());
        this.scheduleLootFinding(this.getOwner());
        this.initStructureInventories();
    }

    private void clearHologram(){
        this.getHologram().getLines().clear();
    }
    private void correctHologramPosition(){
        this.getHologram().setPosition(this.getLocation().clone().add(0.5, this.getThisStructure().getSize().getY() + 0.2 + this.getHologram().getLines().getHeight(), 0.5));
    }

    private void scheduleLootFinding(OfflinePlayer p){

        int totalDuration = 25;
        double oneStoneDuration = (totalDuration / (3 * Math.pow(Math.log(Math.E + this.getDrillTier()), 0.5)));

        this.clearHologram();
        this.getHologram().getLines().appendText("Searching for resources");
        this.correctHologramPosition();
        LootFinder lootFinder = this.getLootFinder();
        Drill drill = this;

        if (!drill.isMuted()) {
            this.getLocation().getWorld().playSound(this.getLocation(), Sound.BLOCK_STONE_BREAK, 1, 1);
        }
        final int[] stage = {0};
        PacketContainer packetContainer = protocolManager.createPacket(PacketType.Play.Server.BLOCK_BREAK_ANIMATION);
        packetContainer.getBlockPositionModifier().write(0, new BlockPosition((int) this.getLocation().getX(), (int) (this.getLocation().getY() - 1), (int) this.getLocation().getZ()));
        this.setPacketKey(ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE));
        packetContainer.getIntegers().write(0, this.getPacketKey());


        new BukkitRunnable() {
            @Override
            public void run() {
                if (!drill.isMuted()) {
                    drill.getLocation().getWorld().playSound(drill.getLocation(), Sound.BLOCK_STONE_BREAK, 1, 1);
                }
                HologramLine hologramLine = drill.getHologram().getLines().get(0);
                TextHologramLine textHologramLine = (TextHologramLine) hologramLine;
                String currentLine = textHologramLine.getText();
                int dotCount = currentLine.length() - currentLine.replaceAll("\\.","").length();
                int newDotCount = dotCount + 1;
                if (newDotCount == 4) newDotCount = 0;
                String newLine = "Searching for resources";
                for (int i = 0; i < newDotCount; i++){
                    newLine += ".";
                }
                textHologramLine.setText(newLine);
                if (stage[0] == 10){
                    packetContainer.getIntegers().write(1, -1);
                    protocolManager.broadcastServerPacket(packetContainer);

                    //30% chance to find resource
                    if (ThreadLocalRandom.current().nextDouble(100.0) <= 70) {
                        scheduleLootFinding(p);
                        this.cancel();
                    } else {
                        int safeIndex = 0;
                        HashMap<Resource, Double> foundResources = lootFinder.findLoot(p);
                        while (foundResources.isEmpty()) {
                            foundResources = lootFinder.findLoot(p);
                            safeIndex++;
                            if (safeIndex > 5000) {
                                System.out.println("ERROR NO LOOT FOUND IN 5000 TRIES");
                                break;
                            }
                        }
                        if (foundResources.isEmpty()) {
                            scheduleLootFinding(p);
                        } else {
                            drill.scheduleLootMining(foundResources, p);
                        }
                        this.cancel();
                    }
                }
                packetContainer.getIntegers().write(1, stage[0]);
                protocolManager.broadcastServerPacket(packetContainer);
                stage[0]++;
            }
        }.runTaskTimer(plugin, (long) Math.ceil((oneStoneDuration / 11) * 20), (long) Math.ceil((oneStoneDuration / 11) * 20));
    }

    private void scheduleLootMining(HashMap<Resource, Double> resources, OfflinePlayer p){
        Drill drill = this;

        Double miningPerSecond = 0.01 * Math.pow(Math.log(Math.E + drill.getDrillTier()), 0.5);

        HashMap<Resource, Double> mined = new HashMap<>();
        for (Resource resource : resources.keySet()){
            mined.put(resource, 0.0);
        }

        this.clearHologram();
        this.getHologram().getLines().appendText("Mining resources:");
        for (Map.Entry<Resource, Double> entry : mined.entrySet()){
            Resource resource = entry.getKey();
            Double kgMined = entry.getValue();
            this.getHologram().getLines().appendText(" - " + resource.getName() + ": " + df.format(kgMined) + "Kg");
        }
        this.correctHologramPosition();

        PacketContainer packetContainer = protocolManager.createPacket(PacketType.Play.Server.BLOCK_BREAK_ANIMATION);
        packetContainer.getBlockPositionModifier().write(0, new BlockPosition((int) this.getLocation().getX(), (int) (this.getLocation().getY() - 1), (int) this.getLocation().getZ()));
        packetContainer.getIntegers().write(0, this.getPacketKey());
        long totalDelay = 0;
        for (Map.Entry<Resource, Double> entry : resources.entrySet()){
            Resource resource = entry.getKey();
            final double[] kgLeft = {entry.getValue()};

            this.getLocation().clone().add(0,-1,0).getBlock().setType(resource.getBlock());

            double totalSeconds = kgLeft[0] / miningPerSecond;
            final int[] stage = {0};

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!drill.isMuted()) {
                        drill.getLocation().getWorld().playSound(drill.getLocation(), Sound.BLOCK_STONE_BREAK, 1, 1);
                    }
                    double kgMined = miningPerSecond * (totalSeconds / 11);
                    kgLeft[0] -= kgMined;
                    if (kgMined > kgLeft[0]) {
                        kgMined = kgLeft[0];
                    }
                    mined.replace(resource, mined.get(resource) + kgMined);

                    int j = drill.getHologram().getLines().size();
                    for (Map.Entry<Resource, Double> entry2 : mined.entrySet()) {
                        Resource resource2 = entry2.getKey();
                        Double kgMined2 = entry2.getValue();
                        for (int i = 0; i < j; i++) {
                            HologramLine hologramLine = drill.getHologram().getLines().get(i);
                            if (hologramLine instanceof TextHologramLine) {
                                TextHologramLine textHologramLine = (TextHologramLine) hologramLine;
                                if (textHologramLine.getText().contains(resource2.getName())) {
                                    textHologramLine.setText(" - " + resource2.getName() + ": " + df.format(kgMined2) + "Kg");
                                }
                            }
                        }
                    }
                    drill.addResource(resource.getKey(), kgMined);
                    if (stage[0] == 10) {
                        packetContainer.getIntegers().write(1, -1);
                        protocolManager.broadcastServerPacket(packetContainer);
                        drill.scheduleLootFinding(p);
                        drill.getLocation().clone().add(0,-1,0).getBlock().setType(Material.STONE);
                        this.cancel();
                    }
                    packetContainer.getIntegers().write(1, stage[0]);
                    protocolManager.broadcastServerPacket(packetContainer);
                    stage[0]++;
                }
            }.runTaskTimer(plugin, (long) ((totalDelay + (totalSeconds / 11)) * 20), (long) (((totalSeconds / 11)) * 20));
            totalDelay += totalSeconds + 0.05;
        }

    }

    @Override
    public void place(Player p){

        Location drillLoc = this.getLocation();

        //facing direction
        String s = getCardinalDirection(p);
        ImmutablePair<Integer, Integer> pair = getXandZ(s);

        //get structure
        int length = (int) this.getThisStructure().getSize().getZ();
        int width = (int) this.getThisStructure().getSize().getX();

        //get all blocks to be destroyed
        ArrayList<Location> locations = aminecraftplugin.aminecraftplugin.drill.structures.Structure.getStructureSpace(drillLoc, this.getThisStructure(), pair);


        for (Location loc : locations){
            Block block = loc.getBlock();
            destroyedBlocks.add(block.getState());
            block.setType(Material.AIR);
        }

        this.getLocation().clone().add(0,-1,0).getBlock().setType(Material.STONE);


        //placing structure with correct rotation
        StructureRotation structureRotation = StructureRotation.NONE;
        if (pair.getValue() == -1){
            structureRotation = StructureRotation.NONE;
        } else if (pair.getValue() == 1){
            structureRotation = StructureRotation.CLOCKWISE_180;
        } else if (pair.getKey() == 1){
            structureRotation = StructureRotation.CLOCKWISE_90;
        } else if (pair.getKey() == -1){
            structureRotation = StructureRotation.COUNTERCLOCKWISE_90;
        }

        structure.place(this.getLocation().clone().add(
                        pair.getValue() * Math.ceil(width / 2) + pair.getKey() * Math.ceil(length / 2),
                        0,
                        -pair.getKey() * Math.ceil(width / 2) + pair.getValue() * Math.floor(length / 2)),
                true, structureRotation, Mirror.NONE, 0, 1, new Random());
    }



    @Override
    public ItemStack destroy(boolean offline){
        for (BlockState destroyedBlock : destroyedBlocks){
            destroyedBlock.update(true);
            if (destroyedBlock.getBlockData().getAsString().contains("half=")) {
                if (!offline) {
                    if (destroyedBlock.getBlockData().getAsString().contains("half=upper")) {
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                destroyedBlock.getBlock().setType(destroyedBlock.getType());
                                Location locUp = destroyedBlock.getLocation().clone().add(0, 1, 0);
                                BlockData blockData2 = Bukkit.createBlockData(destroyedBlock.getType(), "[half=lower]");
                                locUp.getBlock().setBlockData(blockData2);
                            }
                        }.runTaskLater(plugin, 1l);
                    } else if (destroyedBlock.getBlockData().getAsString().contains("half=lower")) {
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                destroyedBlock.getBlock().setType(destroyedBlock.getType());
                                Location locUp = destroyedBlock.getLocation().clone().add(0, 1, 0);
                                BlockData blockData2 = Bukkit.createBlockData(destroyedBlock.getType(), "[half=upper]");
                                locUp.getBlock().setBlockData(blockData2);
                            }
                        }.runTaskLater(plugin, 1l);
                    }
                } else {
                    savedBlocks.put(destroyedBlock.getLocation(), destroyedBlock.getBlockData());
                }

            }

        }
        PacketContainer packetContainer = protocolManager.createPacket(PacketType.Play.Server.BLOCK_BREAK_ANIMATION);
        packetContainer.getBlockPositionModifier().write(0, new BlockPosition((int) this.getLocation().getX(), (int) (this.getLocation().getY() - 1), (int) this.getLocation().getZ()));
        packetContainer.getIntegers().write(0, this.getPacketKey());
        packetContainer.getIntegers().write(1, -1);
        protocolManager.broadcastServerPacket(packetContainer);

        ItemStack drill = new ItemStack(Material.HOPPER);
        net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(drill);
        NBTTagCompound nbt = nmsItem.u();
        if (nbt == null) nbt = new NBTTagCompound();
        nbt.a("drilltier", this.getDrillTier());
        nbt.a("drilltype", this.getDrillType().getNameFromDrillType());
        nmsItem.c(nbt);
        return CraftItemStack.asBukkitCopy(nmsItem);

    }


    //drill place
    @EventHandler
    public void structurePlace(BlockPlaceEvent e){
        if (e.getBlock().getType().equals(Material.HOPPER)) {
            Player p = e.getPlayer();
            e.setCancelled(true);
            if (!e.getBlockReplacedState().getType().equals(Material.AIR)){
                p.sendMessage(format("&cYou can't place that here"));
                return;
            }
            ItemStack itemPlaced = e.getItemInHand();
            if (CraftItemStack.asNMSCopy(itemPlaced).u() == null) return;
            Location placedLoc = e.getBlock().getLocation();

            net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemPlaced);
            NBTTagCompound nbt = nmsItem.u();
            String drillType = nbt.l("drilltype");

            //check if near structures have enough distance
            if (!aminecraftplugin.aminecraftplugin.drill.structures.Structure.canBePlaced(drillType, placedLoc, p)) return;

            //remove drill from hand
            removeHandItem(p, e.getHand());

            //create drill and place
            Drill drill = new Drill(placedLoc, p, itemPlaced);
            new BukkitRunnable() {
                @Override
                public void run() {
                    drill.place(p);
                }
            }.runTaskLater(plugin, 1l);
        }
    }

    public void addResource(Integer key, Double kg){
        if (!this.getResources().containsKey(key)){
            this.getResources().put(key, kg);
        } else {
            this.getResources().put(key, this.getResources().get(key) + kg);
        }
        this.updateInventories();
    }

    public int getMaxAmountOfPages(resourceCategory filterCategory){
        List<Map.Entry<Integer, Double>> resourceList = this.getResources().entrySet().stream().filter(entry -> categories.get(filterCategory).contains(entry.getKey())).collect(Collectors.toList());
        int size = resourceList.size();
        int amountOfPages = (int) Math.ceil(size / 36);
        if (amountOfPages == 0) return 1;
        return amountOfPages;
    }

    public ItemStack getMuteButton(){
        ItemStack muteButton = new ItemStack(Material.JUKEBOX);
        ItemMeta metaMuteButton = muteButton.getItemMeta();
        if (this.isMuted()) {
            metaMuteButton.setDisplayName(format("&aClick to unmute"));
        } else {
            metaMuteButton.setDisplayName(format("&cClick to mute"));
        }
        muteButton.setItemMeta(metaMuteButton);
        return muteButton;
    }

    public Inventory createNewInventory(int page, int maxPages){
        PlayerProfile owner = getPlayerProfile(this.getOwner().getUniqueId());
        String inventoryName = this.getDrillType().getDisplayName() + " tier " + this.getDrillTier() + " page " + page + "/" + maxPages;
        Inventory inventory = getDefaultScrollableInventory(inventoryName, false);
        inventory.setItem(51, getSortItem(owner.getSortingIndex()));
        inventory.setItem(52, getFilterItem(owner.getFilterCategory()));
        inventory.setItem(46, this.getMuteButton());
        return inventory;
    }

    public void initStructureInventories(){
        PlayerProfile owner = getPlayerProfile(this.getOwner().getUniqueId());
        int maxAmountOfPages = getMaxAmountOfPages(owner.getFilterCategory());
        ArrayList<Inventory> inventories = new ArrayList<>();
        for (int i = 0; i < maxAmountOfPages; i++){
            inventories.add(createNewInventory(i + 1, maxAmountOfPages));
        }
        this.setPages(inventories);
    }

    public void updateMuteButton(){
        ArrayList<Inventory> inventories = this.getPages();
        for (Inventory inventory : inventories) {
            inventory.setItem(46, this.getMuteButton());
        }
    }

    public void updateInventories(){


        PlayerProfile owner = getPlayerProfile(this.getOwner().getUniqueId());
        int sortingIndex = owner.getSortingIndex();
        resourceCategory filterCategory = owner.getFilterCategory();

        ItemStack sorter = getSortItem(sortingIndex);
        ItemStack filter = getFilterItem(filterCategory);

        int maxAmountOfPages = getMaxAmountOfPages(owner.getFilterCategory());
        ArrayList<Inventory> inventories = this.getPages();
        int amountOfPages = inventories.size();

        for (int i = 0; i < maxAmountOfPages - amountOfPages; i++){
            inventories.add(createNewInventory(amountOfPages + i + 1, maxAmountOfPages));
        }
        for (int i = amountOfPages - maxAmountOfPages; i > 0; i--){
            inventories.remove(amountOfPages - i + 1);
        }

        List<Map.Entry<Integer, Double>> resourceList = this.getResources().entrySet().stream().filter(entry -> categories.get(filterCategory).contains(entry.getKey())).collect(Collectors.toList());
        Collections.sort(resourceList, resourceComparators[sortingIndex]);

        int inventoryIndex = 0;
        int slotIndex = 0;

        for (Inventory inventory : inventories){
            for (int i = 0; i < 36; i++){
                inventory.setItem(i, null);
            }
            inventory.setItem(51, sorter);
            inventory.setItem(52, filter);
        }

        for (Map.Entry<Integer, Double> entry : resourceList){
            if (slotIndex >= 36){
                slotIndex = 0;
                inventoryIndex++;
            }
            Resource resource = getResourceFromKey(entry.getKey());
            double weight = entry.getValue();
            ItemStack item = resource.getItemStack();
            ItemMeta itemMeta = item.getItemMeta();
            itemMeta.setDisplayName(format("&f" + itemMeta.getDisplayName()));
            ArrayList<String> lore = new ArrayList<>();
            lore.add(format("&7Weight: &f" + df.format(weight) + "kg"));
            lore.add("");
            lore.add(format("&a&nClick to collect"));
            itemMeta.setLore(lore);
            item.setItemMeta(itemMeta);
            inventories.get(inventoryIndex).setItem(slotIndex, item);
            slotIndex++;
        }

    }

    @Override
    public void openStructureMenu(Player p, int page) {
        Inventory inventory = this.getPages().get(page - 1);
        p.openInventory(inventory);
        openedDrillInventory.put(p, this);
    }

    @EventHandler
    private void drillClick(InventoryClickEvent e){
        if (e.getView() == null) return;
        String name = e.getView().getTitle();
        if (name.contains("drill")) {
            e.setCancelled(true);
            Player p = (Player) e.getWhoClicked();
            PlayerProfile playerProfile = getPlayerProfile(p);
            Drill drill = openedDrillInventory.get(p);
            int sortingIndex = playerProfile.getSortingIndex();
            resourceCategory filterCategory = playerProfile.getFilterCategory();
            int maxAmountOfPages = getMaxAmountOfPages(filterCategory);
            int currentPage = Integer.parseInt(name.split("page ")[1].split("/")[0]);
            if (e.getRawSlot() < 36){
                List<Map.Entry<Integer, Double>> resourceList = drill.getResources().entrySet().stream().filter(entry -> categories.get(filterCategory).contains(entry.getKey())).collect(Collectors.toList());
                Collections.sort(resourceList, resourceComparators[sortingIndex]);
                int index = e.getRawSlot() + (35 * (currentPage - 1));
                Map.Entry<Integer, Double> entry = resourceList.get(index);
                int key = entry.getKey();
                double leftOver = playerProfile.getBackPack().addResource(key, entry.getValue());
                drill.getResources().remove(key);
                if (leftOver > 0) {
                    drill.getResources().put(key, leftOver);
                }
                p.playSound(p, Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
                drill.updateInventories();
            } else {
                switch (e.getRawSlot()) {
                    case 46:
                        if (drill.isMuted()) {
                            drill.setMuted(false);
                        } else {
                            drill.setMuted(true);
                        }
                        drill.updateMuteButton();
                        break;
                    case 45:
                        if (currentPage > 1) {
                            drill.openStructureMenu(p, currentPage - 1);
                        }
                        break;
                    case 53:
                        if (currentPage + 1 <= maxAmountOfPages) {
                            drill.openStructureMenu(p, currentPage + 1);
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
                        drill.updateInventories();
                        drill.openStructureMenu(p, 1);
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
                        drill.updateInventories();
                        drill.openStructureMenu(p, 1);
                        break;
                }
            }
        }

    }

    @Override
    public String getStructureName() {
        return this.getDrillType().getNameFromDrillType();
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public ArrayList<Location> getLocations() {
        ArrayList<Location> locations = new ArrayList<>();
        for (BlockState blockState : this.getDestroyedBlocks()){
            locations.add(blockState.getLocation());
        }
        return locations;
    }


    public ArrayList<Inventory> getPages() {
        return this.pages;
    }

    public void setPages(ArrayList<Inventory> pages) {
        this.pages = pages;
    }

    public OfflinePlayer getOwner() {
        return owner;
    }

    public DrillType getDrillType() {
        return drillType;
    }

    public int getDrillTier() {
        return drillTier;
    }

    public ArrayList<BlockState> getDestroyedBlocks() {
        return destroyedBlocks;
    }

    public LootFinder getLootFinder() {
        return lootFinder;
    }

    public HashMap<Integer, Double> getResources() {
        return resources;
    }

    public Hologram getHologram() {
        return hologram;
    }

    public Structure getThisStructure() {
        return structure;
    }

    public int getPacketKey() {
        return packetKey;
    }

    public void setPacketKey(int packetKey) {
        this.packetKey = packetKey;
    }

    public boolean isMuted() {
        return muted;
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
    }
}
