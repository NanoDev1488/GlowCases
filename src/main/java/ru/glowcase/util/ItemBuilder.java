package ru.glowcase.util;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemBuilder {

    private final ItemStack itemStack;
    private final ItemMeta itemMeta;

    public ItemBuilder(Material material) {
        this(material, 1);
    }

    public ItemBuilder(Material material, int amount) {
        this.itemStack = new ItemStack(material != null ? material : Material.STONE, amount);
        ItemMeta meta = null;
        try {
            meta = this.itemStack.getItemMeta();
        } catch (Exception ignored) {}
        this.itemMeta = meta;
    }

    public ItemBuilder(ItemStack itemStack) {
        this.itemStack = itemStack != null ? itemStack.clone() : new ItemStack(Material.STONE, 1);
        ItemMeta meta = null;
        try {
            meta = this.itemStack.getItemMeta();
        } catch (Exception ignored) {}
        this.itemMeta = meta;
    }

    public ItemBuilder name(String name) {
        if (itemMeta != null && name != null) {
            itemMeta.setDisplayName(ColorUtil.color(name));
        }
        return this;
    }

    public ItemBuilder lore(List<String> lore) {
        if (itemMeta != null && lore != null) {
            itemMeta.setLore(ColorUtil.color(lore));
        }
        return this;
    }

    public ItemBuilder lore(String... lore) {
        return lore(Arrays.asList(lore));
    }

    public ItemBuilder addLore(String line) {
        if (itemMeta != null) {
            List<String> lore = itemMeta.getLore();
            if (lore == null) {
                lore = new ArrayList<>();
            }
            lore.add(ColorUtil.color(line));
            itemMeta.setLore(lore);
        }
        return this;
    }

    public ItemBuilder amount(int amount) {
        itemStack.setAmount(amount);
        return this;
    }

    public ItemBuilder enchant(Enchantment enchantment, int level) {
        if (itemMeta != null) {
            itemMeta.addEnchant(enchantment, level, true);
        }
        return this;
    }

    public ItemBuilder flags(ItemFlag... flags) {
        if (itemMeta != null) {
            itemMeta.addItemFlags(flags);
        }
        return this;
    }

    public ItemBuilder hideAllFlags() {
        if (itemMeta != null) {
            itemMeta.addItemFlags(ItemFlag.values());
        }
        return this;
    }

    public ItemBuilder glowing(boolean glowing) {
        if (glowing && itemMeta != null) {
            itemMeta.addEnchant(Enchantment.DURABILITY, 1, true);
            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        return this;
    }

    public ItemBuilder customModelData(Integer customModelData) {
        if (itemMeta != null && customModelData != null && customModelData > 0) {
            try {
                itemMeta.setCustomModelData(customModelData);
            } catch (NoSuchMethodError ignored) {}
        }
        return this;
    }

    public ItemBuilder skullOwner(String ownerName) {
        if (itemMeta instanceof SkullMeta skullMeta && ownerName != null) {
            skullMeta.setOwner(ownerName);
        }
        return this;
    }

    public ItemStack build() {
        if (itemMeta != null) {
            itemStack.setItemMeta(itemMeta);
        }
        return itemStack;
    }
}
