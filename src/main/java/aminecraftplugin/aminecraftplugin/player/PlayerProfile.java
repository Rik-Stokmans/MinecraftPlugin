package aminecraftplugin.aminecraftplugin.player;

import aminecraftplugin.aminecraftplugin.drilling.Backpack;
import aminecraftplugin.aminecraftplugin.drilling.resource.resourceCategory;
import aminecraftplugin.aminecraftplugin.sideSkills.Skill;
import aminecraftplugin.aminecraftplugin.sideSkills.SkillType;
import org.bukkit.Location;
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

public class PlayerProfile {

    public static HashMap<UUID, PlayerProfile> playerProfiles = new HashMap<>();

    //mining
    private Backpack backPack;
    private ArrayList<ItemStack> offlineItems = new ArrayList<>();
    private int sortingIndex;
    private resourceCategory filterCategory;
    private HashMap<SkillType, Skill> skills = new HashMap<>();

    //money
    private double money;

    public static void playerProfileJoinEvent(PlayerJoinEvent e){
        Player p = e.getPlayer();
        if (!playerProfiles.containsKey(p.getUniqueId())) {
            Location spawnLocation = new Location(mainWorld, 468.5, 71, -46.5);
            spawnLocation.setYaw(-90.0f);
            p.teleport(spawnLocation);

            PlayerProfile playerProfile = new PlayerProfile(p);
            playerProfiles.put(p.getUniqueId(), playerProfile);
        }
        PlayerProfile playerProfile = getPlayerProfile(p);
        playerProfile.getBackPack().updateBackpackInPlayerInventory();
    }


    public PlayerProfile(Player p) {
        for (SkillType skillType : SkillType.values()){
            this.skills.put(skillType, new Skill(skillType, 0, 0));
        }
        this.backPack = new Backpack();
        this.money = 0;
        this.filterCategory = resourceCategory.ALL;
        this.sortingIndex = 0;
        playerProfiles.put(p.getUniqueId(), this);
        this.backPack.updateAllPlages(p.getUniqueId());
    }

    public PlayerProfile(UUID uuid, Backpack backpack, double money, ArrayList<ItemStack> offlineItems, int sortingIndex, resourceCategory filterCategory, HashMap<SkillType, Skill> skills) {
        this.skills = skills;
        this.backPack = backpack;
        this.money = money;
        this.offlineItems = offlineItems;
        this.sortingIndex = sortingIndex;
        this.filterCategory = filterCategory;
        playerProfiles.put(uuid, this);
        this.backPack.updateAllPlages(uuid);
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

    public HashMap<SkillType, Skill> getSkills() {
        return skills;
    }

    public void setSkills(HashMap<SkillType, Skill> skills) {
        this.skills = skills;
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

            for (Skill skill : playerProfile.getSkills().values()){
                playerFile.set("skills." + skill.getSkillType().toString() + ".tier", skill.getTier());
                playerFile.set("skills." + skill.getSkillType().toString() + ".xp", skill.getXp());
            }
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

            HashMap<SkillType, Skill> skills = new HashMap<>();
            if (playerFile.contains("skills")){
                playerFile.getConfigurationSection("skills").getKeys(false).forEach(skillName -> {
                    SkillType skillType = SkillType.valueOf(skillName);
                    int tier = playerFile.getInt("skills." + skillName + ".tier");
                    double xp = playerFile.getDouble("skills." + skillName + ".xp");
                    skills.put(skillType, new Skill(skillType, tier, xp));
                });
            }
            if (skills.size() != SkillType.values().length){
                for (SkillType skillType : SkillType.values()){
                    if (!skills.containsKey(skillType)){
                        skills.put(skillType, new Skill(skillType, 0, 0));
                    }
                }
            }

            double money = playerFile.getDouble("money");

            ArrayList<ItemStack> offlineItems = new ArrayList<>();
            if (playerFile.contains("offlineitems")) {
                playerFile.getConfigurationSection("offlineitems").getKeys(false).forEach(key -> {
                    ItemStack item = playerFile.getItemStack("offlineitems." + key);
                    offlineItems.add(item);
                });
            }
            int sortingIndex = playerFile.getInt("sortingindex");
            resourceCategory filterCategory = resourceCategory.getCategory("filtercategory");

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
            PlayerProfile playerProfile = new PlayerProfile(uuid, backpack1, money, offlineItems, sortingIndex, filterCategory, skills);
            playerProfiles.put(uuid, playerProfile);
        }
        return playerProfiles;
    }
}
