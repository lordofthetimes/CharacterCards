package net.lordofthetimes.characterCard.commands;

import dev.jorel.commandapi.CommandAPICommand;
import net.lordofthetimes.characterCard.CharacterCard;
import net.lordofthetimes.characterCard.utils.MessageSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;

public class ReloadCommand {

    private final CharacterCard plugin;

    public ReloadCommand(CharacterCard plugin){
        this.plugin = plugin;

        new CommandAPICommand("charactercard-reload")
                .withPermission("charactercard.reload")
                .executes((sender, args) -> {
                    reload(sender);
                })
                .register();
    }

    private void reload(CommandSender sender){
        try{
            plugin.reload();
            MessageSender.sendMessage(sender, "<green>Plugin reloaded successfully!</green>");
        }
        catch (IOException error){
            MessageSender.sendMessage(sender, "<red>Failed to reload the config! " + error + "</red>");
        }
    }
}
