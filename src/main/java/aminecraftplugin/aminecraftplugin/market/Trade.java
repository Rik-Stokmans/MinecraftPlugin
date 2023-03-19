package aminecraftplugin.aminecraftplugin.market;

import org.bukkit.inventory.ItemStack;

public class Trade {

    ItemStack item;
    String name;
    double value;
    int itemKey;

    public Trade(ItemStack _item, String _name, double _value, int _itemKey) {
        item = _item;
        name = _name;
        value = _value;
        itemKey = _itemKey;
    }

}
