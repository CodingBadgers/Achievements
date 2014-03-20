package nl.lolmewn.achievements.api;

import nl.lolmewn.achievements.player.AchievementPlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * @author Sybren
 */
public class AchievementPlayerLoadedEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final AchievementPlayer player;

    public AchievementPlayerLoadedEvent(AchievementPlayer player) {
        this.player = player;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public AchievementPlayer getPlayer() {
        return player;
    }

}
