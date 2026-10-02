package com.stpa.commands;

import com.stpa.STpa;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StpaReloadCommand implements CommandExecutor {

    private final STpa plugin;

    public StpaReloadCommand(STpa plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player && !player.isOp() && !player.hasPermission("stpa.admin")) {
            sender.sendMessage(plugin.lang().get("no-permission"));
            return true;
        }

        plugin.reloadConfig();
        plugin.lang().reload();
        sender.sendMessage(plugin.lang().get("reload-success"));
        return true;
    }
}
