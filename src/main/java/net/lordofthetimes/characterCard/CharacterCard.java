package net.lordofthetimes.characterCard;

import net.lordofthetimes.characterCard.commands.CharacterCommand;
import net.lordofthetimes.characterCard.listeners.PlayerJoinListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class CharacterCard extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        DatabaseManager db = new DatabaseManager(this);
        db.connect("plugins/CharacterCard/charactercard.db");
        db.generateTables();
        this.getCommand("character").setExecutor(new CharacterCommand(this,db));
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(db,this.getLogger()), this);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
