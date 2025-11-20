package net.lordofthetimes.characterCard.listeners;

import net.lordofthetimes.characterCard.CharacterCard;
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
    private final CharacterCard plugin;

    public PlayerJoinListener(DatabaseManager db, CharacterCard plugin) {
        this.db = db;
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        plugin.loadPlayer(db,player);

    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        db.removePlayerDataCache(player.getUniqueId());
    }
}