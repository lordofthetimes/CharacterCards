package net.lordofthetimes.characterCard.database;

import dev.dejvokep.boostedyaml.YamlDocument;
import net.lordofthetimes.characterCard.CharacterCard;
import net.lordofthetimes.characterCard.utils.CharacterCardLogger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

public class DatabaseManager {
    private final CharacterCard plugin;
    private Connection connection;
    public final CharacterCardLogger logger;
    private final Boolean debug;
    private final String path;
    private final String ageMode;
    private final List<String> columns = List.of("loreName","age","race","description","lore","gender","religion","joinTime");
    private final String defaultValue = "<gray>None</gray>";
    private final ExecutorService dbExecutor =
            Executors.newSingleThreadExecutor();

    private ConcurrentHashMap<UUID, ConcurrentHashMap<String,String>> playerDataCache = new ConcurrentHashMap<>();

    public DatabaseManager(CharacterCard plugin) {
        this.plugin = plugin;
        this.logger = plugin.logger;
        YamlDocument config = plugin.config;
        this.debug = config.getBoolean("database.debug");
        this.path = config.getString("database.path");
        this.ageMode = config.getString("ageMode");
    }

    public void setPlayersDataCache(ConcurrentHashMap<UUID, ConcurrentHashMap<String,String>> map){
        this.playerDataCache = map;
    }

    public void addPlayerDataCache(UUID uuid, ConcurrentHashMap<String,String> data){
        playerDataCache.put(uuid,data);
    }

    public void clearPlayerDataCache(UUID uuid){
        String joinTime = playerDataCache.get(uuid).get("joinTime");
        playerDataCache.remove(uuid);
        playerDataCache.put(uuid, getDefaultDataCache(joinTime));
    }
    public void clearAllPlayerDataCache(){
        playerDataCache.clear();
    }

    public ConcurrentHashMap<String, String> getDefaultDataCache(String joinTime){
        ConcurrentHashMap<String,String> map = new ConcurrentHashMap<>();
        map.put("loreName",defaultValue);
        map.put("age",defaultValue);
        map.put("race",defaultValue);
        map.put("description",defaultValue);
        map.put("lore",defaultValue);
        map.put("gender",defaultValue);
        map.put("religion",defaultValue);
        map.put("joinTime",joinTime);
        return map;
    }

    public void removePlayerDataCache(UUID uuid){
        playerDataCache.remove(uuid);
    }

    public ConcurrentHashMap<String,String> getPlayerDataCache(UUID uuid){
        return playerDataCache.get(uuid);
    }


    public void tryAddColumns(){
        CompletableFuture.runAsync(() -> {
            List<String> mutableColumns = new ArrayList<>(List.copyOf(columns));
            String sql = "PRAGMA table_info(characters)";

            if (debug) {
                logger.logQuery(sql);
            }

            try (Statement query = connection.createStatement();
                 ResultSet rs = query.executeQuery(sql)) {

                while (rs.next()) {
                    mutableColumns.remove(rs.getString("name"));
                }
                if (!mutableColumns.isEmpty()) {
                    for (String column : mutableColumns) {
                        logger.logWarnDB("Column " + column + " is missing. The missing column will be created");
                        String columnSql = "ALTER TABLE characters ADD COLUMN " + column + " TEXT";
                        if(column.equals("joinTime")) columnSql = columnSql.replace("TEXT","INTEGER");

                        if (debug) {
                            logger.logQuery(columnSql);
                        }

                        try (Statement columnQuery = connection.createStatement()) {
                            columnQuery.executeUpdate(columnSql);
                            logger.logInfo("Successfully added missing column " + column);

                            String updateSql = "UPDATE characters SET " + column + " = ?";

                            if (debug) {
                                logger.logQuery(updateSql);
                            }

                            try (PreparedStatement updateQuery = connection.prepareStatement(updateSql)) {
                                if(column.equals("joinTime")){
                                    updateQuery.setLong(1, System.currentTimeMillis());
                                }
                                else{
                                    updateQuery.setString(1, defaultValue);
                                }
                                updateQuery.executeUpdate();
                                logger.logInfo("Successfully filled in default data for all players in column : " + column);
                            } catch (SQLException e) {
                                logger.logErrorDB("Failed to insert default columns: " + e.getMessage());
                            }

                        } catch (SQLException e) {
                            logger.logErrorDB("Failed to add column: " + e.getMessage());
                        }
                    }
                }
            } catch (SQLException e) {
                logger.logErrorDB("Failed PRAGMA INFO for characters, this likely means a major issue with the database: " + e.getMessage());
            }
        }, dbExecutor);
    }


    public void connect() {
        try {
            String url = "jdbc:sqlite:" + path;
            connection = DriverManager.getConnection(url);
            if(connection == null){
                throw new SQLException("SQLite connection is null!");
            }
            logger.logInfoDB("Connected to SQLite database!");
        } catch (SQLException e) {
            connection = null;
            logger.logErrorDB("Failed to connect to SQLite database!", e);
            throw new IllegalStateException("Database connection failed, plugin cannot start.");
        }
    }

    public void generateTables(){
        CompletableFuture.runAsync(() -> {
            String sql = """
                    CREATE TABLE IF NOT EXISTS characters (
                        uuid TEXT PRIMARY KEY,
                        loreName TEXT,
                        age TEXT,
                        gender TEXT,
                        race TEXT,
                        religion TEXT,
                        description TEXT,
                        lore TEXT
                        joinTime INTEGER
                    );
                    """;

            if (debug) {
                logger.logQuery(sql);
            }

            try (Statement query = connection.createStatement()) {
                query.execute(sql);
                logger.logInfoDB("Character table ensured in database.");
            } catch (SQLException e) {
                logger.logErrorDB("Failed to create character table : ", e);
            }
        }, dbExecutor);
    }



    public CompletableFuture<String> getGender(UUID uuid){
        return CompletableFuture.supplyAsync(() ->{

            String sql = "SELECT gender FROM characters where uuid = ? LIMIT 1";

            if(debug){
                logger.logQuery(sql);
            }

            try (PreparedStatement query = connection.prepareStatement(sql)) {
                query.setString(1,uuid.toString());
                try (ResultSet rs = query.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("gender");
                    }
                }
            } catch (SQLException e) {
                logger.logErrorDB("Failed to fetch gender from characters for uuid "+ uuid.toString() + " : ", e);
            }
            return null;
        },dbExecutor);
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
                logger.logErrorDB("Failed to fetch age from characters for uuid "+ uuid.toString() + " : ", e);
            }
            return null;
        },dbExecutor);
    }

    public CompletableFuture<ConcurrentHashMap<String,String>> getPlayerData(UUID uuid){
        return CompletableFuture.supplyAsync(() ->{
            ConcurrentHashMap<String,String> result = new ConcurrentHashMap<>(2);

            String sql = "SELECT uuid, lore, loreName, age, race, description,gender,religion,joinTime FROM characters WHERE uuid = ? LIMIT 1";

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
                        result.put("joinTime", String.valueOf(rs.getLong("joinTime")));
                        result.put("religion",rs.getString("religion"));
                        return result;
                    }
                }

            } catch (SQLException e) {
                logger.logErrorDB("Failed to fetch player data from characters for uuid "+ uuid.toString() + " : ", e);
            }
            return null;
        },dbExecutor);
    }

    public CompletableFuture<ConcurrentHashMap<UUID, ConcurrentHashMap<String, String>>> getAllPlayersData() {
        return CompletableFuture.supplyAsync(() -> {
            ConcurrentHashMap<UUID, ConcurrentHashMap<String, String>> allData = new ConcurrentHashMap<>();

            String sql = "SELECT uuid, lore, loreName, age, race, description,gender,religion,joinTime FROM characters";

            if(debug){
                logger.logQuery(sql);
            }

            try (PreparedStatement query = connection.prepareStatement(sql);
                 ResultSet rs = query.executeQuery()) {

                while (rs.next()) {
                    UUID uuid = UUID.fromString(rs.getString("uuid"));
                    ConcurrentHashMap<String, String> playerData = new ConcurrentHashMap<>(5);

                    playerData.put("lore", rs.getString("lore"));
                    playerData.put("loreName", rs.getString("loreName"));
                    playerData.put("age", rs.getString("age"));
                    playerData.put("race", rs.getString("race"));
                    playerData.put("description", rs.getString("description"));
                    playerData.put("gender", rs.getString("gender"));
                    playerData.put("joinTime", String.valueOf(rs.getLong("joinTime")));
                    playerData.put("religion",rs.getString("religion"));

                    allData.put(uuid, playerData);
                }

            } catch (SQLException e) {
                logger.logErrorDB("Failed to fetch all player data from characters: ", e);
            }

            return allData;
        },dbExecutor);
    }

    public CompletableFuture<Boolean> resetPlayerData(UUID uuid){
        return CompletableFuture.supplyAsync(() ->{

            String sql = "UPDATE characters SET loreName = ?, lore = ?, " +
                    "age = ?, race = ?, description = ?, gender = ?,religion = ? WHERE uuid = ?";

            if(debug){
                logger.logQuery(sql);
            }
            //
            try (PreparedStatement query = connection.prepareStatement(sql)){
                query.setString(1, defaultValue);
                query.setString(2, defaultValue);
                query.setString(3, defaultValue);
                query.setString(4, defaultValue);
                query.setString(5, defaultValue);
                query.setString(6, defaultValue);
                query.setString(7, defaultValue);
                query.setString(8, uuid.toString());
                return query.executeUpdate() > 0;
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Player data update failed for uuid : " + uuid, e);
            }
            return false;
        },dbExecutor);
    }

    public CompletableFuture<Boolean> insertPlayerData(UUID uuid, String loreName, String lore, String age, String race, String description,Long joinTime){
        return CompletableFuture.supplyAsync(() ->{
            String sql = "INSERT INTO characters(uuid,loreName,lore,age,race,description,gender,religion,joinTIme) VALUES(?,?,?,?,?,?,?,?,?)";

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
                query.setString(7,description);
                query.setString(8,description);
                query.setLong(9,joinTime);

                return query.executeUpdate() > 0;
            } catch (SQLException e){
                logger.logErrorDB("Failed to insert data in characters for uuid "+ uuid.toString() + " : ", e);
            }
            return false;
        },dbExecutor);
    }

    public CompletableFuture<Boolean> updateGender(String gender, UUID uuid){
        return CompletableFuture.supplyAsync(()->{
            try {
                ConcurrentHashMap<String,String> data = getPlayerDataCache(uuid);
                if (data == null) {
                    throw new IllegalStateException("Cache missing for UUID: " + uuid);
                }
                data.replace("gender", gender);
                String sql = "UPDATE characters SET gender = ? WHERE uuid = ?";

                if(debug){
                    logger.logQuery(sql);
                }

                try(PreparedStatement query = connection.prepareStatement(sql)){
                    query.setString(1,gender);
                    query.setString(2,uuid.toString());
                    return query.executeUpdate() > 0;
                }
            } catch (SQLException e) {
                logger.logErrorDB("Failed to UPDATE gender from characters for uuid "+ uuid.toString() + " : ", e);
            }
            return false;
        },dbExecutor);
    }

    public CompletableFuture<Boolean> updateReligion(String religion, UUID uuid){
        return CompletableFuture.supplyAsync(()->{
            try {
                ConcurrentHashMap<String,String> data = getPlayerDataCache(uuid);
                if (data == null) {
                    throw new IllegalStateException("Cache missing for UUID: " + uuid);
                }
                data.replace("religion", religion);
                String sql = "UPDATE characters SET religion = ? WHERE uuid = ?";

                if(debug){
                    logger.logQuery(sql);
                }

                try(PreparedStatement query = connection.prepareStatement(sql)){
                    query.setString(1,religion);
                    query.setString(2,uuid.toString());
                    return query.executeUpdate() > 0;
                }
            } catch (SQLException e) {
                logger.logErrorDB("Failed to UPDATE religion from characters for uuid "+ uuid.toString() + " : ", e);
            }
            return false;
        },dbExecutor);
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

                try(PreparedStatement query = connection.prepareStatement(sql)){
                    query.setString(1,age);
                    query.setString(2,uuid.toString());
                    return query.executeUpdate() > 0;
                }
            } catch (SQLException e) {
                logger.logErrorDB("Failed to UPDATE age from characters for uuid "+ uuid.toString() + " : ", e);
            }
            return false;
        },dbExecutor);
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

                try(PreparedStatement query = connection.prepareStatement(sql)){
                    query.setString(1,description);
                    query.setString(2,uuid.toString());
                    return query.executeUpdate() > 0;
                }
            } catch (SQLException e) {
                logger.logErrorDB("Failed to UPDATE description from characters for uuid "+ uuid.toString() + " : ", e);
            }
            return false;
        },dbExecutor);
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

                try(PreparedStatement query = connection.prepareStatement(sql)){
                    query.setString(1,race);
                    query.setString(2,uuid.toString());
                    return query.executeUpdate() > 0;
                }
            } catch (SQLException e) {
                logger.logErrorDB("Failed to UPDATE race from characters for uuid "+ uuid.toString() + " : ", e);
            }
            return false;
        },dbExecutor);
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
                logger.logErrorDB("Async updateLore failed for uuid " + uuid, e);
            }
            return false;
        },dbExecutor);
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
                logger.logErrorDB("Async updateName failed for uuid " + uuid, e);
            }
            return false;
        },dbExecutor);
    }

    public Connection getConnection() {
        return connection;
    }

    public void close() {
        try {
            if (connection != null) connection.close();
        } catch (SQLException e) {
            logger.logErrorDB("Failed to close SQLite connection!", e);
        }
    }


}
