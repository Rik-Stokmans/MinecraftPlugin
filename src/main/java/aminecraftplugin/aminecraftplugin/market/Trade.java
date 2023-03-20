package aminecraftplugin.aminecraftplugin.market;

import aminecraftplugin.aminecraftplugin.drill.Resource;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class Trade {

    ItemStack item;
    String name;
    double baseValue;
    double buyPrice;
    double sellPrice;
    int itemKey;

    //trade logic
    int stockBaseline = 1000;
    int stock = 1000;

    public Trade(int _itemKey) {
        Resource resource = Resource.resources.get(_itemKey);
        item = resource.getItemStack();
        name = resource.getName();
        baseValue = resource.getValue();
        sellPrice = baseValue;
        buyPrice = sellPrice + sellPrice * 0.05;
        itemKey = _itemKey;
    }



    //tick method to update this trade
    public void tick(boolean includeStock) {
        Random rand = new Random();
        double oneDevBaseMultiple = 1 / (stock / stockBaseline);
        if (stock > stockBaseline) {
            sellPrice = baseValue * oneDevBaseMultiple;
            if (includeStock) {
                if (rand.nextDouble() < 1 - oneDevBaseMultiple) stock--;
            }
        }
        else sellPrice = baseValue * ((-1 / stockBaseline) * stock + 2);

        buyPrice = sellPrice * 1.05;
    }

}
