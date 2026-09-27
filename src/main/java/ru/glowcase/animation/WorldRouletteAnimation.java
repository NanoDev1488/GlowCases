package ru.glowcase.animation;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import ru.glowcase.GlowCasesPlugin;
import ru.glowcase.case_.Case;
import ru.glowcase.case_.CaseBlock;
import ru.glowcase.case_.CaseItem;
import ru.glowcase.hologram.CaseHologram;
import ru.glowcase.user.CaseUser;
import ru.glowcase.util.ColorUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 3D-анимация открытия кейса в воздухе прямо над блоком (HolyCases / HolyWorld style).
 * 4-5 парящих предметов вращаются по орбите вокруг сундука с плавной кривой замедления,
 * звуковым нарастанием, эффектами частиц и победной 3D-голограммой с салютом.
 */
public class WorldRouletteAnimation {

    private final GlowCasesPlugin plugin;
    private final Player player;
    private final Case targetCase;
    private final CaseBlock caseBlock;
    private final CaseItem winningItem;
    private final boolean standalone;

    private final List<CaseItem> sequence = new ArrayList<>();
    private final List<ArmorStand> satelliteStands = new ArrayList<>();
    private final List<ArmorStand> winHoloStands = new ArrayList<>();
    private ArmorStand centerItemStand;

    private BukkitTask animationTask;
    private int currentIndex = 0;
    private int step = 0;
    private float currentYaw = 0f;
    private double currentAngle = 0;
    private boolean finished = false;

    public WorldRouletteAnimation(GlowCasesPlugin plugin, Player player, Case targetCase, CaseBlock caseBlock, CaseItem winningItem, boolean standalone) {
        this.plugin = plugin;
        this.player = player;
        this.targetCase = targetCase;
        this.caseBlock = caseBlock;
        this.winningItem = winningItem != null ? winningItem : targetCase.getRandomItem();
        this.standalone = standalone;

        generateSequence();
    }

    private void generateSequence() {
        int totalRolls = 36;
        for (int i = 0; i < totalRolls; i++) {
            sequence.add(targetCase.getRandomItem());
        }
        sequence.set(totalRolls - 1, winningItem);
    }

    public void start() {
        Location baseLoc = caseBlock.getLocation();
        if (baseLoc == null || baseLoc.getWorld() == null) return;
        World world = baseLoc.getWorld();

        plugin.getRunningAnimations().add(this);
        plugin.getActiveBlocks().add(baseLoc);

        // 1. Временно скрываем стандартную голограмму кейса
        CaseHologram holo = plugin.getHologramManager().getHologram(caseBlock);
        if (holo != null) {
            holo.despawn();
        }

        // 2. Спавним центральный парящий стенд
        Location centerLoc = new Location(world, baseLoc.getX() + 0.5, baseLoc.getY() + 1.2, baseLoc.getZ() + 0.5);
        centerItemStand = spawnStand(centerLoc);
        if (!sequence.isEmpty()) {
            centerItemStand.setHelmet(sequence.get(0).toItemStack());
        }

        // 3. Спавним 4 орбитальных мини-стенда вокруг сундука (как на HolyWorld)
        int satellites = 4;
        for (int i = 0; i < satellites; i++) {
            ArmorStand sat = spawnStand(centerLoc);
            sat.setSmall(true);
            satelliteStands.add(sat);
        }

        // 4. Запускаем цикл раскрутки в воздухе
        animationTask = new BukkitRunnable() {
            int tickDelay = 1;
            int ticksPassed = 0;
            int totalTicks = 0;

            @Override
            public void run() {
                totalTicks++;
                currentYaw += 16f;
                currentAngle += 0.20;

                // Вращение и плавное парение центрального предмета
                double hoverY = 1.2 + 0.14 * Math.sin(totalTicks * 0.15);
                Location currentCenter = new Location(world, baseLoc.getX() + 0.5, baseLoc.getY() + hoverY, baseLoc.getZ() + 0.5, currentYaw, 0);
                if (centerItemStand != null && centerItemStand.isValid()) {
                    centerItemStand.teleport(currentCenter);
                }

                // Вращение предметов по орбите вокруг центра кейса
                double orbitRadius = 0.9;
                for (int i = 0; i < satelliteStands.size(); i++) {
                    ArmorStand sat = satelliteStands.get(i);
                    if (sat != null && sat.isValid()) {
                        double satAngle = currentAngle + (i * (2 * Math.PI / satelliteStands.size()));
                        double satX = baseLoc.getX() + 0.5 + orbitRadius * Math.cos(satAngle);
                        double satZ = baseLoc.getZ() + 0.5 + orbitRadius * Math.sin(satAngle);
                        double satY = baseLoc.getY() + 0.95 + 0.10 * Math.cos(totalTicks * 0.18 + i);
                        Location satLoc = new Location(world, satX, satY, satZ, (float) Math.toDegrees(satAngle), 0);
                        sat.teleport(satLoc);

                        int itemIndex = (currentIndex + i + 1) % sequence.size();
                        sat.setHelmet(sequence.get(itemIndex).toItemStack());
                    }
                }

                // Партиклы спиралей и пламенных колец вокруг кейса
                plugin.getWorldEffectManager().playTickParticles(baseLoc, currentAngle, targetCase);

                ticksPassed++;
                if (ticksPassed < tickDelay) {
                    return;
                }
                ticksPassed = 0;

                // Смена текущего предмета рулетки
                currentIndex++;
                if (currentIndex < sequence.size() && centerItemStand != null) {
                    centerItemStand.setHelmet(sequence.get(currentIndex).toItemStack());
                }

                // Звук тика с нарастающим тоном (HolyCases mechanic)
                float pitch = Math.min(1.8f, 0.8f + (step * 0.03f));
                plugin.getWorldEffectManager().playTickSound(baseLoc, pitch);

                step++;

                // Математическая кривая замедления
                if (step > 32) {
                    tickDelay = 12;
                } else if (step > 28) {
                    tickDelay = 8;
                } else if (step > 24) {
                    tickDelay = 5;
                } else if (step > 18) {
                    tickDelay = 3;
                } else if (step > 11) {
                    tickDelay = 2;
                } else {
                    tickDelay = 1;
                }

                // Завершение анимации и остановка на победном предмете
                if (currentIndex >= sequence.size() - 1) {
                    finishCelebration();
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }

    public void finishCelebration() {
        if (finished) return;
        finished = true;

        if (animationTask != null) {
            animationTask.cancel();
            animationTask = null;
        }

        Location baseLoc = caseBlock.getLocation();
        if (baseLoc == null || baseLoc.getWorld() == null) {
            cleanUp();
            return;
        }
        World world = baseLoc.getWorld();

        // Убираем орбитальные стенды
        for (ArmorStand sat : satelliteStands) {
            if (sat != null && sat.isValid()) sat.remove();
        }
        satelliteStands.clear();

        // Устанавливаем победный предмет в центр на возвышении
        Location winCenter = new Location(world, baseLoc.getX() + 0.5, baseLoc.getY() + 1.5, baseLoc.getZ() + 0.5);
        if (centerItemStand != null && centerItemStand.isValid()) {
            centerItemStand.teleport(winCenter);
            centerItemStand.setHelmet(winningItem.toItemStack());
        }

        // Запуск фейерверков, светового столба и звуков
        plugin.getWorldEffectManager().playWinCelebration(baseLoc, winningItem, player);

        // Создаем парящую победную 3D-голограмму над предметом
        spawnWinHologram(baseLoc);

        // Если это standalone открытие в воздухе, выдаем награду и тайтлы
        if (standalone) {
            giveReward();
        }

        // Оставляем победный предмет и голограмму на N секунд, затем восстанавливаем кейс
        int displaySeconds = plugin.getConfig().getInt("world-effects.celebration.display-duration-seconds", 5);
        new BukkitRunnable() {
            @Override
            public void run() {
                cleanUp();
            }
        }.runTaskLater(plugin, displaySeconds * 20L);
    }

    private void spawnWinHologram(Location baseLoc) {
        World world = baseLoc.getWorld();
        if (world == null) return;

        double topY = baseLoc.getY() + 2.6;
        String[] lines = {
                "&#fb8c00&l✦ &#fbb602&lВЫИГРЫШ &#fb8c00&l✦",
                "&fИгрок: &#fb8c00" + player.getName(),
                winningItem.getColoredName(),
                "&7Редкость: " + winningItem.getRarity().getColorCode() + winningItem.getRarity().getDefaultName() + " &8(&a" + winningItem.getChance() + "%&8)"
        };

        for (String line : lines) {
            Location loc = new Location(world, baseLoc.getX() + 0.5, topY, baseLoc.getZ() + 0.5);
            ArmorStand textStand = (ArmorStand) world.spawnEntity(loc, EntityType.ARMOR_STAND);
            textStand.setVisible(false);
            textStand.setGravity(false);
            textStand.setCustomName(ColorUtil.color(line));
            textStand.setCustomNameVisible(true);
            textStand.setMarker(true);
            textStand.setSmall(true);
            textStand.setInvulnerable(true);
            winHoloStands.add(textStand);

            topY -= 0.28;
        }
    }

    private void giveReward() {
        plugin.getActiveOpeners().remove(player.getUniqueId());

        // Send Titles
        String title = plugin.getMessages().getString("case-opened-title", "&6&lПОЗДРАВЛЯЕМ!");
        String subtitle = plugin.getMessages().getString("case-opened-subtitle", "&fВы выиграли: {item_display}")
                .replace("{item_display}", winningItem.getColoredName());
        player.sendTitle(ColorUtil.color(title), ColorUtil.color(subtitle), 10, 50, 15);

        // Execute reward commands from console
        for (String cmd : winningItem.getCommands()) {
            String formatted = cmd.replace("{player}", player.getName())
                    .replace("{case}", targetCase.getId())
                    .replace("{item}", winningItem.getId());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), formatted);
        }

        // Update statistics
        CaseUser user = plugin.getUserManager().getUser(player);
        if (user != null) {
            user.incrementOpened(targetCase.getId());
        }

        // Global chat broadcast
        if (winningItem.isBroadcast()) {
            List<String> broadcastLines = plugin.getMessages().getStringList("win-broadcast");
            for (String rawLine : broadcastLines) {
                String line = rawLine
                        .replace("{player}", player.getName())
                        .replace("{case}", targetCase.getId())
                        .replace("{case_display}", targetCase.getColoredDisplayName())
                        .replace("{item}", winningItem.getId())
                        .replace("{item_display}", winningItem.getColoredName())
                        .replace("{rarity_name}", winningItem.getRarity().getDefaultName())
                        .replace("{rarity_color}", ColorUtil.color(winningItem.getRarity().getColorCode()))
                        .replace("{chance}", String.valueOf(winningItem.getChance()));
                Bukkit.broadcastMessage(ColorUtil.color(line));
            }
        }
    }

    public void cleanUp() {
        plugin.getRunningAnimations().remove(this);
        if (caseBlock != null && caseBlock.getLocation() != null) {
            plugin.getActiveBlocks().remove(caseBlock.getLocation());
        }

        if (animationTask != null) {
            animationTask.cancel();
            animationTask = null;
        }
        if (centerItemStand != null && centerItemStand.isValid()) {
            centerItemStand.remove();
            centerItemStand = null;
        }
        for (ArmorStand sat : satelliteStands) {
            if (sat != null && sat.isValid()) sat.remove();
        }
        satelliteStands.clear();

        for (ArmorStand holo : winHoloStands) {
            if (holo != null && holo.isValid()) holo.remove();
        }
        winHoloStands.clear();

        // Восстанавливаем стандартную голограмму кейса
        CaseHologram holo = plugin.getHologramManager().getHologram(caseBlock);
        if (holo != null) {
            holo.spawn();
        }
    }

    private ArmorStand spawnStand(Location loc) {
        World world = loc.getWorld();
        ArmorStand stand = (ArmorStand) world.spawnEntity(loc, EntityType.ARMOR_STAND);
        stand.setVisible(false);
        stand.setGravity(false);
        stand.setMarker(true);
        stand.setInvulnerable(true);
        stand.setCollidable(false);
        return stand;
    }
}
