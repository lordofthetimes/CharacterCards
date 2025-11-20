package net.lordofthetimes.characterCard;

import me.angeschossen.lands.api.LandsIntegration;
import net.lordofthetimes.characterCard.commands.CharacterCommand;
import net.lordofthetimes.characterCard.listeners.PlayerJoinListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.ConcurrentHashMap;

public final class CharacterCard extends JavaPlugin {

    public boolean landsEnabled = false;
    public LandsHook lands;


    @Override
    public void onEnable() {
        saveDefaultConfig();

        if (getServer().getPluginManager().getPlugin("Lands") != null) {
            getLogger().info("Lands detected! Enabling town features...");
            enableLandsSupport();
        }

        DatabaseManager db = new DatabaseManager(this);
        db.connect("plugins/CharacterCard/charactercard.db");
        db.generateTables();

        for(Player player : Bukkit.getOnlinePlayers()){
            loadPlayer(db,player);
        }

        this.getCommand("character").setExecutor(new CharacterCommand(this,db));
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(db,this), this);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void enableLandsSupport(){
        this.landsEnabled = true;
        this.lands = new LandsHook(this);
    }

    public void loadPlayer(DatabaseManager db, Player player){
        db.getPlayerData(player.getUniqueId()).thenAccept(data -> {
            if (data == null) {

                ConcurrentHashMap<String,String> map = new ConcurrentHashMap<>(2);
                map.put("loreName","<gray>None</gray>");
                map.put("lore","<gray>None</gray>");
                db.addPlayerDataCache(player.getUniqueId(),map);

                db.insertPlayerData(player.getUniqueId(),"<gray>None</gray>","<gray>None</gray>","null")
                        .thenAccept(success -> {
                            if (!success) {
                                this.getLogger().warning("Failed to insert default data for " + player.getName());
                            }
                        });
            }
            else{
                db.addPlayerDataCache(player.getUniqueId(),data);
            }

        });
    }


}
