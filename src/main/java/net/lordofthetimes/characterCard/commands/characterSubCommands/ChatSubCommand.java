package net.lordofthetimes.characterCard.commands.characterSubCommands;

import net.lordofthetimes.characterCard.CharacterCard;
import net.lordofthetimes.characterCard.DatabaseManager;
import net.lordofthetimes.characterCard.commands.SubCommand;
import net.lordofthetimes.characterCard.utils.MessageSender;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static java.awt.Color.yellow;
import static net.kyori.adventure.identity.Identity.UUID;

public class ChatSubCommand implements SubCommand {

    private final CharacterCard plugin;
    private final DatabaseManager db;

    public ChatSubCommand(CharacterCard plugin, DatabaseManager db) {
        this.plugin = plugin;
        this.db = db;
    }

    @Override
    public String getName() {
        return "chat";
    }

    @Override
    public String getDescription() {
        return "Opens character card as a chat message";
    }

    @Override
    public String getUsage() {
        return "/character chat <player>";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {

        if(!sender.hasPermission("charactercard.character.chat")){
            MessageSender.sendPermissionMessage(sender,"charactercard.character.chat");
            return true;
        }

        if(args.length == 0 && sender instanceof Player player){
            return openCharacter(player,player, db.getPlayerDataCache(player.getUniqueId()));
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[0]);

            if (!offlinePlayer.hasPlayedBefore()) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    MessageSender.sendMessage(sender,"<yellow>This player has never joined the server before!</yellow>");
                });
                return;
            }


            Bukkit.getScheduler().runTask(plugin, () -> {
                openCharacter(sender,offlinePlayer,db.getPlayerDataCache(offlinePlayer.getUniqueId()));
            });
        });

        return true;
    }

    private boolean openCharacter(CommandSender sender, OfflinePlayer player,ConcurrentHashMap<String,String> data){

        String loreName = data.get("loreName");
        String lore = data.get("lore");

        List<String> part = new ArrayList<String >();
        part.add("""
        <gold><bold>———===[ Character Card ]===———</bold></gold>
        <yellow>Name:</yellow> <white><bold>%s</bold></white>
        """.formatted(loreName));
        if(plugin.landsEnabled){

            String nationNames = plugin.lands.getNationNames(player.getUniqueId());
            String townNames = plugin.lands.getTownNames(player.getUniqueId());

            part.add("""
            <dark_aqua>Nation(s):</dark_aqua> <white>%s</white>
            <blue>Town(s):</blue> <white>%s</white>
            """.formatted(nationNames,townNames));
        }
        part.add("""
        <green>Lore:</green>
        <italic><white>%s</white></italic>
        <gold><bold>————=====================————</bold></gold>
        """.formatted(lore));
        MessageSender.sendCharacterCard(sender,String.join("",part));
        return true;
    }
}
