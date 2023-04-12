package aminecraftplugin.aminecraftplugin.drilling.drill;

public enum DrillType {


    starterDrill(10.0, 5.0),
    NULL(0, 0);

    DrillType(double maxHP, double minRadius) {
        this.maxHP = maxHP;
        this.minRadius = minRadius;
    }

    private double maxHP;
    private double minRadius;

    public String getDisplayName(){
        switch (this){
            case starterDrill:
                return "Starter drill";
        }
        return "";
    }

    public String getNameFromDrillType(){
        switch (this){
            case starterDrill:
                return "starterdrill";
        }
        return "";
    }

    public static DrillType getDrillTypeFromName(String s){
        switch (s){
            case "starterdrill":
                return starterDrill;
        }
        return NULL;
    }

    public double getMaxHP() {
        return maxHP;
    }

    public double getMinRadius() {
        return minRadius;
    }
}
