package aminecraftplugin.aminecraftplugin.sideSkills.mining;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class Ore {

    private ItemStack reward;
    private Material blockType;
    private Material blockReplacement;
    private double oreHealth;
    private double xpReward;

    public Ore(ItemStack reward, Material blockType, Material blockReplacement, double oreHealth, double xpReward) {
        this.reward = reward;
        this.blockType = blockType;
        this.blockReplacement = blockReplacement;
        this.oreHealth = oreHealth;
        this.xpReward = xpReward;
    }

    public Material getBlockType() {
        return blockType;
    }
    public void setBlockType(Material blockType) {
        this.blockType = blockType;
    }
    public double getOreHealth() {
        return oreHealth;
    }
    public void setOreHealth(double oreHealth) {
        this.oreHealth = oreHealth;
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
}
