package com.stpa.managers;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class RequestManager {

    private record PendingRequest(UUID requester, long sentAtMillis) {
    }

    private final JavaPlugin plugin;
    private final File dataFile;
    private YamlConfiguration dataConfig;

    // target UUID -> bekleyen istekler (en yeni en sonda)
    private final Map<UUID, Deque<PendingRequest>> pendingRequests = new HashMap<>();

    // requester UUID -> son istek gonderim zamani (cooldown icin)
    private final Map<UUID, Long> lastRequestSentAt = new HashMap<>();

    private final Set<UUID> disabledPlayers = new HashSet<>();
    private boolean frozen = false;

    public RequestManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "data.yml");
        load();
    }

    // ---------- Cooldown ----------

    /**
     * requester icin kalan cooldown suresini (saniye) dondurur; 0 ise hemen istek gonderebilir.
     */
    public long getCooldownRemaining(UUID requester) {
        int cooldownSeconds = plugin.getConfig().getInt("cooldown-seconds", 0);
        if (cooldownSeconds <= 0) {
            return 0;
        }

        Long lastSent = lastRequestSentAt.get(requester);
        if (lastSent == null) {
            return 0;
        }

        long elapsed = (System.currentTimeMillis() - lastSent) / 1000L;
        long remaining = cooldownSeconds - elapsed;
        return Math.max(remaining, 0);
    }

    // ---------- Istek yonetimi ----------

    public void sendRequest(UUID requester, UUID target) {
        boolean allowMultiple = plugin.getConfig().getBoolean("allow-multiple-requests", false);

        Deque<PendingRequest> deque = pendingRequests.computeIfAbsent(target, k -> new ArrayDeque<>());
        if (!allowMultiple) {
            deque.clear();
        }
        deque.addLast(new PendingRequest(requester, System.currentTimeMillis()));

        lastRequestSentAt.put(requester, System.currentTimeMillis());
    }

    /**
     * target oyuncusunun en guncel bekleyen istegini (varsa) dondurur; yoksa veya suresi dolduysa null doner.
     * Suresi dolmus eski istekleri de temizler.
     */
    public UUID getPendingRequester(UUID target) {
        Deque<PendingRequest> deque = pendingRequests.get(target);
        if (deque == null) {
            return null;
        }

        int expireSeconds = plugin.getConfig().getInt("request-expire-seconds", 60);

        while (!deque.isEmpty()) {
            PendingRequest last = deque.peekLast();
            if (expireSeconds > 0) {
                long elapsed = (System.currentTimeMillis() - last.sentAtMillis()) / 1000L;
                if (elapsed > expireSeconds) {
                    deque.pollLast();
                    continue;
                }
            }
            return last.requester();
        }

        pendingRequests.remove(target);
        return null;
    }

    /**
     * target icin en guncel (kabul edilen) istegi kaldirir.
     */
    public void clearRequest(UUID target) {
        Deque<PendingRequest> deque = pendingRequests.get(target);
        if (deque == null) {
            return;
        }
        deque.pollLast();
        if (deque.isEmpty()) {
            pendingRequests.remove(target);
        }
    }

    /**
     * target icin hala bekleyen (bir onceki kabulden sonra kalan) istek sayisini dondurur.
     */
    public int remainingRequestCount(UUID target) {
        Deque<PendingRequest> deque = pendingRequests.get(target);
        return deque == null ? 0 : deque.size();
    }

    // ---------- Disable/Enable yonetimi ----------

    public boolean isDisabled(UUID uuid) {
        return disabledPlayers.contains(uuid);
    }

    public boolean disable(UUID uuid) {
        boolean changed = disabledPlayers.add(uuid);
        if (changed) {
            save();
        }
        return changed;
    }

    public boolean enable(UUID uuid) {
        boolean changed = disabledPlayers.remove(uuid);
        if (changed) {
            save();
        }
        return changed;
    }

    // ---------- Freeze yonetimi ----------

    public boolean isFrozen() {
        return frozen;
    }

    public void setFrozen(boolean frozen) {
        this.frozen = frozen;
        save();
    }

    // ---------- Kalici veri (data.yml) ----------

    private void load() {
        if (!dataFile.exists()) {
            plugin.getDataFolder().mkdirs();
            dataConfig = new YamlConfiguration();
            return;
        }

        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        frozen = dataConfig.getBoolean("frozen", false);

        for (String uuidStr : dataConfig.getStringList("disabled-players")) {
            try {
                disabledPlayers.add(UUID.fromString(uuidStr));
            } catch (IllegalArgumentException ignored) {
                // gecersiz UUID satirini atla
            }
        }
    }

    public void save() {
        if (dataConfig == null) {
            dataConfig = new YamlConfiguration();
        }

        dataConfig.set("frozen", frozen);
        dataConfig.set("disabled-players", disabledPlayers.stream().map(UUID::toString).toList());

        try {
            plugin.getDataFolder().mkdirs();
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().warning("data.yml kaydedilemedi: " + e.getMessage());
        }
    }
}
