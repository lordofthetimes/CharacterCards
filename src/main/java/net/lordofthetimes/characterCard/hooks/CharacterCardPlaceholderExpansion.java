package net.lordofthetimes.characterCard.hooks;

import dev.dejvokep.boostedyaml.YamlDocument;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.lordofthetimes.characterCard.CharacterCard;
import net.lordofthetimes.characterCard.database.DatabaseManager;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class CharacterCardPlaceholderExpansion extends PlaceholderExpansion{

    private final CharacterCard plugin;
    private final DatabaseManager db;
    private final YamlDocument config;

    public CharacterCardPlaceholderExpansion(CharacterCard plugin, DatabaseManager db, YamlDocument config){
        this.plugin = plugin;
        this.db = db;
        this.config = config;
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

        switch (params.toLowerCase()){
            case "name": return data.get("loreName");
            case "age": return data.get("age");
            case "race": return data.get("race");
            case "description": return data.get("description");
            case "lore": return data.get("lore");
            case "gender": return data.get("gender");
            case "religion": return data.get("religion");

            case "name_plain": return stripFormatting(data.get("loreName"));
            case "age_plain": return stripFormatting(data.get("age"));
            case "race_plain": return stripFormatting(data.get("race"));
            case "description_plain": return stripFormatting(data.get("description"));
            case "lore_plain": return stripFormatting(data.get("lore"));
            case "gender_plain": return stripFormatting(data.get("gender"));
            case "religion_plain": return stripFormatting(data.get("religion"));
            default: return null;
        }
    }

    private String stripFormatting(String text){
        MiniMessage mm = MiniMessage.miniMessage();
        return PlainTextComponentSerializer.plainText().serialize(mm.deserialize(text));
    }
}
