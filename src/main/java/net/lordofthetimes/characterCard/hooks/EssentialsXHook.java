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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    public String getNickname(UUID uuid){
        User user = essentials.getUser(uuid);
        return user.getNickname();
    }

    public List<String> getAllOnlineNicknames(){
         Iterable<User> users = essentials.getOnlineUsers();
         List<String> nicknames = new ArrayList<>();
         users.forEach(user ->{
             String nickname = user.getNickname();
             if(nickname != null && !nickname.equals(user.getName())){
                 nicknames.add(user.getNickname());
             }
         });
        return nicknames;
    }

    public User getOnlineUserByNickname(String nickname) {
        for (User user : essentials.getOnlineUsers()) {
            String nick = user.getNickname();

            if (nick == null) continue;
            if (nick.equalsIgnoreCase(nickname)) {
                return user;
            }
        }
        return null;
    }


}
