package net.lordofthetimes.characterCard.commands.characterSubCommands;

import net.lordofthetimes.characterCard.DatabaseManager;
import net.lordofthetimes.characterCard.commands.SubCommand;
import net.lordofthetimes.characterCard.commands.characterSubCommands.setSubCommands.SetLoreSubCommand;
import net.lordofthetimes.characterCard.commands.characterSubCommands.setSubCommands.SetNameSubCommand;
import net.lordofthetimes.characterCard.utils.MessageSender;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class SetSubCommand implements SubCommand {

    private final JavaPlugin plugin;
    private final Map<String, SubCommand> subCommands = new HashMap<>();

    public SetSubCommand(JavaPlugin plugin, DatabaseManager db) {
        this.plugin = plugin;
        registerSubCommand(new SetLoreSubCommand(plugin,db));
        registerSubCommand(new SetNameSubCommand(plugin,db));
    }

    private void registerSubCommand(SubCommand subCommand) {
        subCommands.put(subCommand.getName(), subCommand);
    }


    @Override
    public String getName() {
        return "set";
    }

    @Override
    public String getDescription() {
        return "Sets a specific part of character card";
    }

    @Override
    public String getUsage() {
        return "/character set <type> <value>";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
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
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if(args.length == 1){
            List<String> list = new ArrayList<>();
            for(String subCommand : subCommands.keySet()){
                if(sender.hasPermission("charactercard.character.set")){
                    list.add(subCommand);
                }
            }
            return list;
        }
        if(args.length == 2){
            SubCommand subCommand = subCommands.get(args[0].toLowerCase());

            if (subCommand != null) {
                String[] subArgs = java.util.Arrays.copyOfRange(args, 1, args.length);
                return subCommand.tabComplete(sender,subArgs);
            }

        }
        return List.of();
    }
}
