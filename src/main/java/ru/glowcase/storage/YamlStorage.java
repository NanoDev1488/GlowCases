package ru.glowcase.storage;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.glowcase.GlowCasesPlugin;
import ru.glowcase.user.CaseUser;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class YamlStorage implements DataStorage {

    private final GlowCasesPlugin plugin;
    private final File userFolder;

    public YamlStorage(GlowCasesPlugin plugin) {
        this.plugin = plugin;
        this.userFolder = new File(plugin.getDataFolder(), "userdata");
    }

    @Override
    public void init() throws Exception {
        if (!userFolder.exists()) {
            userFolder.mkdirs();
        }
    }

    @Override
    public CaseUser loadUser(UUID uuid, String name) {
        File file = new File(userFolder, uuid.toString() + ".yml");
        CaseUser user = new CaseUser(uuid, name);
        if (!file.exists()) {
            return user;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        String storedName = config.getString("name");
        if (storedName != null && name == null) {
            user.setName(storedName);
        }
        user.setTotalOpened(config.getInt("total-opened", 0));

        ConfigurationSection keysSec = config.getConfigurationSection("keys");
        if (keysSec != null) {
            for (String caseId : keysSec.getKeys(false)) {
                user.setKeys(caseId, keysSec.getInt(caseId, 0));
            }
        }

        ConfigurationSection statsSec = config.getConfigurationSection("stats");
        if (statsSec != null) {
            for (String caseId : statsSec.getKeys(false)) {
                user.setCaseOpened(caseId, statsSec.getInt(caseId, 0));
            }
        }

        user.setDirty(false);
        return user;
    }

    @Override
    public void saveUser(CaseUser user) {
        if (user == null) return;
        File file = new File(userFolder, user.getUuid().toString() + ".yml");
        FileConfiguration config = new YamlConfiguration();

        config.set("name", user.getName());
        config.set("total-opened", user.getTotalOpened());

        for (Map.Entry<String, Integer> entry : user.getAllKeys().entrySet()) {
            config.set("keys." + entry.getKey(), entry.getValue());
        }

        for (Map.Entry<String, Integer> entry : user.getAllCaseOpened().entrySet()) {
            config.set("stats." + entry.getKey(), entry.getValue());
        }

        try {
            config.save(file);
            user.setDirty(false);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save user " + user.getUuid() + " to YAML", e);
        }
    }

    @Override
    public void saveAll() {
        // Handled by UserManager
    }

    @Override
    public void close() {
    }
}
