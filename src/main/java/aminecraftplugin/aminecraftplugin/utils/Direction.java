package aminecraftplugin.aminecraftplugin.utils;

import it.unimi.dsi.fastutil.Pair;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.bukkit.entity.Player;

import java.util.Map;

public class Direction {


    /**
     * Get the cardinal compass direction of a player.
     *
     * @param player
     * @return
     */
    public static String getCardinalDirection(Player player) {
        double rot = (player.getLocation().getYaw() - 90) % 360;
        if (rot < 0) {
            rot += 360.0;
        }
        return getDirection(rot);
    }

    private static String getDirection(double rot) {
        if (45 <= rot && rot < 135) {
            return "North";
        }  else if (135 <= rot && rot < 225) {
            return "East";
        } else if (225 <= rot && rot < 315) {
            return "South";
        }  else if (315 <= rot || rot < 45) {
            return "West";
        } else {
            return null;
        }
    }

    public static ImmutablePair<Integer, Integer> getXandZ(String s){
        switch (s){
            case "North":
                return new ImmutablePair<>(0, -1);
            case "East":
                return new ImmutablePair<>(1, 0);
            case "South":
                return new ImmutablePair<>(0, 1);
            case "West":
                return new ImmutablePair<>(-1, 0);
        }
        return new ImmutablePair<>(0, 0);
    }

}
