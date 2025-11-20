package net.lordofthetimes.characterCard.commands.characterSubCommands;


import me.angeschossen.lands.api.LandsIntegration;
import me.angeschossen.lands.api.land.Land;
import me.angeschossen.lands.api.land.LandArea;
import me.angeschossen.lands.api.nation.Nation;
import me.angeschossen.lands.api.player.LandPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.lordofthetimes.characterCard.CharacterCard;
import net.lordofthetimes.characterCard.DatabaseManager;
import net.lordofthetimes.characterCard.commands.SubCommand;
import net.lordofthetimes.characterCard.utils.MessageSender;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class BookSubCommand implements SubCommand {
    private final CharacterCard plugin;
    private final DatabaseManager db;

    public BookSubCommand(CharacterCard plugin, DatabaseManager db){
        this.plugin = plugin;
        this.db = db;
    }

    @Override
    public String getName() {
        return "book";
    }

    @Override
    public String getDescription() {
        return "Opens character card as a book";
    }

    @Override
    public String getUsage() {
        return "/character book";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }

        if (!player.hasPermission("charactercard.character.book")) {
            MessageSender.sendPermissionMessage(player,"charactercard.character.book");
            return true;
        }

        if (args.length == 0) {
            if (player.hasPermission("charactercard.character.book.self")) {
                openBook(player,db.getPlayerDataCache(player.getUniqueId()));
                return true;
            }
            MessageSender.sendPermissionMessage(player,"charactercard.character.book.self");
            return true;
        }


        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[0]);

            if (!offlinePlayer.hasPlayedBefore()) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    MessageSender.sendMessage(player,"<yellow>Player has never joined the server before!</yellow>");
                });
                return;
            }


            Bukkit.getScheduler().runTask(plugin, () -> {
                openBook(player,db.getPlayerDataCache(offlinePlayer.getUniqueId()), offlinePlayer);
            });
        });

        return true;
    }


    private void openBook(Player player, ConcurrentHashMap<String,String> data, OfflinePlayer offlinePlayer){
        ItemStack bookItem = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) bookItem.getItemMeta();

        String name = data.get("loreName");
        String lore = data.get("lore");

        meta.setTitle("CharacterCards");
        meta.setAuthor("CharacterCards");
        Component component = MiniMessage.miniMessage().deserialize("<gold><bold>Name: </bold>" +name + "</gold>");
        if(plugin.landsEnabled){

            component = component.append(MiniMessage.miniMessage().deserialize("\n<dark_blue><bold>Nations: </bold>"+
                    String.join(", ",plugin.lands.getNationNames(offlinePlayer.getUniqueId())) + "</dark_blue>"));

            component = component.append(MiniMessage.miniMessage().deserialize("\n<aqua><bold>Towns: </bold>"+
                    String.join(", ",plugin.lands.getTownNames(offlinePlayer.getUniqueId())) + "</aqua>"));
        }
        component = component.append(MiniMessage.miniMessage().deserialize("\n<black><bold>Story: </bold>"+lore+"</black>"));
        meta.addPages(component);
        bookItem.setItemMeta(meta);
        player.openBook(bookItem);
    }

    private  void  openBook(Player player,ConcurrentHashMap<String,String> data){
        openBook(player,data,Bukkit.getOfflinePlayer(player.getUniqueId()));
    }

}
