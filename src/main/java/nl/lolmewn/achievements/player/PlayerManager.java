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
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.lolmewn.achievements.Main;
import nl.lolmewn.achievements.api.AchievementPlayerLoadedEvent;
import nl.lolmewn.stats.player.StatsPlayer;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 *
 * @author Lolmewn <info@lolmewn.nl>
 */
public class PlayerManager {

    private final Main plugin;
    private final ConcurrentHashMap<UUID, AchievementPlayer> players = new ConcurrentHashMap<UUID, AchievementPlayer>();
    private final HashSet<UUID> beingLoaded = new HashSet<UUID>();
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

    public void loadPlayer(UUID uuid) {
        OfflinePlayer player = plugin.getServer().getOfflinePlayer(uuid);
        if (players.containsKey(player.getUniqueId())) {
            return;
        }
        if (this.beingLoaded.contains(player.getUniqueId())) {
            return;
        }
        this.beingLoaded.add(player.getUniqueId());
        Connection con = this.plugin.getAPI().getConnection();
        AchievementPlayer aPlayer = new AchievementPlayer(player.getName());
        try {
            PreparedStatement st = con.prepareStatement("SELECT * FROM "
                    + this.plugin.getAPI().getDatabasePrefix() + "achievements"
                    + " WHERE player_id=?");
            st.setInt(1, plugin.getAPI().getPlayerId(aPlayer.getPlayername()));
            ResultSet set = st.executeQuery();
            if (set != null) {
                while (set.next()) {
                    int completed = set.getInt("achievement_id");
                    aPlayer.markAsCompleted(completed);
                }
                set.close();
            }
            st.close();
        } catch (SQLException ex) {
            Logger.getLogger(PlayerManager.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            this.beingLoaded.remove(player.getUniqueId());
            this.players.put(player.getUniqueId(), aPlayer);
            try {
                con.close();
            } catch (SQLException ex) {
                Logger.getLogger(PlayerManager.class.getName()).log(Level.SEVERE, null, ex);
            }
            AchievementPlayerLoadedEvent event = new AchievementPlayerLoadedEvent(aPlayer);
            this.plugin.getServer().getPluginManager().callEvent(event);
        }
    }

    public AchievementPlayer getPlayer(String name) {
        OfflinePlayer player = plugin.getServer().getOfflinePlayer(name);
        if (!this.players.containsKey(player.getUniqueId())) {
            return null;
        }
        return this.players.get(player.getUniqueId());
    }

    public AchievementPlayer getPlayer(UUID uuid) {
        return this.players.get(uuid);
    }

    public void savePlayer(final UUID uuid, final boolean remove) {
        final AchievementPlayer player = this.getPlayer(uuid);
        StatsPlayer sPlayer = plugin.getAPI().getPlayer(uuid);
        if (player == null) {
            return;
        }
        final int id = sPlayer.getId();
        try {
            Connection con = plugin.getAPI().getConnection();

            PreparedStatement presentCheck = con.prepareStatement("SELECT * FROM " + plugin.getAPI().getDatabasePrefix() + "achievements WHERE player_id=? AND achievement_id=?");
            PreparedStatement st = con.prepareStatement("INSERT INTO " + plugin.getAPI().getDatabasePrefix() + "achievements (player_id, achievement_id) VALUES (?, ?)");
            presentCheck.setInt(1, id);
            st.setInt(1, id);

            boolean anyInsert = false;

            for (int completed : player.getCompletedAchievements()) {
                presentCheck.setInt(2, completed);
                ResultSet set = presentCheck.executeQuery();
                if (!set.next()) {
                    st.setInt(2, completed);
                    st.addBatch();
                    if (!anyInsert) {
                        con.setAutoCommit(false);
                        anyInsert = true;
                    }
                }
                set.close();
            }

            if (anyInsert) {
                st.executeBatch();
                con.commit();
            }
            st.close();
            presentCheck.close();
            con.close();
        } catch (SQLException ex) {
            Logger.getLogger(PlayerManager.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (remove) {
                removePlayer(uuid);
            }
            c.set(plugin.getServer().getOfflinePlayer(uuid).getName() + ".done", player.getCompletedAchievements());
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

    public Set<UUID> getPlayers() {
        return this.players.keySet();
    }

    public void removePlayer(String name) {
        OfflinePlayer op = plugin.getServer().getOfflinePlayer(name);
        this.removePlayer(op.getUniqueId());
    }

    public void removePlayer(UUID uuid) {
        this.players.remove(uuid);
    }
}
