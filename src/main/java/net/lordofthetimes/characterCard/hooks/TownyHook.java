package net.lordofthetimes.characterCard.hooks;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.entity.Player;

public class TownyHook {

    public static String getTown(Player player){
        Town town = TownyAPI.getInstance().getTown(player);

        if(town != null) return town.getName();
        return "None";
    }

    public static String getNation(Player player){
        Nation nation = TownyAPI.getInstance().getNation(player);

        if(nation != null) return nation.getName();
        return "None";
    }
}
