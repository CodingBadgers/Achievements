/*
 *  Copyright 2013 Lolmewn <info@lolmewn.nl>.
 */

package nl.lolmewn.achievements;

import nl.lolmewn.stats.api.StatUpdateEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 *
 * @author Lolmewn <info@lolmewn.nl>
 */
public class EventListener implements Listener{
    
    private Main plugin;
    
    public EventListener(Main m) {
        plugin = m;
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onStatUpdate(StatUpdateEvent event){
        Player player = plugin.getServer().getPlayerExact(event.getPlayer().getPlayername());
        
    }

}
