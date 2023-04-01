package aminecraftplugin.aminecraftplugin.drill;

import aminecraftplugin.aminecraftplugin.drill.loot.resourceCategory;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

import static aminecraftplugin.aminecraftplugin.drill.loot.Resource.getResourceFromKey;
import static aminecraftplugin.aminecraftplugin.utils.ChatUtils.format;

public class ResourceSorters {


    private static final Comparator<Map.Entry<Integer, Double>> totalValueDescending = new Comparator<Map.Entry<Integer, Double>>() {
        @Override
        public int compare(Map.Entry<Integer, Double> o1, Map.Entry<Integer, Double> o2) {
            Double firstDouble = getResourceFromKey(o2.getKey()).getValue() * o2.getValue();
            Double secondDouble = getResourceFromKey(o1.getKey()).getValue() * o1.getValue();
            return firstDouble.compareTo(secondDouble);
        }

    };
    private static final Comparator<Map.Entry<Integer, Double>> totalValueAscending = new Comparator<Map.Entry<Integer, Double>>() {
        @Override
        public int compare(Map.Entry<Integer, Double> o1, Map.Entry<Integer, Double> o2) {
            Double firstDouble = getResourceFromKey(o1.getKey()).getValue() * o1.getValue();
            Double secondDouble = getResourceFromKey(o2.getKey()).getValue() * o2.getValue();
            return firstDouble.compareTo(secondDouble);
        }

    };

    private static final Comparator<Map.Entry<Integer, Double>> KGComparatorDescending = new Comparator<Map.Entry<Integer, Double>>() {
        @Override
        public int compare(Map.Entry<Integer, Double> o1, Map.Entry<Integer, Double> o2) {
            return o2.getValue().compareTo(o1.getValue());
        }
    };

    private static final Comparator<Map.Entry<Integer, Double>> KGComparatorAscending = new Comparator<Map.Entry<Integer, Double>>() {
        @Override
        public int compare(Map.Entry<Integer, Double> o1, Map.Entry<Integer, Double> o2) {
            return o1.getValue().compareTo(o2.getValue());
        }

    };

    public static ItemStack getSortItem(int sortingIndex){

        ItemStack comparator = new ItemStack(Material.COMPARATOR);
        ItemMeta comparatorMeta = comparator.getItemMeta();
        comparatorMeta.setDisplayName(format("&aSort"));
        ArrayList<String> lore = new ArrayList<>();
        lore.add("");

        for (int i = 0; i < resourceComparators.length; i++){

            String s = "";
            if (i == sortingIndex) {
                s = format("&b&l► ");
            } else {
                s = format("&7");
            }
            switch (i){
                case 0:
                    s += "High value";
                    break;
                case 1:
                    s += "Low value";
                    break;
                case 2:
                    s += "High weight";
                    break;
                case 3:
                    s += "Low weight";
                    break;
            }
            lore.add(s);
        }
        lore.add("");
        lore.add(format("&eClick to switch sort!"));
        comparatorMeta.setLore(lore);
        comparator.setItemMeta(comparatorMeta);
        return comparator;
    }

    public static ItemStack getFilterItem(resourceCategory filterCategory){
        ItemStack filter = new ItemStack(Material.HOPPER);
        ItemMeta filterMeta = filter.getItemMeta();
        filterMeta.setDisplayName(format("&aFilter"));
        ArrayList<String> lore = new ArrayList<>();
        lore.add("");
        for (resourceCategory resourceCategory : resourceCategory.values()){
            if (filterCategory.equals(resourceCategory)){
                lore.add(format("&b&l► " + resourceCategory.getDisplayName()));
            } else {
                lore.add(format("&7" + resourceCategory.getDisplayName()));
            }
        }
        lore.add("");
        lore.add(format("&eClick to switch filter!"));
        filterMeta.setLore(lore);
        filter.setItemMeta(filterMeta);
        return filter;
    }

    public static final Comparator<Map.Entry<Integer, Double>>[] resourceComparators = new Comparator[]{totalValueDescending, totalValueAscending, KGComparatorDescending, KGComparatorAscending};


    public static List<Map.Entry<Integer, Double>> returnSortedList(Set<Map.Entry<Integer, Double>> resourceSet, int sortingIndex){
        List<Map.Entry<Integer, Double>> resourceList = new ArrayList<>(resourceSet);
        Collections.sort(resourceList, resourceComparators[sortingIndex]);
        return resourceList;
    }
}
