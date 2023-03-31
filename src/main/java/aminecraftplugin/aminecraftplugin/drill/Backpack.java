package aminecraftplugin.aminecraftplugin.drill;

import java.util.HashMap;

public class Backpack {


    //key int is item key/ID
    //value is the amount of the item the player has in Kg
    private HashMap<Integer, Double> backpack;

    public Backpack() {
        backpack = new HashMap<>();
    }

    public Backpack(HashMap<Integer, Double> backpack){
        this.backpack = backpack;
    }

    public double getItemAmountInBackpack(int key) {
        if (!backpack.containsKey(key)) return 0;
        else {
            return backpack.get(key);
        }
    }

    public void removeItemFromBackpack(int key, double removeAmount) {
        if (backpack.containsKey(key)) {
            double oldAmount = backpack.get(key);
            double newAmount = oldAmount - removeAmount;
            if (newAmount <= 0) backpack.remove(key);
            else backpack.put(key, newAmount);

        }
    }

    public HashMap<Integer, Double> getBackpack() {
        return backpack;
    }
}
