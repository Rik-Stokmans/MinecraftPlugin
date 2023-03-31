package aminecraftplugin.aminecraftplugin.drill.structures;

import aminecraftplugin.aminecraftplugin.drill.loot.LootFinder;
import aminecraftplugin.aminecraftplugin.drill.loot.Resource;
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
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.structure.Structure;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static aminecraftplugin.aminecraftplugin.Main.*;
import static aminecraftplugin.aminecraftplugin.drill.structures.DrillType.getDrillTypeFromName;
import static aminecraftplugin.aminecraftplugin.utils.ChatUtils.format;
import static aminecraftplugin.aminecraftplugin.utils.Direction.getCardinalDirection;
import static aminecraftplugin.aminecraftplugin.utils.Direction.getXandZ;
import static aminecraftplugin.aminecraftplugin.utils.RemoveHandItem.removeHandItem;

public class Drill implements Listener, aminecraftplugin.aminecraftplugin.drill.structures.Structure {

    private OfflinePlayer owner;
    private Location location;
    private LootFinder lootFinder;
    private DrillType drillType;
    private Inventory inventory;
    private int drillTier;
    private Hologram hologram;
    private Structure structure;
    private HashMap<Resource, Double> resources = new HashMap<>();
    private ArrayList<BlockState> destroyedBlocks = new ArrayList<>();
    private int packetKey;

    private static DecimalFormat df = new DecimalFormat("#.##");


    public Drill(){
    }

    public Drill(Location location, OfflinePlayer owner, ItemStack drill){
        this.location = location;
        this.owner = owner;
        this.inventory = Bukkit.createInventory(null, 54, "Drill");

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

        this.getLocation().getWorld().playSound(this.getLocation(), Sound.BLOCK_STONE_BREAK, 1, 1);
        final int[] stage = {0};
        PacketContainer packetContainer = protocolManager.createPacket(PacketType.Play.Server.BLOCK_BREAK_ANIMATION);
        packetContainer.getBlockPositionModifier().write(0, new BlockPosition((int) this.getLocation().getX(), (int) (this.getLocation().getY() - 1), (int) this.getLocation().getZ()));
        this.setPacketKey(ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE));
        packetContainer.getIntegers().write(0, this.getPacketKey());


        new BukkitRunnable() {
            @Override
            public void run() {
                drill.getLocation().getWorld().playSound(drill.getLocation(), Sound.BLOCK_STONE_BREAK, 1, 1);
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
            for (Player p1 : Bukkit.getOnlinePlayers()){
                p1.sendMessage(String.valueOf(kgLeft[0]));
                p1.sendMessage(String.valueOf(totalSeconds));
            }
            final int[] stage = {0};

            new BukkitRunnable() {
                @Override
                public void run() {
                    drill.getLocation().getWorld().playSound(drill.getLocation(), Sound.BLOCK_STONE_BREAK, 1, 1);
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
                    drill.addResource(resource, kgMined);
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

    public void addResource(Resource resource, Double kg){
        if (!this.getResources().containsKey(resource)){
            this.getResources().put(resource, kg);
        } else {
            this.getResources().put(resource, this.getResources().get(resource) + kg);
        }
        this.updateStructureInventory();
    }

    public void updateStructureInventory(){
        int index = 0;
        for (Map.Entry<Resource, Double> resource : this.getResources().entrySet()){
            ItemStack item = resource.getKey().getItemStack();
            ItemMeta itemMeta = item.getItemMeta();
            ArrayList<String> lore = new ArrayList<>();
            lore.add(format("&7kg: &f" + df.format(resource.getValue())));
            itemMeta.setLore(lore);
            item.setItemMeta(itemMeta);
            inventory.setItem(index, item);

            index++;
        }
    }

    @Override
    public void openStructureMenu(Player p) {
        Inventory inventory = this.getInventory();
        int index = 0;
        for (Map.Entry<Resource, Double> resource : this.getResources().entrySet()){
            ItemStack item = resource.getKey().getItemStack();
            ItemMeta itemMeta = item.getItemMeta();
            ArrayList<String> lore = new ArrayList<>();
            lore.add(format("&7kg: &f" + df.format(resource.getValue())));
            itemMeta.setLore(lore);
            item.setItemMeta(itemMeta);
            inventory.setItem(index, item);

            index++;
        }
        p.openInventory(inventory);
    }

    @EventHandler
    private void drillClick(InventoryClickEvent e){
        if (e.getView() == null) return;
        String name = e.getView().getTitle();
        if (name.contains("Drill")) {
            e.setCancelled(true);
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


    public Inventory getInventory() {
        return inventory;
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

    public HashMap<Resource, Double> getResources() {
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
}
