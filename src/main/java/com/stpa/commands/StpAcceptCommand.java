package com.stpa.commands;

import com.stpa.STpa;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class StpAcceptCommand implements CommandExecutor {

    private final STpa plugin;

    public StpAcceptCommand(STpa plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player accepter)) {
            sender.sendMessage(plugin.lang().get("only-players"));
            return true;
        }

        if (plugin.requests().isFrozen() && !accepter.isOp()) {
            accepter.sendMessage(plugin.lang().get("frozen-active"));
            plugin.sounds().play(accepter, "error");
            return true;
        }

        UUID requesterId = plugin.requests().getPendingRequester(accepter.getUniqueId());
        if (requesterId == null) {
            accepter.sendMessage(plugin.lang().get("no-pending-request"));
            plugin.sounds().play(accepter, "error");
            return true;
        }

        Player requester = Bukkit.getPlayer(requesterId);
        if (requester == null || !requester.isOnline()) {
            plugin.requests().clearRequest(accepter.getUniqueId());
            accepter.sendMessage(plugin.lang().get("no-pending-request"));
            plugin.sounds().play(accepter, "error");
            return true;
        }

        plugin.requests().clearRequest(accepter.getUniqueId());

        // Hedef konumu su an itibariyle sabitliyoruz; warmup suresince kabul eden oyuncu hareket etse bile
        // isinlanma bu konuma gerceklesir.
        Location destination = accepter.getLocation();

        int remaining = plugin.requests().remainingRequestCount(accepter.getUniqueId());
        if (remaining > 0) {
            accepter.sendMessage(plugin.lang().get("multiple-requests-remaining", "%count%", String.valueOf(remaining)));
        }

        plugin.warmup().start(requester, () -> {
            // teleportAsync Paper/Folia uyumlu isinlanma icin kullanilir (Folia'da bolge zamanlayicisini otomatik yonetir).
            requester.teleportAsync(destination).thenAccept(success -> {
                if (Boolean.TRUE.equals(success)) {
                    plugin.sounds().play(requester, "teleport-success");
                    plugin.sounds().play(accepter, "teleport-success");
                    requester.sendMessage(plugin.lang().get("teleported-you", "%player%", accepter.getName()));
                    accepter.sendMessage(plugin.lang().get("teleported-notify", "%player%", requester.getName()));
                }
            });
        });

        return true;
    }
}
