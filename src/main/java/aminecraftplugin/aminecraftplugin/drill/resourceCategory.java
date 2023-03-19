package aminecraftplugin.aminecraftplugin.drill;

public enum resourceCategory {

        METALS,
        ENERGY,
        GEMSTONES,
        ARCHEOLOGY,
        NULL;

    public static resourceCategory getCategory(String s){
        switch (s){
            case "METALS":
                return resourceCategory.METALS;
            case "ENERGY":
                return resourceCategory.ENERGY;
            case "GEMSTONES":
                return resourceCategory.GEMSTONES;
            case "ARCHEOLOGY":
                return resourceCategory.ARCHEOLOGY;
        }
        return resourceCategory.NULL;
    }
}
