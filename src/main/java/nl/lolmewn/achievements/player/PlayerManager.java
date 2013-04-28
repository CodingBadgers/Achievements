/*
 *  Copyright 2013 Lolmewn <info@lolmewn.nl>.
 */

package nl.lolmewn.achievements.player;

import java.util.HashMap;
import nl.lolmewn.achievements.Main;

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
        this.players.put(name, new AchievementPlayer(plugin, name));
    }

    public AchievementPlayer getPlayer(String name) {
        return this.players.get(name);
    }

}
