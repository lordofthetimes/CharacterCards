package net.lordofthetimes.characterCard.utils;

import java.util.logging.Level;
import java.util.logging.Logger;

public class CharacterCardLogger {

    public final Logger logger;

    public CharacterCardLogger(Logger logger){
        this.logger = logger;
    }

    public void logQuery(String sql){
        logger.log(Level.INFO,"[DB] Executing query: "+ sql);
    }

    public void logErrorDB(String message, Throwable e){
        logger.log(Level.SEVERE, "[DB] " + message, e);
    }
    public void logErrorDB(String message){
        logger.log(Level.SEVERE, "[DB] " + message);
    }

    public void logWarnDB(String message){
        logger.log(Level.WARNING, "[DB] " + message);
    }

    public void logInfoDB(String message){
        logger.log(Level.INFO, "[DB] " + message);
    }

    public void logInfo(String message){
        logger.log(Level.INFO,"[CC] " + message);
    }

    public void logWarn(String message){
        logger.log(Level.WARNING,"[CC] " + message);
    }
}
