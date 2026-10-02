package com.stpa.commands;

import com.stpa.STpa;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StpaFreezeCommand implements CommandExecutor {

    private final STpa plugin;

    public StpaFreezeCommand(STpa plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player && !player.isOp() && !player.hasPermission("stpa.admin")) {
            sender.sendMessage(plugin.lang().get("no-permission"));
            return true;
        }

        if (plugin.requests().isFrozen()) {
            sender.sendMessage(plugin.lang().get("freeze-already"));
            return true;
        }

        plugin.requests().setFrozen(true);
        if (sender instanceof Player player) {
            plugin.sounds().play(player, "admin-action");
        }
        sender.sendMessage(plugin.lang().get("freeze-enabled"));
        return true;
    }
}
