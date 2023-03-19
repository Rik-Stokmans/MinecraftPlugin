package aminecraftplugin.aminecraftplugin.market;

import aminecraftplugin.aminecraftplugin.drill.Resource;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.HashMap;

public class Market implements Listener {

    //Hashmap loaded from file containing all markets
    public static HashMap<Integer, Market> markets = new HashMap<>();

    //all market attributes
    String name;
    Location location;
    ArrayList<Trade> trades;
    Inventory marketInterface;

    //market constructors
    public Market() {

    }

    public Market(String _name, Location _location) {
        name = _name;
        location = _location;
        trades = new ArrayList<>();
    }

    public Market(String _name, Location _location, ArrayList<Integer> _tradeItemKeys) {
        name = _name;
        location = _location;
        trades = generateTrades(_tradeItemKeys);
    }



    //method to generate all the trade items from a list of keys
    private ArrayList<Trade> generateTrades(ArrayList<Integer> tradeItemKeys) {
        ArrayList<Trade> trades = new ArrayList<>();
        for(int key : tradeItemKeys) {
            if (Resource.resources.containsKey(key)) {

                Resource r;


                metals, energy, gemstones, archeology


                Trade trade = new Trade(r.getItemStack(), r.getName(), r.getValue(), key);
                trades.add(trade);
            }
        }
        return trades;
    }



    //gui methods
    private void openMarketInterface(Market m) {

    }



    //function that detects if a player clicks on a market block
    @EventHandler
    public void marketClick(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        if (e.getClickedBlock() == null) return;
        Location clickLocation = e.getClickedBlock().getLocation();

        for(Market m : markets.values()) {
            if (m.location.equals(clickLocation)) {
                openMarketInterface(m);
            }
        }
    }



    //function that slowly shifts the prices to base price
    public void tick() {

    }



    //methods to load and save the markets
    public void saveMarketsToFile() {

    }

    public void loadMarketsFromFile() {

    }

}
