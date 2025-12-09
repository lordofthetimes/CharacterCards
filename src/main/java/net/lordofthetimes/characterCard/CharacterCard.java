package net.lordofthetimes.characterCard;

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


public final class CharacterCard extends JavaPlugin {

    public final CharacterCardLogger logger = new CharacterCardLogger(this.getLogger());

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
        saveDefaultConfig();

        if (getServer().getPluginManager().getPlugin("Lands") != null && getConfig().getBoolean("lands.enabled")) {
            getLogger().info("Lands detected, support enabled!");
            enableLandsSupport();
        }

        if (getServer().getPluginManager().getPlugin("Essentials") != null && getConfig().getBoolean("essentials.enabled")) {
            getLogger().info("Essentials detected, support enabled!");
            enableEssentialsXSupport();
        }



        db = new DatabaseManager(this);
        db.connect();
        db.generateTables();
        db.tryAddColumns();
        tryUpdateConfig();
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
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null && getConfig().getBoolean("papi.enabled")) {
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
        this.lands = new LandsHook(this);
    }

    private void enablePAPISupport(){
        this.papiEnabled = true;
        this.papi= new CharacterCardPlaceholderExpansion(this,db);
        papi.register();
    }

    private void enableEssentialsXSupport(){
        this.essentialsXEnabled = true;
        this.essentials = new EssentialsXHook(this);
    }

    private void tryUpdateConfig(){
        FileConfiguration config = this.getConfig();

        if(!config.contains("version")){
            config.set("version",100);
            logger.logWarn("Version inside config file is missing, default value for 1.0.0 is set! If this was not empty before, God bless your config ;(");
        }

        int configVersion = config.getInt("version");
        int pluginVersion = Integer.parseInt(getDescription().getVersion().replace(".",""));

        if(pluginVersion == configVersion){
            logger.logInfo("Config is up to date");
            return;
        }

        if(configVersion < 110){

            config.set("genderMessage","\\n<gold><bold>Gender: </bold><%gender%></gold>\\n");

            ConfigurationSection essentialsSection = config.createSection("essentials");
            essentialsSection.set("enabled",true);
            essentialsSection.set("nickname",true);

            logger.logInfo("Successfully added config section for version 1.1.0");

        }
        config.set("version",pluginVersion);
        saveConfig();

    }

}
