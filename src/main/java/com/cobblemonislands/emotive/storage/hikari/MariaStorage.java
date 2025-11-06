package com.cobblemonislands.emotive.storage.hikari;

import com.cobblemonislands.emotive.Emotive;
import com.cobblemonislands.emotive.storage.DatabaseConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MariaStorage extends AbstractHikariStorage {

    public MariaStorage(DatabaseConfig cfg) {
        super(Type.MARIADB, cfg);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt1 = conn.prepareStatement(
                     "CREATE TABLE IF NOT EXISTS " + Emotive.MODID + "_emotes (" +
                             "uuid VARCHAR(36) NOT NULL," +
                             "`key` VARCHAR(255) NOT NULL," +
                             "ts BIGINT NOT NULL," +
                             "PRIMARY KEY (uuid, `key`))"
             );
             PreparedStatement stmt2 = conn.prepareStatement(
                     "CREATE TABLE IF NOT EXISTS " + Emotive.MODID + "_emote_favourites (" +
                             "uuid VARCHAR(36) NOT NULL," +
                             "`key` VARCHAR(255) NOT NULL," +
                             "ts BIGINT NOT NULL," +
                             "PRIMARY KEY (uuid, `key`))"
             )) {
            stmt1.execute();
            stmt2.execute();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create MariaDB tables", e);
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

        String insert = "INSERT IGNORE INTO " + Emotive.MODID + "_emotes (uuid, `key`, ts) VALUES (?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insert)) {
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
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(delete)) {
            stmt.setString(1, uuid);
            stmt.setString(2, key);
            stmt.executeUpdate();

            String deleteFav = "DELETE FROM " + Emotive.MODID + "_emote_favourites WHERE uuid = ? AND `key` = ?";
            try (PreparedStatement favStmt = conn.prepareStatement(deleteFav)) {
                favStmt.setString(1, uuid);
                favStmt.setString(2, key);
                favStmt.executeUpdate();
            }

            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public boolean addFav(ServerPlayer player, ResourceLocation emote) {
        String uuid = player.getUUID().toString();
        String key = Emotive.MODID + "." + emote.toLanguageKey();
        long ts = currentTs();

        if (!owns(player, emote)) {
            return false;
        }

        String insert = "INSERT IGNORE INTO " + Emotive.MODID + "_emote_favourites (uuid, `key`, ts) VALUES (?, ?, ?)";
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
    public boolean removeFav(ServerPlayer player, ResourceLocation emote) {
        String uuid = player.getUUID().toString();
        String key = Emotive.MODID + "." + emote.toLanguageKey();

        String delete = "DELETE FROM " + Emotive.MODID + "_emote_favourites WHERE uuid = ? AND `key` = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(delete)) {
            stmt.setString(1, uuid);
            stmt.setString(2, key);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public List<String> listFavs(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        String query = "SELECT `key` FROM " + Emotive.MODID + "_emote_favourites WHERE uuid = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, uuid);
            try (ResultSet rs = stmt.executeQuery()) {
                List<String> result = new ArrayList<>();
                while (rs.next()) {
                    String k = rs.getString("key");
                    if (k != null && k.startsWith(Emotive.MODID)) {
                        result.add(k.substring(Emotive.MODID.length()+1));
                    }
                }
                return result;
            }
        } catch (SQLException e) {
            return Collections.emptyList();
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
    public List<String> list(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        String query = "SELECT `key` FROM " + Emotive.MODID + "_emotes WHERE uuid = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, uuid);
            try (ResultSet rs = stmt.executeQuery()) {
                List<String> result = new ArrayList<>();
                while (rs.next()) {
                    String k = rs.getString("key");
                    if (k != null && k.startsWith(Emotive.MODID)) {
                        result.add(k.substring(Emotive.MODID.length()+1));
                    }
                }
                return result;
            }
        } catch (SQLException e) {
            return Collections.emptyList();
        }
    }
}
