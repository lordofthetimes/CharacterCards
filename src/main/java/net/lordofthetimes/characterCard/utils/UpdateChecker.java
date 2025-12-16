package net.lordofthetimes.characterCard.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import net.lordofthetimes.characterCard.CharacterCard;
import org.bukkit.entity.Player;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public class UpdateChecker {

    private final CharacterCard plugin;
    private final String version;
    private String latestVersion;
    private String changelog;

    public final boolean checkForUpdate;

    public UpdateChecker(CharacterCard plugin, String version) {
        this.plugin = plugin;
        this.version = version;
        checkForUpdate = plugin.config.getBoolean("checkForUpdate");
        if(checkForUpdate) getInfo().thenRun(() -> sendVersionConsole());
    }

    public CompletableFuture<Void> getInfo() {
        return CompletableFuture.runAsync(() -> {
            try{
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://api.modrinth.com/v2/project/character-cards/version"))
                        .header("User-Agent", "lordofthetimes/CharacterCards" + version + " (lordofthetimes100@gmail.com)")
                        .GET()
                        .build();;
                HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

                JsonArray versionsArray = JsonParser.parseString(response.body()).getAsJsonArray();

                if (!versionsArray.isEmpty()) {
                    var latestVersionObject = versionsArray.get(0).getAsJsonObject();
                    latestVersion = latestVersionObject.get("version_number").getAsString();
                    changelog = latestVersionObject.get("changelog").getAsString()
                            .replaceAll("(\\*\\*|#+|`{3})", "")
                            .replaceAll("\\n{2,}", "\n");
                }
            }
            catch(Exception e){
                plugin.logger.logError("Failed to fetch latest version info",e);
            }
        });
    }

    public void sendVersionConsole(){
        if(!checkForUpdate) return;
        if(isNotLatest(version,latestVersion)){
            plugin.logger.logInfo("Current version of Character Cards is not the latest!");
            plugin.logger.logInfo("Running version " + version + " when latest version is " + latestVersion + "!");
            plugin.logger.logInfo("Changelog : " + changelog.replaceAll("\\[[^\\]]*\\]\\(([^\\)]+)\\)", "$1"));
            plugin.logger.logInfo("Download the latest version here : https://modrinth.com/plugin/character-cards/version/" + latestVersion);
        }
        else{
            plugin.logger.logInfo("Currently running the latest version " + version);
        }
    }
    public void sendVersionPlayer(Player player){
        if(!checkForUpdate) return;
        if(isNotLatest(version,latestVersion)){
            MessageSender.sendMessage(player,"<red><bold>Current version of Character Cards is not the latest!</bold></red>");
            MessageSender.sendMessage(player,"<yellow>Running version<red><bold> " + version + "</bold></red> when latest version is<green><bold> " + latestVersion + "</bold></green></yellow>");
            MessageSender.sendMessage(player,"<aqua>Changelog:</aqua> " + changelog.replaceAll(
                    "\\[([^]]+)\\]\\(([^\\)]+)\\)",
                    "<click:open_url:'$2'><underlined><blue>$1</blue></underlined></click>"
            ));
            MessageSender.sendMessage(player,
                    "<green>Download the latest version here: </green>" +
                            "<click:open_url:'https://modrinth.com/plugin/character-cards/version/" + latestVersion + "'>" +
                            "<underlined><blue>Modrinth</blue></underlined>" +
                            "</click>"
            );
        }
    }

    private boolean isNotLatest(String current,String latest){
        int[] splitCurrent = Arrays.stream(current.split("\\."))
                .mapToInt(Integer::parseInt)
                .toArray();
        int[] splitLatest = Arrays.stream(latest.split("\\."))
                .mapToInt(Integer::parseInt)
                .toArray();

        if(splitCurrent[0] < splitLatest[0]) return true;
        if(splitCurrent[1] < splitLatest[1]) return true;
        if(splitCurrent[2] < splitLatest[2]) return true;
        return false;
    }
}
