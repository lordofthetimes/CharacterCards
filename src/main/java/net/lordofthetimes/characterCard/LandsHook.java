package net.lordofthetimes.characterCard;

import me.angeschossen.lands.api.LandsIntegration;
import me.angeschossen.lands.api.land.Land;
import me.angeschossen.lands.api.nation.Nation;
import me.angeschossen.lands.api.player.LandPlayer;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class LandsHook {

    public final LandsIntegration api;

    public LandsHook(CharacterCard plugin) {
        api = LandsIntegration.of(plugin);
    }

    public String getTownNames(UUID uuid){
        LandPlayer landPlayer = api.getLandPlayer(uuid);

        List<String> townNames = landPlayer.getLands().stream()
                .map(Land::getName)
                .toList();
        townNames = townNames.isEmpty() ? List.of("None") : townNames;

        return String.join(", ",townNames);
    }

    public String getNationNames(UUID uuid){
        LandPlayer landPlayer = api.getLandPlayer(uuid);
        List<String> nationNames = landPlayer.getLands().stream()
                .map(Land::getNation)
                .filter(Objects::nonNull)
                .map(Nation::getName)
                .toList();
        nationNames = nationNames.isEmpty() ? List.of("None") : nationNames;
        return String.join(", ",nationNames);
    }
}
