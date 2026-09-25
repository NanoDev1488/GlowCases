package ru.glowcase.api;

import org.bukkit.entity.Player;
import ru.glowcase.GlowCasesPlugin;
import ru.glowcase.case_.Case;
import ru.glowcase.user.CaseUser;

import java.util.Collection;
import java.util.UUID;

public final class GlowCasesAPI {

    private GlowCasesAPI() {}

    public static GlowCasesPlugin getPlugin() {
        return GlowCasesPlugin.getInstance();
    }

    public static Case getCase(String caseId) {
        return getPlugin().getCaseManager().getCase(caseId);
    }

    public static Collection<Case> getCases() {
        return getPlugin().getCaseManager().getCases();
    }

    public static int getKeys(UUID uuid, String caseId) {
        CaseUser user = getPlugin().getUserManager().getUser(uuid);
        return user != null ? user.getKeys(caseId) : 0;
    }

    public static int getKeys(Player player, String caseId) {
        return getKeys(player.getUniqueId(), caseId);
    }

    public static void addKeys(UUID uuid, String caseId, int amount) {
        CaseUser user = getPlugin().getUserManager().getUser(uuid);
        if (user != null) {
            user.addKeys(caseId, amount);
        }
    }

    public static void addKeys(Player player, String caseId, int amount) {
        addKeys(player.getUniqueId(), caseId, amount);
    }

    public static boolean takeKeys(UUID uuid, String caseId, int amount) {
        CaseUser user = getPlugin().getUserManager().getUser(uuid);
        return user != null && user.takeKeys(caseId, amount);
    }

    public static boolean takeKeys(Player player, String caseId, int amount) {
        return takeKeys(player.getUniqueId(), caseId, amount);
    }
}
