package net.lordofthetimes.characterCard;

import org.bukkit.plugin.java.JavaPlugin;

import java.sql.*;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseManager {
    private final JavaPlugin plugin;
    private final Logger logger;
    private Connection connection;

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

    public CompletableFuture<List<String>> getPlayerData(UUID uuid){
        return CompletableFuture.supplyAsync(() ->{

            String sql = "SELECT loreName,lore FROM characters WHERE uuid = ? LIMIT 1";

            try (PreparedStatement query = connection.prepareStatement(sql)){
                query.setString(1,uuid.toString());
                try (ResultSet rs = query.executeQuery()) {
                    if (rs.next()) {
                        return List.of(rs.getString("loreName"),rs.getString("lore"));
                    }
                }

            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Failed to fetch player data from characters for uuid "+ uuid.toString() + " : ", e);
            }

            return null;
        });
    }

    public CompletableFuture<Boolean> insertPlayerData(UUID uuid, String loreName, String lore, String bookData){
        return CompletableFuture.supplyAsync(() ->{
            String sql = "INSERT INTO characters(uuid,loreName,lore,bookData) VALUES(?,?,?,?)";

            try(PreparedStatement query = connection.prepareStatement(sql)){
                query.setString(1,uuid.toString());
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
