package ru.glowcase.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.glowcase.GlowCasesPlugin;

public class PlayerListener implements Listener {

    private final GlowCasesPlugin plugin;

    public PlayerListener(GlowCasesPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getUserManager().loadUser(player.getUniqueId(), player.getName());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getActiveOpeners().remove(player.getUniqueId());
        plugin.getUserManager().unloadUser(player.getUniqueId(), true);
    }
}
