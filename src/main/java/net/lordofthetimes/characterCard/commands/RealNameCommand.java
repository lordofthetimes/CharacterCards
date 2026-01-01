package net.lordofthetimes.characterCard.commands;

import com.mojang.brigadier.suggestion.Suggestions;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import net.lordofthetimes.characterCard.CharacterCard;
import net.lordofthetimes.characterCard.hooks.EssentialsXHook;
import net.lordofthetimes.characterCard.utils.MessageSender;

public class RealNameCommand {

    private final CharacterCard plugin;
    private final EssentialsXHook essentialsX;

    public RealNameCommand(CharacterCard plugin) {
        this.plugin = plugin;
        essentialsX = plugin.essentials;
        new CommandAPICommand("realname")
                .withAliases("rn","named")
                .withPermission("charactercard.realname")
                .withArguments(new GreedyStringArgument("nickname")
                        .replaceSuggestions(
                                ArgumentSuggestions.strings(info ->
                                        essentialsX.getAllOnlineNicknames().toArray(new String[0])
                                )
                        )
                )
                .executes((sender,args) ->{
                    String nickname = args.get("nickname").toString();
                    String playerName = essentialsX.getOnlineUserByNickname(nickname).getName();
                    if(playerName != null){
                        MessageSender.sendMessage(sender,"<green>The real name of " + nickname + " is " + playerName + "</green>");
                    }
                    else{
                        MessageSender.sendMessage(sender,"<yellow>There is no player with nickname of" + nickname + " !</yellow>");

                    }
                }).register();

    }
}
