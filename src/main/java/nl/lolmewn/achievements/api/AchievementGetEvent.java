package nl.lolmewn.achievements.api;

import nl.lolmewn.achievements.Achievement;
import nl.lolmewn.achievements.player.AchievementPlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 *
 * @author Lolmewn
 */
public class AchievementGetEvent extends Event{
   
    private static final HandlerList handlers = new HandlerList();
    
    private final Achievement ach;
    private final AchievementPlayer player;
    
    public AchievementGetEvent(Achievement ach, AchievementPlayer player){
        this.ach = ach;
        this.player = player;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    public static HandlerList getHandlerList(){
        return handlers;
    }

    public Achievement getAchievement() {
        return ach;
    }

    public AchievementPlayer getPlayer() {
        return player;
    }
}
