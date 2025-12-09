package net.lordofthetimes.characterCard.hooks;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import dev.dejvokep.boostedyaml.YamlDocument;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.lordofthetimes.characterCard.CharacterCard;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class EssentialsXHook {

    public final Essentials essentials;
    public final YamlDocument config;
    public final boolean useNickname;
    public EssentialsXHook(YamlDocument config) {
        this.useNickname = config.getBoolean("essentials.nickname");
        this.essentials = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
        this.config = config;
    }

    public void updateNickname(OfflinePlayer player, String name){
        User user = essentials.getUser(player.getUniqueId());
        if(name == null){
            user.setNickname(null);
            return;
        }

        String cleanName = PlainTextComponentSerializer.plainText()
                .serialize(MiniMessage.miniMessage().deserialize(name))
                .replaceAll("(?i)[§&][0-9A-FK-ORX]", "");

        user.setNickname(cleanName);
    }

}
