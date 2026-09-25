package ru.glowcase.case_;

import org.bukkit.Color;

public enum CaseRarity {
    COMMON("Обычный", "&f", Color.WHITE),
    UNCOMMON("Необычный", "&a", Color.LIME),
    RARE("Редкий", "&b", Color.AQUA),
    EPIC("Эпический", "&3", Color.BLUE),
    MYTHICAL("Мифический", "&#d633ff", Color.FUCHSIA),
    LEGENDARY("Легендарный", "&#ffd700", Color.YELLOW);

    private final String defaultName;
    private final String colorCode;
    private final Color fireworkColor;

    CaseRarity(String defaultName, String colorCode, Color fireworkColor) {
        this.defaultName = defaultName;
        this.colorCode = colorCode;
        this.fireworkColor = fireworkColor;
    }

    public String getDefaultName() {
        return defaultName;
    }

    public String getColorCode() {
        return colorCode;
    }

    public Color getFireworkColor() {
        return fireworkColor;
    }

    public static CaseRarity fromString(String name) {
        if (name == null) return COMMON;
        try {
            return CaseRarity.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return COMMON;
        }
    }
}
