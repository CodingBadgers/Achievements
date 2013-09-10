package nl.lolmewn.achievements.api;

import nl.lolmewn.achievements.AchievementsAPI;
import org.bukkit.plugin.Plugin;

/**
 * @author Lolmewn
 */
public abstract class AchievementAdapter {
    
    public AchievementAdapter(){
    }
    
    public abstract Plugin getPlugin();
    
    public abstract AchievementsAPI getAPI();

}
