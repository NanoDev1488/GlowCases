package ru.glowcase.storage;

import ru.glowcase.GlowCasesPlugin;

import java.util.logging.Level;

public class StorageManager {

    private final GlowCasesPlugin plugin;
    private DataStorage storage;

    public StorageManager(GlowCasesPlugin plugin) {
        this.plugin = plugin;
    }

    public void init() {
        String type = plugin.getConfig().getString("storage.type", "SQLITE").toUpperCase();
        try {
            switch (type) {
                case "MYSQL":
                    this.storage = new MySQLStorage(plugin);
                    break;
                case "YAML":
                    this.storage = new YamlStorage(plugin);
                    break;
                case "SQLITE":
                default:
                    this.storage = new SQLiteStorage(plugin);
                    break;
            }
            this.storage.init();
            plugin.getLogger().info("Storage initialized with backend: " + type);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to initialize storage type " + type + ", falling back to YAML!", e);
            try {
                this.storage = new YamlStorage(plugin);
                this.storage.init();
            } catch (Exception ex) {
                plugin.getLogger().log(Level.SEVERE, "Failed to initialize fallback YAML storage!", ex);
            }
        }
    }

    public DataStorage getStorage() {
        return storage;
    }

    public void shutdown() {
        if (storage != null) {
            storage.close();
        }
    }
}
