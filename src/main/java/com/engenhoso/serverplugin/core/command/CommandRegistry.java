package com.engenhoso.serverplugin.core.command;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

public class CommandRegistry {

    private final JavaPlugin plugin;

    public CommandRegistry(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void register(String commandName, CommandExecutor executor) {
        PluginCommand command = plugin.getCommand(commandName);

        if (command == null) {
            plugin.getLogger().warning("Comando não encontrado no plugin.yml: " + commandName);
            return;
        }

        command.setExecutor(executor);

        if (executor instanceof TabCompleter tabCompleter) {
            command.setTabCompleter(tabCompleter);
        }
    }
}