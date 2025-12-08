package net.lordofthetimes.characterCard.hooks;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.lordofthetimes.characterCard.CharacterCard;
import net.lordofthetimes.characterCard.database.DatabaseManager;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ConcurrentHashMap;

public class CharacterCardPlaceholderExpansion extends PlaceholderExpansion{

    private final CharacterCard plugin;
    private final DatabaseManager db;

    public CharacterCardPlaceholderExpansion(CharacterCard plugin, DatabaseManager db){
        this.plugin = plugin;
        this.db = db;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "charactercard";
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join("' ",plugin.getDescription().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        ConcurrentHashMap<String,String> data = db.getPlayerDataCache(player.getUniqueId());
        if (params.equalsIgnoreCase("name")) {
            return data.get("loreName");
        }
        if (params.equalsIgnoreCase("age")) {
            return data.get("age");
        }
        if (params.equalsIgnoreCase("race")) {
            return data.get("race");
        }
        if (params.equalsIgnoreCase("description")) {
            return data.get("description");
        }
        if (params.equalsIgnoreCase("lore")) {
            return data.get("lore");
        }
        return null;
    }
}
