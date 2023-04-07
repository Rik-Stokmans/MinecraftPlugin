package aminecraftplugin.aminecraftplugin.drilling.drill;

public enum DrillType {

    starterDrill,
    NULL;

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

}
