package com.stpa.commands;

import com.stpa.STpa;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StpAdminCommand implements CommandExecutor {

    private final STpa plugin;

    public StpAdminCommand(STpa plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player admin)) {
            sender.sendMessage(plugin.lang().get("only-players"));
            return true;
        }

        // Sadece op olan kisiler (sunucu sahibi) kullanabilir.
        if (!admin.isOp() && !admin.hasPermission("stpa.admin")) {
            admin.sendMessage(plugin.lang().get("no-permission"));
            plugin.sounds().play(admin, "error");
            return true;
        }

        if (args.length < 1) {
            admin.sendMessage(plugin.lang().get("usage-stpadmin"));
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null || !target.isOnline()) {
            admin.sendMessage(plugin.lang().get("player-not-found", "%player%", args[0]));
            plugin.sounds().play(admin, "error");
            return true;
        }

        plugin.requests().clearRequest(admin.getUniqueId());

        target.teleportAsync(admin.getLocation()).thenAccept(success -> {
            if (Boolean.TRUE.equals(success)) {
                plugin.sounds().play(admin, "admin-action");
                plugin.sounds().play(target, "teleport-success");
                admin.sendMessage(plugin.lang().get("admin-teleport-success", "%player%", target.getName()));
                target.sendMessage(plugin.lang().get("teleported-notify", "%player%", admin.getName()));
            }
        });

        return true;
    }
}
