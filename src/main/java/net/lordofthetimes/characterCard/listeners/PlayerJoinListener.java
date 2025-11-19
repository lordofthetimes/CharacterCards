package net.lordofthetimes.characterCard.listeners;

import net.lordofthetimes.characterCard.DatabaseManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class PlayerJoinListener implements Listener {

    private final DatabaseManager db;
    private final Logger logger;

    public PlayerJoinListener(DatabaseManager db, Logger logger) {
        this.db = db;
        this.logger = logger;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (!player.hasPlayedBefore()) {
            db.insertPlayerData(player.getUniqueId(),"null","null","null")
                    .thenAccept(success -> {
                        if (!success) {
                            logger.warning("Failed to insert default data for " + player.getName());
                        }
                    });

        }

        db.getPlayerData(player.getUniqueId()).thenAccept(data -> {
            db.addPlayerDataCache(player.getUniqueId(),data);
        });
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        db.removePlayerDataCache(player.getUniqueId());
    }
}