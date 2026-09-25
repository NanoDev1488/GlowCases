package ru.glowcase.command;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import ru.glowcase.GlowCasesPlugin;
import ru.glowcase.case_.Case;
import ru.glowcase.case_.CaseBlock;
import ru.glowcase.user.CaseUser;
import ru.glowcase.util.ColorUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class GlowCasesCommand implements CommandExecutor, TabCompleter {

    private final GlowCasesPlugin plugin;

    public GlowCasesCommand(GlowCasesPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("glowcases.admin")) {
            sender.sendMessage(ColorUtil.color(plugin.getMessages().getString("no-permission", "&cУ вас нет прав!")));
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "reload" -> {
                plugin.reload();
                sender.sendMessage(ColorUtil.color(plugin.getMessages().getString("config-reloaded", "&aКонфигурация успешно перезагружена!")));
            }
            case "givekey" -> {
                if (args.length < 4) {
                    sender.sendMessage(ColorUtil.color("&cИспользование: /" + label + " givekey <игрок> <кейс> <кол-во>"));
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(ColorUtil.color(plugin.getMessages().getString("player-not-found", "&cИгрок не найден!").replace("{player}", args[1])));
                    return true;
                }
                Case c = plugin.getCaseManager().getCase(args[2]);
                if (c == null) {
                    sender.sendMessage(ColorUtil.color(plugin.getMessages().getString("case-not-found", "&cКейс не найден!").replace("{case}", args[2])));
                    return true;
                }
                int amount;
                try {
                    amount = Integer.parseInt(args[3]);
                    if (amount <= 0) throw new NumberFormatException();
                } catch (NumberFormatException e) {
                    sender.sendMessage(ColorUtil.color(plugin.getMessages().getString("invalid-amount", "&cНеверное число!")));
                    return true;
                }

                CaseUser user = plugin.getUserManager().getUser(target);
                if (user != null) {
                    user.addKeys(c.getId(), amount);
                }

                sender.sendMessage(ColorUtil.color(plugin.getMessages().getString("keys-given", "&fИгроку {player} выдано {amount} ключей!")
                        .replace("{player}", target.getName())
                        .replace("{case}", c.getId())
                        .replace("{case_display}", c.getColoredDisplayName())
                        .replace("{amount}", String.valueOf(amount))));

                target.sendMessage(ColorUtil.color(plugin.getMessages().getString("keys-received", "&fВы получили {amount} ключей!")
                        .replace("{case}", c.getId())
                        .replace("{case_display}", c.getColoredDisplayName())
                        .replace("{amount}", String.valueOf(amount))));
            }
            case "setkey" -> {
                if (args.length < 4) {
                    sender.sendMessage(ColorUtil.color("&cИспользование: /" + label + " setkey <игрок> <кейс> <кол-во>"));
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(ColorUtil.color(plugin.getMessages().getString("player-not-found", "&cИгрок не найден!").replace("{player}", args[1])));
                    return true;
                }
                Case c = plugin.getCaseManager().getCase(args[2]);
                if (c == null) {
                    sender.sendMessage(ColorUtil.color(plugin.getMessages().getString("case-not-found", "&cКейс не найден!").replace("{case}", args[2])));
                    return true;
                }
                int amount;
                try {
                    amount = Integer.parseInt(args[3]);
                    if (amount < 0) throw new NumberFormatException();
                } catch (NumberFormatException e) {
                    sender.sendMessage(ColorUtil.color(plugin.getMessages().getString("invalid-amount", "&cНеверное число!")));
                    return true;
                }

                CaseUser user = plugin.getUserManager().getUser(target);
                if (user != null) {
                    user.setKeys(c.getId(), amount);
                }

                sender.sendMessage(ColorUtil.color(plugin.getMessages().getString("keys-set", "&fИгроку {player} установлено {amount} ключей!")
                        .replace("{player}", target.getName())
                        .replace("{case}", c.getId())
                        .replace("{case_display}", c.getColoredDisplayName())
                        .replace("{amount}", String.valueOf(amount))));
            }
            case "takekey" -> {
                if (args.length < 4) {
                    sender.sendMessage(ColorUtil.color("&cИспользование: /" + label + " takekey <игрок> <кейс> <кол-во>"));
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(ColorUtil.color(plugin.getMessages().getString("player-not-found", "&cИгрок не найден!").replace("{player}", args[1])));
                    return true;
                }
                Case c = plugin.getCaseManager().getCase(args[2]);
                if (c == null) {
                    sender.sendMessage(ColorUtil.color(plugin.getMessages().getString("case-not-found", "&cКейс не найден!").replace("{case}", args[2])));
                    return true;
                }
                int amount;
                try {
                    amount = Integer.parseInt(args[3]);
                    if (amount <= 0) throw new NumberFormatException();
                } catch (NumberFormatException e) {
                    sender.sendMessage(ColorUtil.color(plugin.getMessages().getString("invalid-amount", "&cНеверное число!")));
                    return true;
                }

                CaseUser user = plugin.getUserManager().getUser(target);
                if (user != null) {
                    user.takeKeys(c.getId(), amount);
                }

                sender.sendMessage(ColorUtil.color(plugin.getMessages().getString("keys-taken", "&fУ игрока {player} забрано {amount} ключей!")
                        .replace("{player}", target.getName())
                        .replace("{case}", c.getId())
                        .replace("{case_display}", c.getColoredDisplayName())
                        .replace("{amount}", String.valueOf(amount))));
            }
            case "keys" -> {
                Player target = sender instanceof Player p ? p : null;
                if (args.length > 1) {
                    target = Bukkit.getPlayer(args[1]);
                }
                if (target == null) {
                    sender.sendMessage(ColorUtil.color("&cУкажите игрока: /" + label + " keys <игрок>"));
                    return true;
                }
                CaseUser user = plugin.getUserManager().getUser(target);
                sender.sendMessage(ColorUtil.color("&#fb6b02&lКлючи игрока &e" + target.getName() + "&8:"));
                for (Case c : plugin.getCaseManager().getCases()) {
                    int k = user != null ? user.getKeys(c.getId()) : 0;
                    sender.sendMessage(ColorUtil.color(" &8▸ " + c.getColoredDisplayName() + "&7: &6" + k + " шт."));
                }
            }
            case "setcase" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(ColorUtil.color(plugin.getMessages().getString("player-only", "&cТолько для игроков!")));
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(ColorUtil.color("&cИспользование: /" + label + " setcase <кейс>"));
                    return true;
                }
                Case c = plugin.getCaseManager().getCase(args[1]);
                if (c == null) {
                    sender.sendMessage(ColorUtil.color(plugin.getMessages().getString("case-not-found", "&cКейс не найден!").replace("{case}", args[1])));
                    return true;
                }
                Block targetBlock = player.getTargetBlockExact(5);
                if (targetBlock == null) {
                    sender.sendMessage(ColorUtil.color(plugin.getMessages().getString("not-looking-at-block", "&cВы должны смотреть на блок!")));
                    return true;
                }

                CaseBlock caseBlock = new CaseBlock(targetBlock.getLocation(), c.getId());
                plugin.getCaseManager().addCaseBlock(caseBlock);
                plugin.getHologramManager().spawnForBlock(caseBlock);

                sender.sendMessage(ColorUtil.color(plugin.getMessages().getString("case-block-set", "&aБлок кейса установлен!")
                        .replace("{case}", c.getId())
                        .replace("{case_display}", c.getColoredDisplayName())));
            }
            case "delcase" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(ColorUtil.color(plugin.getMessages().getString("player-only", "&cТолько для игроков!")));
                    return true;
                }
                Block targetBlock = player.getTargetBlockExact(5);
                if (targetBlock == null) {
                    sender.sendMessage(ColorUtil.color(plugin.getMessages().getString("not-looking-at-block", "&cВы должны смотреть на блок!")));
                    return true;
                }

                boolean removed = plugin.getCaseManager().removeCaseBlock(targetBlock.getLocation());
                if (removed) {
                    plugin.getHologramManager().removeForBlock(targetBlock.getLocation());
                    sender.sendMessage(ColorUtil.color(plugin.getMessages().getString("case-block-removed", "&cБлок кейса удален!")));
                } else {
                    sender.sendMessage(ColorUtil.color(plugin.getMessages().getString("target-not-case-block", "&cЭтот блок не привязан к кейсу!")));
                }
            }
            default -> sendHelp(sender);
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        List<String> lines = plugin.getMessages().getStringList("help");
        for (String line : lines) {
            sender.sendMessage(ColorUtil.color(line));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("glowcases.admin")) return Collections.emptyList();

        if (args.length == 1) {
            List<String> subs = Arrays.asList("help", "givekey", "setkey", "takekey", "keys", "setcase", "delcase", "reload");
            return filter(subs, args[0]);
        }

        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("givekey") || sub.equals("setkey") || sub.equals("takekey") || sub.equals("keys")) {
                return null; // suggest players
            }
            if (sub.equals("setcase")) {
                List<String> caseIds = plugin.getCaseManager().getCases().stream().map(Case::getId).collect(Collectors.toList());
                return filter(caseIds, args[1]);
            }
        }

        if (args.length == 3) {
            String sub = args[0].toLowerCase();
            if (sub.equals("givekey") || sub.equals("setkey") || sub.equals("takekey")) {
                List<String> caseIds = plugin.getCaseManager().getCases().stream().map(Case::getId).collect(Collectors.toList());
                return filter(caseIds, args[2]);
            }
        }

        if (args.length == 4) {
            String sub = args[0].toLowerCase();
            if (sub.equals("givekey") || sub.equals("setkey") || sub.equals("takekey")) {
                return Arrays.asList("1", "5", "10", "50");
            }
        }

        return Collections.emptyList();
    }

    private List<String> filter(List<String> list, String input) {
        String lower = input.toLowerCase();
        List<String> result = new ArrayList<>();
        for (String s : list) {
            if (s.toLowerCase().startsWith(lower)) {
                result.add(s);
            }
        }
        return result;
    }
}
