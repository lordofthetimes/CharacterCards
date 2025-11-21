package net.lordofthetimes.characterCard;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import net.lordofthetimes.characterCard.commands.CharacterCommand;
import net.lordofthetimes.characterCard.database.DatabaseManager;
import net.lordofthetimes.characterCard.listeners.PlayerJoinListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public final class CharacterCard extends JavaPlugin {

    public boolean landsEnabled = false;
    public LandsHook lands;
    private DatabaseManager db;


    @Override
    public void onLoad() {
        CommandAPI.onLoad(new CommandAPIBukkitConfig(this));
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();

        if (getServer().getPluginManager().getPlugin("Lands") != null && getConfig().getBoolean("lands.enabled")) {
            getLogger().info("Lands detected! Enabling town features...");
            enableLandsSupport();
        }

        db = new DatabaseManager(this);
        db.connect("plugins/CharacterCard/charactercard.db");
        db.generateTables();

        for(Player player : Bukkit.getOnlinePlayers()){
            loadPlayerData(player.getUniqueId());
        }
        new CharacterCommand(this, db);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(db,this), this);

    }

    @Override
    public void onDisable() {
        this.db.close();
        this.db.clearAllPlayerDataCache();
    }

    private void enableLandsSupport(){
        this.landsEnabled = true;
        this.lands = new LandsHook(this);
    }

    public void loadPlayerData(UUID uuid){
        db.getPlayerData(uuid).thenAccept(data -> {
            if (data == null) {
                db.addPlayerDataCache(uuid,db.getDefaultDataCache());
                String def = "<gray>None</gray>";
                db.insertPlayerData(uuid,def,def,def,def,def)
                        .thenAccept(success -> {
                            if (!success) {
                                this.getLogger().warning("Failed to insert default data for uuid : " + uuid);
                            }
                        });
            }
            else{
                db.addPlayerDataCache(uuid,data);
            }

        });
    }


}
