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
            // Open case
            if (plugin.getActiveOpeners().contains(player.getUniqueId())) {
                player.sendMessage(ColorUtil.color(plugin.getMessages().getString("case-already-opening", "&cВы уже открываете кейс!")));
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

            // Deduct key and launch roulette
            user.takeKey(targetCase.getId());
            new RouletteAnimation(plugin, player, targetCase, plugin.getActiveOpeners()).start();
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
