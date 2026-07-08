package net.lordofthetimes.characterCard.listeners;

import dev.dejvokep.boostedyaml.YamlDocument;
import net.lordofthetimes.characterCard.CharacterCard;
import net.lordofthetimes.characterCard.database.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.HashSet;
import java.util.UUID;

public class CharacterManager implements Listener {

    private final DatabaseManager db;
    private final CharacterCard plugin;
    private final YamlDocument config;

    private final HashSet<UUID> locked = new HashSet<>();

    public CharacterManager(DatabaseManager db, CharacterCard plugin) {
        this.db = db;
        this.plugin = plugin;
        this.config = plugin.config;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if(player.isOp() || player.hasPermission("charactercard.updateinfo")){
            plugin.updateChecker.sendVersionPlayer(player);
        }
        if(db.getPlayerDataCache(uuid) == null){
            String def = "<gray>None</gray>";
            Long joinTime = System.currentTimeMillis();
            db.insertPlayerData(uuid,def,def,def,def,def,joinTime)
                    .thenAccept(success -> {
                        if (success) {
                            db.addPlayerDataCache(uuid,db.getDefaultDataCache(String.valueOf(joinTime)));
                        }
                        else{
                            plugin.getLogger().warning("Failed to insert default data for uuid : " + uuid);
                        }
                    });
        }
    }

    @EventHandler
    public void shiftClickCard(PlayerInteractAtEntityEvent event){
        if(!config.getBoolean("shiftCard.enabled") || event.isCancelled()) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();

        if(isLocked(player)) return;
        if(!player.isSneaking()) return;
        if(!(event.getRightClicked() instanceof Player interacted)) return;

        event.setCancelled(true);
        UUID uuid = player.getUniqueId();
        locked.add(uuid);
        if(config.getString("shiftCard.mode").equals("BOOK")){
            if(player.hasPermission("charactercard.character.book")){
                plugin.characterCommand.openBook(player,interacted);
            }
        }
        else{
            if(player.hasPermission("charactercard.character.chat")){
                plugin.characterCommand.openCharacter(player,interacted);
            }
        }
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            locked.remove(uuid);
        }, 20L);
    }

    private Boolean isLocked(Player player){
        return locked.contains(player.getUniqueId());
    }
}