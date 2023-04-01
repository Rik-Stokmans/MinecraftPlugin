package aminecraftplugin.aminecraftplugin.player;

import aminecraftplugin.aminecraftplugin.drill.Backpack;
import aminecraftplugin.aminecraftplugin.drill.loot.Resource;
import aminecraftplugin.aminecraftplugin.drill.loot.resourceCategory;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static aminecraftplugin.aminecraftplugin.Main.*;

public class PlayerProfile implements Listener {

    public static HashMap<UUID, PlayerProfile> playerProfiles = new HashMap<>();

    //mining
    private int miningSkill;
    private int prospectingSkill;
    private Backpack backPack;
    private ArrayList<ItemStack> offlineItems = new ArrayList<>();
    private int sortingIndex;
    private resourceCategory filterCategory;

    //money
    private double money;

    @EventHandler
    private void joinEvent(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        if (!playerProfiles.containsKey(p.getUniqueId())) {
            PlayerProfile playerProfile = new PlayerProfile(p);
            playerProfiles.put(p.getUniqueId(), playerProfile);
        }
        PlayerProfile playerProfile = getPlayerProfile(p);
        playerProfile.getBackPack().updateBackpackInPlayerInventory();
    }

    public PlayerProfile() {

    }

    public PlayerProfile(Player p) {
        this.miningSkill = 1;
        this.prospectingSkill = 1;
        this.backPack = new Backpack();
        this.money = 0;
        this.filterCategory = resourceCategory.ALL;
        this.sortingIndex = 0;
        playerProfiles.put(p.getUniqueId(), this);
    }

    public PlayerProfile(UUID uuid, int miningSkill, int prospectingSkill, Backpack backpack, double money, ArrayList<ItemStack> offlineItems, int sortingIndex, resourceCategory filterCategory) {
        this.miningSkill = miningSkill;
        this.prospectingSkill = prospectingSkill;
        this.backPack = backpack;
        this.money = money;
        this.offlineItems = offlineItems;
        this.sortingIndex = sortingIndex;
        this.filterCategory = filterCategory;
        playerProfiles.put(uuid, this);
    }


    public static PlayerProfile getPlayerProfile(Player p) {
        return playerProfiles.get(p.getUniqueId());
    }

    public static PlayerProfile getPlayerProfile(UUID uuid) {
        return playerProfiles.get(uuid);
    }

    public void addOfflineItem(ItemStack itemStack) {
        this.getOfflineItems().add(itemStack);
    }

    public ArrayList<ItemStack> getOfflineItems() {
        return offlineItems;
    }

    public void setOfflineItems(ArrayList<ItemStack> offlineItems) {
        this.offlineItems = offlineItems;
    }

    public int getMiningSkill() {
        return miningSkill;
    }

    public void setMiningSkill(int miningSkill) {
        this.miningSkill = miningSkill;
    }

    public int getProspectingSkill() {
        return prospectingSkill;
    }

    public void setProspectingSkill(int prospectingSkill) {
        this.prospectingSkill = prospectingSkill;
    }

    public Backpack getBackPack() {
        return backPack;
    }

    public void setBackPack(Backpack backPack) {
        this.backPack = backPack;
    }

    public double getMoney() {
        return money;
    }

    public void setMoney(double money) {
        this.money = money;
    }

    public void addMoney(double money) {
        this.money += money;
        if (this.getMoney() < 0) {
            this.money = 0;
        }
    }

    public int getSortingIndex() {
        return sortingIndex;
    }

    public void setSortingIndex(int sortingIndex) {
        this.sortingIndex = sortingIndex;
    }

    public resourceCategory getFilterCategory() {
        return filterCategory;
    }

    public void setFilterCategory(resourceCategory filterCategory) {
        this.filterCategory = filterCategory;
    }

    public static void init() {
        try {
            playerProfiles = loadPlayerprofiles();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void save() {
        try {
            savePlayerprofiles();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void savePlayerprofiles() throws IOException {

        for (Map.Entry<UUID, PlayerProfile> entry : playerProfiles.entrySet()) {
            YamlConfiguration playerFile = new YamlConfiguration();
            UUID uuid = entry.getKey();
            PlayerProfile playerProfile = entry.getValue();

            playerFile.set("skills.mining", playerProfile.getMiningSkill());
            playerFile.set("skills.prospecting", playerProfile.getProspectingSkill());
            playerFile.set("money", playerProfile.getMoney());

            int index = 1;
            for (ItemStack item : playerProfile.getOfflineItems()) {
                playerFile.set("offlineitems." + index, item);
                index++;
            }

            for (Map.Entry<Integer, Double> resource : playerProfile.getBackPack().getBackpack().entrySet()) {
                playerFile.set("backpack." + resource.getKey(), resource.getValue());
            }
            playerFile.set("backpack.space", playerProfile.getBackPack().getSpace());

            playerFile.set("sortingindex", playerProfile.getSortingIndex());
            playerFile.set("filtercategory", playerProfile.getFilterCategory());

            saveFile(playerFile, "playerdata/" + uuid.toString() + ".yml");
        }

    }

    private static HashMap<UUID, PlayerProfile> loadPlayerprofiles() throws IOException {

        HashMap<UUID, PlayerProfile> playerProfiles = new HashMap<>();

        File[] allPlayerFiles = new File(plugin.getDataFolder() + "/playerdata").listFiles();

        if (allPlayerFiles == null) return new HashMap<>();

        for (File file : allPlayerFiles) {

            YamlConfiguration playerFile = loadFile("playerdata/" + file.getName());
            if (playerFile == null) continue;

            String uuidName = file.getName().substring(0, file.getName().length() - 4);
            UUID uuid = UUID.fromString(uuidName);

            int miningSkill = playerFile.getInt("skills.mining");
            int prospectingSkill = playerFile.getInt("skills.prospecting");
            double money = playerFile.getDouble("money");

            ArrayList<ItemStack> offlineItems = new ArrayList<>();
            if (playerFile.contains("offlineitems")) {
                playerFile.getConfigurationSection("offlineitems").getKeys(false).forEach(key -> {
                    ItemStack item = playerFile.getItemStack("offlineitems." + key);
                    offlineItems.add(item);
                });
            }
            int sortingIndex = playerFile.getInt("sortingindex");
            resourceCategory filterCategory = aminecraftplugin.aminecraftplugin.drill.loot.resourceCategory.getCategory("filtercategory");

            HashMap<Integer, Double> backpack = new HashMap<>();
            Backpack backpack1 = null;
            double space = 5.0;
            if (playerFile.contains("backpack")) {
                playerFile.getConfigurationSection("backpack").getKeys(false).forEach(key -> {
                    try {
                        int i = Integer.parseInt(key);
                        double kg = playerFile.getDouble("backpack." + key);
                        backpack.put(i, kg);
                    } catch (NumberFormatException nfe) {
                    }
                });
                if (playerFile.contains("backpack.space")) {
                    space = playerFile.getDouble("backpack.space");
                }

                backpack1 = new Backpack(backpack, space, uuid);

            }
            PlayerProfile playerProfile = new PlayerProfile(uuid, miningSkill, prospectingSkill, backpack1, money, offlineItems, sortingIndex, filterCategory);
            playerProfiles.put(uuid, playerProfile);
        }
        return playerProfiles;
    }
}
