package ru.glowcase.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryHolder;
import ru.glowcase.GlowCasesPlugin;
import ru.glowcase.animation.RouletteAnimation;
import ru.glowcase.case_.Case;
import ru.glowcase.gui.CasePreviewMenu;
import ru.glowcase.gui.MainCasesMenu;
import ru.glowcase.user.CaseUser;
import ru.glowcase.util.ColorUtil;

public class InventoryListener implements Listener {

    private final GlowCasesPlugin plugin;

    public InventoryListener(GlowCasesPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        // Block interaction if player is currently in a roulette animation
        if (plugin.getActiveOpeners().contains(player.getUniqueId())) {
            event.setCancelled(true);
            return;
        }

        InventoryHolder holder = event.getInventory().getHolder();

        if (holder instanceof MainCasesMenu menu) {
            event.setCancelled(true);
            int slot = event.getRawSlot();
            if (slot < 0 || slot >= event.getInventory().getSize()) return;

            Case targetCase = menu.getCaseAtSlot(slot);
            if (targetCase == null) return;

            if (event.getClick() == ClickType.LEFT) {
                // Open preview
                new CasePreviewMenu(plugin, player, targetCase).open();
            } else if (event.getClick() == ClickType.RIGHT) {
                // Open case
                CaseUser user = plugin.getUserManager().getUser(player);
                if (user == null || user.getKeys(targetCase.getId()) <= 0) {
                    String noKeysMsg = plugin.getMessages().getString("no-keys", "&cУ вас нет ключа для открытия кейса {case_display}!")
                            .replace("{case}", targetCase.getId())
                            .replace("{case_display}", targetCase.getColoredDisplayName());
                    player.sendMessage(ColorUtil.color(noKeysMsg));
                    return;
                }

                user.takeKey(targetCase.getId());
                new RouletteAnimation(plugin, player, targetCase, plugin.getActiveOpeners()).start();
            }
        } else if (holder instanceof CasePreviewMenu preview) {
            event.setCancelled(true);
            int slot = event.getRawSlot();
            if (slot == preview.getBackSlot()) {
                new MainCasesMenu(plugin, player).open();
            }
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            if (plugin.getActiveOpeners().contains(player.getUniqueId())) {
                event.setCancelled(true);
                return;
            }
            InventoryHolder holder = event.getInventory().getHolder();
            if (holder instanceof MainCasesMenu || holder instanceof CasePreviewMenu) {
                event.setCancelled(true);
            }
        }
    }
}
