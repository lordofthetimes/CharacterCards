package net.lordofthetimes.characterCard;

import org.bukkit.plugin.java.JavaPlugin;

import java.sql.*;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseManager {
    private final JavaPlugin plugin;
    private final Logger logger;
    private Connection connection;

    private final ConcurrentHashMap<UUID, ConcurrentHashMap<String,String>> playerDataCache = new ConcurrentHashMap<>();

    public void addPlayerDataCache(UUID uuid, ConcurrentHashMap<String,String> data){
        playerDataCache.put(uuid,data);
    }

    public void clearPlayerDataCache(UUID uuid){
        playerDataCache.remove(uuid);
        ConcurrentHashMap<String, String> data = new ConcurrentHashMap<>(2);
        data.put("loreName","<gray>None</gray>");
        data.put("lore","<gray>None</gray>");
        playerDataCache.put(uuid,data);
    }

    public void removePlayerDataCache(UUID uuid){
        playerDataCache.remove(uuid);
    }

    public ConcurrentHashMap<String,String> getPlayerDataCache(UUID uuid){
        return playerDataCache.get(uuid);
    }


    public DatabaseManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    public void connect(String filePath) {
        try {
            String url = "jdbc:sqlite:" + "plugins/CharacterCard/charactercard.db";
            connection = DriverManager.getConnection(url);
            logger.info("Connected to SQLite database!");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to connect to SQLite database!", e);
        }
    }

    public CompletableFuture<Void> generateTables(){
        return CompletableFuture.runAsync(() -> {
            String sql = """
            CREATE TABLE IF NOT EXISTS characters (
                uuid TEXT PRIMARY KEY,
                loreName TEXT,
                lore TEXT,
                bookData TEXT
            );
            """;

            try (Statement query = connection.createStatement()) {
                query.execute(sql);
                logger.info("Character table ensured in database.");
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Failed to create character table : ", e);
            }
        });
    }

    public CompletableFuture<String> getJsonBookData(UUID uuid){
        return CompletableFuture.supplyAsync(() ->{

            String sql = "SELECT bookData FROM characters where uuid = ? LIMIT 1";

            try (PreparedStatement query = connection.prepareStatement(sql)) {
                query.setString(1,uuid.toString());
                try (ResultSet rs = query.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("bookData");
                    }
                }
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Failed to fetch bookData from characters for uuid "+ uuid.toString() + " : ", e);
            }
            return null;
        });
    }
    public CompletableFuture<ConcurrentHashMap<String,String>> getPlayerData(UUID uuid){
        return CompletableFuture.supplyAsync(() ->{
            ConcurrentHashMap<String,String> result = new ConcurrentHashMap<>(2);

            String sql = "SELECT loreName,lore FROM characters WHERE uuid = ? LIMIT 1";

            try (PreparedStatement query = connection.prepareStatement(sql)){
                query.setString(1,uuid.toString());
                try (ResultSet rs = query.executeQuery()) {
                    if (rs.next()) {
                        result.put("lore",rs.getString("lore"));
                        result.put("loreName",rs.getString("loreName"));
                        return result;
                    }
                }

            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Failed to fetch player data from characters for uuid "+ uuid.toString() + " : ", e);
            }
            return null;
        });
    }

    public CompletableFuture<Boolean> resetPlayerData(UUID uuid){
        return CompletableFuture.supplyAsync(() ->{

            String sql = "UPDATE characters SET loreName = '<gray>None</gray>', lore = '<gray>None</gray>' WHERE uuid = ?";

            try (PreparedStatement query = connection.prepareStatement(sql)){
                query.setString(1, uuid.toString());
                return query.executeUpdate() > 0;
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Player data update failed for uuid : " + uuid, e);
            }
            return false;
        });
    }

    public CompletableFuture<Boolean> insertPlayerData(UUID uuid, String loreName, String lore, String bookData){
        return CompletableFuture.supplyAsync(() ->{
            String sql = "INSERT INTO characters(uuid,loreName,lore,bookData) VALUES(?,?,?,?)";

            try(PreparedStatement query = connection.prepareStatement(sql)){
                query.setString(1,uuid.toString());
                query.setString(2,loreName);
                query.setString(3,lore);
                query.setString(4,bookData);

                return query.executeUpdate() > 0;
            } catch (SQLException e){
                logger.log(Level.SEVERE,"Failed to insert data in characters for uuid "+ uuid.toString() + " : ", e);
            }
            return false;
        });
    }
    public CompletableFuture<Void> updateJsonBookData(String bookData, UUID uuid){
        return CompletableFuture.runAsync(()->{
            String sql = "UPDATE characters SET bookData = ? WHERE uuid = ?";
            try (PreparedStatement query = connection.prepareStatement(sql)) {
                query.setString(1,bookData);
                query.setString(2,uuid.toString());
                query.executeUpdate();
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Failed to UPDATE bookData from characters for uuid "+ uuid.toString() + " : ", e);
            }
        });
    }

    public CompletableFuture<Boolean> updateLore(String lore, UUID uuid){
        return CompletableFuture.supplyAsync(() ->{
            try {
                ConcurrentHashMap<String,String> data = getPlayerDataCache(uuid);
                if (data == null) {
                    throw new IllegalStateException("Cache missing for UUID: " + uuid);
                }
                data.replace("lore", lore);

                String sql = "UPDATE characters SET lore = ? WHERE uuid = ?";
                try (PreparedStatement query = connection.prepareStatement(sql)){
                    query.setString(1, lore);
                    query.setString(2, uuid.toString());
                    return query.executeUpdate() > 0;
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Async updateLore failed for uuid " + uuid, e);
                return false;
            }
        });
    }
    public CompletableFuture<Boolean> updateName(String name, UUID uuid){
        return CompletableFuture.supplyAsync(() ->{
            try {
                ConcurrentHashMap<String,String> data = getPlayerDataCache(uuid);
                if (data == null) {
                    throw new IllegalStateException("Cache missing for UUID: " + uuid);
                }
                data.replace("loreName", name);

                String sql = "UPDATE characters SET loreName = ? WHERE uuid = ?";
                try (PreparedStatement query = connection.prepareStatement(sql)){
                    query.setString(1, name);
                    query.setString(2, uuid.toString());
                    return query.executeUpdate() > 0;
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Async updateName failed for uuid " + uuid, e);
                return false;
            }
        });
    }

    public Connection getConnection() {
        return connection;
    }

    public void close() {
        try {
            if (connection != null) connection.close();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to close SQLite connection!", e);
        }
    }


}
