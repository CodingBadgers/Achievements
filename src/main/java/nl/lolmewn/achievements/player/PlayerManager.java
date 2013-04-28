/*
 *  Copyright 2013 Lolmewn <info@lolmewn.nl>.
 */

package nl.lolmewn.achievements.player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.lolmewn.achievements.Main;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 *
 * @author Lolmewn <info@lolmewn.nl>
 */
public class PlayerManager {

    private Main plugin;
    private HashMap<String, AchievementPlayer> players = new HashMap<String, AchievementPlayer>();
    
    public PlayerManager(Main m) {
        this.plugin = m;
    }
    
    public void loadPlayer(String name){
        AchievementPlayer player = new AchievementPlayer(plugin, name);
        File f = new File(plugin.getDataFolder(), "players.yml");
        if(!f.exists()){
            try {
                f.createNewFile();
            } catch (IOException ex) {
                Logger.getLogger(PlayerManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }else{
            YamlConfiguration c = YamlConfiguration.loadConfiguration(f);
            if(c.contains(name)){
                for(String stringId : c.getStringList(name + ".done")){
                    Integer id = Integer.parseInt(stringId);
                    player.markAsCompleted(id);
                }
            }
        }
        this.players.put(name, new AchievementPlayer(plugin, name));
    }

    public AchievementPlayer getPlayer(String name) {
        return this.players.get(name);
    }
    
    public void savePlayer(String name, boolean remove){
        AchievementPlayer player = this.getPlayer(name);
        if(player == null){
            return;
        }
        File f = new File(plugin.getDataFolder(), "players.yml");
        if(!f.exists()){
            try {
                f.createNewFile();
            } catch (IOException ex) {
                Logger.getLogger(PlayerManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        YamlConfiguration c = YamlConfiguration.loadConfiguration(f);
        c.set(name + ".done", player.getCompletedAchievements());
    }
        

}
