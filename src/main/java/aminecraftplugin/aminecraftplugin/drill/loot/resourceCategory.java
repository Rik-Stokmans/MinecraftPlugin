package aminecraftplugin.aminecraftplugin.drill.loot;

public enum resourceCategory {

        ALL,
        METALS,
        ENERGY,
        GEMSTONES,
        ARCHEOLOGY,
        OTHER;


    public String getDisplayName(){
        switch (this){
            case ALL:
                return "ALL";
            case METALS:
                return "METALS";
            case ENERGY:
                return "ENERGY";
            case GEMSTONES:
                return "GEMSTONES";
            case ARCHEOLOGY:
                return "ARCHEOLOGY";
            case OTHER:
                return "OTHER";
        }
        return "";
    }

    public static resourceCategory getCategory(String s){
        switch (s){
            case "ALL":
                return ALL;
            case "METALS":
                return METALS;
            case "ENERGY":
                return ENERGY;
            case "GEMSTONES":
                return GEMSTONES;
            case "ARCHEOLOGY":
                return ARCHEOLOGY;
            case "OTHER":
                return OTHER;
        }
        return ALL;
    }

}
