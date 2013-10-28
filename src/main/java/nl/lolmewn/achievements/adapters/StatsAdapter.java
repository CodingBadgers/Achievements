package nl.lolmewn.achievements.adapters;

import nl.lolmewn.achievements.AchievementsAPI;
import nl.lolmewn.achievements.api.AchievementAdapter;
import nl.lolmewn.stats.api.StatsAPI;
import org.bukkit.plugin.Plugin;

/**
 * @author Lolmewn
 */
public class StatsAdapter extends AchievementAdapter{
    
    private final StatsAPI sapi;
    private final AchievementsAPI aapi;
    
    public StatsAdapter(AchievementsAPI aapi, StatsAPI sapi){
        this.sapi = sapi;
        this.aapi = aapi;
    }

    @Override
    public Plugin getPlugin() {
        return aapi.getPlugin();
    }

    @Override
    public AchievementsAPI getAPI() {
        return aapi;
    }

}
