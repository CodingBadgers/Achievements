/*
 *  Copyright 2013 Lolmewn <info@lolmewn.nl>.
 */

package nl.lolmewn.achievements;

import java.io.File;
import org.bukkit.configuration.file.FileConfiguration;

/**
 *
 * @author Lolmewn <info@lolmewn.nl>
 */
public class Settings {

    private Main plugin;
    
    
    private boolean debug;
    private boolean update;
    private String version;
    
    
    public Settings(Main m) {
        this.plugin = m;
    }
    
    public void checkExistance(){
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        if(!configFile.exists()){
            plugin.saveDefaultConfig();
        }
    }
    
    public void loadConfig(){
        FileConfiguration c = plugin.getConfig();
        debug = c.getBoolean("debug", false);
        update = c.getBoolean("update", true);
        version = c.getString("version");
    }

    public boolean isDebug() {
        return debug;
    }

    public boolean isUpdate() {
        return update;
    }

    public double getVersion(){
        return version.contains("-") ? Double.parseDouble(version.split("-")[0]) : Double.parseDouble(version);
    }
    
    public String getVersionString(){
        return version;
    }

}
