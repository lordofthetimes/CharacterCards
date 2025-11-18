package net.lordofthetimes.characterCard.commands.characterSubCommands;

import net.kyori.adventure.text.Component;
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
import org.bukkit.plugin.java.JavaPlugin;

import javax.xml.crypto.Data;
import java.util.UUID;

public class BookSubCommand implements SubCommand {
    private final JavaPlugin plugin;
    private final DatabaseManager db;

    public BookSubCommand(JavaPlugin plugin, DatabaseManager db){
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
                openBook(player);
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
                player.sendMessage("This was run by opening a book by username");
                openBook(player, offlinePlayer);
            });
        });

        return true;
    }


    private void openBook(Player player,OfflinePlayer offlinePlayer){
        ItemStack bookItem = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) bookItem.getItemMeta();

        UUID uuid = offlinePlayer.getUniqueId();
        String username = offlinePlayer.getName();

        meta.setTitle("CharacterCards");
        meta.setAuthor("CharacterCards");
        Component component = Component.text(uuid.toString());
        component = component.append(Component.text("\n "+username));
        meta.addPages(component);
        bookItem.setItemMeta(meta);
        player.openBook(bookItem);
    }

    private  void  openBook(Player player){
        openBook(player,Bukkit.getOfflinePlayer(player.getUniqueId()));
    }

}
