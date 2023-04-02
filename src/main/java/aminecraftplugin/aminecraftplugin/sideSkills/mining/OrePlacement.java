package aminecraftplugin.aminecraftplugin.sideSkills.mining;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import static aminecraftplugin.aminecraftplugin.sideSkills.mining.MiningSkill.*;

public class OrePlacement {

    private Location location;
    private boolean isActive;
    private int ID;
    private int respawnTime;

    //called when a new orePlacement is created
    public OrePlacement(Location location, int ID, int respawnTime) {
        this.location = location;
        this.ID = ID;
        this.respawnTime = respawnTime;
        isActive = false;
        createOrePlacement();
    }

    public void createOrePlacement() {
        isActive = true;
        location.getBlock().setType(getOreFromID(ID).getBlockType());
    }
    public ItemStack removeOrePlacement() {
        isActive = false;
        Ore ore = getOreFromID(ID);
        location.getBlock().setType(ore.getBlockReplacement());
        return ore.getReward();
    }

    public Location getLocation() {
        return location;
    }
    public void setLocation(Location location) {
        this.location = location;
    }
    public boolean isActive() {
        return isActive;
    }
    public void setActive(boolean active) {
        isActive = active;
    }
    public int getID() {
        return ID;
    }
    public void setID(int ID) {
        this.ID = ID;
    }
    public int getRespawnTime() {
        return respawnTime;
    }
    public void setRespawnTime(int respawnTime) {
        this.respawnTime = respawnTime;
    }
}
