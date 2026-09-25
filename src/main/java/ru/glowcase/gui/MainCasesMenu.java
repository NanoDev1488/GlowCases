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
import ru.glowcase.user.CaseUser;
import ru.glowcase.util.ColorUtil;
import ru.glowcase.util.ItemBuilder;

import java.util.ArrayList;
import java.util.List;

public class MainCasesMenu implements InventoryHolder {

    private final GlowCasesPlugin plugin;
    private final Player player;
    private final Inventory inventory;

    public MainCasesMenu(GlowCasesPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;

        String title = plugin.getConfig().getString("main-menu.title", "&6&lСПИСОК КЕЙСОВ");
        int size = plugin.getConfig().getInt("main-menu.size", 36);
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

        CaseUser user = plugin.getUserManager().getUser(player);

        // Profile head item
        int profileSlot = plugin.getConfig().getInt("main-menu.profile-slot", 4);
        if (profileSlot < inventory.getSize()) {
            StringBuilder keysSummary = new StringBuilder();
            if (user != null) {
                for (Case c : plugin.getCaseManager().getCases()) {
                    int k = user.getKeys(c.getId());
                    keysSummary.append("\n&8▸ ").append(c.getColoredDisplayName()).append("&7: &6").append(k).append(" шт.");
                }
            }

            List<String> rawLore = plugin.getConfig().getStringList("main-menu.profile-item.lore");
            List<String> lore = new ArrayList<>();
            for (String line : rawLore) {
                if (line.contains("%keys_summary%")) {
                    for (String kLine : keysSummary.toString().split("\n")) {
                        if (!kLine.trim().isEmpty()) {
                            lore.add(ColorUtil.color(kLine));
                        }
                    }
                } else {
                    String formatted = line.replace("%player_name%", player.getName())
                            .replace("%glowcases_opened%", String.valueOf(user != null ? user.getTotalOpened() : 0));
                    lore.add(ColorUtil.color(formatted));
                }
            }

            ItemStack head = new ItemBuilder(Material.PLAYER_HEAD)
                    .name(plugin.getConfig().getString("main-menu.profile-item.name", "&6&lВАШ ПРОФИЛЬ"))
                    .skullOwner(player.getName())
                    .lore(lore)
                    .build();
            inventory.setItem(profileSlot, head);
        }

        // Cases items
        for (Case c : plugin.getCaseManager().getCases()) {
            int slot = plugin.getConfig().getInt("main-menu.cases." + c.getId() + ".slot", -1);
            if (slot >= 0 && slot < inventory.getSize()) {
                ItemStack item = c.getMenuItem();
                ItemMeta meta = item.getItemMeta();
                if (meta != null && meta.getLore() != null) {
                    List<String> formattedLore = new ArrayList<>();
                    int keys = user != null ? user.getKeys(c.getId()) : 0;
                    for (String line : meta.getLore()) {
                        formattedLore.add(line.replace("%glowcases_keys_" + c.getId() + "%", String.valueOf(keys)));
                    }
                    meta.setLore(formattedLore);
                    item.setItemMeta(meta);
                }
                inventory.setItem(slot, item);
            }
        }
    }

    public void open() {
        player.openInventory(inventory);
    }

    public Case getCaseAtSlot(int slot) {
        for (Case c : plugin.getCaseManager().getCases()) {
            int configSlot = plugin.getConfig().getInt("main-menu.cases." + c.getId() + ".slot", -1);
            if (configSlot == slot) {
                return c;
            }
        }
        return null;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
