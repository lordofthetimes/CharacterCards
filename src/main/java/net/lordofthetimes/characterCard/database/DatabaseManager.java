package net.lordofthetimes.characterCard.database;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.*;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseManager {
    private final JavaPlugin plugin;
    private Connection connection;
    private final DatabaseLogger logger;
    private final Boolean debug;
    private final String path;

    private final ConcurrentHashMap<UUID, ConcurrentHashMap<String,String>> playerDataCache = new ConcurrentHashMap<>();

    public DatabaseManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.logger = new DatabaseLogger(plugin.getLogger());
        FileConfiguration config = plugin.getConfig();
        this.debug = config.getBoolean("database.debug");
        this.path = config.getString("database.path");
    }

    public void addPlayerDataCache(UUID uuid, ConcurrentHashMap<String,String> data){
        playerDataCache.put(uuid,data);
    }

    public void clearPlayerDataCache(UUID uuid){
        playerDataCache.remove(uuid);
        playerDataCache.put(uuid, getDefaultDataCache());
    }
    public void clearAllPlayerDataCache(){
        playerDataCache.clear();
    }

    public ConcurrentHashMap<String, String> getDefaultDataCache(){
        ConcurrentHashMap<String,String> map = new ConcurrentHashMap<>(5);
        map.put("loreName","<gray>None</gray>");
        map.put("age","<gray>None</gray>");
        map.put("race","<gray>None</gray>");
        map.put("description","<gray>None</gray>");
        map.put("lore","<gray>None</gray>");
        return map;
    }

    public void removePlayerDataCache(UUID uuid){
        playerDataCache.remove(uuid);
    }

    public ConcurrentHashMap<String,String> getPlayerDataCache(UUID uuid){
        return playerDataCache.get(uuid);
    }

    public void connect(String filePath) {
        try {
            String url = "jdbc:sqlite:" + plugin.getConfig().getString("database.path");
            connection = DriverManager.getConnection(url);
            logger.logInfo("Connected to SQLite database!");
        } catch (SQLException e) {
            logger.logError("Failed to connect to SQLite database! ",e);
        }
    }

    public CompletableFuture<Void> generateTables(){
        return CompletableFuture.runAsync(() -> {
            String sql = """
            CREATE TABLE IF NOT EXISTS characters (
                uuid TEXT PRIMARY KEY,
                loreName TEXT,
                age TEXT,
                race TEXT,
                description TEXT,
                lore TEXT
            );
            """;

            if(debug){
                logger.logQuery(sql);
            }

            try (Statement query = connection.createStatement()) {
                query.execute(sql);
                logger.logInfo("Character table ensured in database.");
            } catch (SQLException e) {
                logger.logError("Failed to create character table : ", e);
            }
        });
    }

    public CompletableFuture<String> getAge(UUID uuid){
        return CompletableFuture.supplyAsync(() ->{

            String sql = "SELECT age FROM characters where uuid = ? LIMIT 1";

            if(debug){
                logger.logQuery(sql);
            }

            try (PreparedStatement query = connection.prepareStatement(sql)) {
                query.setString(1,uuid.toString());
                try (ResultSet rs = query.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("age");
                    }
                }
            } catch (SQLException e) {
                logger.logError("Failed to fetch age from characters for uuid "+ uuid.toString() + " : ", e);
            }
            return null;
        });
    }
    public CompletableFuture<ConcurrentHashMap<String,String>> getPlayerData(UUID uuid){
        return CompletableFuture.supplyAsync(() ->{
            ConcurrentHashMap<String,String> result = new ConcurrentHashMap<>(2);

            String sql = "SELECT lore, loreName, age, race, description FROM characters WHERE uuid = ? LIMIT 1";

            if(debug){
                logger.logQuery(sql);
            }

            try (PreparedStatement query = connection.prepareStatement(sql)){
                query.setString(1,uuid.toString());
                try (ResultSet rs = query.executeQuery()) {
                    if (rs.next()) {
                        result.put("lore",rs.getString("lore"));
                        result.put("loreName",rs.getString("loreName"));
                        result.put("age",rs.getString("age"));
                        result.put("race",rs.getString("race"));
                        result.put("description",rs.getString("description"));
                        return result;
                    }
                }

            } catch (SQLException e) {
                logger.logError("Failed to fetch player data from characters for uuid "+ uuid.toString() + " : ", e);
            }
            return null;
        });
    }

    public CompletableFuture<Boolean> resetPlayerData(UUID uuid){
        return CompletableFuture.supplyAsync(() ->{

            String sql = "UPDATE characters SET loreName = '<gray>None</gray>', lore = '<gray>None</gray>', " +
                    "age = '<gray>None</gray>', race = '<gray>None</gray>', description = '<gray>None</gray>' WHERE uuid = ?";

            if(debug){
                logger.logQuery(sql);
            }

            try (PreparedStatement query = connection.prepareStatement(sql)){
                query.setString(1, uuid.toString());
                return query.executeUpdate() > 0;
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Player data update failed for uuid : " + uuid, e);
            }
            return false;
        });
    }

    public CompletableFuture<Boolean> insertPlayerData(UUID uuid, String loreName, String lore, String age, String race, String description){
        return CompletableFuture.supplyAsync(() ->{
            String sql = "INSERT INTO characters(uuid,loreName,lore,age,race,description) VALUES(?,?,?,?,?,?)";

            if(debug){
                logger.logQuery(sql);
            }

            try(PreparedStatement query = connection.prepareStatement(sql)){
                query.setString(1,uuid.toString());
                query.setString(2,loreName);
                query.setString(3,lore);
                query.setString(4,age);
                query.setString(5,race);
                query.setString(6,description);

                return query.executeUpdate() > 0;
            } catch (SQLException e){
                logger.logError("Failed to insert data in characters for uuid "+ uuid.toString() + " : ", e);
            }
            return false;
        });
    }

    public CompletableFuture<Boolean> updateAge(String age, UUID uuid){
        return CompletableFuture.supplyAsync(()->{
            try {
                ConcurrentHashMap<String,String> data = getPlayerDataCache(uuid);
                if (data == null) {
                    throw new IllegalStateException("Cache missing for UUID: " + uuid);
                }
                data.replace("age", age);
                String sql = "UPDATE characters SET age = ? WHERE uuid = ?";

                if(debug){
                    logger.logQuery(sql);
                }

                PreparedStatement query = connection.prepareStatement(sql);
                query.setString(1,age);
                query.setString(2,uuid.toString());
                return query.executeUpdate() > 0;
            } catch (SQLException e) {
                logger.logError("Failed to UPDATE age from characters for uuid "+ uuid.toString() + " : ", e);
            }
            return false;
        });
    }

    public CompletableFuture<Boolean> updateDescription(String description, UUID uuid){
        return CompletableFuture.supplyAsync(()->{
            try {
                ConcurrentHashMap<String,String> data = getPlayerDataCache(uuid);
                if (data == null) {
                    throw new IllegalStateException("Cache missing for UUID: " + uuid);
                }
                data.replace("description", description);
                String sql = "UPDATE characters SET description = ? WHERE uuid = ?";

                if(debug){
                    logger.logQuery(sql);
                }

                PreparedStatement query = connection.prepareStatement(sql);
                query.setString(1,description);
                query.setString(2,uuid.toString());
                return query.executeUpdate() > 0;
            } catch (SQLException e) {
                logger.logError("Failed to UPDATE description from characters for uuid "+ uuid.toString() + " : ", e);
            }
            return false;
        });
    }

    public CompletableFuture<Boolean> updateRace(String race, UUID uuid){
        return CompletableFuture.supplyAsync(()->{
            try {
                ConcurrentHashMap<String,String> data = getPlayerDataCache(uuid);
                if (data == null) {
                    throw new IllegalStateException("Cache missing for UUID: " + uuid);
                }
                data.replace("race", race);
                String sql = "UPDATE characters SET race = ? WHERE uuid = ?";

                if(debug){
                    logger.logQuery(sql);
                }

                PreparedStatement query = connection.prepareStatement(sql);
                query.setString(1,race);
                query.setString(2,uuid.toString());
                return query.executeUpdate() > 0;
            } catch (SQLException e) {
                logger.logError("Failed to UPDATE race from characters for uuid "+ uuid.toString() + " : ", e);
            }
            return false;
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

                if(debug){
                    logger.logQuery(sql);
                }

                try (PreparedStatement query = connection.prepareStatement(sql)){
                    query.setString(1, lore);
                    query.setString(2, uuid.toString());
                    return query.executeUpdate() > 0;
                }
            } catch (Exception e) {
                logger.logError("Async updateLore failed for uuid " + uuid, e);
            }
            return false;
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
                logger.logError("Async updateName failed for uuid " + uuid, e);
            }
            return false;
        });
    }

    public Connection getConnection() {
        return connection;
    }

    public void close() {
        try {
            if (connection != null) connection.close();
        } catch (SQLException e) {
            logger.logError("Failed to close SQLite connection!", e);
        }
    }


}
