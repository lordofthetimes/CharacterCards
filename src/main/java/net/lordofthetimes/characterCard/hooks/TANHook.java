package net.lordofthetimes.characterCard.hooks;

import org.bukkit.entity.Player;
import org.tan.api.TanAPI;
import org.tan.api.interfaces.TanPlayer;
import org.tan.api.interfaces.territory.TanNation;
import org.tan.api.interfaces.territory.TanRegion;
import org.tan.api.interfaces.territory.TanTown;

public record TANHook() {

    private static final TanAPI api = TanAPI.getInstance();

    public static String getTown(Player player){
        TanTown town =  api.getPlayerManager().get(player).getTown();

        if(town != null) return town.getName();
        return "None";
    }

    public static String getRegion(Player player){
        TanRegion region =  api.getPlayerManager().get(player).getRegion();

        if(region != null) return region.getName();
        return "None";
    }

    public static String getNation(Player player){

        for(TanNation nation : api.getTerritoryManager().getNations()){
            if(nation.isPlayerIn(player)) return nation.getName();
        }

        return "None";
    }
}
