/*
 *  Copyright 2013 Lolmewn <info@lolmewn.nl>.
 */

package nl.lolmewn.achievements;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 *
 * @author Lolmewn <info@lolmewn.nl>
 */
public class AchievementManager {

    private Main plugin;
    private HashMap<Integer, Achievement> achievements = new HashMap<Integer, Achievement>();
    
    public AchievementManager(Main aThis) {
        plugin = aThis;
    }
    
    public void loadAchievements(){
        File file = new File(plugin.getDataFolder(), "achievements.yml");
        if(!file.exists()){
            plugin.saveResource("achievements.yml", true);
            return;
        }
        YamlConfiguration c = YamlConfiguration.loadConfiguration(file);
        for(String key : c.getConfigurationSection("").getKeys(false)){
            try{
                int id = Integer.parseInt(key);
                ConfigurationSection section = c.getConfigurationSection(key);
                Achievement ach = new Achievement(plugin);
                if(ach.load(section)){
                    this.achievements.put(id, ach);
                }
            }catch(NumberFormatException e){
                plugin.getLogger().warning("Failed to load achievement, ID was not an int: " + key);
            }
        }
    }

    public Collection<Achievement> getAchievements() {
        return achievements.values();
    }

}
