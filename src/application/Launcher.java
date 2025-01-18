package application;

import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class Launcher {

    private static final Logger logger = Logger.getLogger(Launcher.class.getName());

    static {
        // Create a custom formatter
        Formatter formatter = new Formatter() {
            @Override
            public String format(LogRecord record) {
                return record.getMessage() + "\n";
            }
        };

        // Get the console handler and set the custom formatter
        Handler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(formatter);
        consoleHandler.setLevel(Level.ALL);
        logger.addHandler(consoleHandler);
        logger.setUseParentHandlers(false);
    }
    
	public static void logInfo(String message) {
		logger.info(message);
	}

    public static void main(String[] args) {
        logInfo("application lancée");
        Main.main(args);
    }
}