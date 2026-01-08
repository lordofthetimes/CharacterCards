package net.lordofthetimes.characterCard.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.lordofthetimes.characterCard.CharacterCard;
import net.lordofthetimes.characterCard.utils.MessageSender;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LocalCommand {

    private final CharacterCard plugin;

    public final Map<UUID,Boolean> localMode = new HashMap<>();
    public final Map<UUID,Boolean> bypass = new HashMap<>();

    public int distance;
    public String prefix;
    public String name;

    public LocalCommand(CharacterCard plugin){
        prefix = plugin.config.getString("localChat.prefix");
        distance = plugin.config.getInt("localChat.distance");
        name = plugin.config.getString("localChat.name");
        this.plugin = plugin;

        new CommandAPICommand("local")
                .withPermission("charactercard.local.use")
                .withAliases("lc")
                .withOptionalArguments(new GreedyStringArgument("message"))
                .executes((sender,args) ->{
                    if(!(sender instanceof Player player))  return;
                    if(args.get("message") == null){
                        UUID uuid = player.getUniqueId();
                        localMode.put(uuid,!localMode.get(uuid));
                        String state = localMode.get(uuid) ? "enabled" : "disabled";
                        MessageSender.sendMessageNoPrefix(player,prefix + "You have " + state + " local chat!");
                    }
                    else{
                        String message = args.get("message").toString();
                        sendLocal(player, LegacyComponentSerializer.legacySection().deserialize(message));
                    }
                }).register();

        new CommandAPICommand("localspy")
                .withPermission("charactercard.local.spy")
                .withAliases("lcspy")
                .executes((sender,args) ->{
                    if(!(sender instanceof Player player))  return;
                    UUID uuid = player.getUniqueId();
                    bypass.put(uuid,!bypass.get(uuid));
                    String state = bypass.get(uuid) ? "enabled" : "disabled";
                    MessageSender.sendMessage(player,"<yellow> Local chat spy mode is now " + state + "!</yellow>");
                }).register();
    }

    public void sendLocal(Player player, Component message){

        String finalMessage = prefix + name;

        if (plugin.essentialsXEnabled) {
            String nickname = plugin.essentials.getNickname(player.getUniqueId());

            if (nickname == null || nickname.equals(player.getName())) {
                finalMessage = finalMessage.replace("<%nickname%>", player.getName());
            } else {
                finalMessage = finalMessage.replace("<%nickname%>", nickname + "*");
            }
        }

        finalMessage = finalMessage.replace("<%player_name%>", player.getName());

        if (plugin.papiEnabled) {
            finalMessage = PlaceholderAPI.setPlaceholders(player, finalMessage);
        }

        Bukkit.getServer().getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(finalMessage).append(message));
        for(Player onlinePlayer : Bukkit.getOnlinePlayers()){
            if(withinDistance(onlinePlayer,player) ||
                    (onlinePlayer.hasPermission("charactercard.local.spy")
                            && bypass.get(onlinePlayer.getUniqueId())
                    )){
                onlinePlayer.sendMessage(MiniMessage.miniMessage().deserialize(finalMessage).append(message));
            }
        }
    }

    public boolean withinDistance(Player p1, Player p2) {
        if (!p1.getWorld().equals(p2.getWorld())) return false;

        return p1.getLocation().distanceSquared(p2.getLocation()) <= distance * distance;
    }

}
