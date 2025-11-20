package net.lordofthetimes.characterCard.commands.characterSubCommands;

import net.lordofthetimes.characterCard.CharacterCard;
import net.lordofthetimes.characterCard.DatabaseManager;
import net.lordofthetimes.characterCard.commands.SubCommand;
import net.lordofthetimes.characterCard.utils.MessageSender;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ClearSubCommand implements SubCommand {

    private final CharacterCard plugin;
    private final DatabaseManager db;

    public ClearSubCommand(CharacterCard plugin, DatabaseManager db) {
        this.plugin = plugin;
        this.db = db;
    }

    @Override
    public String getName() {
        return "clear";
    }

    @Override
    public String getDescription() {
        return "Clears all character card data for a specific player";
    }

    @Override
    public String getUsage() {
        return "/character clear <player>";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if(args.length == 0 && sender instanceof Player player ){

            if(!sender.hasPermission("charactercard.character.clear.self")){
                MessageSender.sendPermissionMessage(sender,"charactercard.character.clear.self");
                return true;
            }

            return clearData(sender,player);
        }
        if(!sender.hasPermission("charactercard.character.clear.others")){
            MessageSender.sendPermissionMessage(sender,"charactercard.character.clear.others");
            return true;
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
                clearData(sender,offlinePlayer);
            });
        });

        return true;
    }

    private boolean clearData(CommandSender sender, OfflinePlayer player){
        db.clearPlayerDataCache(player.getUniqueId());
        db.resetPlayerData(player.getUniqueId());
        MessageSender.sendMessage(sender,"<yellow>Character data was cleared to default state!</yellow>");
        return true;
    }
}
