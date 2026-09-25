package ru.glowcase.hologram;

import org.bukkit.Location;
import ru.glowcase.GlowCasesPlugin;
import ru.glowcase.case_.CaseBlock;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class HologramManager {

    private final GlowCasesPlugin plugin;
    private final List<CaseHologram> holograms = new ArrayList<>();

    public HologramManager(GlowCasesPlugin plugin) {
        this.plugin = plugin;
    }

    public void spawnAll() {
        despawnAll();
        for (CaseBlock block : plugin.getCaseManager().getCaseBlocks()) {
            CaseHologram holo = new CaseHologram(plugin, block);
            holo.spawn();
            holograms.add(holo);
        }
    }

    public void spawnForBlock(CaseBlock block) {
        removeForBlock(block.getLocation());
        CaseHologram holo = new CaseHologram(plugin, block);
        holo.spawn();
        holograms.add(holo);
    }

    public void removeForBlock(Location loc) {
        Iterator<CaseHologram> iterator = holograms.iterator();
        while (iterator.hasNext()) {
            CaseHologram holo = iterator.next();
            if (holo.getCaseBlock().matches(loc)) {
                holo.despawn();
                iterator.remove();
            }
        }
    }

    public void despawnAll() {
        for (CaseHologram holo : holograms) {
            holo.despawn();
        }
        holograms.clear();
    }
}
