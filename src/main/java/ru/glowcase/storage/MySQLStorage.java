package ru.glowcase.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.ConfigurationSection;
import ru.glowcase.GlowCasesPlugin;
import ru.glowcase.user.CaseUser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class MySQLStorage implements DataStorage {

    private final GlowCasesPlugin plugin;
    private final String tablePrefix;
    private HikariDataSource dataSource;

    public MySQLStorage(GlowCasesPlugin plugin) {
        this.plugin = plugin;
        this.tablePrefix = plugin.getConfig().getString("storage.mysql.table-prefix", "glowcases_");
    }

    @Override
    public void init() throws Exception {
        ConfigurationSection sec = plugin.getConfig().getConfigurationSection("storage.mysql");
        if (sec == null) {
            throw new IllegalStateException("Missing storage.mysql configuration section!");
        }

        String host = sec.getString("host", "localhost");
        int port = sec.getInt("port", 3306);
        String database = sec.getString("database", "glowcases");
        String username = sec.getString("username", "root");
        String password = sec.getString("password", "");

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=utf8");
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(sec.getInt("maximum-pool-size", 10));
        config.setMinimumIdle(sec.getInt("minimum-idle", 2));
        config.setConnectionTimeout(sec.getLong("connection-timeout", 30000));
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        this.dataSource = new HikariDataSource(config);

        try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + tablePrefix + "users (" +
                    "uuid VARCHAR(36) PRIMARY KEY, " +
                    "name VARCHAR(32), " +
                    "total_opened INT DEFAULT 0" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + tablePrefix + "keys (" +
                    "uuid VARCHAR(36), " +
                    "case_id VARCHAR(32), " +
                    "amount INT DEFAULT 0, " +
                    "PRIMARY KEY (uuid, case_id)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + tablePrefix + "stats (" +
                    "uuid VARCHAR(36), " +
                    "case_id VARCHAR(32), " +
                    "opened INT DEFAULT 0, " +
                    "PRIMARY KEY (uuid, case_id)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");
        }
    }

    @Override
    public CaseUser loadUser(UUID uuid, String name) {
        CaseUser user = new CaseUser(uuid, name);
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement("SELECT name, total_opened FROM " + tablePrefix + "users WHERE uuid = ?")) {
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

            try (PreparedStatement ps = conn.prepareStatement("SELECT case_id, amount FROM " + tablePrefix + "keys WHERE uuid = ?")) {
                ps.setString(1, uuid.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        user.setKeys(rs.getString("case_id"), rs.getInt("amount"));
                    }
                }
            }

            try (PreparedStatement ps = conn.prepareStatement("SELECT case_id, opened FROM " + tablePrefix + "stats WHERE uuid = ?")) {
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
            plugin.getLogger().log(Level.SEVERE, "Failed to load user " + uuid + " from MySQL", e);
            return user;
        }
    }

    @Override
    public void saveUser(CaseUser user) {
        if (user == null) return;
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO " + tablePrefix + "users (uuid, name, total_opened) VALUES (?, ?, ?) " +
                            "ON DUPLICATE KEY UPDATE name = VALUES(name), total_opened = VALUES(total_opened);")) {
                ps.setString(1, user.getUuid().toString());
                ps.setString(2, user.getName());
                ps.setInt(3, user.getTotalOpened());
                ps.executeUpdate();
            }

            for (Map.Entry<String, Integer> entry : user.getAllKeys().entrySet()) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO " + tablePrefix + "keys (uuid, case_id, amount) VALUES (?, ?, ?) " +
                                "ON DUPLICATE KEY UPDATE amount = VALUES(amount);")) {
                    ps.setString(1, user.getUuid().toString());
                    ps.setString(2, entry.getKey());
                    ps.setInt(3, entry.getValue());
                    ps.executeUpdate();
                }
            }

            for (Map.Entry<String, Integer> entry : user.getAllCaseOpened().entrySet()) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO " + tablePrefix + "stats (uuid, case_id, opened) VALUES (?, ?, ?) " +
                                "ON DUPLICATE KEY UPDATE opened = VALUES(opened);")) {
                    ps.setString(1, user.getUuid().toString());
                    ps.setString(2, entry.getKey());
                    ps.setInt(3, entry.getValue());
                    ps.executeUpdate();
                }
            }

            user.setDirty(false);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save user " + user.getUuid() + " to MySQL", e);
        }
    }

    @Override
    public void saveAll() {
        // Handled by user manager
    }

    @Override
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
