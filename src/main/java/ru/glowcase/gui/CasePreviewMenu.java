package ru.glowcase.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.glowcase.GlowCasesPlugin;
import ru.glowcase.case_.Case;
import ru.glowcase.case_.CaseItem;
import ru.glowcase.util.ColorUtil;
import ru.glowcase.util.ItemBuilder;

import java.util.ArrayList;
import java.util.List;

public class CasePreviewMenu implements InventoryHolder {

    private final GlowCasesPlugin plugin;
    private final Player player;
    private final Case targetCase;
    private final Inventory inventory;

    public CasePreviewMenu(GlowCasesPlugin plugin, Player player, Case targetCase) {
        this.plugin = plugin;
        this.player = player;
        this.targetCase = targetCase;

        String rawTitle = plugin.getConfig().getString("preview-menu.title", "&6&lПРОСМОТР &8» &f%case_display_name%");
        String title = rawTitle.replace("%case_display_name%", targetCase.getColoredDisplayName());
        int size = plugin.getConfig().getInt("preview-menu.size", 45);
        this.inventory = Bukkit.createInventory(this, size, ColorUtil.color(title));

        buildMenu();
    }

    private void buildMenu() {
        Material bgMat = Material.matchMaterial(plugin.getConfig().getString("main-menu.background.material", "BLACK_STAINED_GLASS_PANE"));
        if (bgMat == null) bgMat = Material.BLACK_STAINED_GLASS_PANE;
        ItemStack bgItem = new ItemBuilder(bgMat).name(" ").build();

        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, bgItem);
        }

        // Add drops
        int[] availableSlots = {
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34
        };

        List<CaseItem> items = targetCase.getItems();
        for (int i = 0; i < items.size() && i < availableSlots.length; i++) {
            CaseItem item = items.get(i);
            ItemStack is = item.toItemStack();
            ItemMeta meta = is.getItemMeta();
            if (meta != null) {
                List<String> lore = meta.getLore();
                if (lore == null) lore = new ArrayList<>();
                lore.add(" ");
                lore.add(ColorUtil.color("&7Редкость: " + item.getRarity().getColorCode() + item.getRarity().getDefaultName()));
                lore.add(ColorUtil.color("&7Шанс выпадения: &e" + item.getChance() + "%"));
                meta.setLore(lore);
                is.setItemMeta(meta);
            }
            inventory.setItem(availableSlots[i], is);
        }

        // Back button
        int backSlot = plugin.getConfig().getInt("preview-menu.back-button-slot", 40);
        if (backSlot < inventory.getSize()) {
            Material backMat = Material.matchMaterial(plugin.getConfig().getString("preview-menu.back-button-item.material", "BARRIER"));
            if (backMat == null) backMat = Material.BARRIER;
            String backName = plugin.getConfig().getString("preview-menu.back-button-item.name", "&c&lНазад");
            List<String> backLore = plugin.getConfig().getStringList("preview-menu.back-button-item.lore");
            inventory.setItem(backSlot, new ItemBuilder(backMat).name(backName).lore(backLore).build());
        }
    }

    public void open() {
        player.openInventory(inventory);
    }

    public int getBackSlot() {
        return plugin.getConfig().getInt("preview-menu.back-button-slot", 40);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
