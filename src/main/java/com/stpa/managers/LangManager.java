package com.stpa.managers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

public class LangManager {

    private final JavaPlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private String language;

    public LangManager(JavaPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        String lang = plugin.getConfig().getString("language", "turkish").toLowerCase();
        if (!lang.equals("turkish") && !lang.equals("russia") && !lang.equals("english")) {
            plugin.getLogger().warning("Gecersiz language degeri: '" + lang + "'. 'turkish' kullaniliyor.");
            lang = "turkish";
        }
        this.language = lang;
    }

    private String raw(String key) {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("messages." + language);
        String value = section != null ? section.getString(key) : null;

        if (value == null) {
            // Yedek olarak turkish'e dus
            ConfigurationSection fallback = plugin.getConfig().getConfigurationSection("messages.turkish");
            value = fallback != null ? fallback.getString(key) : null;
        }

        return value;
    }

    /**
     * key icin config.yml -> messages.<dil>.<key> degerini MiniMessage olarak render edilmis Component halinde dondurur.
     */
    public Component get(String key) {
        String value = raw(key);
        if (value == null) {
            return Component.text("[missing message: " + key + "]", NamedTextColor.RED);
        }
        return miniMessage.deserialize(value);
    }

    public Component get(String key, String placeholder, String value) {
        String template = raw(key);
        if (template == null) {
            return Component.text("[missing message: " + key + "]", NamedTextColor.RED);
        }
        template = template.replace(placeholder, value);
        return miniMessage.deserialize(template);
    }
}
