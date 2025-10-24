package com.cobblemonislands.emotive.storage.hikari;

import com.cobblemonislands.emotive.Emotive;
import com.cobblemonislands.emotive.storage.DatabaseConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SqliteStorage extends AbstractHikariStorage {
    public SqliteStorage(DatabaseConfig cfg) {
        super(Type.SQLITE, cfg);

        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(
                     "CREATE TABLE IF NOT EXISTS " + Emotive.MODID + "_emotes (" +
                     "uuid TEXT NOT NULL," +
                     "`key` TEXT NOT NULL," +
                     "ts INTEGER NOT NULL," +
                     "PRIMARY KEY (uuid, `key`))"
             )) {
            stmt.execute();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create emote table", e);
        }
    }

    private static long currentTs() {
        return System.currentTimeMillis() / 1000L;
    }

    @Override
    public boolean add(ServerPlayer player, ResourceLocation animation) {
        String uuid = player.getUUID().toString();
        String key = Emotive.MODID + "." + animation.toLanguageKey();
        long ts = currentTs();

        String insert = "INSERT OR IGNORE INTO " + Emotive.MODID + "_emotes (uuid, `key`, ts) VALUES (?, ?, ?)";
        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(insert)) {
            stmt.setString(1, uuid);
            stmt.setString(2, key);
            stmt.setLong(3, ts);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public boolean remove(ServerPlayer player, ResourceLocation animation) {
        String uuid = player.getUUID().toString();
        String key = Emotive.MODID + "." + animation.toLanguageKey();

        String delete = "DELETE FROM " + Emotive.MODID + "_emotes WHERE uuid = ? AND `key` = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(delete)) {
            stmt.setString(1, uuid);
            stmt.setString(2, key);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public boolean owns(ServerPlayer player, ResourceLocation animation) {
        String uuid = player.getUUID().toString();
        String key = Emotive.MODID + "." + animation.toLanguageKey();

        String query = "SELECT 1 FROM " + Emotive.MODID + "_emotes WHERE uuid = ? AND `key` = ? LIMIT 1";
        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, uuid);
            stmt.setString(2, key);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public int timestamp(ServerPlayer player, ResourceLocation animation) {
        String uuid = player.getUUID().toString();
        String key = Emotive.MODID + "." + animation.toLanguageKey();

        String query = "SELECT ts FROM " + Emotive.MODID + "_emotes WHERE uuid = ? AND `key` = ? LIMIT 1";
        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, uuid);
            stmt.setString(2, key);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt("ts");
            }
        } catch (SQLException ignored) {
        }
        return 0;
    }

    @Override
    public List<String> list(ServerPlayer player) {
        String uuid = player.getUUID().toString();

        String query = "SELECT `key` FROM " + Emotive.MODID + "_emotes WHERE uuid = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, uuid);
            try (ResultSet rs = stmt.executeQuery()) {
                List<String> result = new ArrayList<>();
                while (rs.next()) {
                    String k = rs.getString("key");
                    if (k != null && k.startsWith(Emotive.MODID)) result.add(k);
                }
                return result;
            }
        } catch (SQLException e) {
            return Collections.emptyList();
        }
    }
}
