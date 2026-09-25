package ru.glowcase.user;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.glowcase.GlowCasesPlugin;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class UserManager {

    private final GlowCasesPlugin plugin;
    private final Map<UUID, CaseUser> users = new ConcurrentHashMap<>();

    public UserManager(GlowCasesPlugin plugin) {
        this.plugin = plugin;
    }

    public CaseUser getUser(UUID uuid) {
        if (uuid == null) return null;
        return users.get(uuid);
    }

    public CaseUser getUser(Player player) {
        if (player == null) return null;
        return getUser(player.getUniqueId());
    }

    public CompletableFuture<CaseUser> loadUser(UUID uuid, String name) {
        CaseUser cached = users.get(uuid);
        if (cached != null) {
            if (name != null) cached.setName(name);
            return CompletableFuture.completedFuture(cached);
        }

        return CompletableFuture.supplyAsync(() -> {
            CaseUser user = plugin.getStorageManager().getStorage().loadUser(uuid, name);
            if (user == null) {
                user = new CaseUser(uuid, name);
            }
            users.put(uuid, user);
            return user;
        });
    }

    public void unloadUser(UUID uuid, boolean save) {
        CaseUser user = users.remove(uuid);
        if (user != null && save && user.isDirty()) {
            CompletableFuture.runAsync(() -> plugin.getStorageManager().getStorage().saveUser(user));
        }
    }

    public void saveAll(boolean async) {
        if (async) {
            CompletableFuture.runAsync(() -> {
                for (CaseUser user : users.values()) {
                    if (user.isDirty()) {
                        plugin.getStorageManager().getStorage().saveUser(user);
                    }
                }
            });
        } else {
            for (CaseUser user : users.values()) {
                if (user.isDirty()) {
                    plugin.getStorageManager().getStorage().saveUser(user);
                }
            }
        }
    }

    public Collection<CaseUser> getLoadedUsers() {
        return users.values();
    }
}
