package com.stpa.managers;

import com.stpa.STpa;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * teleport-warmup-seconds config degeri 0'dan buyukse, isinlanma kabul edildikten sonra
 * oyuncu birkac saniye yerinde beklemek zorunda kalir. Bu sure icinde hareket eder ya da
 * hasar alirsa isinlanma iptal olur. 0 ise (varsayilan) hicbir bekleme olmadan aninda calisir.
 *
 * Folia (ve bolge bazli diger forklar) icin Bukkit'in global scheduler'i yerine
 * oyuncuya ozel EntityScheduler kullanilir; bu sayede ayni kod hem Paper'da hem Folia'da calisir.
 */
public class WarmupManager implements Listener {

    private final STpa plugin;
    private final Map<UUID, ScheduledTask> tasks = new HashMap<>();
    private final Map<UUID, Location> startLocations = new HashMap<>();

    public WarmupManager(STpa plugin) {
        this.plugin = plugin;
    }

    public boolean isWarmingUp(UUID uuid) {
        return tasks.containsKey(uuid);
    }

    /**
     * toTeleport oyuncusu icin warmup baslatir; sure 0 ise onSuccess aninda calisir (bekleme yok).
     */
    public void start(Player toTeleport, Runnable onSuccess) {
        int seconds = plugin.getConfig().getInt("teleport-warmup-seconds", 0);

        if (seconds <= 0) {
            onSuccess.run();
            return;
        }

        // Zaten bir warmup varsa iptal et, yenisiyle degistir.
        cancelSilently(toTeleport.getUniqueId());

        startLocations.put(toTeleport.getUniqueId(), toTeleport.getLocation());
        toTeleport.sendMessage(plugin.lang().get("warmup-started", "%seconds%", String.valueOf(seconds)));

        int[] remaining = {seconds};
        ScheduledTask task = toTeleport.getScheduler().runAtFixedRate(plugin, scheduledTask -> {
            if (remaining[0] <= 0) {
                cancelSilently(toTeleport.getUniqueId());
                onSuccess.run();
                return;
            }
            plugin.sounds().play(toTeleport, "warmup-tick");
            toTeleport.sendMessage(plugin.lang().get("warmup-tick", "%seconds%", String.valueOf(remaining[0])));
            remaining[0]--;
        }, () -> cancelSilently(toTeleport.getUniqueId()), 1L, 20L);

        if (task != null) {
            tasks.put(toTeleport.getUniqueId(), task);
        }
    }

    private void cancelSilently(UUID uuid) {
        ScheduledTask task = tasks.remove(uuid);
        startLocations.remove(uuid);
        if (task != null) {
            task.cancel();
        }
    }

    private void cancelWithMessage(UUID uuid) {
        boolean wasActive = tasks.containsKey(uuid);
        cancelSilently(uuid);

        if (wasActive) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                plugin.sounds().play(player, "warmup-cancelled");
                player.sendMessage(plugin.lang().get("warmup-cancelled"));
            }
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if (!isWarmingUp(uuid)) {
            return;
        }

        Location from = startLocations.get(uuid);
        Location to = event.getTo();
        if (from == null || to == null) {
            return;
        }

        // Sadece blok bazinda gercek hareketi say, kafa donmelerini (bakis acisi) yok say.
        if (from.getBlockX() != to.getBlockX()
                || from.getBlockY() != to.getBlockY()
                || from.getBlockZ() != to.getBlockZ()) {
            cancelWithMessage(uuid);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        cancelWithMessage(player.getUniqueId());
    }
}
