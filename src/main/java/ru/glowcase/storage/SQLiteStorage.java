package ru.glowcase.storage;

import ru.glowcase.GlowCasesPlugin;
import ru.glowcase.user.CaseUser;

import java.io.File;
import java.sql.*;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class SQLiteStorage implements DataStorage {

    private final GlowCasesPlugin plugin;
    private final File dbFile;
    private Connection connection;

    public SQLiteStorage(GlowCasesPlugin plugin) {
        this.plugin = plugin;
        this.dbFile = new File(plugin.getDataFolder(), "data" + File.separator + "database.db");
    }

    private synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            if (!dbFile.getParentFile().exists()) {
                dbFile.getParentFile().mkdirs();
            }
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
        }
        return connection;
    }

    @Override
    public void init() throws Exception {
        Class.forName("org.sqlite.JDBC");
        try (Statement stmt = getConnection().createStatement()) {
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS glowcases_users (" +
                    "uuid VARCHAR(36) PRIMARY KEY, " +
                    "name VARCHAR(32), " +
                    "total_opened INT DEFAULT 0" +
                    ");");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS glowcases_keys (" +
                    "uuid VARCHAR(36), " +
                    "case_id VARCHAR(32), " +
                    "amount INT DEFAULT 0, " +
                    "PRIMARY KEY (uuid, case_id)" +
                    ");");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS glowcases_stats (" +
                    "uuid VARCHAR(36), " +
                    "case_id VARCHAR(32), " +
                    "opened INT DEFAULT 0, " +
                    "PRIMARY KEY (uuid, case_id)" +
                    ");");
        }
    }

    @Override
    public CaseUser loadUser(UUID uuid, String name) {
        CaseUser user = new CaseUser(uuid, name);
        try {
            Connection conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement("SELECT name, total_opened FROM glowcases_users WHERE uuid = ?")) {
                ps.setString(1, uuid.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String storedName = rs.getString("name");
                        if (storedName != null && name == null) {
                            user.setName(storedName);
                        }
                        user.setTotalOpened(rs.getInt("total_opened"));
                    }
                }
            }

            try (PreparedStatement ps = conn.prepareStatement("SELECT case_id, amount FROM glowcases_keys WHERE uuid = ?")) {
                ps.setString(1, uuid.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        user.setKeys(rs.getString("case_id"), rs.getInt("amount"));
                    }
                }
            }

            try (PreparedStatement ps = conn.prepareStatement("SELECT case_id, opened FROM glowcases_stats WHERE uuid = ?")) {
                ps.setString(1, uuid.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        user.setCaseOpened(rs.getString("case_id"), rs.getInt("opened"));
                    }
                }
            }

            user.setDirty(false);
            return user;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load user " + uuid + " from SQLite", e);
            return user;
        }
    }

    @Override
    public void saveUser(CaseUser user) {
        if (user == null) return;
        try {
            Connection conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO glowcases_users (uuid, name, total_opened) VALUES (?, ?, ?) " +
                            "ON CONFLICT(uuid) DO UPDATE SET name = excluded.name, total_opened = excluded.total_opened;")) {
                ps.setString(1, user.getUuid().toString());
                ps.setString(2, user.getName());
                ps.setInt(3, user.getTotalOpened());
                ps.executeUpdate();
            }

            // Save keys
            for (Map.Entry<String, Integer> entry : user.getAllKeys().entrySet()) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO glowcases_keys (uuid, case_id, amount) VALUES (?, ?, ?) " +
                                "ON CONFLICT(uuid, case_id) DO UPDATE SET amount = excluded.amount;")) {
                    ps.setString(1, user.getUuid().toString());
                    ps.setString(2, entry.getKey());
                    ps.setInt(3, entry.getValue());
                    ps.executeUpdate();
                }
            }

            // Save stats
            for (Map.Entry<String, Integer> entry : user.getAllCaseOpened().entrySet()) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO glowcases_stats (uuid, case_id, opened) VALUES (?, ?, ?) " +
                                "ON CONFLICT(uuid, case_id) DO UPDATE SET opened = excluded.opened;")) {
                    ps.setString(1, user.getUuid().toString());
                    ps.setString(2, entry.getKey());
                    ps.setInt(3, entry.getValue());
                    ps.executeUpdate();
                }
            }

            user.setDirty(false);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save user " + user.getUuid() + " to SQLite", e);
        }
    }

    @Override
    public void saveAll() {
        // Handled by UserManager iterating dirty users
    }

    @Override
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException ignored) {}
    }
}
