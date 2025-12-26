package net.lordofthetimes.characterCard.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Set;

public class MessageSender {

    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    public static void sendMessage(CommandSender sender, String message){
        sender.sendMessage(deserialize("<gold><bold>[CC]</bold></gold> " + message));
    }

    public static void sendMessageNoPrefix(CommandSender sender, String message){
        sender.sendMessage(deserialize(message));
    }

    public static void sendCooldown(CommandSender sender, long time){
        sendMessage(sender,"<red>This command is on cooldown! Please try again in <bold>" + time/1000f +"</bold> !</red>");
    }

    public static Component deserialize(String input) {
        return miniMessage.deserialize(input);
    }
}
