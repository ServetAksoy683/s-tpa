package com.stpa.commands;

import com.stpa.STpa;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StpaEnableCommand implements CommandExecutor {

    private final STpa plugin;

    public StpaEnableCommand(STpa plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.lang().get("only-players"));
            return true;
        }

        boolean changed = plugin.requests().enable(player.getUniqueId());
        if (changed) {
            player.sendMessage(plugin.lang().get("enable-success"));
        } else {
            player.sendMessage(plugin.lang().get("enable-already"));
            plugin.sounds().play(player, "error");
        }
        return true;
    }
}
