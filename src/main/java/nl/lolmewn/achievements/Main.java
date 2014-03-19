package nl.lolmewn.achievements;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.lolmewn.achievements.Updater.UpdateType;
import nl.lolmewn.achievements.player.PlayerManager;
import nl.lolmewn.stats.api.StatsAPI;
import nl.lolmewn.stats.api.mysql.MySQLAttribute;
import nl.lolmewn.stats.api.mysql.MySQLType;
import nl.lolmewn.stats.api.mysql.StatsTable;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;
import org.mcstats.Metrics.Graph;
import org.mcstats.Metrics.Plotter;

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
            this.playerManager.savePlayer(player, true);
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
        if (!stats.isEnabled()) {
            this.getLogger().severe("Stats plugin has been disabled, Achievements cannot start!");
            this.getLogger().severe("Please resolve any Stats issues first!");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }
        api = getServer().getServicesManager().getRegistration(nl.lolmewn.stats.api.StatsAPI.class).getProvider();
        settings = new Settings(this);
        settings.checkExistance();
        settings.loadConfig();
        playerManager = new PlayerManager(this);
        loadOnlinePlayers();
        aManager = new AchievementManager(this);
        try {
            aManager.loadAchievements();
        } catch (InvalidConfigurationException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.getServer().getPluginManager().registerEvents(new EventListener(this), this);
        hasSpout = this.getServer().getPluginManager().getPlugin("Spout") != null;
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
            m.start();
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (this.getSettings().isUpdate()) {
            new Updater(this, 55920, this.getFile(), UpdateType.DEFAULT, false);
        }
        final String tableName = api.getDatabasePrefix() + "achievements";
        if (!api.getStatsTableManager().containsKey(tableName)) {
            api.getStatsTableManager().put(tableName, new StatsTable(tableName, false, api.isCreatingSnapshots()));
        }
        StatsTable table = api.getStatsTable(tableName);
        table.addColumn("id", MySQLType.INTEGER).addAttributes(MySQLAttribute.AUTO_INCREMENT, MySQLAttribute.NOT_NULL, MySQLAttribute.PRIMARY_KEY);
        table.addColumn("player_id", MySQLType.INTEGER).addAttributes(MySQLAttribute.NOT_NULL);
        table.addColumn("achievement_id", MySQLType.INTEGER).addAttributes(MySQLAttribute.NOT_NULL);
        
        this.getCommand("achievements").setExecutor(new CommandHandler(this));
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

    public PlayerManager getPlayerManager() {
        return this.playerManager;
    }

    public void debug(String message) {
        if (this.getSettings().isDebug()) {
            this.getLogger().info("[Debug] " + message);
        }
    }

    public void loadOnlinePlayers() {
        for (Player p : this.getServer().getOnlinePlayers()) {
            this.playerManager.loadPlayer(p.getName());
        }
    }

    public boolean hasSpout() {
        return this.hasSpout;
    }
}
