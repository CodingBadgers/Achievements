/*
 *  Copyright 2013 Lolmewn <info@lolmewn.nl>.
 */
package nl.lolmewn.achievements.player;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.lolmewn.achievements.Main;
import nl.lolmewn.achievements.api.AchievementPlayerLoadedEvent;
import nl.lolmewn.stats.player.StatsPlayer;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 *
 * @author Lolmewn <info@lolmewn.nl>
 */
public class PlayerManager {
    
    private final Main plugin;
    private final ConcurrentHashMap<String, AchievementPlayer> players = new ConcurrentHashMap<String, AchievementPlayer>();
    private final HashSet<String> beingLoaded = new HashSet<String>();
    private final YamlConfiguration c;
    
    public PlayerManager(Main m) {
        this.plugin = m;
        File f = new File(plugin.getDataFolder(), "players.yml");
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException ex) {
                Logger.getLogger(PlayerManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        c = YamlConfiguration.loadConfiguration(f);
    }
    
    public void loadPlayer(String name) {
        if (players.containsKey(name)) {
            return;
        }
        if (this.beingLoaded.contains(name)) {
            return;
        }
        this.beingLoaded.add(name);
        Connection con = this.plugin.getAPI().getConnection();
        AchievementPlayer player = new AchievementPlayer(name);
//<editor-fold defaultstate="collapsed" desc="old loading code">
        if (c.contains(name)) {
            plugin.debug("Config contains " + name);
            for (String stringId : c.getStringList(name + ".done")) {
                Integer id = Integer.parseInt(stringId);
                plugin.debug("Loaded " + id + " as complete");
                player.markAsCompleted(id);
            }
        }
//</editor-fold>
        int id = this.plugin.getAPI().getPlayerId(name);
        try {
            PreparedStatement st = con.prepareStatement("SELECT * FROM " 
                    + this.plugin.getAPI().getDatabasePrefix() + "achievements"
                    + " WHERE player_id=?");
            st.setInt(1, id);
            ResultSet set = st.executeQuery();
            if(set != null){
                while(set.next()){
                    int completed = set.getInt("achievement_id");
                    player.markAsCompleted(completed);
                }
                set.close();
            }
            st.close();
        } catch (SQLException ex) {
            Logger.getLogger(PlayerManager.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            this.beingLoaded.remove(name);
            this.players.put(name, player);
            try {
                con.close();
            } catch (SQLException ex) {
                Logger.getLogger(PlayerManager.class.getName()).log(Level.SEVERE, null, ex);
            }
            AchievementPlayerLoadedEvent event = new AchievementPlayerLoadedEvent(player);
            this.plugin.getServer().getPluginManager().callEvent(event);
        }
    }
    
    public AchievementPlayer getPlayer(String name) {
        if(!this.players.containsKey(name)){
            return null;
        }
        return this.players.get(name);
    }
    
    public void savePlayer(final String name, final boolean remove) {
        final AchievementPlayer player = this.getPlayer(name);
        StatsPlayer sPlayer = plugin.getAPI().getPlayer(name);
        if (player == null) {
            return;
        }
        final int id = sPlayer.getId();
        try {
            Connection con = plugin.getAPI().getConnection();
            con.setAutoCommit(false);
            PreparedStatement st = con.prepareStatement("INSERT IGNORE INTO " + plugin.getAPI().getDatabasePrefix() + "achievements (player_id, achievement_id) VALUES (?, ?)");
            st.setInt(1, id);
            for (int completed : player.getCompletedAchievements()) {
                st.setInt(2, completed);
                st.addBatch();
            }
            st.executeBatch();
            con.commit();
            con.close();
        } catch (SQLException ex) {
            Logger.getLogger(PlayerManager.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (remove) {
                removePlayer(name);
            }
            c.set(name + ".done", player.getCompletedAchievements());
            try {
                c.save(new File(plugin.getDataFolder(), "players.yml"));
            } catch (IOException ex) {
                Logger.getLogger(PlayerManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public Collection<AchievementPlayer> getAchievementPlayers() {
        return this.players.values();
    }
    
    public Set<String> getPlayers() {
        return this.players.keySet();
    }
    
    public void removePlayer(String name) {
        this.players.remove(name);
    }
}
