package net.lordofthetimes.characterCard.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Set;

public class MessageSender {

    private static final MiniMessage miniMessage = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY =
            LegacyComponentSerializer.builder()
                    .character('&')
                    .hexColors()
                    .build();


    public static void sendMissingSubCommand(CommandSender sender, Set<String> commands){
        sendMessage(sender,"<red>Missing subcommand!</red> <gold>Available:</gold> <yellow>" +
                String.join(", ", commands) + "</yellow>");
    }

    public static void sendUsageMessage(CommandSender sender, Set<String> commands){
        sendMessage(sender,"<red>Unknown subcommand!</red> <gold>Available:</gold> <yellow>" +
                String.join(", ", commands) + "</yellow>");
    }

    public static void sendPermissionMessage(CommandSender sender, String permission) {
        sendMessage(sender, "<red>You lack permissions to use the command, required permission: <yellow>" + permission + "</yellow></red>");
    }

    public static void sendMessage(CommandSender sender, String message){
        sender.sendMessage(deserializeMixed("<gold><bold>[CC]</bold></gold> "+ message));
    }

    public static void sendMessageNoPrefix(CommandSender sender, String message){
        sender.sendMessage(deserializeMixed(message));
    }

    public static void sendCooldown(CommandSender sender, long time){
        sendMessage(sender,"<red>This command is on cooldown! Please try again in <bold>" + time/1000f +"</bold> !</red>");
    }

    public static Component deserializeMixed(String input) {
        // legacy color codes support for messages - specifically needed by local chat for wider support
        Component legacyComponent = LEGACY.deserialize(
                ChatColor.translateAlternateColorCodes('&', input)
        );
        String miniString = miniMessage.serialize(legacyComponent);
        return miniMessage.deserialize(miniString);
    }
}
