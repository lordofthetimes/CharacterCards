package net.lordofthetimes.characterCard.listeners;

import net.lordofthetimes.characterCard.CharacterCard;
import net.lordofthetimes.characterCard.database.DatabaseManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

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
        UUID uuid = player.getUniqueId();
        if(!player.hasPlayedBefore()){
            db.addPlayerDataCache(uuid,db.getDefaultDataCache());
            String def = "<gray>None</gray>";
            db.insertPlayerData(uuid,def,def,def,def,def)
                    .thenAccept(success -> {
                        if (!success) {
                            plugin.getLogger().warning("Failed to insert default data for uuid : " + uuid);
                        }
                    });
        }
    }
}