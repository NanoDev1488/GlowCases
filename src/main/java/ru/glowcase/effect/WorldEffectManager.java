package ru.glowcase.effect;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;
import ru.glowcase.GlowCasesPlugin;
import ru.glowcase.case_.Case;
import ru.glowcase.case_.CaseItem;

/**
 * Менеджер визуальных эффектов и частиц в мире для GlowCases (HolyWorld style).
 */
public class WorldEffectManager {

    private final GlowCasesPlugin plugin;

    public WorldEffectManager(GlowCasesPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Воспроизводит частицы во время тика прокрутки в воздухе.
     */
    public void playTickParticles(Location baseLoc, double angleOffset, Case targetCase) {
        if (baseLoc == null || baseLoc.getWorld() == null) return;
        if (!plugin.getConfig().getBoolean("world-effects.particles.enabled", true)) return;

        Location center = baseLoc.clone().add(0.5, 0.5, 0.5);

        // Получаем частицы из конфига
        String spiralParticleName = plugin.getConfig().getString("world-effects.particles.spiral-particle", "REDSTONE");
        Particle spiralParticle = ParticleUtil.getParticle(spiralParticleName, Particle.REDSTONE);
        Color spiralColor = parseColor(plugin.getConfig().getString("world-effects.particles.spiral-color", "#FB8C00"));

        // Двойная восходящая спираль частиц вокруг сундука
        ParticleUtil.spawnHelix(center, 0.9, 1.8, angleOffset, spiralParticle, spiralColor);

        // Нижнее кольцо пламени у основания блока
        if (plugin.getConfig().getBoolean("world-effects.particles.rings-enabled", true)) {
            Particle ringParticle = ParticleUtil.getParticle(plugin.getConfig().getString("world-effects.particles.ring-particle", "FLAME"), Particle.FLAME);
            ParticleUtil.spawnRing(center, 1.1, 10, ringParticle, null);
        }
    }

    /**
     * Воспроизводит праздничные визуальные эффекты в момент выигрыша.
     */
    public void playWinCelebration(Location baseLoc, CaseItem winningItem, Player player) {
        if (baseLoc == null || baseLoc.getWorld() == null) return;
        World world = baseLoc.getWorld();
        Location center = baseLoc.clone().add(0.5, 1.0, 0.5);

        // 1. Вспышка / Сферический взрыв частиц
        if (plugin.getConfig().getBoolean("world-effects.celebration.nova-enabled", true)) {
            Particle novaParticle = ParticleUtil.getParticle(plugin.getConfig().getString("world-effects.celebration.nova-particle", "FIREWORKS_SPARK"), Particle.FIREWORKS_SPARK);
            ParticleUtil.spawnSphereBurst(center, 1.8, 35, novaParticle, null);
        }

        // 2. Столб частиц света в небеса
        if (plugin.getConfig().getBoolean("world-effects.celebration.pillar-enabled", true)) {
            Particle pillarParticle = ParticleUtil.getParticle(plugin.getConfig().getString("world-effects.celebration.pillar-particle", "END_ROD"), Particle.END_ROD);
            ParticleUtil.spawnPillar(center, 4.5, 0.4, pillarParticle, null);
        }

        // 3. Золотые/Мистические кольца цвета редкости
        Color rarityColor = winningItem.getRarity().getFireworkColor();
        ParticleUtil.spawnRing(center.clone().add(0, 0.5, 0), 1.5, 16, Particle.REDSTONE, rarityColor);
        ParticleUtil.spawnRing(center.clone().add(0, 1.2, 0), 0.8, 12, Particle.REDSTONE, rarityColor);

        // 4. Запуск фейерверков
        if (plugin.getConfig().getBoolean("world-effects.fireworks.enabled", true)) {
            int burstCount = plugin.getConfig().getInt("world-effects.fireworks.burst-count", 2);
            spawnCelebrationFireworks(center, winningItem, burstCount);
        }

        // 5. 3D Звук праздника вокруг кейса
        playWinSound(center);
    }

    /**
     * Каскадный запуск нескольких салютов с цветами редкости предмета.
     */
    public void spawnCelebrationFireworks(Location center, CaseItem item, int count) {
        if (center == null || center.getWorld() == null) return;
        World world = center.getWorld();

        for (int i = 0; i < count; i++) {
            long delay = i * 10L;
            int finalI = i;

            new BukkitRunnable() {
                @Override
                public void run() {
                    try {
                        double offsetX = (Math.random() - 0.5) * 1.5;
                        double offsetZ = (Math.random() - 0.5) * 1.5;
                        Location fwLoc = center.clone().add(offsetX, 0.5, offsetZ);

                        Firework fw = world.spawn(fwLoc, Firework.class);
                        FireworkMeta meta = fw.getFireworkMeta();

                        FireworkEffect.Type type = finalI % 2 == 0 ? FireworkEffect.Type.BALL_LARGE : FireworkEffect.Type.STAR;
                        Color primary = item.getRarity().getFireworkColor();
                        Color secondary = Color.YELLOW;

                        meta.addEffect(FireworkEffect.builder()
                                .with(type)
                                .withColor(primary, Color.ORANGE)
                                .withFade(secondary, Color.WHITE)
                                .flicker(true)
                                .trail(true)
                                .build());

                        meta.setPower(1);
                        fw.setFireworkMeta(meta);
                    } catch (Exception ignored) {}
                }
            }.runTaskLater(plugin, delay);
        }
    }

    public void playTickSound(Location loc, float pitch) {
        if (loc == null || loc.getWorld() == null) return;
        try {
            ConfigurationSection sec = plugin.getConfig().getConfigurationSection("roulette.tick-sound");
            String soundName = sec != null ? sec.getString("sound", "BLOCK_NOTE_BLOCK_HAT") : "BLOCK_NOTE_BLOCK_HAT";
            float vol = sec != null ? (float) sec.getDouble("volume", 0.8) : 0.8f;

            Sound sound = Sound.valueOf(soundName);
            loc.getWorld().playSound(loc, sound, vol, pitch);
        } catch (Exception ignored) {}
    }

    public void playWinSound(Location loc) {
        if (loc == null || loc.getWorld() == null) return;
        try {
            ConfigurationSection sec = plugin.getConfig().getConfigurationSection("roulette.win-sound");
            String soundName = sec != null ? sec.getString("sound", "UI_TOAST_CHALLENGE_COMPLETE") : "UI_TOAST_CHALLENGE_COMPLETE";
            float vol = sec != null ? (float) sec.getDouble("volume", 1.0) : 1.0f;
            float pitch = sec != null ? (float) sec.getDouble("pitch", 1.0) : 1.0f;

            Sound sound = Sound.valueOf(soundName);
            loc.getWorld().playSound(loc, sound, vol, pitch);
        } catch (Exception ignored) {}
    }

    private Color parseColor(String hex) {
        if (hex == null || !hex.startsWith("#")) return Color.ORANGE;
        try {
            int r = Integer.parseInt(hex.substring(1, 3), 16);
            int g = Integer.parseInt(hex.substring(3, 5), 16);
            int b = Integer.parseInt(hex.substring(5, 7), 16);
            return Color.fromRGB(r, g, b);
        } catch (Exception e) {
            return Color.ORANGE;
        }
    }
}
