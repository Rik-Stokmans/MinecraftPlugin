package aminecraftplugin.aminecraftplugin.utils;

import org.bukkit.Location;

public class inRegion {

    public static boolean inRegion(Location eventloc, Location loc1, Location loc2) {
        double x1 = loc1.getX();
        double y1 = loc1.getY();
        double z1 = loc1.getZ();

        double x2 = loc2.getX();
        double y2 = loc2.getY();
        double z2 = loc2.getZ();

        double xP = eventloc.getX();
        double yP = eventloc.getY();
        double zP = eventloc.getZ();

        if((x1 <= xP && xP <= x2 || x1 >= xP && xP >= x2)
                && (z1 <= zP && zP <= z2 || z1 >= zP && zP >= z2)
                && (y1 <= yP && yP <= y2 || y1 >= yP && yP >= y2)){
            return true;
        } else {
            return false;
        }
    }

}
