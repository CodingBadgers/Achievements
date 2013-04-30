package nl.lolmewn.achievements;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.lolmewn.achievements.Metrics.Graph;
import nl.lolmewn.achievements.Metrics.Plotter;
import nl.lolmewn.achievements.Updater.UpdateType;
import nl.lolmewn.achievements.player.PlayerManager;
import nl.lolmewn.stats.api.StatsAPI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    
    private StatsAPI api;
    private Settings settings;
    private AchievementManager aManager;
    private PlayerManager playerManager;
    
    private boolean hasSpout;
    protected double newVersion;
    
    @Override
    public void onDisable() {
        for(String player : playerManager.getPlayers()){
            playerManager.savePlayer(player, true);
        }
    }
    
    @Override
    public void onEnable() {
        Plugin stats = this.getServer().getPluginManager().getPlugin("Stats");
        if (stats == null) {
            this.getLogger().severe("Stats not found, disabling! You can download stats here: http://dev.bukkit.org/server-mods/lolmewnstats/");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }
        api = getServer().getServicesManager().getRegistration(nl.lolmewn.stats.api.StatsAPI.class).getProvider();
        settings = new Settings(this);
        settings.checkExistance();
        settings.loadConfig();
        aManager = new AchievementManager(this);
        aManager.loadAchievements();
        playerManager = new PlayerManager(this);
        loadOnlinePlayers();
        this.getServer().getPluginManager().registerEvents(new EventListener(this), this);
        hasSpout = this.getServer().getPluginManager().getPlugin("SpoutPlugin") != null;
        try {
            Metrics m = new Metrics(this);
            Graph g = m.createGraph("Stats");
            Plotter p = new Plotter() {
                @Override
                public String getColumnName() {
                    return "Amount of achievements";
                }

                @Override
                public int getValue() {
                    return getAchievementManager().getAchievements().size();
                }
            };
            g.addPlotter(p);
            m.addGraph(g);
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        if(this.getSettings().isUpdate()){
            new Updater(this, "lolmewnachievements", this.getFile(), UpdateType.DEFAULT, true);
        }
    }
    
    public Settings getSettings() {
        return settings;
    }
    
    public StatsAPI getAPI() {
        return api;
    }
    
    public AchievementManager getAchievementManager() {
        return aManager;
    }
    
    public PlayerManager getPlayerManager(){
        return this.playerManager;
    }
    
    public void debug(String message) {
        if (this.getSettings().isDebug()) {
            this.getLogger().info("[Debug] " + message);
        }
    }
    
    public void loadOnlinePlayers(){
        for(Player p : this.getServer().getOnlinePlayers()){
            this.playerManager.loadPlayer(p.getName());
        }
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String cm, String[] args){
        return false;
    }
}
