package ru.glowcase.util;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public final class SoundUtil {

    private SoundUtil() {}

    public static void play(Player player, String soundName, float volume, float pitch) {
        if (player == null || soundName == null || soundName.isEmpty()) return;
        try {
            Sound sound = Sound.valueOf(soundName.toUpperCase());
            player.playSound(player.getLocation(), sound, volume, pitch);
        } catch (IllegalArgumentException ignored) {}
    }

    public static void play(Player player, ConfigurationSection section) {
        if (player == null || section == null) return;
        String soundName = section.getString("sound");
        if (soundName == null || soundName.isEmpty()) return;
        float volume = (float) section.getDouble("volume", 1.0);
        float pitch = (float) section.getDouble("pitch", 1.0);
        play(player, soundName, volume, pitch);
    }

    public static void playAt(Location loc, String soundName, float volume, float pitch) {
        if (loc == null || loc.getWorld() == null || soundName == null || soundName.isEmpty()) return;
        try {
            Sound sound = Sound.valueOf(soundName.toUpperCase());
            loc.getWorld().playSound(loc, sound, volume, pitch);
        } catch (IllegalArgumentException ignored) {}
    }
}
