package aminecraftplugin.aminecraftplugin;

import aminecraftplugin.aminecraftplugin.drill.Resource;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic. test

        //init
        Resource.init();


        //testvermulst
        // makes an object of the main class
        // makes an object of the main class.
        //test
        Main main = this;

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
