package nl.lolmewn.achievements.api;

import nl.lolmewn.achievements.Achievement;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 *
 * @author Lolmewn
 */
public class AchievementGetEvent extends Event{
   
    private static final HandlerList handlers = new HandlerList();
    
    private Achievement ach;
    private Player player;
    
    public AchievementGetEvent(Achievement ach, Player player){
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

    public Player getPlayer() {
        return player;
    }
}
