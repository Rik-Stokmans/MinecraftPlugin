package aminecraftplugin.aminecraftplugin;

import aminecraftplugin.aminecraftplugin.commands.tabcompleters.nullTabCompleter;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;

import static aminecraftplugin.aminecraftplugin.Main.plugin;


public class Command {

    public Command(String name, CommandExecutor commandExecutor){

        plugin.getServer().getPluginCommand(name).setExecutor(commandExecutor);
        plugin.getServer().getPluginCommand(name).setTabCompleter(new nullTabCompleter());

    }

    public Command(String name, CommandExecutor commandExecutor, TabCompleter tabCompleter){

        plugin.getServer().getPluginCommand(name).setExecutor(commandExecutor);
        plugin.getServer().getPluginCommand(name).setTabCompleter(tabCompleter);

    }

}
