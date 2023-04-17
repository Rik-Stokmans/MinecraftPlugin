package aminecraftplugin.aminecraftplugin.sideSkills;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import static aminecraftplugin.aminecraftplugin.sideSkills.SkillType.skillTypeHashMap;

public class Skill {

    private SkillType skillType;
    private int tier;
    private double xp;

    public Skill(SkillType skillType, int tier, double xp){
        this.skillType = skillType;
        this.tier = tier;
        this.xp = xp;
    }

    public double getNeededXP(){
        return 50.0;
    }

    public void addXP(double xp){
        this.xp += xp;
        double neededXP = this.getNeededXP();
        while (this.getXp() >= neededXP){
            this.tier += 1;
            this.xp -= neededXP;
            neededXP = this.getNeededXP();
        }
    }

    public SkillType getSkillType() {
        return skillType;
    }

    public int getTier() {
        return tier;
    }

    public double getXp() {
        return xp;
    }

    public double getMultiplier(){
        if (this.getSkillType().isPercentage()) {
            return 1.0 + ((this.getTier() * this.getSkillType().getEffect()) / 100.0);
        } else {
            return 1.0;
        }
    }
}
