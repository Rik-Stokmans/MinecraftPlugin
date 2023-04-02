package aminecraftplugin.aminecraftplugin.sideSkills;

public class SideSkill {

    double xp = 0;
    int level = 0;
    double xpNeeded = 10;



    public boolean addXp(double addedAmount) {
        xp += addedAmount;

        if (xp >= xpNeeded) {
            level++;
            xpNeeded = 10 * Math.pow(Math.E, Math.pow(level, 0.4));
            return true;
        } else return false;
    }
}
