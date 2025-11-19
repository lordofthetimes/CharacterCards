package net.lordofthetimes.characterCard.utils;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;

import java.util.Set;

public class MessageSender {


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
        MiniMessage mm = MiniMessage.miniMessage();
        sender.sendMessage(mm.deserialize("<gold><bold>[CC]</bold></gold> "+ message));
    }
}
