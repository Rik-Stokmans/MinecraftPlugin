package aminecraftplugin.aminecraftplugin.player;

import aminecraftplugin.aminecraftplugin.drill.Backpack;
import aminecraftplugin.aminecraftplugin.drill.loot.Resource;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
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

    @EventHandler
    private void joinEvent(PlayerJoinEvent e){
        Player p = e.getPlayer();
        if (!playerProfiles.containsKey(p.getUniqueId())){
            PlayerProfile playerProfile = new PlayerProfile(p);
            playerProfiles.put(p.getUniqueId(), playerProfile);
        }
    }

    public PlayerProfile(){

    }

    public PlayerProfile(Player p){
        this.miningSkill = 1;
        this.prospectingSkill = 1;
        this.backPack = new Backpack();
    }

    public static PlayerProfile getPlayerProfile(Player p){
        return playerProfiles.get(p.getUniqueId());
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

    public static void init(){
        try {
            playerProfiles = loadPlayerprofiles();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void save(){
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

            for (Map.Entry<Integer, Double> resource : playerProfile.getBackPack().getBackpack().entrySet()){
                playerFile.set("backpack." + resource.getKey(), resource.getValue());
            }

            saveFile(playerFile, "playerdata/" + uuid.toString() + ".yml");
        }

    }

    private static HashMap<UUID, PlayerProfile> loadPlayerprofiles() throws IOException{

        HashMap<UUID, PlayerProfile> playerProfiles = new HashMap<>();

        File[] allPlayerFiles = new File(plugin.getDataFolder() + "/playerdata").listFiles();

        for (File file : allPlayerFiles){

            YamlConfiguration playerFile = loadFile("playerdata/" + file.getName());
            if (playerFile == null) continue;

            String uuidName = file.getName().split(".")[0];
            UUID uuid = UUID.fromString(uuidName);

            int miningSkill = playerFile.getInt("skills.mining");
            int prospectingSkill = playerFile.getInt("skills.prospecting");
            HashMap<Integer, Double> backpack = new HashMap<>();
            playerFile.getConfigurationSection("backpack").getKeys(false).forEach(key -> {
                double kg = playerFile.getDouble("backpack." + key);
                backpack.put(Integer.valueOf(key), kg);
            });

            PlayerProfile playerProfile = new PlayerProfile();
            playerProfile.setMiningSkill(miningSkill);
            playerProfile.setProspectingSkill(prospectingSkill);
            playerProfile.setBackPack(new Backpack(backpack));


            playerProfiles.put(uuid, playerProfile);
        }
        return playerProfiles;

    }
}
