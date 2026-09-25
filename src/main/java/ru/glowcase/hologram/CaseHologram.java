package ru.glowcase.hologram;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.EulerAngle;
import ru.glowcase.GlowCasesPlugin;
import ru.glowcase.case_.Case;
import ru.glowcase.case_.CaseBlock;
import ru.glowcase.util.ColorUtil;

import java.util.ArrayList;
import java.util.List;

public class CaseHologram {

    private final GlowCasesPlugin plugin;
    private final CaseBlock caseBlock;
    private final List<ArmorStand> textStands = new ArrayList<>();
    private ArmorStand itemStand;
    private BukkitTask rotationTask;
    private float currentYaw = 0f;

    public CaseHologram(GlowCasesPlugin plugin, CaseBlock caseBlock) {
        this.plugin = plugin;
        this.caseBlock = caseBlock;
    }

    public void spawn() {
        despawn();

        Location baseLoc = caseBlock.getLocation();
        if (baseLoc == null || baseLoc.getWorld() == null) return;
        World world = baseLoc.getWorld();

        Case c = plugin.getCaseManager().getCase(caseBlock.getCaseId());
        String caseDisplayName = c != null ? c.getColoredDisplayName() : caseBlock.getCaseId();

        boolean holoEnabled = plugin.getConfig().getBoolean("holograms.enabled", true);
        double offsetY = plugin.getConfig().getDouble("holograms.offset-y", 1.6);
        List<String> rawLines = plugin.getConfig().getStringList("holograms.lines");

        if (holoEnabled && !rawLines.isEmpty()) {
            double currentY = baseLoc.getY() + offsetY + (rawLines.size() * 0.25);
            for (String rawLine : rawLines) {
                String line = rawLine.replace("%case_display_name%", caseDisplayName);
                Location lineLoc = new Location(world, baseLoc.getX() + 0.5, currentY, baseLoc.getZ() + 0.5);

                if (!line.isEmpty()) {
                    ArmorStand stand = (ArmorStand) world.spawnEntity(lineLoc, EntityType.ARMOR_STAND);
                    stand.setVisible(false);
                    stand.setGravity(false);
                    stand.setCustomName(ColorUtil.color(line));
                    stand.setCustomNameVisible(true);
                    stand.setMarker(true);
                    stand.setSmall(true);
                    stand.setInvulnerable(true);
                    stand.setCollidable(false);
                    textStands.add(stand);
                }
                currentY -= 0.28;
            }
        }

        // Floating rotating item
        boolean itemEnabled = plugin.getConfig().getBoolean("holograms.floating-item.enabled", true);
        if (itemEnabled && c != null) {
            double itemOffsetY = plugin.getConfig().getDouble("holograms.floating-item.offset-y", 0.8);
            Location itemLoc = new Location(world, baseLoc.getX() + 0.5, baseLoc.getY() + itemOffsetY, baseLoc.getZ() + 0.5);

            itemStand = (ArmorStand) world.spawnEntity(itemLoc, EntityType.ARMOR_STAND);
            itemStand.setVisible(false);
            itemStand.setGravity(false);
            itemStand.setMarker(true);
            itemStand.setSmall(true);
            itemStand.setInvulnerable(true);
            itemStand.setCollidable(false);
            itemStand.setHelmet(c.getMenuItem());

            boolean rotate = plugin.getConfig().getBoolean("holograms.floating-item.rotate", true);
            if (rotate) {
                startRotation();
            }
        }
    }

    private void startRotation() {
        if (rotationTask != null) {
            rotationTask.cancel();
        }

        rotationTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (itemStand == null || !itemStand.isValid()) {
                    cancel();
                    return;
                }
                currentYaw += 3.5f;
                if (currentYaw >= 360f) {
                    currentYaw -= 360f;
                }
                Location loc = itemStand.getLocation();
                loc.setYaw(currentYaw);
                itemStand.teleport(loc);
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }

    public void despawn() {
        if (rotationTask != null) {
            rotationTask.cancel();
            rotationTask = null;
        }
        for (ArmorStand stand : textStands) {
            if (stand != null && stand.isValid()) {
                stand.remove();
            }
        }
        textStands.clear();

        if (itemStand != null && itemStand.isValid()) {
            itemStand.remove();
            itemStand = null;
        }
    }

    public CaseBlock getCaseBlock() {
        return caseBlock;
    }
}
