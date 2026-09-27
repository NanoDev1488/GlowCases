package ru.glowcase.listener;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import ru.glowcase.GlowCasesPlugin;
import ru.glowcase.animation.RouletteAnimation;
import ru.glowcase.animation.WorldRouletteAnimation;
import ru.glowcase.case_.Case;
import ru.glowcase.case_.CaseBlock;
import ru.glowcase.gui.CasePreviewMenu;
import ru.glowcase.user.CaseUser;
import ru.glowcase.util.ColorUtil;

public class CaseBlockListener implements Listener {

    private final GlowCasesPlugin plugin;

    public CaseBlockListener(GlowCasesPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getClickedBlock() == null) return;

        Block block = event.getClickedBlock();
        CaseBlock caseBlock = plugin.getCaseManager().getCaseBlockAt(block.getLocation());
        if (caseBlock == null) return;

        event.setCancelled(true);
        Player player = event.getPlayer();

        Case targetCase = plugin.getCaseManager().getCase(caseBlock.getCaseId());
        if (targetCase == null) {
            player.sendMessage(ColorUtil.color(plugin.getMessages().getString("case-not-found", "&cКейс не найден!").replace("{case}", caseBlock.getCaseId())));
            return;
        }

        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            // Preview case
            new CasePreviewMenu(plugin, player, targetCase).open();
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            // Check if player is already opening
            if (plugin.getActiveOpeners().contains(player.getUniqueId())) {
                player.sendMessage(ColorUtil.color(plugin.getMessages().getString("case-already-opening", "&cВы уже открываете кейс!")));
                return;
            }

            // Check if this physical case block is currently animating
            if (plugin.getActiveBlocks().contains(block.getLocation())) {
                player.sendMessage(ColorUtil.color(plugin.getMessages().getString("case-block-busy", "&cЭтот кейс сейчас открывает другой игрок! Подождите...")));
                return;
            }

            CaseUser user = plugin.getUserManager().getUser(player);
            if (user == null || user.getKeys(targetCase.getId()) <= 0) {
                String noKeysMsg = plugin.getMessages().getString("no-keys", "&cУ вас нет ключа для открытия кейса {case_display}!")
                        .replace("{case}", targetCase.getId())
                        .replace("{case_display}", targetCase.getColoredDisplayName());
                player.sendMessage(ColorUtil.color(noKeysMsg));
                return;
            }

            // Deduct key
            user.takeKey(targetCase.getId());

            String mode = plugin.getConfig().getString("world-effects.mode", "WORLD").toUpperCase();
            if (mode.equals("GUI")) {
                new RouletteAnimation(plugin, player, targetCase, plugin.getActiveOpeners(), null).start();
            } else if (mode.equals("BOTH")) {
                new RouletteAnimation(plugin, player, targetCase, plugin.getActiveOpeners(), caseBlock).start();
            } else {
                // Default: HolyCases in-air 3D animation
                plugin.getActiveOpeners().add(player.getUniqueId());
                new WorldRouletteAnimation(plugin, player, targetCase, caseBlock, null, true).start();
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        CaseBlock caseBlock = plugin.getCaseManager().getCaseBlockAt(event.getBlock().getLocation());
        if (caseBlock != null) {
            if (!event.getPlayer().hasPermission("glowcases.admin")) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(ColorUtil.color("&cВы не можете сломать этот кейс-блок!"));
            }
        }
    }
}
