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
import net.lordofthetimes.characterCard.commands.LocalCommand;
import net.lordofthetimes.characterCard.commands.RealNameCommand;
import net.lordofthetimes.characterCard.database.DatabaseManager;

import net.lordofthetimes.characterCard.hooks.CharacterCardPlaceholderExpansion;
import net.lordofthetimes.characterCard.hooks.EssentialsXHook;
import net.lordofthetimes.characterCard.hooks.LandsHook;
import net.lordofthetimes.characterCard.listeners.LocalManager;
import net.lordofthetimes.characterCard.listeners.PlayerJoinListener;
import net.lordofthetimes.characterCard.utils.CharacterCardLogger;
import net.lordofthetimes.characterCard.utils.UpdateChecker;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static dev.dejvokep.boostedyaml.route.Route.fromSingleKey;


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

    public PlayerJoinListener playerJoinListener;
    public LocalManager localManager;




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


        playerJoinListener = new PlayerJoinListener(db,this);

        if(config.getBoolean("localChat.enabled")){
            localCommand = new LocalCommand(this);
            localManager = new LocalManager(this);
            getLogger().info("Local chat now enabled!");
        }

        getServer().getPluginManager().registerEvents(playerJoinListener,this);
        getServer().getPluginManager().registerEvents(localManager,this);

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
    @SuppressWarnings("unchecked")
    private void enableEssentialsXSupport(){
        this.essentialsXEnabled = true;
        this.essentials = new EssentialsXHook(config);
        Bukkit.getScheduler().runTask(this, () -> unregisterCommand("realname"));

    }

    @SuppressWarnings("unchecked")
    private void unregisterCommand(String name) {
        try {
            Field field = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            field.setAccessible(true);

            CommandMap commandMap = (CommandMap) field.get(Bukkit.getServer());
            Map<String, Command> knownCommands = commandMap.getKnownCommands();

            // Collect keys to remove first
            List<String> keysToRemove = new ArrayList<>();
            Command target = knownCommands.get(name);
            if (target == null) return;

            for (Map.Entry<String, Command> entry : knownCommands.entrySet()) {
                if (entry.getValue() == target) {
                    keysToRemove.add(entry.getKey());
                }
            }

            // Remove safely after iteration
            for (String key : keysToRemove) {
                knownCommands.remove(key);
            }
            this.realNameCommand = new RealNameCommand(this);

        } catch (Exception e) {
            logger.logError("Failed to unregister realname command from essentials! ",e);
        }
    }



}
