package aminecraftplugin.aminecraftplugin.drill.structures;

import aminecraftplugin.aminecraftplugin.drill.loot.LootFinder;
import aminecraftplugin.aminecraftplugin.drill.loot.Resource;
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
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.structure.Structure;
import org.bukkit.util.BlockVector;

import java.util.*;

import static aminecraftplugin.aminecraftplugin.Main.plugin;
import static aminecraftplugin.aminecraftplugin.drill.structures.DrillType.getDrillTypeFromName;
import static aminecraftplugin.aminecraftplugin.utils.Direction.getCardinalDirection;
import static aminecraftplugin.aminecraftplugin.utils.Direction.getXandZ;
import static aminecraftplugin.aminecraftplugin.utils.RemoveHandItem.removeHandItem;

public class Drill implements Listener, aminecraftplugin.aminecraftplugin.drill.structures.Structure {

    private OfflinePlayer owner;
    private Location location;
    private DrillType drillType;
    private int drillTier;
    private ArrayList<BlockState> destroyedBlocks = new ArrayList<>();

    public Drill(){

    }

    public Drill(Location location, OfflinePlayer owner, ItemStack drill){
        this.location = location;
        this.owner = owner;

        net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(drill);
        NBTTagCompound nbt = nmsItem.u();
        String drillType = nbt.l("drilltype");
        int drillTier = nbt.h("drilltier");

        this.drillType = getDrillTypeFromName(drillType);
        this.drillTier = drillTier;

        aminecraftplugin.aminecraftplugin.drill.structures.Structure.addStructure(owner.getUniqueId(), this);
    }

    @Override
    public void place(Player p){

        Location drillLoc = this.getLocation();

        //facing direction
        String s = getCardinalDirection(p);
        ImmutablePair<Integer, Integer> pair = getXandZ(s);

        //get structure
        Structure structure = aminecraftplugin.aminecraftplugin.drill.structures.Structure.getStructure(this.getDrillType().getNameFromDrillType());

        int length = (int) structure.getSize().getZ();
        int width = (int) structure.getSize().getX();
        int height = (int) structure.getSize().getY();

        //get all blocks to be destroyed
        ArrayList<Location> locations = new ArrayList<>();
        ArrayList<Location> ignoredLocations = new ArrayList<>();
        int lengthFloor = (int) -Math.ceil(((length + 2) - 1) / 2);
        int lengthCeiling = (int) Math.ceil((length + 2) / 2);
        for (int l = lengthFloor; l <= lengthCeiling; l++){
            int widthFloor = (int) -Math.ceil(((width + 2) - 1) / 2);
            int widthCeiling = (int) Math.ceil(((width + 2) - 1) / 2);
            for (int w = widthFloor; w <= widthCeiling; w++){

                for (int h = 1; h <= height; h++){
                    Location loc = drillLoc.clone().add(
                            (l * pair.getKey() + w * pair.getValue()),
                            h - 1,
                            (w * pair.getKey() + l * pair.getValue()));

                    Material type = loc.getBlock().getType();
                    Location locUp = loc.clone().add(0,1,0);

                    //exception to not place tall grass twice
                    for (Material material : doubleBlocks){
                        if (type.equals(material)){
                            ignoredLocations.add(locUp);
                        }
                    }

                    //exception to not break surroundings
                    while ((locUp.getBlock().isPassable() || locUp.getBlock().getType().hasGravity())
                            && !locUp.getBlock().getType().equals(Material.AIR)){
                        if (!ignoredLocations.contains(locUp)) {
                            locations.add(locUp);
                        }
                        locUp = locUp.clone().add(0,1,0);
                    }

                    if (!locations.contains(loc) && !ignoredLocations.contains(loc)) {
                        locations.add(loc);
                    }
                }
            }
        }
        for (Location loc : locations){
            Block block = loc.getBlock();
            destroyedBlocks.add(block.getState());
            block.setType(Material.AIR);
        }
        for (Location loc : ignoredLocations){
            Block block = loc.getBlock();
            destroyedBlocks.add(block.getState());
            block.setType(Material.AIR);
        }


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
            if (destroyedBlock.getBlockData().getAsString().contains("half=lower")) {
                if (!offline) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            destroyedBlock.getBlock().setType(destroyedBlock.getType());
                            Location locUp = destroyedBlock.getLocation().clone().add(0, 1, 0);
                            BlockData blockData2 = Bukkit.createBlockData(destroyedBlock.getType(), "[half=upper]");
                            locUp.getBlock().setBlockData(blockData2);
                        }
                    }.runTaskLater(plugin, 1l);
                } else {
                    savedBlocks.put(destroyedBlock.getLocation(), new ItemStack(destroyedBlock.getType()));
                }
            }

        }

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
            ItemStack itemPlaced = e.getItemInHand();
            if (CraftItemStack.asNMSCopy(itemPlaced).u() == null) return;
            e.setCancelled(true);
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

    @Override
    public String getStructureName() {
        return this.getDrillType().getNameFromDrillType();
    }

    @Override
    public Location getLocation() {
        return location;
    }


    //todo: GUI to display loot
    //oven/hopper GUI for collecting
    public void drill(Player p){
        LootFinder loot = new LootFinder(this.getLocation());
        for (Map.Entry<Resource, Double> resource : loot.findLoot(p).entrySet()){
            //todo: add to backpack
            p.sendMessage(resource.getKey().getName() + ": " + resource.getValue() + "kg");
        }
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
}
