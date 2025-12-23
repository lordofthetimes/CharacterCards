package net.lordofthetimes.characterCard.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.lordofthetimes.characterCard.CharacterCard;
import net.lordofthetimes.characterCard.commands.LocalCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class LocalManager implements Listener {

    private final LocalCommand localCommand;


    public LocalManager(CharacterCard plugin){
        this.localCommand = plugin.localCommand;
    }


    @EventHandler
    public void playerJoinEvent(PlayerJoinEvent event){
        Player player = event.getPlayer();
        localCommand.localMode.put(player.getUniqueId(),false);
        localCommand.bypass.put(player.getUniqueId(),false);
    }

    @EventHandler
    public void playerLeaveEvent(PlayerQuitEvent event){
        Player player = event.getPlayer();
        localCommand.localMode.remove(player.getUniqueId());
        localCommand.bypass.remove(player.getUniqueId());
    }

    @EventHandler
    public void onLocalChat(AsyncChatEvent event){
        Player player = event.getPlayer();
        if(event.isCancelled() || event.message().equals(Component.empty())) return;
        if(localCommand.localMode.get(player.getUniqueId())){
            event.setCancelled(true);
            localCommand.sendLocal(player, LegacyComponentSerializer.legacySection().serialize(event.message()));
        }
    }

}
