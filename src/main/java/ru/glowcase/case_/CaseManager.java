package ru.glowcase.case_;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import ru.glowcase.GlowCasesPlugin;
import ru.glowcase.util.ColorUtil;
import ru.glowcase.util.ItemBuilder;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public class CaseManager {

    private final GlowCasesPlugin plugin;
    private final Map<String, Case> cases = new LinkedHashMap<>();
    private final List<CaseBlock> caseBlocks = new ArrayList<>();
    private final File casesFolder;
    private final File blocksFile;

    public CaseManager(GlowCasesPlugin plugin) {
        this.plugin = plugin;
        this.casesFolder = new File(plugin.getDataFolder(), "cases");
        this.blocksFile = new File(plugin.getDataFolder(), "data" + File.separator + "case_blocks.yml");
    }

    public void load() {
        cases.clear();
        caseBlocks.clear();

        if (!casesFolder.exists()) {
            casesFolder.mkdirs();
            saveDefaultCase("donate.yml");
            saveDefaultCase("runes.yml");
            saveDefaultCase("spawners.yml");
            saveDefaultCase("titles.yml");
            saveDefaultCase("money.yml");
        }

        File[] files = casesFolder.listFiles((dir, name) -> name.endsWith(".yml") || name.endsWith(".yaml"));
        if (files != null) {
            for (File file : files) {
                loadCaseFile(file);
            }
        }
        plugin.getLogger().info("Loaded " + cases.size() + " cases.");

        loadCaseBlocks();
    }

    private void saveDefaultCase(String name) {
        File file = new File(casesFolder, name);
        if (!file.exists()) {
            plugin.saveResource("cases/" + name, false);
        }
    }

    public void loadCaseFile(File file) {
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        String id = config.getString("id", file.getName().replace(".yml", "").replace(".yaml", ""));
        String displayName = config.getString("display-name", id);

        // Menu item
        ItemStack menuItem = parseItem(config.getConfigurationSection("menu-item"), Material.CHEST, displayName);

        // Key item
        ItemStack keyItem = parseItem(config.getConfigurationSection("key"), Material.TRIPWIRE_HOOK, "&6Ключ от " + displayName);

        // Items
        List<CaseItem> items = new ArrayList<>();
        ConfigurationSection itemsSection = config.getConfigurationSection("items");
        if (itemsSection != null) {
            for (String key : itemsSection.getKeys(false)) {
                ConfigurationSection sec = itemsSection.getConfigurationSection(key);
                if (sec == null) continue;

                String matStr = sec.getString("material", "STONE");
                Material material = Material.matchMaterial(matStr.toUpperCase());
                if (material == null) material = Material.STONE;

                String name = sec.getString("name", key);
                List<String> lore = sec.getStringList("lore");
                CaseRarity rarity = CaseRarity.fromString(sec.getString("rarity", "COMMON"));
                double chance = sec.getDouble("chance", 1.0);
                boolean broadcast = sec.getBoolean("broadcast", false);
                List<String> commands = sec.getStringList("commands");
                Integer customModelData = sec.contains("custom-model-data") ? sec.getInt("custom-model-data") : null;
                boolean glowing = sec.getBoolean("glowing", false);

                items.add(new CaseItem(key, material, name, lore, rarity, chance, broadcast, commands, customModelData, glowing));
            }
        }

        Case loadedCase = new Case(id.toLowerCase(), displayName, menuItem, keyItem, items);
        cases.put(id.toLowerCase(), loadedCase);
    }

    private ItemStack parseItem(ConfigurationSection section, Material fallbackMaterial, String fallbackName) {
        if (section == null) {
            return new ItemBuilder(fallbackMaterial).name(fallbackName).build();
        }
        String matStr = section.getString("material", fallbackMaterial.name());
        Material mat = Material.matchMaterial(matStr.toUpperCase());
        if (mat == null) mat = fallbackMaterial;

        String name = section.getString("name", fallbackName);
        List<String> lore = section.getStringList("lore");
        boolean glowing = section.getBoolean("glowing", false);
        Integer customModelData = section.contains("custom-model-data") ? section.getInt("custom-model-data") : null;

        return new ItemBuilder(mat)
                .name(name)
                .lore(lore)
                .glowing(glowing)
                .customModelData(customModelData)
                .build();
    }

    public void loadCaseBlocks() {
        caseBlocks.clear();
        if (!blocksFile.exists()) return;

        FileConfiguration config = YamlConfiguration.loadConfiguration(blocksFile);
        List<Map<?, ?>> list = config.getMapList("blocks");
        for (Map<?, ?> map : list) {
            String world = (String) map.get("world");
            int x = ((Number) map.get("x")).intValue();
            int y = ((Number) map.get("y")).intValue();
            int z = ((Number) map.get("z")).intValue();
            String caseId = (String) map.get("case");

            if (world != null && caseId != null) {
                caseBlocks.add(new CaseBlock(world, x, y, z, caseId.toLowerCase()));
            }
        }
        plugin.getLogger().info("Loaded " + caseBlocks.size() + " case block bindings.");
    }

    public void saveCaseBlocks() {
        if (!blocksFile.getParentFile().exists()) {
            blocksFile.getParentFile().mkdirs();
        }
        FileConfiguration config = new YamlConfiguration();
        List<Map<String, Object>> list = new ArrayList<>();
        for (CaseBlock block : caseBlocks) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("world", block.getWorldName());
            map.put("x", block.getX());
            map.put("y", block.getY());
            map.put("z", block.getZ());
            map.put("case", block.getCaseId());
            list.add(map);
        }
        config.set("blocks", list);
        try {
            config.save(blocksFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save case blocks file!", e);
        }
    }

    public Case getCase(String id) {
        if (id == null) return null;
        return cases.get(id.toLowerCase());
    }

    public Collection<Case> getCases() {
        return cases.values();
    }

    public List<CaseBlock> getCaseBlocks() {
        return caseBlocks;
    }

    public CaseBlock getCaseBlockAt(Location loc) {
        for (CaseBlock cb : caseBlocks) {
            if (cb.matches(loc)) {
                return cb;
            }
        }
        return null;
    }

    public void addCaseBlock(CaseBlock caseBlock) {
        caseBlocks.removeIf(cb -> cb.equals(caseBlock));
        caseBlocks.add(caseBlock);
        saveCaseBlocks();
    }

    public boolean removeCaseBlock(Location loc) {
        boolean removed = caseBlocks.removeIf(cb -> cb.matches(loc));
        if (removed) {
            saveCaseBlocks();
        }
        return removed;
    }
}
