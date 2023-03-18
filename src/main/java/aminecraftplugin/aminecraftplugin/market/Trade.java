package aminecraftplugin.aminecraftplugin.market;

import org.bukkit.inventory.ItemStack;

public class Trade {

    ItemStack item;
    String name;
    double value;

    public Trade(ItemStack _item, String _name, double _value) {
        item = _item;
        name = _name;
        value = _value;
    }

}
