
import org.slf4j.LoggerFactory;

/**
 * Simple logging wrapper around SLF4J with convenience methods.
 */
public class Logger {
    private final org.slf4j.Logger logger;

    public Logger(Class<?> clazz) {
        logger = LoggerFactory.getLogger(clazz);
    }

    public void error(String msg, Object... args) { 
        logger.error(msg, args); 
    }
    public void warn(String msg, Object... args) { 
        logger.warn(msg, args); 
    }
    public void info(String msg, Object... args) { 
        logger.info(msg, args); 
    }
    public void debug(String msg, Object... args) { 
        logger.debug(msg, args); 
    }

    public void success(String msg, Object... args) { 
        logger.info("✅ " + msg, args); 
    }
    
    public void step(int stepNumber, int totalSteps, String message, Object... args) {
        logger.info("\nStep " + stepNumber + "/" + totalSteps + ": " + message, args);
    }
}


