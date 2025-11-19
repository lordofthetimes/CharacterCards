package net.lordofthetimes.characterCard.commands.characterSubCommands.setSubCommands;

import net.lordofthetimes.characterCard.DatabaseManager;
import net.lordofthetimes.characterCard.commands.SubCommand;
import net.lordofthetimes.characterCard.utils.MessageSender;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class SetNameSubCommand implements SubCommand {

    private final JavaPlugin plugin;
    private final DatabaseManager db;

    public SetNameSubCommand(JavaPlugin plugin, DatabaseManager db) {
        this.plugin = plugin;
        this.db = db;
    }


    @Override
    public String getName() {
        return "name";
    }

    @Override
    public String getDescription() {
        return "Sets name for character card";
    }

    @Override
    public String getUsage() {
        return "/character set name <name>";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {

        if(args.length == 0){
            return false;
        }

        if (!sender.hasPermission("charactercard.character.set")) {
            MessageSender.sendPermissionMessage(sender, "charactercard.character.set");
            return true;
        }
        if(!(sender instanceof Player player)){
            MessageSender.sendMessage(sender,"<red>This command can be only run by a player! </red>");
            return true;
        }

        String name = String.join(" ",args);
        db.updateName(name,player.getUniqueId()).thenAccept(success ->{
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (success) {
                    MessageSender.sendMessage(player,"<green>Character name has been set</green>");
                } else {
                    MessageSender.sendMessage(player,"<red>Failed to save name!</red>");
                }
            });
        });
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args){

        if(sender instanceof Player player) {
            return List.of(db.getPlayerDataCache(player.getUniqueId()).get("loreName"));
        }
        return List.of();
    }
}
