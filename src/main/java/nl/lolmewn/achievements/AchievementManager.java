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

    private final Main plugin;
    private final HashMap<Integer, Achievement> achievements = new HashMap<Integer, Achievement>();
    private int nextFreeId;
    
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
                Achievement ach = new Achievement(plugin, id);
                if(ach.load(section)){
                    this.addAchievement(id, ach);
                    calculateNextFreeId(id);
                }
            }catch(NumberFormatException e){
                plugin.getLogger().warning("Failed to load achievement, ID was not an int: " + key);
            }
        }
    }
    
    public void addAchievement(int id, Achievement a){
        if(this.achievements.containsKey(id)){
            plugin.getLogger().warning("Duplicate ID found for achievements, "
                    + "you can only use an ID once. ID: " + id + ", achievement trying to add (failing): " + a.getName());
        }
        this.achievements.put(id, a);
    }

    public Collection<Achievement> getAchievements() {
        return achievements.values();
    }

    private void calculateNextFreeId(int id) {
        while(this.achievements.containsKey(id + 1)){
            id++;
        }
        this.nextFreeId = id + 1;
    }
    
    public int getNextFreeId(){
        return this.nextFreeId;
    }

    public Achievement findAchievement(String q) {
        try{
            int i = Integer.parseInt(q);
            return this.achievements.get(i);
        }catch(NumberFormatException e){
            for(Achievement ach : this.getAchievements()){
                if(ach.getName().toLowerCase().startsWith(q.toLowerCase())){
                    return ach;
                }
            }
            for(Achievement ach : this.getAchievements()){
                if(ach.getName().toLowerCase().contains(q.toLowerCase())){
                    return ach;
                }
            }
        }
        return null;
    }

}
