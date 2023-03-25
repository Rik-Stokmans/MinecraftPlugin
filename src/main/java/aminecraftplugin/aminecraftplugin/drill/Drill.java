package aminecraftplugin.aminecraftplugin.drill;

import aminecraftplugin.aminecraftplugin.drill.loot.LootFinder;
import aminecraftplugin.aminecraftplugin.drill.loot.Resource;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.structure.Mirror;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.craftbukkit.v1_19_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.structure.Structure;
import org.bukkit.structure.StructureManager;

import java.util.*;

import static aminecraftplugin.aminecraftplugin.Main.plugin;
import static aminecraftplugin.aminecraftplugin.drill.DrillType.getDrillTypeFromName;
import static aminecraftplugin.aminecraftplugin.utils.ChatUtils.format;
import static aminecraftplugin.aminecraftplugin.utils.Direction.getCardinalDirection;
import static aminecraftplugin.aminecraftplugin.utils.Direction.getXandZ;

public class Drill implements Listener {

    public static HashMap<UUID, Drill> drills = new HashMap<>();

    private OfflinePlayer owner;
    private Location location;
    private DrillType drillType;
    private int drillTier;
    private HashMap<Location, Material> destroyedBlocks = new HashMap();

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

        drills.put(owner.getUniqueId(), this);
    }

    //drill place
    @EventHandler
    private void drillPlace(BlockPlaceEvent e){
        if (e.getBlock().getType().equals(Material.HOPPER)) {
            Player p = e.getPlayer();
            ItemStack itemPlaced = e.getItemInHand();
            if (CraftItemStack.asNMSCopy(itemPlaced).u() == null) return;

            e.setCancelled(true);
            Location placedLoc = e.getBlock().getLocation();
            Drill drill = new Drill(placedLoc, p, itemPlaced);
            new BukkitRunnable() {
                @Override
                public void run() {
                    drill.place(p);
                }
            }.runTaskLater(plugin, 1l);
            new BukkitRunnable() {
                @Override
                public void run() {
                    drill.destroy();
                }
            }.runTaskLater(plugin, 20l);
        }
    }

    private void place(Player p){

        int length = 0; //facing direction
        int width = 0;
        int height = 0;

        String s = getCardinalDirection(p);
        ImmutablePair<Integer, Integer> pair = getXandZ(s);

        p.sendMessage("x: " + pair.getKey() + ", z: " + pair.getValue());

        ArrayList<Location> locations = new ArrayList<>();
        Location drillLoc = this.getLocation();

        StructureManager structureManager = Bukkit.getStructureManager();
        Map<NamespacedKey, Structure> structureMap = structureManager.getStructures();
        Structure structure = null;
        for (NamespacedKey namespacedKey : structureMap.keySet()){
            if (namespacedKey.asString().equals("minecraft:" + this.getDrillType().getNameFromDrillType())){
                structure = structureMap.get(namespacedKey);
            }
        }
        length = (int) structure.getSize().getZ();
        width = (int) structure.getSize().getX();
        height = (int) structure.getSize().getY();

        p.sendMessage("length: " + length, ", width: " + width + ", height: " + height);


        //get all destroyed blocks
        int lengthRemainder = length % 2;
        int widthRemainder = width % 2;
        int lengthFloor = (int) -Math.ceil((length - 1) / 2);
        int lengthCeiling = (int) Math.ceil((length - 1) / 2);
        for (int l = lengthFloor; l <= lengthCeiling; l++){
            int widthFloor = (int) -Math.ceil((width - 1) / 2);
            int widthCeiling = (int) Math.ceil((width - 1) / 2);
            for (int w = widthFloor; w <= widthCeiling; w++){

                for (int h = 1; h <= height; h++){
                    Location loc = drillLoc.clone().add(
                            (l * pair.getKey() + w * pair.getValue()),
                            h - 1,
                            (w * pair.getKey() + l * pair.getValue()));
                    locations.add(loc);
                }
            }
        }
        for (Location loc : locations){
            Block block = loc.getBlock();
            Material material = block.getType();
            block.setType(Material.AIR);
            destroyedBlocks.put(loc, material);
        }


        //structure default = facing north
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

    private void destroy(){
        for (Map.Entry<Location, Material> destroyedBlock : destroyedBlocks.entrySet()){
            Location loc = destroyedBlock.getKey();
            Material material = destroyedBlock.getValue();
            loc.getBlock().setType(material);
        }
        destroyedBlocks = new HashMap<>();
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

    public Location getLocation() {
        return location;
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

    public HashMap<Location, Material> getDestroyedBlocks() {
        return destroyedBlocks;
    }
}
