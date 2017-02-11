package wow.hey.org.heygame;


import com.jme3.app.AndroidHarness;

import java.util.logging.Level;
import java.util.logging.LogManager;

public class MainActivity extends AndroidHarness {

    public MainActivity() {
        // Set the application class to run
        appClass = "com.hey.wow.Game";

        // Exit Dialog title & message
        exitDialogTitle = "Exit?";
        exitDialogMessage = "Are you sure you want to quit?";

        // Enable MouseEvents being generated from TouchEvents (default = true)
        mouseEventsEnabled = true;

        // Set the default logging level (default=Level.INFO, Level.ALL=All Debug Info)
        LogManager.getLogManager().getLogger("").setLevel(Level.INFO);
    }
}