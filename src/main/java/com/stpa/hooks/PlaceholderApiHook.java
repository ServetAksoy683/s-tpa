package com.stpa.hooks;

import com.stpa.STpa;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * %stpa_disabled% -> bu oyuncu istekleri kapatmis mi (true/false)
 * %stpa_frozen%   -> tum istekler dondurulmus mu (true/false)
 * %stpa_pending%  -> bu oyuncunun bekleyen istek sayisi
 */
public class PlaceholderApiHook extends PlaceholderExpansion {

    private final STpa plugin;

    public PlaceholderApiHook(STpa plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "stpa";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Hadi";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.1.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) {
            return "";
        }

        return switch (params.toLowerCase()) {
            case "disabled" -> plugin.requests().isDisabled(player.getUniqueId()) ? "true" : "false";
            case "frozen" -> plugin.requests().isFrozen() ? "true" : "false";
            case "pending" -> String.valueOf(plugin.requests().remainingRequestCount(player.getUniqueId()));
            default -> null;
        };
    }
}
