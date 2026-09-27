package ru.glowcase.animation;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import ru.glowcase.GlowCasesPlugin;
import ru.glowcase.case_.Case;
import ru.glowcase.case_.CaseBlock;
import ru.glowcase.case_.CaseItem;
import ru.glowcase.user.CaseUser;
import ru.glowcase.util.ColorUtil;
import ru.glowcase.util.ItemBuilder;
import ru.glowcase.util.SoundUtil;

import java.util.*;

public class RouletteAnimation {

    private final GlowCasesPlugin plugin;
    private final Player player;
    private final Case targetCase;
    private final CaseBlock caseBlock;
    private final CaseItem winningItem;
    private final Inventory inventory;
    private final List<CaseItem> sequence = new ArrayList<>();
    private final Set<UUID> activeOpeners;

    private int currentIndex = 0;
    private int step = 0;
    private double currentAngle = 0;
    private boolean finished = false;
    private WorldRouletteAnimation worldAnimation;

    // Slots for standard 27-slot chest roulette
    private static final int[] ROULETTE_SLOTS = {9, 10, 11, 12, 13, 14, 15, 16, 17};

    public RouletteAnimation(GlowCasesPlugin plugin, Player player, Case targetCase, Set<UUID> activeOpeners) {
        this(plugin, player, targetCase, activeOpeners, null);
    }

    public RouletteAnimation(GlowCasesPlugin plugin, Player player, Case targetCase, Set<UUID> activeOpeners, CaseBlock caseBlock) {
        this.plugin = plugin;
        this.player = player;
        this.targetCase = targetCase;
        this.caseBlock = caseBlock;
        this.activeOpeners = activeOpeners;
        this.winningItem = targetCase.getRandomItem();

        String rawTitle = plugin.getConfig().getString("roulette.title", "&6&lКЕЙС &8» &f%case_display_name%");
        String title = rawTitle.replace("%case_display_name%", targetCase.getColoredDisplayName());
        int size = plugin.getConfig().getInt("roulette.size", 27);
        this.inventory = Bukkit.createInventory(null, size, ColorUtil.color(title));

        generateSequence();
        setupLayout();
    }

    private void generateSequence() {
        int totalRolls = 40; // Number of shifts
        for (int i = 0; i < totalRolls; i++) {
            sequence.add(targetCase.getRandomItem());
        }
        sequence.set(totalRolls - 1, winningItem);
    }

    private void setupLayout() {
        Material bgMat = Material.matchMaterial(plugin.getConfig().getString("roulette.background-item.material", "BLACK_STAINED_GLASS_PANE"));
        if (bgMat == null) bgMat = Material.BLACK_STAINED_GLASS_PANE;
        ItemStack bgItem = new ItemBuilder(bgMat).name(" ").build();

        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, bgItem);
        }

        // Indicator items
        Material indMat = Material.matchMaterial(plugin.getConfig().getString("roulette.indicator-item.material", "HOPPER"));
        if (indMat == null) indMat = Material.HOPPER;
        String indName = plugin.getConfig().getString("roulette.indicator-item.name", "&6&l▲ &e&lВЫИГРЫШ &6&l▲");
        List<String> indLore = plugin.getConfig().getStringList("roulette.indicator-item.lore");
        ItemStack indicator = new ItemBuilder(indMat).name(indName).lore(indLore).build();

        int topSlot = plugin.getConfig().getInt("roulette.indicator-slot-top", 4);
        int botSlot = plugin.getConfig().getInt("roulette.indicator-slot-bottom", 22);
        if (topSlot < inventory.getSize()) inventory.setItem(topSlot, indicator);
        if (botSlot < inventory.getSize()) inventory.setItem(botSlot, indicator);

        updateRouletteRow();
    }

    private void updateRouletteRow() {
        for (int i = 0; i < ROULETTE_SLOTS.length; i++) {
            int seqIdx = currentIndex + i;
            if (seqIdx < sequence.size()) {
                CaseItem item = sequence.get(seqIdx);
                inventory.setItem(ROULETTE_SLOTS[i], item.toItemStack());
            }
        }
    }

    public void start() {
        activeOpeners.add(player.getUniqueId());
        player.openInventory(inventory);

        // Если открыт у физического блока и включен режим BOTH - запускаем параллельную 3D анимацию в мире
        String mode = plugin.getConfig().getString("world-effects.mode", "BOTH").toUpperCase();
        if (caseBlock != null && mode.equals("BOTH")) {
            worldAnimation = new WorldRouletteAnimation(plugin, player, targetCase, caseBlock, winningItem, false);
            worldAnimation.start();
        }

        // Schedule deceleration ticks:
        new BukkitRunnable() {
            int tickDelay = 1;
            int ticksPassed = 0;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    finishReward();
                    cancel();
                    return;
                }

                currentAngle += 0.25;

                // Партиклы спирали у блока во время прокрутки в GUI
                if (caseBlock != null && (worldAnimation == null)) {
                    plugin.getWorldEffectManager().playTickParticles(caseBlock.getLocation(), currentAngle, targetCase);
                }

                ticksPassed++;
                if (ticksPassed < tickDelay) {
                    return;
                }
                ticksPassed = 0;

                currentIndex++;
                updateRouletteRow();

                // Play tick sound
                SoundUtil.play(player, plugin.getConfig().getConfigurationSection("roulette.tick-sound"));

                step++;

                // Deceleration curve
                if (step > 30) {
                    tickDelay = 10;
                } else if (step > 27) {
                    tickDelay = 7;
                } else if (step > 23) {
                    tickDelay = 5;
                } else if (step > 18) {
                    tickDelay = 3;
                } else if (step > 12) {
                    tickDelay = 2;
                } else {
                    tickDelay = 1;
                }

                if (currentIndex >= sequence.size() - ROULETTE_SLOTS.length) {
                    finishReward();
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 2L, 1L);
    }

    public void finishReward() {
        if (finished) return;
        finished = true;
        activeOpeners.remove(player.getUniqueId());

        if (winningItem == null) return;

        // Play win sound
        SoundUtil.play(player, plugin.getConfig().getConfigurationSection("roulette.win-sound"));

        // Визуальные эффекты празднования в мире (фейерверки, столбы света, взрыв частиц)
        Location effectLoc = caseBlock != null ? caseBlock.getLocation() : player.getLocation();
        if (worldAnimation == null) {
            plugin.getWorldEffectManager().playWinCelebration(effectLoc, winningItem, player);
        }

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

    public Inventory getInventory() {
        return inventory;
    }

    public boolean isFinished() {
        return finished;
    }
}
