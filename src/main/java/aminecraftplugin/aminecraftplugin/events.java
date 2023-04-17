package aminecraftplugin.aminecraftplugin;

import aminecraftplugin.aminecraftplugin.drilling.drill.Drill;
import aminecraftplugin.aminecraftplugin.drilling.energy.EnergySource;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.minecraft.nbt.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_19_R2.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;


import static aminecraftplugin.aminecraftplugin.Main.mainWorld;
import static aminecraftplugin.aminecraftplugin.commands.addSpawnCommand.spawnLocations;
import static aminecraftplugin.aminecraftplugin.commands.miningResourceMenu.miningResourceMenuInventoryClickEvent;
import static aminecraftplugin.aminecraftplugin.drilling.Backpack.backPackInventoryClickEvent;
import static aminecraftplugin.aminecraftplugin.drilling.Backpack.backPackPlayerInteractEvent;
import static aminecraftplugin.aminecraftplugin.drilling.loot.LootTable.lootTableInventoryClickEvent;
import static aminecraftplugin.aminecraftplugin.drilling.resource.Resource.resourceInventoryClickEvent;
import static aminecraftplugin.aminecraftplugin.drilling.structures.StructureEvent.structurePlayerJoinEvent;
import static aminecraftplugin.aminecraftplugin.drilling.structures.StructureEvent.structurePlayerQuitEvent;
import static aminecraftplugin.aminecraftplugin.market.Market.marketInventoryClickEvent;
import static aminecraftplugin.aminecraftplugin.market.Market.rightClickMarketEvent;
import static aminecraftplugin.aminecraftplugin.player.PlayerProfile.playerProfileJoinEvent;
import static aminecraftplugin.aminecraftplugin.shop.Shop.rightClickShopEvent;
import static aminecraftplugin.aminecraftplugin.shop.Shop.shopCategoryInventoryClickEvent;
import static aminecraftplugin.aminecraftplugin.sideSkills.mining.MiningSkill.miningSkillPlayerInteractEvent;

public class events implements Listener {


    private Drill eventDrill;
    private EnergySource eventEnergySource;

    public events(Drill eventDrill, EnergySource eventEnergySource){
        this.eventDrill = eventDrill;
        this.eventEnergySource = eventEnergySource;
    }

    @EventHandler
    public void playerJoinEvent(PlayerJoinEvent e){
        playerProfileJoinEvent(e);
        structurePlayerJoinEvent(e);
    }

    @EventHandler
    public void playerQuitEvent(PlayerQuitEvent e){
        structurePlayerQuitEvent(e);
    }


    @EventHandler
    public void inventoryClickEvent(InventoryClickEvent e){
        marketInventoryClickEvent(e);
        resourceInventoryClickEvent(e);
        lootTableInventoryClickEvent(e);
        backPackInventoryClickEvent(e);
        miningResourceMenuInventoryClickEvent(e);
        shopCategoryInventoryClickEvent(e);
    }

    @EventHandler
    public void rightClickNPCEvent(NPCRightClickEvent e){
        rightClickShopEvent(e);
        rightClickMarketEvent(e);
    }

    @EventHandler
    public void placeEvent(BlockPlaceEvent e){
        Player p = e.getPlayer();
        if (!p.getGameMode().equals(GameMode.CREATIVE)){
            e.setCancelled(true);
        }

        ItemStack item = e.getItemInHand();
        net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
        NBTTagCompound nbt = nmsItem.u();
        if (nbt == null) nbt = new NBTTagCompound();

        if ((nbt.e("drilltype"))) {
            this.getEventDrill().placeEvent(e);
        }
        else if (nbt.e("energytype")){
            this.getEventEnergySource().placeEvent(e);
        }
    }

    @EventHandler
    private void playerInteractEvent(PlayerInteractEvent e){
        backPackPlayerInteractEvent(e);
        miningSkillPlayerInteractEvent(e);

    }


    @EventHandler
    private void entitySpawn(EntitySpawnEvent e){
        if (e.getEntityType().equals(EntityType.BAT)){
            e.setCancelled(true);
        }
    }

    @EventHandler
    private void dropBlockEvent(BlockDropItemEvent e){
        e.setCancelled(true);
    }

    @EventHandler
    private void breakEvent(BlockBreakEvent e){
        Player p = e.getPlayer();
        if (!p.getGameMode().equals(GameMode.CREATIVE)){
            e.setCancelled(true);
        }
    }

    @EventHandler
    private void damageEvent(EntityDamageByEntityEvent e){
        if (e.getDamager() instanceof Player) {
            Player p = (Player) e.getDamager();
            if (!p.getGameMode().equals(GameMode.CREATIVE)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    private void rightClickArmorStand(PlayerArmorStandManipulateEvent e){
        Player p = e.getPlayer();
        if (!p.getGameMode().equals(GameMode.CREATIVE)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    private void onDeath(PlayerRespawnEvent e){
        Location deathLoc = e.getPlayer().getLastDeathLocation();
        Location spawnLoc = getSpawnLocation(deathLoc);
        e.setRespawnLocation(spawnLoc);
    }


    public Location getSpawnLocation(Location deathLoc){
        double shortestDistance = Double.MAX_VALUE;
        Location spawnLoc = new Location(mainWorld, 468.5, 71, -46.5);
        spawnLoc.setYaw(-90.0f);
        for (Location loc : spawnLocations.values()){
            double distance = loc.distance(deathLoc);
            if (distance < shortestDistance){
                shortestDistance = distance;
                spawnLoc = loc;
            }
        }
        return spawnLoc;
    }


    public Drill getEventDrill() {
        return eventDrill;
    }

    public EnergySource getEventEnergySource() {
        return eventEnergySource;
    }
}
