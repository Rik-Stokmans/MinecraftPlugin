package aminecraftplugin.aminecraftplugin.market;

import org.bukkit.Location;
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
    Location location;
    ArrayList<Integer> trades;

    //market constructors
    public Market() {

    }

    public Market(Location _location) {
        location = _location;
        trades = new ArrayList<>();
    }

    public Market(Location _location, ArrayList<Integer> _trades) {
        location = _location;
        trades = _trades;
    }

    private void openMarketMenu(Market m) {

    }

    //function that detects if a player clicks on a market block
    @EventHandler
    public void marketClick(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        if (e.getClickedBlock() == null) return;
        Location clickLocation = e.getClickedBlock().getLocation();

        for(Market m : markets.values()) {
            if (m.location.equals(clickLocation)) {
                openMarketMenu(m);
            }
        }
    }

    //function that updates all market prices
    public void tick() {

    }

    //methods to load and save the markets
    public void saveMarketsToFile() {

    }

    public void loadMarketsFromFile() {

    }

}
