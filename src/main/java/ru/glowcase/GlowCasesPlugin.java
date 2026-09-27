package ru.glowcase;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import ru.glowcase.animation.WorldRouletteAnimation;
import ru.glowcase.case_.CaseManager;
import ru.glowcase.command.CasesCommand;
import ru.glowcase.command.GlowCasesCommand;
import ru.glowcase.effect.WorldEffectManager;
import ru.glowcase.hologram.HologramManager;
import ru.glowcase.listener.CaseBlockListener;
import ru.glowcase.listener.InventoryListener;
import ru.glowcase.listener.PlayerListener;
import ru.glowcase.placeholder.GlowCasesExpansion;
import ru.glowcase.storage.StorageManager;
import ru.glowcase.user.UserManager;

import java.io.File;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GlowCasesPlugin extends JavaPlugin {

    private static GlowCasesPlugin instance;

    private CaseManager caseManager;
    private UserManager userManager;
    private StorageManager storageManager;
    private HologramManager hologramManager;
    private WorldEffectManager worldEffectManager;

    private FileConfiguration messagesConfig;
    private File messagesFile;

    private final Set<UUID> activeOpeners = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Set<Location> activeBlocks = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Set<WorldRouletteAnimation> runningAnimations = Collections.newSetFromMap(new ConcurrentHashMap<>());

    @Override
    public void onEnable() {
        instance = this;

        // Save default configs
        saveDefaultConfig();
        loadMessages();

        // Initialize Managers
        this.storageManager = new StorageManager(this);
        this.storageManager.init();

        this.userManager = new UserManager(this);
        this.caseManager = new CaseManager(this);
        this.caseManager.load();

        this.hologramManager = new HologramManager(this);
        this.worldEffectManager = new WorldEffectManager(this);

        // Load online players (e.g. during reload)
        for (Player player : Bukkit.getOnlinePlayers()) {
            this.userManager.loadUser(player.getUniqueId(), player.getName());
        }

        // Register Listeners
        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
        Bukkit.getPluginManager().registerEvents(new InventoryListener(this), this);
        Bukkit.getPluginManager().registerEvents(new CaseBlockListener(this), this);

        // Register Commands
        GlowCasesCommand glowCasesCmd = new GlowCasesCommand(this);
        if (getCommand("glowcases") != null) {
            getCommand("glowcases").setExecutor(glowCasesCmd);
            getCommand("glowcases").setTabCompleter(glowCasesCmd);
        }

        if (getCommand("cases") != null) {
            getCommand("cases").setExecutor(new CasesCommand(this));
        }

        // Register PlaceholderAPI expansion if present
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new GlowCasesExpansion(this).register();
            getLogger().info("Registered PlaceholderAPI expansion successfully.");
        }

        // Spawn holograms after server finished loading
        Bukkit.getScheduler().runTaskLater(this, () -> {
            this.hologramManager.spawnAll();
        }, 20L);

        // Auto-save task every 5 minutes
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            if (userManager != null) {
                userManager.saveAll(true);
            }
        }, 6000L, 6000L);

        getLogger().info("GlowCases v" + getDescription().getVersion() + " has been enabled!");
    }

    @Override
    public void onDisable() {
        // Clean up all running world animations
        for (WorldRouletteAnimation anim : runningAnimations) {
            if (anim != null) {
                anim.cleanUp();
            }
        }
        runningAnimations.clear();
        activeBlocks.clear();
        activeOpeners.clear();

        if (hologramManager != null) {
            hologramManager.despawnAll();
        }

        if (userManager != null) {
            userManager.saveAll(false);
        }

        if (storageManager != null) {
            storageManager.shutdown();
        }

        getLogger().info("GlowCases has been safely disabled.");
        instance = null;
    }

    public void reload() {
        reloadConfig();
        loadMessages();
        if (caseManager != null) {
            caseManager.load();
        }
        if (hologramManager != null) {
            hologramManager.spawnAll();
        }
    }

    public void loadMessages() {
        if (messagesFile == null) {
            messagesFile = new File(getDataFolder(), "messages.yml");
        }
        if (!messagesFile.exists()) {
            saveResource("messages.yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public FileConfiguration getMessages() {
        if (messagesConfig == null) {
            loadMessages();
        }
        return messagesConfig;
    }

    public static GlowCasesPlugin getInstance() {
        return instance;
    }

    public CaseManager getCaseManager() {
        return caseManager;
    }

    public UserManager getUserManager() {
        return userManager;
    }

    public StorageManager getStorageManager() {
        return storageManager;
    }

    public HologramManager getHologramManager() {
        return hologramManager;
    }

    public WorldEffectManager getWorldEffectManager() {
        return worldEffectManager;
    }

    public Set<UUID> getActiveOpeners() {
        return activeOpeners;
    }

    public Set<Location> getActiveBlocks() {
        return activeBlocks;
    }

    public Set<WorldRouletteAnimation> getRunningAnimations() {
        return runningAnimations;
    }
}
