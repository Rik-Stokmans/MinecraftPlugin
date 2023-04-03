package aminecraftplugin.aminecraftplugin.commands;

import aminecraftplugin.aminecraftplugin.market.Market;
import aminecraftplugin.aminecraftplugin.market.Trade;
import aminecraftplugin.aminecraftplugin.sideSkills.mining.MiningSkill;
import aminecraftplugin.aminecraftplugin.sideSkills.mining.Ore;
import net.minecraft.nbt.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_19_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;

import static aminecraftplugin.aminecraftplugin.drill.loot.Resource.getKeyFromItemstack;
import static aminecraftplugin.aminecraftplugin.market.Market.*;
import static aminecraftplugin.aminecraftplugin.sideSkills.mining.MiningSkill.addOre;
import static aminecraftplugin.aminecraftplugin.sideSkills.mining.MiningSkill.ores;
import static aminecraftplugin.aminecraftplugin.utils.ChatUtils.format;
import static aminecraftplugin.aminecraftplugin.utils.Compress.roundAvoid;

public class miningResourceMenu implements CommandExecutor, Listener {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) return false;
        Player p = (Player) sender;

        openMiningResourceMenu(p);
        return true;
    }
    
    private void openMiningResourceMenu(Player p) {
        Inventory miningResourceMenu = Bukkit.createInventory(null, 54, format("&eMining Resource Menu"));

        if (!ores.isEmpty()) for (Ore ore : ores.values()) {
                miningResourceMenu.addItem(generateOreGuiItem(ore));
            }

        for (int i = 36; i <= 44; i++) {
            miningResourceMenu.setItem(i, lightDivider);
        }

        miningResourceMenu.setItem(47, addMiningResource);
        miningResourceMenu.setItem(51, removeMiningResource);

        p.openInventory(miningResourceMenu);
    }

    private void openAddMiningResourceMenu(Player p, int ID) {
        if (!ores.containsKey(ID)) return;
        Inventory miningResourceMenu = Bukkit.createInventory(null, 27, format("&eEdit Mining Resource"));

        Ore ore = ores.get(ID);

        miningResourceMenu.setItem(10, new ItemStack(ore.getBlockType()));

        miningResourceMenu.setItem(12, new ItemStack(ore.getBlockReplacement()));

        miningResourceMenu.setItem(14, new ItemStack(ore.getReward()));

        //generating the xp bottle item
        ItemStack item = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta iMeta = item.getItemMeta();
        iMeta.setDisplayName(format("&aXp Reward"));
        ArrayList<String> itemLore = new ArrayList<>();
        itemLore.add(format("&eXp: " + ore.getXpReward()));
        iMeta.setLore(itemLore);
        item.setItemMeta(iMeta);
        miningResourceMenu.setItem(16, item);

        p.openInventory(miningResourceMenu);
    }

    private void updateAllOpenMiningResourceMenus() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getOpenInventory().getTitle().equals(format("&eMining Resource Menu"))) openMiningResourceMenu(p);
        }
    }



    @EventHandler
    public void inventoryClick(InventoryClickEvent e) {
        String invName = e.getView().getTitle();
        if (!invName.equals(format("&eMining Resource Menu")) && !invName.equals(format("&eEdit Mining Resource"))) return;

        e.setCancelled(true);
        ItemStack clickedItem = e.getCurrentItem();
        if (clickedItem == null) return;
        Player p = (Player) e.getWhoClicked();

        if (invName.equals(format("&eMining Resource Menu"))) {
            if (e.getSlot() <= 35) {
                //getting the ID from the nbt
                net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(clickedItem);
                NBTTagCompound nbt = nmsItem.u();
                int ID = nbt != null ? nbt.h("id") : -1;
                p.sendMessage(String.valueOf(ID));
                if (ID != -1 && ores.containsKey(ID)) openAddMiningResourceMenu(p, ID);
            }
            else if (clickedItem.isSimilar(addMiningResource)) {
                Ore ore = new Ore(new ItemStack(Material.COAL), Material.COAL_ORE, Material.STONE, 0);
                addOre(ore);
                updateAllOpenMiningResourceMenus();
            }
        }
        else if (invName.equals(format("&eEdit Mining Resource"))) {
            if (clickedItem.isSimilar(backButton)) openMiningResourceMenu(p);
        }
    }



    private ItemStack generateOreGuiItem(Ore ore) {
        ItemStack item = new ItemStack(ore.getBlockType());
        ItemMeta iMeta = item.getItemMeta();

        ArrayList<String> itemLore = new ArrayList<>();
        itemLore.add(format("&eXp: " + ore.getXpReward()));
        itemLore.add(format("&7Drop: " + ore.getReward().toString()));
        iMeta.setLore(itemLore);
        item.setItemMeta(iMeta);

        //adds the id to the nbt
        net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
        NBTTagCompound nbt = nmsItem.u();
        if (nbt == null) nbt = new NBTTagCompound();
        nbt.a("id", ore.getID());
        nmsItem.c(nbt);
        return CraftItemStack.asBukkitCopy(nmsItem);
    }
}











