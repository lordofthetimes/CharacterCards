package net.lordofthetimes.characterCard;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIPaperConfig;
import net.lordofthetimes.characterCard.commands.CharacterCommand;
import net.lordofthetimes.characterCard.commands.LocalCommand;
import net.lordofthetimes.characterCard.commands.RealNameCommand;
import net.lordofthetimes.characterCard.database.DatabaseManager;

import net.lordofthetimes.characterCard.hooks.CharacterCardPlaceholderExpansion;
import net.lordofthetimes.characterCard.hooks.EssentialsXHook;
import net.lordofthetimes.characterCard.hooks.LandsHook;
import net.lordofthetimes.characterCard.hooks.bstats.Metrics;
import net.lordofthetimes.characterCard.listeners.LocalManager;
import net.lordofthetimes.characterCard.listeners.CharacterManager;
import net.lordofthetimes.characterCard.utils.CharacterCardLogger;
import net.lordofthetimes.characterCard.utils.UpdateChecker;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;


public final class CharacterCard extends JavaPlugin {

    public final CharacterCardLogger logger = new CharacterCardLogger(this.getLogger());
    public YamlDocument config;
    private DatabaseManager db;
    public UpdateChecker updateChecker;

    public boolean landsEnabled = false;
    public boolean papiEnabled = false;
    public boolean essentialsXEnabled = false;

    public LandsHook lands;
    public CharacterCardPlaceholderExpansion papi;
    public EssentialsXHook essentials;

    public RealNameCommand realNameCommand;
    public LocalCommand localCommand;
    public CharacterCommand characterCommand;

    public CharacterManager playerJoinListener;
    public LocalManager localManager;




    @Override
    public void onLoad() {
        CommandAPI.onLoad(new CommandAPIPaperConfig(this).verboseOutput(false));
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
                            .setVersioning(new BasicVersioning("config-version"))
                            .setOptionSorting(UpdaterSettings.OptionSorting.SORT_BY_DEFAULTS)
                            .build()
            );
            config.update();
        } catch (Exception e) {
            logger.logError("Failed to load or update config! Plugin is being disabled ",e);
            onDisable();
        }
        logger.logInfo("Config loaded successfully!");

        updateChecker = new UpdateChecker(this,version);

        if (getServer().getPluginManager().getPlugin("Lands") != null && config.getBoolean("lands.enabled")) {
            logger.logInfo("Lands detected, support enabled!");
            enableLandsSupport();
        }
        logger.logError(String.valueOf("" + getServer().getPluginManager().getPlugin("Essentials")  + "&&" + config.getBoolean("essentials.enabled")));
        if (getServer().getPluginManager().getPlugin("Essentials") != null && config.getBoolean("essentials.enabled")) {
            logger.logInfo("Essentials detected, support enabled!");
            enableEssentialsXSupport();
        }



        db = new DatabaseManager(this);
        db.connect();
        db.generateTables();
        db.tryAddColumns();
        try {
            db.setPlayersDataCache(db.getAllPlayersData().get());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null && config.getBoolean("papi.enabled")) {
            getLogger().info("PAPI detected, support enabled!");
            enablePAPISupport();
        }
        characterCommand = new CharacterCommand(this, db);


        playerJoinListener = new CharacterManager(db,this);

        if(config.getBoolean("localChat.enabled")){
            localCommand = new LocalCommand(this);
            localManager = new LocalManager(this);
            getLogger().info("Local chat now enabled!");

            getServer().getPluginManager().registerEvents(localManager,this);
        }

        getServer().getPluginManager().registerEvents(playerJoinListener,this);


        int pluginId = 28746;
        Metrics metrics = new Metrics(this, pluginId);

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
        this.realNameCommand = new RealNameCommand(this);
    }
}
