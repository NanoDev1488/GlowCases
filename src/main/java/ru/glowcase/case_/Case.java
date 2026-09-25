package ru.glowcase.case_;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import ru.glowcase.util.ColorUtil;
import ru.glowcase.util.ItemBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Case {

    private final String id;
    private final String displayName;
    private final ItemStack menuItem;
    private final ItemStack keyItem;
    private final List<CaseItem> items;
    private final double totalWeight;

    public Case(String id, String displayName, ItemStack menuItem, ItemStack keyItem, List<CaseItem> items) {
        this.id = id;
        this.displayName = displayName != null ? displayName : id;
        this.menuItem = menuItem != null ? menuItem : new ItemBuilder(Material.CHEST).name(this.displayName).build();
        this.keyItem = keyItem != null ? keyItem : new ItemBuilder(Material.TRIPWIRE_HOOK).name("&6Ключ от " + this.displayName).build();
        this.items = items != null ? Collections.unmodifiableList(items) : Collections.emptyList();

        double sum = 0.0;
        for (CaseItem item : this.items) {
            sum += Math.max(0.0001, item.getChance());
        }
        this.totalWeight = sum;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColoredDisplayName() {
        return ColorUtil.color(displayName);
    }

    public ItemStack getMenuItem() {
        return menuItem.clone();
    }

    public ItemStack getKeyItem() {
        return keyItem.clone();
    }

    public List<CaseItem> getItems() {
        return items;
    }

    public double getTotalWeight() {
        return totalWeight;
    }

    /**
     * Weighted random selection algorithm for rolling drops.
     */
    public CaseItem getRandomItem() {
        if (items.isEmpty()) {
            return null;
        }
        if (items.size() == 1 || totalWeight <= 0) {
            return items.get(0);
        }

        double randomValue = ThreadLocalRandom.current().nextDouble() * totalWeight;
        double currentWeight = 0.0;
        for (CaseItem item : items) {
            currentWeight += Math.max(0.0001, item.getChance());
            if (randomValue <= currentWeight) {
                return item;
            }
        }

        return items.get(items.size() - 1);
    }
}
