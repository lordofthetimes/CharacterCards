package net.lordofthetimes.characterCard.commands;

import dev.jorel.commandapi.arguments.GreedyStringArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.lordofthetimes.characterCard.CharacterCard;
import net.lordofthetimes.characterCard.DatabaseManager;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.StringArgument;
import net.lordofthetimes.characterCard.utils.MessageSender;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class CharacterCommand {

    private final CharacterCard plugin;
    private final DatabaseManager db;

    public CharacterCommand(CharacterCard plugin, DatabaseManager db) {
        this.plugin = plugin;
        this.db = db;
        new CommandAPICommand("character")
                .withPermission("charactercard.character")
                .withAliases("profile2")
                .withSubcommand(
                    new CommandAPICommand("book")
                            .withPermission("charactercard.character.book")
                            .withOptionalArguments(new StringArgument("player"))
                            .executes((sender,args) ->{
                                if(sender instanceof  Player player){
                                    if(args.get("player") == null){
                                        openBook(player);
                                    }
                                    else{
                                        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                                            String target = args.get("player").toString();
                                            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(target);

                                            if (!offlinePlayer.hasPlayedBefore()) {
                                                Bukkit.getScheduler().runTask(plugin, () -> {
                                                    MessageSender.sendMessage(player,"<yellow>Player has never joined the server before!</yellow>");
                                                });
                                                return;
                                            }


                                            Bukkit.getScheduler().runTask(plugin, () -> {
                                                openBook(player,offlinePlayer);
                                            });
                                        });;
                                    }
                                }
                            })
                )
                .withSubcommand(
                        new CommandAPICommand("set")
                                .withPermission("charactercard.character.set")
                                .withSubcommand(
                                        new CommandAPICommand("lore")
                                                .withPermission("charactercard.character.set")
                                                .withArguments(new GreedyStringArgument("lore"))
                                                .executes((sender,args) -> { if(sender instanceof Player player) setLore(player,(String) args.get("lore")); })
                                ).withSubcommand(
                                        new CommandAPICommand("name")
                                                .withPermission("charactercard.character.set")
                                                .withArguments(new GreedyStringArgument("name"))
                                                .executes((sender,args) -> { if(sender instanceof Player player) setName(player,(String) args.get("name")); })
                                )
                )
                .withSubcommand(
                        new CommandAPICommand("chat")
                                .withPermission("charactercard.character.chat")
                                .withOptionalArguments(new StringArgument("player"))
                                .executes((sender,args) ->{
                                    if((sender instanceof Player player)) {
                                        if (args.get("player") == null) {
                                            openCharacter(player);
                                        } else {
                                            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                                                String target = args.get("player").toString();
                                                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(target);

                                                if (!offlinePlayer.hasPlayedBefore()) {
                                                    Bukkit.getScheduler().runTask(plugin, () -> {
                                                        MessageSender.sendMessage(player, "<yellow>Player has never joined the server before!</yellow>");
                                                    });
                                                    return;
                                                }


                                                Bukkit.getScheduler().runTask(plugin, () -> {
                                                    openCharacter(player, offlinePlayer);
                                                });
                                            });
                                            ;
                                        }
                                    }
                                })
                )
                .withSubcommand(
                        new CommandAPICommand("clear")
                                .withPermission("charactercard.character.clear")
                                .withOptionalArguments(new StringArgument("player")
                                        .withPermission("charactercard.character.clear.others"))
                                .executes((sender,args)->{
                                    if(args.get("player") == null && sender instanceof Player player){
                                        clearData(player);
                                    }
                                    else{
                                        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                                            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args.get("player").toString());

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
                                    }
                                })
                ).register();
    }

    private void setName(Player player,String name){
        db.updateName(name,player.getUniqueId()).thenAccept(success ->{
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (success) {
                    MessageSender.sendMessage(player,"<green>Character name has been set</green>");
                } else {
                    MessageSender.sendMessage(player,"<red>Failed to save name!</red>");
                }
            });
        });
    }

    private void setLore(Player player,String lore){
        db.updateLore(lore,player.getUniqueId()).thenAccept(success ->{
            Bukkit.getScheduler().runTask(plugin, () -> {
                if(success) {
                    MessageSender.sendMessage(player,"<green>Character lore has been set</green>");
                } else {
                    MessageSender.sendMessage(player,"<red>Failed to save lore!</red>");
                }
            });
        });
    }

    private void setAge(Player player, String age){

    }

    private void openBook(Player viewer, OfflinePlayer target) {
        ConcurrentHashMap<String,String> data = db.getPlayerDataCache(target.getUniqueId());

        if (data == null) {
            MessageSender.sendMessage(viewer, "<red>Player data is not loaded yet!</red>");
            return;
        }

        String name = data.get("loreName");
        String lore = data.get("lore");

        ItemStack bookItem = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) bookItem.getItemMeta();
        meta.setTitle("CharacterCards");
        meta.setAuthor("CharacterCards");

        Component component = MiniMessage.miniMessage().deserialize("<gold><bold>Name: </bold>" + name + "</gold>");

        if (plugin.landsEnabled) {
            component = component.append(MiniMessage.miniMessage().deserialize(
                    "\n<dark_blue><bold>Nations: </bold>" + String.join(", ", plugin.lands.getNationNames(target.getUniqueId())) + "</dark_blue>"));

            component = component.append(MiniMessage.miniMessage().deserialize(
                    "\n<aqua><bold>Towns: </bold>" + String.join(", ", plugin.lands.getTownNames(target.getUniqueId())) + "</aqua>"));
        }

        component = component.append(MiniMessage.miniMessage().deserialize(
                "\n<black><bold>Story: </bold>" + lore + "</black>"));

        meta.addPages(component);
        bookItem.setItemMeta(meta);

        viewer.openBook(bookItem);
    }
    public void openBook(Player viewer) {
        openBook(viewer, viewer);
    }

    private void openCharacter(Player player, OfflinePlayer offlinePlayer){
        ConcurrentHashMap<String,String> data = db.getPlayerDataCache(offlinePlayer.getUniqueId());
        String loreName = data.get("loreName");
        String lore = data.get("lore");

        List<String> part = new ArrayList<String >();
        part.add("""
        <gold><bold>———===[ Character Card ]===———</bold></gold>
        <yellow>Name:</yellow> <white><bold>%s</bold></white>
        """.formatted(loreName));
        if(plugin.landsEnabled){

            String nationNames = plugin.lands.getNationNames(offlinePlayer.getUniqueId());
            String townNames = plugin.lands.getTownNames(offlinePlayer.getUniqueId());

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
        MessageSender.sendCharacterCard(player,String.join("",part));
    }

    private void openCharacter(Player player){
        openCharacter(player,player);
    }

    private void clearData(CommandSender sender, OfflinePlayer player){
        db.clearPlayerDataCache(player.getUniqueId());
        db.resetPlayerData(player.getUniqueId());
        MessageSender.sendMessage(sender,"<yellow>Character data was cleared to default state!</yellow>");
    }
    private void clearData(Player player){
        clearData(player,player);
    }
}
