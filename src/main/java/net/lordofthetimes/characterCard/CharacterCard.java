package net.lordofthetimes.characterCard;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.route.Route;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import net.lordofthetimes.characterCard.commands.CharacterCommand;
import net.lordofthetimes.characterCard.database.DatabaseManager;

import net.lordofthetimes.characterCard.hooks.CharacterCardPlaceholderExpansion;
import net.lordofthetimes.characterCard.hooks.EssentialsXHook;
import net.lordofthetimes.characterCard.hooks.LandsHook;
import net.lordofthetimes.characterCard.listeners.PlayerJoinListener;
import net.lordofthetimes.characterCard.utils.CharacterCardLogger;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

import static dev.dejvokep.boostedyaml.route.Route.fromSingleKey;


public final class CharacterCard extends JavaPlugin {

    public final CharacterCardLogger logger = new CharacterCardLogger(this.getLogger());
    public YamlDocument config;
    public boolean landsEnabled = false;
    public boolean papiEnabled = false;
    public boolean essentialsXEnabled = false;

    public LandsHook lands;
    public CharacterCardPlaceholderExpansion papi;
    public EssentialsXHook essentials;

    private DatabaseManager db;


    @Override
    public void onLoad() {
        CommandAPI.onLoad(new CommandAPIBukkitConfig(this));
    }

    @Override
    public void onEnable() {

        String version = this.getPluginMeta().getVersion();

        logger.logInfo("Enabling CharacterCard v" + version);
        logger.logInfo("\n################################\n\n" +
                "▄▖▖▖▄▖▄▖▄▖▄▖▄▖▄▖▄▖  ▄▖▄▖▄▖▄ \n" +
                "▌ ▙▌▌▌▙▘▌▌▌ ▐ ▙▖▙▘  ▌ ▌▌▙▘▌▌\n" +
                "▙▖▌▌▛▌▌▌▛▌▙▖▐ ▙▖▌▌  ▙▖▛▌▌▌▙▘\n" +
                "                            \n" +
                "CharacterCard v" + version + " - Enjoy your roleplay!\n\n" +
                "################################");

        try {
            config = YamlDocument.create(
                    new File(this.getDataFolder(), "config.yml"),
                    this.getResource("config.yml"),
                    GeneralSettings.DEFAULT,
                    LoaderSettings.DEFAULT,
                    DumperSettings.DEFAULT,
                    UpdaterSettings.builder()
                            .setVersioning(new BasicVersioning("version"))
                            .setOptionSorting(UpdaterSettings.OptionSorting.SORT_BY_DEFAULTS)
                            .build()
            );
            config.update();
        } catch (Exception e) {
            logger.logError("Failed to load or update config! Plugin is being disabled ",e);
            onDisable();
        }
        logger.logInfo("Config loaded successfully!");


        if (getServer().getPluginManager().getPlugin("Lands") != null && config.getBoolean("lands.enabled")) {
            getLogger().info("Lands detected, support enabled!");
            enableLandsSupport();
        }

        if (getServer().getPluginManager().getPlugin("Essentials") != null && config.getBoolean("essentials.enabled")) {
            getLogger().info("Essentials detected, support enabled!");
            enableEssentialsXSupport();
        }



        db = new DatabaseManager(this);
        db.connect();
        db.generateTables();
        db.tryAddColumns();
        db.getAllPlayersData().thenAccept(allData -> {
            if (allData != null) {
                db.setPlayersDataCache(allData);
                db.logger.logInfoDB("Loaded " + allData.size() + " player(s) into cache!");
            } else {
                db.logger.logErrorDB("Failed to load player data into cache!");
                db.logger.logErrorDB("Failed to load player data into cache!");
                db.logger.logErrorDB("Failed to load player data into cache!");
                db.logger.logErrorDB("Failed to load player data into cache!");
                this.onDisable();
            }
        });
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null && config.getBoolean("papi.enabled")) {
            getLogger().info("PAPI detected, support enabled!");
            enablePAPISupport();
        }


        new CharacterCommand(this, db);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(db,this),this);

    }

    @Override
    public void onDisable() {
        this.db.close();
        this.db.clearAllPlayerDataCache();
    }

    private void enableLandsSupport(){
        this.landsEnabled = true;
        this.lands = new LandsHook(this,config);
    }

    private void enablePAPISupport(){
        this.papiEnabled = true;
        this.papi= new CharacterCardPlaceholderExpansion(this,db,config);
        papi.register();
    }

    private void enableEssentialsXSupport(){
        this.essentialsXEnabled = true;
        this.essentials = new EssentialsXHook(config);
    }
}
