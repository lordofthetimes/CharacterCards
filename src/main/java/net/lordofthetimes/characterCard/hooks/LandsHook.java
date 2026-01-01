package net.lordofthetimes.characterCard.hooks;

import dev.dejvokep.boostedyaml.YamlDocument;
import me.angeschossen.lands.api.LandsIntegration;
import me.angeschossen.lands.api.land.Land;
import me.angeschossen.lands.api.nation.Nation;
import me.angeschossen.lands.api.player.LandPlayer;
import net.lordofthetimes.characterCard.CharacterCard;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class LandsHook {

    public final LandsIntegration api;
    public final Boolean townsCard;
    public final Boolean nationsCard;
    public final YamlDocument config;

    public LandsHook(CharacterCard plugin, YamlDocument config) {
        api = LandsIntegration.of(plugin);
        townsCard = config.getBoolean("lands.towns");
        nationsCard = config.getBoolean("lands.nations");
        this.config = config;
    }           

    public String getLandsNames(UUID uuid){
        LandPlayer landPlayer = api.getLandPlayer(uuid);

        if(landPlayer == null){
            return "<gray>None</gray>";
        }

        List<String> townNames = landPlayer.getLands().stream()
                .map(Land::getName)
                .distinct()
                .toList();
        townNames = townNames.isEmpty() ? List.of("None") : townNames;

        return String.join(", ",townNames);
    }

    public String getNationNames(UUID uuid){
        LandPlayer landPlayer = api.getLandPlayer(uuid);

        if(landPlayer == null){
            return "<gray>None</gray>";
        }

        List<String> nationNames = landPlayer.getLands().stream()
                .map(Land::getNation)
                .filter(Objects::nonNull)
                .map(Nation::getName)
                .distinct()
                .toList();
        nationNames = nationNames.isEmpty() ? List.of("None") : nationNames;
        return String.join(", ",nationNames);
    }
}
