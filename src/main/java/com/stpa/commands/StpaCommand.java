package com.stpa.commands;

import com.stpa.STpa;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StpaCommand implements CommandExecutor {

    private final STpa plugin;

    public StpaCommand(STpa plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player requester)) {
            sender.sendMessage(plugin.lang().get("only-players"));
            return true;
        }

        if (plugin.requests().isFrozen() && !requester.isOp()) {
            requester.sendMessage(plugin.lang().get("frozen-active"));
            plugin.sounds().play(requester, "error");
            return true;
        }

        if (args.length < 1) {
            requester.sendMessage(plugin.lang().get("usage-stpa"));
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null || !target.isOnline()) {
            requester.sendMessage(plugin.lang().get("player-not-found", "%player%", args[0]));
            plugin.sounds().play(requester, "error");
            return true;
        }

        if (target.getUniqueId().equals(requester.getUniqueId())) {
            requester.sendMessage(plugin.lang().get("cannot-request-self"));
            plugin.sounds().play(requester, "error");
            return true;
        }

        if (plugin.requests().isDisabled(target.getUniqueId())) {
            requester.sendMessage(plugin.lang().get("requests-disabled-by-target", "%player%", target.getName()));
            plugin.sounds().play(requester, "error");
            return true;
        }

        long cooldown = plugin.requests().getCooldownRemaining(requester.getUniqueId());
        if (cooldown > 0) {
            requester.sendMessage(plugin.lang().get("cooldown-active", "%seconds%", String.valueOf(cooldown)));
            plugin.sounds().play(requester, "error");
            return true;
        }

        plugin.requests().sendRequest(requester.getUniqueId(), target.getUniqueId());

        requester.sendMessage(plugin.lang().get("request-sent", "%player%", target.getName()));
        plugin.sounds().play(requester, "request-sent");

        target.sendMessage(plugin.lang().get("request-received", "%player%", requester.getName()));
        plugin.sounds().play(target, "request-received");

        return true;
    }
}
