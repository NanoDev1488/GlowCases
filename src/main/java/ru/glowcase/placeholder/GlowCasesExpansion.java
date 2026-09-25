package ru.glowcase.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import ru.glowcase.GlowCasesPlugin;
import ru.glowcase.case_.Case;
import ru.glowcase.user.CaseUser;

public class GlowCasesExpansion extends PlaceholderExpansion {

    private final GlowCasesPlugin plugin;

    public GlowCasesExpansion(GlowCasesPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "glowcases";
    }

    @Override
    public String getAuthor() {
        return "GlowDev";
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer offlinePlayer, String params) {
        if (offlinePlayer == null) return "";

        CaseUser user = plugin.getUserManager().getUser(offlinePlayer.getUniqueId());

        String lower = params.toLowerCase();

        // %glowcases_keys_<case_id>%
        if (lower.startsWith("keys_")) {
            String caseId = lower.substring(5);
            return String.valueOf(user != null ? user.getKeys(caseId) : 0);
        }

        // %glowcases_opened%
        if (lower.equals("opened")) {
            return String.valueOf(user != null ? user.getTotalOpened() : 0);
        }

        // %glowcases_opened_<case_id>%
        if (lower.startsWith("opened_")) {
            String caseId = lower.substring(7);
            return String.valueOf(user != null ? user.getCaseOpened(caseId) : 0);
        }

        // %glowcases_casename_<case_id>%
        if (lower.startsWith("casename_")) {
            String caseId = lower.substring(9);
            Case c = plugin.getCaseManager().getCase(caseId);
            return c != null ? c.getColoredDisplayName() : caseId;
        }

        return null;
    }
}
