package aminecraftplugin.aminecraftplugin.drill.structures;

public enum DrillType {

    starterDrill,
    NULL;


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
