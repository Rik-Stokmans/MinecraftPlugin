package aminecraftplugin.aminecraftplugin.sideSkills;

import java.util.HashMap;

public enum SkillType {


    miningskill(0.1, 100, false),
    prospectingskill(5.0, 100, true);

    SkillType(double effect, int maxTier, boolean isPercentage){
        this.effect = effect;
        this.maxTier = maxTier;
        this.isPercentage = isPercentage;
    }

    public static HashMap<Integer, SkillType> skillTypeHashMap = new HashMap<>();
    private double effect;
    private int maxTier;
    private boolean isPercentage;


    public int getMaxTier() {
        return maxTier;
    }

    public double getEffect() {
        return effect;
    }

    public boolean isPercentage() {
        return isPercentage;
    }

}

