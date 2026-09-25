package ru.glowcase.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.glowcase.GlowCasesPlugin;
import ru.glowcase.gui.MainCasesMenu;
import ru.glowcase.util.ColorUtil;

public class CasesCommand implements CommandExecutor {

    private final GlowCasesPlugin plugin;

    public CasesCommand(GlowCasesPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ColorUtil.color(plugin.getMessages().getString("player-only", "&cЭта команда доступна только игрокам!")));
            return true;
        }

        new MainCasesMenu(plugin, player).open();
        return true;
    }
}
