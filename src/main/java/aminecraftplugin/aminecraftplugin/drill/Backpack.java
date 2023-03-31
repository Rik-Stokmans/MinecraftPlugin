package aminecraftplugin.aminecraftplugin.drill;

import java.util.HashMap;

public class Backpack {


    //key int is item key/ID
    //value is the amount of the item the player has in Kg
    private double space;
    private HashMap<Integer, Double> backpack;

    public Backpack() {
        this.space = 5.0;
        this.backpack = new HashMap<>();
    }

    public Backpack(HashMap<Integer, Double> backpack, double space){
        this.backpack = backpack;
        this.space = space;
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

    public double addResource(int key, double amount) {
        double leftOver = 0.0;

        double emptySpace = getEmptySpace();

        //not enough room in backpack
        if (emptySpace < amount) {
            leftOver = amount - emptySpace;
            if (backpack.containsKey(key)) backpack.put(key, backpack.get(key) + emptySpace);
            else backpack.put(key, emptySpace);
        } //has enough room
        else {
            if (backpack.containsKey(key)) backpack.put(key, backpack.get(key) + amount);
            else backpack.put(key, amount);
        }
        //returns the amount of items it was unable to put in the backpack
        return leftOver;
    }

    public double getEmptySpace() {
        double emptySpace = space;
        for (double itemAmount : backpack.values()) {
            emptySpace -= itemAmount;
        }
        return emptySpace;
    }


    public HashMap<Integer, Double> getBackpack() {
        return backpack;
    }
}
