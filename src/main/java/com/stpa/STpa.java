package com.stpa;

import com.stpa.commands.StpAcceptCommand;
import com.stpa.commands.StpAdminCommand;
import com.stpa.commands.StpaCommand;
import com.stpa.commands.StpaDisableCommand;
import com.stpa.commands.StpaEnableCommand;
import com.stpa.commands.StpaFreeCommand;
import com.stpa.commands.StpaFreezeCommand;
import com.stpa.commands.StpaReloadCommand;
import com.stpa.hooks.PlaceholderApiHook;
import com.stpa.managers.LangManager;
import com.stpa.managers.RequestManager;
import com.stpa.managers.SoundManager;
import com.stpa.managers.WarmupManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class STpa extends JavaPlugin {

    private LangManager langManager;
    private RequestManager requestManager;
    private SoundManager soundManager;
    private WarmupManager warmupManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.langManager = new LangManager(this);
        this.requestManager = new RequestManager(this);
        this.soundManager = new SoundManager(this);
        this.warmupManager = new WarmupManager(this);

        getServer().getPluginManager().registerEvents(warmupManager, this);

        getCommand("stpa").setExecutor(new StpaCommand(this));
        getCommand("stpaccept").setExecutor(new StpAcceptCommand(this));
        getCommand("stpadisable").setExecutor(new StpaDisableCommand(this));
        getCommand("stpaenable").setExecutor(new StpaEnableCommand(this));
        getCommand("stpadmin").setExecutor(new StpAdminCommand(this));
        getCommand("stpafreeze").setExecutor(new StpaFreezeCommand(this));
        getCommand("stpafree").setExecutor(new StpaFreeCommand(this));
        getCommand("stpareload").setExecutor(new StpaReloadCommand(this));

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PlaceholderApiHook(this).register();
            getLogger().info("PlaceholderAPI bulundu, %stpa_...% placeholder'lari aktif edildi.");
        }

        getLogger().info("S-Tpa aktif edildi.");
    }

    @Override
    public void onDisable() {
        if (requestManager != null) {
            requestManager.save();
        }
        getLogger().info("S-Tpa devre disi birakildi.");
    }

    public LangManager lang() {
        return langManager;
    }

    public RequestManager requests() {
        return requestManager;
    }

    public SoundManager sounds() {
        return soundManager;
    }

    public WarmupManager warmup() {
        return warmupManager;
    }
}
