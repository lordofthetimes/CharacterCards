package net.lordofthetimes.characterCard.utils;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import net.lordofthetimes.characterCard.CharacterCard;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class ConfigMigration {

    static public void migrateAndUpdate(YamlDocument config, CharacterCard plugin, File configFile){
        int version = config.getInt("config-version");

        if(version < 8 && config.contains("name") && !config.isSection("name")) toV8(config, plugin, configFile);
        else try{ config.update(); } catch (IOException e){ plugin.logger.logError("Failed to update config! " + e); };

    }

    static private void toV8(YamlDocument config, CharacterCard plugin, File configFile){
        plugin.logger.logInfo("This update includes config migration.");
        YamlDocument backupConfig;
        try {
            plugin.logger.logInfo("Saving backup file to prev8config.yml");
            Files.copy(
                    configFile.toPath(),
                    new File(plugin.getDataFolder(), "prev8config.yml").toPath(),
                    StandardCopyOption.REPLACE_EXISTING
            );
            backupConfig = YamlDocument.create(
                    new File(plugin.getDataFolder(), "prev8config.yml"),
                    GeneralSettings.DEFAULT,
                    LoaderSettings.DEFAULT,
                    DumperSettings.DEFAULT
            );
            config.update();
        }
        catch(Exception e){
            plugin.logger.logError("Failed to save backup file or update config during migration! " + e);
            plugin.onDisable();
            return;
        }



        plugin.logger.logInfo("Migrating values...");

        move(config, backupConfig, "name", "name.enabled");
        move(config, backupConfig, "nameMessage", "name.message");
        move(config, backupConfig, "defaultName", "name.default");

        move(config, backupConfig, "age", "age.enabled");
        move(config, backupConfig, "ageMessage", "age.message");
        move(config, backupConfig, "defaultAge", "age.default");
        move(config, backupConfig, "ageMode", "age.mode");

        move(config, backupConfig, "race", "race.enabled");
        move(config, backupConfig, "raceMessage", "race.message");
        move(config, backupConfig, "defaultRace", "race.default");

        move(config, backupConfig, "gender", "gender.enabled");
        move(config, backupConfig, "genderMessage", "gender.message");
        move(config, backupConfig, "defaultGender", "gender.default");

        move(config, backupConfig, "religion", "religion.enabled");
        move(config, backupConfig, "religionMessage", "religion.message");
        move(config, backupConfig, "defaultReligion", "religion.default");

        move(config, backupConfig, "description", "description.enabled");
        move(config, backupConfig, "descriptionMessage", "description.message");
        move(config, backupConfig, "defaultDescription", "description.default");

        move(config, backupConfig, "lore", "lore.enabled");
        move(config, backupConfig, "loreMessage", "lore.message");
        move(config, backupConfig, "defaultLore", "lore.default");

        config.set("config-version", 8);
        plugin.logger.logInfo("Config migrated successfully!");
    }

    private static void move(YamlDocument config, YamlDocument backupConfig, String oldPath, String newPath) {
        if (!backupConfig.contains(oldPath)) return;

        config.set(newPath, backupConfig.get(oldPath));
    }
}
