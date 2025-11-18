package net.lordofthetimes.characterCard.commands;

import net.lordofthetimes.characterCard.DatabaseManager;
import net.lordofthetimes.characterCard.commands.characterSubCommands.BookSubCommand;
import net.lordofthetimes.characterCard.utils.MessageSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CharacterCommand implements CommandExecutor, TabCompleter {

    private final Map<String, SubCommand> subCommands = new HashMap<>();
    private final JavaPlugin plugin;
    private final DatabaseManager db;

    public CharacterCommand(JavaPlugin plugin, DatabaseManager db) {
        this.plugin = plugin;
        this.db = db;
        registerSubCommand(new BookSubCommand(this.plugin,db));
    }

    private void registerSubCommand(SubCommand subCommand) {
        subCommands.put(subCommand.getName(), subCommand);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
        //no args
        if(args.length == 0){
            return false;
        }
        SubCommand subCommand = subCommands.get(args[0].toLowerCase());

        if (subCommand == null) {

            MessageSender.sendUsageMessage(sender,subCommands.keySet());
            return true;
        }
        String[] subArgs = args.length > 1 ? java.util.Arrays.copyOfRange(args, 1, args.length) : new String[0];
        return subCommand.execute(sender, subArgs);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
        if(args.length == 1){
            List<String> list = new ArrayList<>();
            for(String subCommand : subCommands.keySet()){
                if(sender.hasPermission("charactercard.character."+subCommand)){
                    list.add(subCommand);
                }
            }
            return list;
        }
        if(args.length == 2){
            SubCommand subCommand = subCommands.get(args[0].toLowerCase());

            if (subCommand != null) {
                String[] subArgs = args.length > 1 ? java.util.Arrays.copyOfRange(args, 1, args.length) : new String[0];
                return subCommand.tabComplete(sender,subArgs);
            }

        }
        return List.of();
    }
}
