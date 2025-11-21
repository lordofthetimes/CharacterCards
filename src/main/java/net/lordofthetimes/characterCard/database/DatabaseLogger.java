package net.lordofthetimes.characterCard.database;

import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseLogger{

    public final Logger logger;

    DatabaseLogger(Logger logger){
        this.logger = logger;
    }

    public void logQuery(String sql){
        logger.log(Level.INFO,"[CC][DB] Executing query: "+ sql);
    }

    public void logError(String message, Throwable e){
        logger.log(Level.SEVERE, "[CC][DB] " + message, e);
    }

    public void logInfo(String message){
        logger.log(Level.INFO, "[CC][DB] " + message);
    }
}
