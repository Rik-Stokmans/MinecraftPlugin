package aminecraftplugin.aminecraftplugin.sideSkills.mining;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import static aminecraftplugin.aminecraftplugin.sideSkills.mining.MiningSkill.ores;

public class Ore {

    private int ID;
    private ItemStack reward;
    private Material blockType;
    private Material blockReplacement;
    private double xpReward;

    public Ore(ItemStack reward, Material blockType, Material blockReplacement, double xpReward, int ID) {
        this.reward = reward;
        this.blockType = blockType;
        this.blockReplacement = blockReplacement;
        this.xpReward = xpReward;
        this.ID = ID;
    }

    public Ore(ItemStack reward, Material blockType, Material blockReplacement, double xpReward) {
        this.reward = reward;
        this.blockType = blockType;
        this.blockReplacement = blockReplacement;
        this.xpReward = xpReward;
        this.ID = findEmptyID();
    }

    private int findEmptyID() {
        int ID = -1;
        int i = 1;
        while (i <= 1000)
            if (!ores.containsKey(i)) {
                ID = i;
                break;
            } else i++;
        return ID;
    }

    public Material getBlockType() {
        return blockType;
    }
    public void setBlockType(Material blockType) {
        this.blockType = blockType;
    }
    public double getXpReward() {
        return xpReward;
    }
    public void setXpReward(double xpReward) {
        this.xpReward = xpReward;
    }
    public Material getBlockReplacement() {
        return blockReplacement;
    }
    public void setBlockReplacement(Material blockReplacement) {
        this.blockReplacement = blockReplacement;
    }
    public ItemStack getReward() {
        return reward;
    }
    public void setReward(ItemStack reward) {
        this.reward = reward;
    }
    public int getID() {
        return ID;
    }
    public void setID(int ID) {
        this.ID = ID;
    }
}
