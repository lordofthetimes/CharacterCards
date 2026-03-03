package net.lordofthetimes.characterCard.commands;

import dev.dejvokep.boostedyaml.YamlDocument;
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
import org.checkerframework.checker.regex.qual.Regex;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LocalCommand {

    private final CharacterCard plugin;

    public final Map<UUID,Boolean> localMode = new HashMap<>();
    public final Map<UUID,Boolean> bypass = new HashMap<>();

    public int distance;
    public String prefix;
    public boolean formattingEnabled;
    public String screamPrefix;
    public String shoutPrefix;
    public String mutterPrefix;
    public String whisperPrefix;
    public String name;

    public LocalCommand(CharacterCard plugin){
        YamlDocument config = plugin.config;
        prefix = config.getString("localChat.prefix");
        distance = config.getInt("localChat.distance");
        name = config.getString("localChat.name");
        formattingEnabled = config.getBoolean("localChat.format.enabled",true);
        screamPrefix = config.getString("localChat.format.scream");
        shoutPrefix = config.getString("localChat.format.shout");
        mutterPrefix = config.getString("localChat.format.mutter");
        whisperPrefix = config.getString("localChat.format.whisper");
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
            if(withinDistance(onlinePlayer,player,MiniMessage.miniMessage().serialize(message)) ||
                    (onlinePlayer.hasPermission("charactercard.local.spy")
                            && bypass.get(onlinePlayer.getUniqueId())
                    )){
                onlinePlayer.sendMessage(MiniMessage.miniMessage().deserialize(finalMessage).append(formatMessage(message)));
            }
        }
    }

    public boolean withinDistance(Player p1, Player p2, String message) {

        int distance = this.distance;
        if(message.startsWith(screamPrefix)) distance = distance * 2;
        else if(message.startsWith(shoutPrefix)) distance = (int) (distance * 1.5f);
        else if(message.startsWith(whisperPrefix)) distance = distance / 5;
        else if(message.startsWith(mutterPrefix)) distance = distance / 3;

        if(message.length() < 2 || message.equals(screamPrefix)) distance = this.distance;

        if (!p1.getWorld().equals(p2.getWorld())) return false;

        return p1.getLocation().distanceSquared(p2.getLocation()) <= distance * distance;
    }

    private Component formatMessage(Component message){
        String stringMessage = LegacyComponentSerializer.legacyAmpersand().serialize(message);
        if(stringMessage.length() < 2) return message;

        if(stringMessage.startsWith(screamPrefix)) stringMessage =  stringMessage.replaceAll("^" + screamPrefix,"&o[Screams]&r ");
        else if(stringMessage.startsWith(shoutPrefix)) stringMessage =  stringMessage.replaceAll("^" + shoutPrefix,"&o[Shouts]&r ");
        else if(stringMessage.startsWith(whisperPrefix)) stringMessage =  stringMessage.replaceAll("^\\%s".formatted(whisperPrefix),"&o[Whispers]&r ");
        else if(stringMessage.startsWith(mutterPrefix)) stringMessage =  stringMessage.replaceAll("^\\%s".formatted(mutterPrefix),"&o[Mutters]&r ");
        return LegacyComponentSerializer.legacyAmpersand().deserialize(stringMessage);
    }

}
