/*
 *  Copyright 2013 Lolmewn <info@lolmewn.nl>.
 */
package nl.lolmewn.achievements.player;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.lolmewn.achievements.Main;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 *
 * @author Lolmewn <info@lolmewn.nl>
 */
public class PlayerManager {

    private final Main plugin;
    private ConcurrentHashMap<String, AchievementPlayer> players = new ConcurrentHashMap<String, AchievementPlayer>();
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
        AchievementPlayer player = new AchievementPlayer(plugin, name);
        if (c.contains(name)) {
            plugin.debug("Config contains " + name);
            for (String stringId : c.getStringList(name + ".done")) {
                Integer id = Integer.parseInt(stringId);
                plugin.debug("Loaded " + id + " as complete");
                player.markAsCompleted(id);
            }
        }
        this.players.put(name, player);
    }

    public AchievementPlayer getPlayer(String name) {
        return this.players.get(name);
    }

    public void savePlayer(final String name, final boolean remove) {
        final AchievementPlayer player = this.getPlayer(name);
        if (player == null) {
            return;
        }
        this.plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {

            @Override
            public void run() {
                c.set(name + ".done", player.getCompletedAchievements());
                try {
                    c.save(new File(plugin.getDataFolder(), "players.yml"));
                } catch (IOException ex) {
                    Logger.getLogger(PlayerManager.class.getName()).log(Level.SEVERE, null, ex);
                }
                if (remove) {
                    removePlayer(name);
                }
            }
        });
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
