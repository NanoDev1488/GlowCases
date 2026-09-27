package ru.glowcase.effect;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;

import java.util.HashMap;
import java.util.Map;

/**
 * Утилита для безопасного создания партиклов и математических визуальных паттернов.
 * Полностью совместима с версиями Spigot / Paper 1.16.5 - 1.20.x+.
 */
public class ParticleUtil {

    private static final Map<String, Particle> PARTICLE_CACHE = new HashMap<>();

    static {
        for (Particle p : Particle.values()) {
            PARTICLE_CACHE.put(p.name().toUpperCase(), p);
        }
    }

    /**
     * Безопасное получение типа частицы по строковому названию с запасным вариантом.
     */
    public static Particle getParticle(String name, Particle fallback) {
        if (name == null || name.isEmpty()) return fallback;
        Particle p = PARTICLE_CACHE.get(name.toUpperCase());
        if (p != null) return p;

        // Попробуем мягкое сопоставление
        for (Map.Entry<String, Particle> entry : PARTICLE_CACHE.entrySet()) {
            if (entry.getKey().contains(name.toUpperCase())) {
                return entry.getValue();
            }
        }
        return fallback;
    }

    /**
     * Спавн частицы с поддержкой цвета для REDSTONE (DustOptions).
     */
    public static void spawnParticle(Location loc, Particle particle, int count, double ox, double oy, double oz, double speed, Color dustColor) {
        if (loc == null || loc.getWorld() == null || particle == null) return;
        World world = loc.getWorld();

        try {
            if (particle == Particle.REDSTONE && dustColor != null) {
                Particle.DustOptions dust = new Particle.DustOptions(dustColor, 1.2f);
                world.spawnParticle(particle, loc, count, ox, oy, oz, speed, dust);
            } else {
                world.spawnParticle(particle, loc, count, ox, oy, oz, speed);
            }
        } catch (Exception ignored) {
            // Защита от ошибок версии
        }
    }

    /**
     * Создает восходящую двойную спираль (Helix) вокруг центра.
     */
    public static void spawnHelix(Location center, double radius, double height, double angleOffset, Particle particle, Color color) {
        if (center == null || center.getWorld() == null) return;

        int points = 12;
        for (int i = 0; i < points; i++) {
            double progress = (double) i / points;
            double currentY = progress * height;
            double angle = angleOffset + (progress * Math.PI * 4);

            // Первая ветвь спирали
            double x1 = center.getX() + radius * Math.cos(angle);
            double z1 = center.getZ() + radius * Math.sin(angle);
            Location loc1 = new Location(center.getWorld(), x1, center.getY() + currentY, z1);
            spawnParticle(loc1, particle, 1, 0, 0, 0, 0, color);

            // Вторая ветвь спирали (на 180 градусов со смещением)
            double x2 = center.getX() + radius * Math.cos(angle + Math.PI);
            double z2 = center.getZ() + radius * Math.sin(angle + Math.PI);
            Location loc2 = new Location(center.getWorld(), x2, center.getY() + currentY, z2);
            spawnParticle(loc2, particle, 1, 0, 0, 0, 0, color);
        }
    }

    /**
     * Создает горизонтальное расширяющееся кольцо частиц.
     */
    public static void spawnRing(Location center, double radius, int points, Particle particle, Color color) {
        if (center == null || center.getWorld() == null) return;

        for (int i = 0; i < points; i++) {
            double angle = (2 * Math.PI * i) / points;
            double x = center.getX() + radius * Math.cos(angle);
            double z = center.getZ() + radius * Math.sin(angle);
            Location loc = new Location(center.getWorld(), x, center.getY(), z);
            spawnParticle(loc, particle, 1, 0, 0, 0, 0, color);
        }
    }

    /**
     * Создает вертикальный столб частиц (Pillar of light).
     */
    public static void spawnPillar(Location center, double height, double step, Particle particle, Color color) {
        if (center == null || center.getWorld() == null) return;

        for (double y = 0; y <= height; y += step) {
            Location loc = new Location(center.getWorld(), center.getX(), center.getY() + y, center.getZ());
            spawnParticle(loc, particle, 2, 0.05, 0.05, 0.05, 0.01, color);
        }
    }

    /**
     * Создает сферический взрыв частиц (Nova / Burst).
     */
    public static void spawnSphereBurst(Location center, double radius, int points, Particle particle, Color color) {
        if (center == null || center.getWorld() == null) return;

        for (int i = 0; i < points; i++) {
            double u = Math.random();
            double v = Math.random();
            double theta = 2 * Math.PI * u;
            double phi = Math.acos(2 * v - 1);

            double r = Math.cbrt(Math.random()) * radius;
            double sinPhi = Math.sin(phi);
            double x = center.getX() + r * sinPhi * Math.cos(theta);
            double y = center.getY() + r * sinPhi * Math.sin(theta);
            double z = center.getZ() + r * Math.cos(phi);

            Location loc = new Location(center.getWorld(), x, y, z);
            spawnParticle(loc, particle, 1, 0, 0, 0, 0.02, color);
        }
    }
}
