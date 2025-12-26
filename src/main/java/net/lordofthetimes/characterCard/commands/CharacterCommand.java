package net.lordofthetimes.characterCard.commands;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.lordofthetimes.characterCard.CharacterCard;
import net.lordofthetimes.characterCard.database.DatabaseManager;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.StringArgument;
import net.lordofthetimes.characterCard.utils.MessageSender;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.jetbrains.annotations.NotNull;

import javax.swing.text.StyledEditorKit;
import java.io.Console;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;

public class CharacterCommand {

    private final CharacterCard plugin;
    private final DatabaseManager db;
    private final HashMap<UUID, Long> lastUse = new HashMap<>();
    private final String ageMode;



    public CharacterCommand(CharacterCard plugin, DatabaseManager db) {
        this.plugin = plugin;
        this.db = db;
        this.ageMode = plugin.config.getString("ageMode");
        CommandAPICommand character =  new CommandAPICommand("character")
                .withPermission("charactercard.character")
                .withAliases("profile", "card")
                .withSubcommand(
                    new CommandAPICommand("book")
                            .withPermission("charactercard.character.book")
                            .withOptionalArguments(new StringArgument("player")
                                    .replaceSuggestions(ArgumentSuggestions.strings(info ->
                                            Bukkit.getOnlinePlayers().stream()
                                                    .map(Player::getName)
                                                    .toArray(String[]::new))))
                            .executes((sender,args) ->{
                                if(sender instanceof  Player player && !isOnCooldown(player)){
                                    if(args.get("player") == null){
                                        openBook(player,player);
                                    }
                                    else{
                                        executeCommand(args.get("player").toString(),player, this::openBook);
                                    }
                                }
                            })
                )
                .withSubcommand(
                        new CommandAPICommand("chat")
                                .withPermission("charactercard.character.chat")
                                .withOptionalArguments(new StringArgument("player")
                                        .replaceSuggestions(suggestPlayers()))
                                .executes((sender,args) ->{

                                    if((sender instanceof Player player && !isOnCooldown(player))) {
                                        if (args.get("player") == null) {
                                            openCharacter(player,player);
                                        } else {
                                            executeCommand(args.get("player").toString(),player, this::openCharacter);
                                        }
                                    }
                                })
                )
                .withSubcommand(
                        new CommandAPICommand("clear")
                                .withPermission("charactercard.character.clear")
                                .withOptionalArguments(new StringArgument("player")
                                        .replaceSuggestions(suggestPlayers())
                                        .withPermission("charactercard.character.clear.others"))
                                .executes((sender,args)->{
                                    if(sender instanceof Player player && !isOnCooldown(player)){
                                        if(args.get("player") == null){
                                            clearData(player,player);
                                        }
                                        else{
                                            if(player.hasPermission("charactercard.character.clear.others")) executeCommand(args.get("player").toString(),player, this::clearData);
                                        }
                                    }
                                })
                )
                .withSubcommand(
                        new CommandAPICommand("help")
                                .withPermission("charactercard.character")
                                .executes((sender)->{
                                    if(sender instanceof  Player player && !isOnCooldown(player)){
                                        MessageSender.sendMessageNoPrefix(sender.sender(),getHelp(player));
                                    }
                                    else{
                                        MessageSender.sendMessageNoPrefix(sender.sender(),getHelp(sender.sender()));
                                    }

                                })
                )
                .executes((sender)->{
                        if(sender instanceof  Player player && !isOnCooldown(player)){
                            MessageSender.sendMessageNoPrefix(sender.sender(),getHelp(player));
                        }
                        else{
                            MessageSender.sendMessageNoPrefix(sender.sender(),getHelp(sender.sender()));
                        }
                });


        CommandAPICommand characterSet = new CommandAPICommand("set")
                .withPermission("charactercard.character.set")
                .withSubcommand(
                        new CommandAPICommand("lore")
                                .withPermission("charactercard.character.set")
                                .withArguments(new GreedyStringArgument("lore")
                                )
                                .executes((sender,args) -> { if(sender instanceof Player player) setLore(player,(String) args.get("lore")); })
                ).withSubcommand(
                        new CommandAPICommand("name")
                                .withPermission("charactercard.character.set")
                                .withArguments(new GreedyStringArgument("name"))
                                .executes((sender,args) -> { if(sender instanceof Player player) setName(player,(String) args.get("name")); })
                ).withSubcommand(
                        new CommandAPICommand("race")
                                .withPermission("charactercard.character.set")
                                .withArguments(new GreedyStringArgument("race"))
                                .executes((sender,args) -> { if(sender instanceof Player player) setRace(player,(String) args.get("race")); })
                ).withSubcommand(
                        new CommandAPICommand("description")
                                .withPermission("charactercard.character.set")
                                .withArguments(new GreedyStringArgument("description"))
                                .executes((sender,args) -> { if(sender instanceof Player player) setDescription(player,(String) args.get("description")); })
                ).withSubcommand(
                        new CommandAPICommand("gender")
                                .withPermission("charactercard.character.set")
                                .withArguments(new GreedyStringArgument("gender"))
                                .executes((sender,args) -> { if(sender instanceof Player player) setGender(player,(String) args.get("gender")); })
                );



            if(plugin.config.getString("ageMode").equals("SET")){
                characterSet.withSubcommand(
                        new CommandAPICommand("age")
                                .withPermission("charactercard.character.set")
                                .withArguments(new GreedyStringArgument("age"))
                                .executes((sender,args) -> { if(sender instanceof Player player) setAge(player,(String) args.get("age")); })
                );
            }

            character.withSubcommand(characterSet).register();

    }


    private void setName(Player player,String name){

        if(isOnCooldown(player)) return;

        if(plugin.essentialsXEnabled){
            plugin.essentials.updateNickname(player,name);
        }

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

        if(isOnCooldown(player)) return;

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

        if(isOnCooldown(player)) return;

        db.updateAge(age,player.getUniqueId()).thenAccept(success ->{
            Bukkit.getScheduler().runTask(plugin, () -> {
                if(success) {
                    MessageSender.sendMessage(player,"<green>Character age has been set</green>");
                } else {
                    MessageSender.sendMessage(player,"<red>Failed to save age!</red>");
                }
            });
        });
    }

    private void setRace(Player player, String updateRace){

        if(isOnCooldown(player)) return;

        db.updateRace(updateRace,player.getUniqueId()).thenAccept(success ->{
            Bukkit.getScheduler().runTask(plugin, () -> {
                if(success) {
                    MessageSender.sendMessage(player,"<green>Character race has been set</green>");
                } else {
                    MessageSender.sendMessage(player,"<red>Failed to save race!</red>");
                }
            });
        });
    }

    private void setDescription(Player player, String description){

        if(isOnCooldown(player)) return;

        db.updateDescription(description,player.getUniqueId()).thenAccept(success ->{
            Bukkit.getScheduler().runTask(plugin, () -> {
                if(success) {
                    MessageSender.sendMessage(player,"<green>Character description has been set</green>");
                } else {
                    MessageSender.sendMessage(player,"<red>Failed to save description!</red>");
                }
            });
        });
    }

    private void setGender(Player player, String gender){

        if(isOnCooldown(player)) return;

        db.updateGender(gender,player.getUniqueId()).thenAccept(success ->{
            Bukkit.getScheduler().runTask(plugin, () -> {
                if(success) {
                    MessageSender.sendMessage(player,"<green>Character gender has been set</green>");
                } else {
                    MessageSender.sendMessage(player,"<red>Failed to save gender!</red>");
                }
            });
        });
    }

    private void openBook(Player viewer, OfflinePlayer target) {
        ConcurrentHashMap<String,String> data = db.getPlayerDataCache(target.getUniqueId());

        if (data == null) {
            MessageSender.sendMessage(viewer, "<red>Player data is not loaded yet!</red>");
            return;
        }

        String name = data.get("loreName");
        String age = data.get("age");
        String lore = data.get("lore");
        String race = data.get("race");
        String gender = data.get("gender");
        String description = data.get("description");
        long joinTime = Long.parseLong(data.get("joinTime"));

        YamlDocument config = plugin.config;

        ItemStack bookItem = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) bookItem.getItemMeta();

        String page1String = config.getString("nameMessage").replace("<%name%>",name);

        if(ageMode.equals("SET")){
            page1String += config.getString("ageMessage").replace("<%age%>",age);
        }
        else if(ageMode.equals("JOIN")){
            page1String += config.getString("ageMessage").replace("<%age%>",formatDuration(System.currentTimeMillis() - joinTime));
        }

        page1String +=  config.getString("raceMessage").replace("<%race%>",race) +
                config.getString("genderMessage").replace("<%gender%>",gender);
        if (plugin.landsEnabled) {


            if(plugin.lands.townsCard){
                String townsNames = String.join(", ", plugin.lands.getTownNames(target.getUniqueId()));
                page1String += config.getString("lands.townsMessage")
                        .replace("<%towns%>",townsNames);
            }

            if(plugin.lands.townsCard){
                String nationsNames = String.join(", ", plugin.lands.getNationNames(target.getUniqueId()));
                page1String += config.getString("lands.nationsMessage")
                        .replace("<%nations%>",nationsNames);
            }

        }

        page1String += config.getString("descriptionMessage").replace("<%description%>",description);



        String page2String = config.getString("loreMessage").replace("<%lore%>",lore);

        if(plugin.papiEnabled){
            page1String = PlaceholderAPI.setPlaceholders(target,page1String);
            page2String = PlaceholderAPI.setPlaceholders(target,page2String);
        }

        Component page1 = MiniMessage.miniMessage().deserialize(page1String);
        Component page2 = MiniMessage.miniMessage().deserialize(page2String);
        meta.addPages(page1,page2);
        bookItem.setItemMeta(meta);

        viewer.openBook(bookItem);
    }

    private void openCharacter(Player viewer, OfflinePlayer offlinePlayer){
        ConcurrentHashMap<String,String> data = db.getPlayerDataCache(offlinePlayer.getUniqueId());
        if (data == null) {
            MessageSender.sendMessage(viewer, "<red>Player data is not loaded yet!</red>");
            return;
        }

        String name = data.get("loreName");
        String age = data.get("age");
        String lore = data.get("lore");
        String race = data.get("race");
        String gender = data.get("gender");
        String description = data.get("description");
        YamlDocument config = plugin.config;
        Long joinTime = Long.parseLong(data.get("joinTime"));
        List<String> part = new ArrayList<String >();
        part.add(
                "<gold><bold>———===[ Character Card ]===———</bold></gold>\n" +
                        config.getString("nameMessage").replace("<%name%>",name)
        );

        if(ageMode.equals("SET")){
            part.add(
                    config.getString("ageMessage").replace("<%age%>",age)
            );
        }
        else if(ageMode.equals("JOIN")) {
            part.add(
                    config.getString("ageMessage").replace("<%age%>",formatDuration(System.currentTimeMillis() - joinTime))
            );
        }

        part.add(
                config.getString("raceMessage").replace("<%race%>",race) +
                        config.getString("genderMessage").replace("<%gender%>",gender)
        );
        if(plugin.landsEnabled){

            if(plugin.lands.townsCard){
                String townsNames = String.join(", ", plugin.lands.getTownNames(offlinePlayer.getUniqueId()));
                part.add(config.getString("lands.townsMessage").replace("<%towns%>",townsNames));
            }

            if(plugin.lands.townsCard){
                String nationsNames = String.join(", ", plugin.lands.getNationNames(offlinePlayer.getUniqueId()));
                part.add(config.getString("lands.nationsMessage").replace("<%nations%>",nationsNames));
            }
        }
        part.add(
                config.getString("descriptionMessage").replace("<%description%>",description) + "\n" +
                config.getString("loreMessage").replace("<%lore%>",lore) +
                "\n<gold><bold>————=====================————</bold></gold>"
        );

        if(plugin.papiEnabled){
            part = PlaceholderAPI.setPlaceholders(offlinePlayer,part);
        }

        MessageSender.sendMessageNoPrefix(viewer,String.join("",part));
    }

    private void clearData(CommandSender sender, OfflinePlayer player){
        if(plugin.essentialsXEnabled){
            plugin.essentials.updateNickname(player,null);
        }
        db.clearPlayerDataCache(player.getUniqueId());
        db.resetPlayerData(player.getUniqueId());
        MessageSender.sendMessage(sender,"<yellow>Character data was cleared to default state!</yellow>");
    }

    private void executeCommand(String arg,Player sender, BiConsumer<Player, OfflinePlayer> command){

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {

            Player online = Bukkit.getPlayerExact(arg);
            OfflinePlayer offlinePlayer = online != null ? online : Bukkit.getOfflinePlayer(arg);

            boolean offline = !offlinePlayer.isOnline();

            if (!offlinePlayer.hasPlayedBefore() && offline) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    MessageSender.sendMessage(sender, "<yellow>Player has never joined the server before!</yellow>");
                });
                return;
            }
            command.accept(sender,offlinePlayer);
        });
    }

    private ArgumentSuggestions<CommandSender> suggestPlayers(){
        return ArgumentSuggestions.strings(info ->
                Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .toArray(String[]::new));
    }

    private String getHelp(CommandSender sender){
        String help =
                "<gold><bold>———===[ <#FFD54F>Character Card</#FFD54F> ]===———</bold></gold>\n" +
                "<yellow><bold>Version: <white>" + plugin.getDescription().getVersion() + "</white></bold></yellow>\n" +
                "<yellow><bold>By: <white>" + String.join(", ", plugin.getDescription().getAuthors()) + "</white></bold></yellow>\n" +
                "<yellow><bold>Aliases: <green>profile</green> , <green>card</green></bold></yellow>\n" +
                "<yellow><bold>Commands:</bold></yellow>\n";
        if(sender.hasPermission("charactercard.character.set")){
            help += "<green><bold>/character set</bold></green>\n";
        }
        if(sender.hasPermission("charactercard.character.book")){
            help += "<green><bold>/character book</bold></green>\n";
        }
        if(sender.hasPermission("charactercard.character.chat")){
            help += "<green><bold>/character chat</bold></green>\n";
        }
        if(sender.hasPermission("charactercard.character.clear")){
            help += "<green><bold>/character clear</bold></green>\n";
        }
        if(sender.hasPermission("charactercard.local.use")){
            help += "<green><bold>/local</bold></green>\n";
        }
        if(sender.hasPermission("charactercard.local.spy")){
            help += "<green><bold>/localspy</bold></green>\n";
        }
        help += "<gold><bold>————=====================————</bold></gold>";
        return help;
    }

    private Boolean isOnCooldown(Player player){
        UUID uuid = player.getUniqueId();
        long timeLeft;
        if(lastUse.containsKey(uuid)) {
            timeLeft = System.currentTimeMillis() - lastUse.get(uuid);
        }
        else{
            timeLeft = System.currentTimeMillis();
        }

        if(timeLeft < 1000){
            MessageSender.sendCooldown(player,timeLeft);
            return true;
        }
        lastUse.put(player.getUniqueId(),System.currentTimeMillis());
        return false;
    }

    public static String formatDuration(long millis) {
        long totalDays = millis / (1000L * 60 * 60 * 24);

        long years  = totalDays / 365;
        long months = (totalDays % 365) / 30;
        long days   = (totalDays % 365) % 30;

        StringBuilder sb = new StringBuilder();

        if (years > 0)  sb.append(years).append(" year").append(years == 1 ? "" : "s").append(" ");
        if (months > 0) sb.append(months).append(" month").append(months == 1 ? "" : "s").append(" ");
        if (days > 0 || sb.isEmpty())
            sb.append(days).append(" day").append(days == 1 ? "" : "s");

        return sb.toString().trim();
    }

}
