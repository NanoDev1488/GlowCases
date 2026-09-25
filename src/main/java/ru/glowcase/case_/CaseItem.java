package ru.glowcase.case_;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import ru.glowcase.util.ColorUtil;
import ru.glowcase.util.ItemBuilder;

import java.util.ArrayList;
import java.util.List;

public class CaseItem {

    private final String id;
    private final Material material;
    private final String name;
    private final List<String> lore;
    private final CaseRarity rarity;
    private final double chance;
    private final boolean broadcast;
    private final List<String> commands;
    private final Integer customModelData;
    private final boolean glowing;

    public CaseItem(String id, Material material, String name, List<String> lore,
                    CaseRarity rarity, double chance, boolean broadcast,
                    List<String> commands, Integer customModelData, boolean glowing) {
        this.id = id;
        this.material = material != null ? material : Material.STONE;
        this.name = name;
        this.lore = lore != null ? lore : new ArrayList<>();
        this.rarity = rarity != null ? rarity : CaseRarity.COMMON;
        this.chance = chance;
        this.broadcast = broadcast;
        this.commands = commands != null ? commands : new ArrayList<>();
        this.customModelData = customModelData;
        this.glowing = glowing;
    }

    public String getId() {
        return id;
    }

    public Material getMaterial() {
        return material;
    }

    public String getName() {
        return name;
    }

    public String getColoredName() {
        return ColorUtil.color(name);
    }

    public List<String> getLore() {
        return lore;
    }

    public CaseRarity getRarity() {
        return rarity;
    }

    public double getChance() {
        return chance;
    }

    public boolean isBroadcast() {
        return broadcast;
    }

    public List<String> getCommands() {
        return commands;
    }

    public Integer getCustomModelData() {
        return customModelData;
    }

    public boolean isGlowing() {
        return glowing;
    }

    /**
     * Builds the preview / roulette icon for this case item.
     */
    public ItemStack toItemStack() {
        ItemBuilder builder = new ItemBuilder(material)
                .name(name)
                .lore(lore)
                .glowing(glowing)
                .customModelData(customModelData)
                .hideAllFlags();
        return builder.build();
    }
}
