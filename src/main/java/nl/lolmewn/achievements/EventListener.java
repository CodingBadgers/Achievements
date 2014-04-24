/*
 *  Copyright 2013 Lolmewn <info@lolmewn.nl>.
 */
package nl.lolmewn.achievements;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.milkbowl.vault.economy.Economy;
import nl.lolmewn.achievements.api.AchievementGetEvent;
import nl.lolmewn.achievements.api.AchievementPlayerLoadedEvent;
import nl.lolmewn.achievements.completion.Completion;
import nl.lolmewn.achievements.goal.Goal;
import nl.lolmewn.achievements.player.AchievementPlayer;
import nl.lolmewn.achievements.reward.Reward;
import nl.lolmewn.stats.api.StatUpdateEvent;
import nl.lolmewn.stats.player.StatData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 *
 * @author Lolmewn <info@lolmewn.nl>
 */
public class EventListener implements Listener {

    private final Main plugin;
    private Economy economy;
    private final HashMap<String, HashSet<StatUpdateEvent>> playerLoadWait = new HashMap<String, HashSet<StatUpdateEvent>>();

    public EventListener(Main m) {
        plugin = m;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onStatUpdate(final StatUpdateEvent event) throws Exception {
        OfflinePlayer player = plugin.getServer().getOfflinePlayer(event.getPlayer().getPlayername());
        AchievementPlayer aPlayer = plugin.getPlayerManager().getPlayer(player.getUniqueId());
        if (aPlayer == null) {
            //not loaded yet
            if (this.playerLoadWait.containsKey(event.getPlayer().getPlayername())) {
                this.playerLoadWait.get(event.getPlayer().getPlayername()).add(event);
            } else {
                this.playerLoadWait.put(event.getPlayer().getPlayername(), new HashSet<StatUpdateEvent>() {
                    {
                        this.add(event);
                    }
                });
            }
            return;
        }
        for (Achievement ach : plugin.getAchievementManager().getAchievements()) {
            if (aPlayer.hasCompletedAchievement(ach.getId())) {
                continue;
            }
            if (!achievementGet(event, ach)) {
                continue;
            }
            handleAchievementGet(player, aPlayer, ach);
        }
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        plugin.getPlayerManager().loadPlayer(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onLogout(final PlayerQuitEvent event) {
        final String name = event.getPlayer().getName();
        plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {

            @Override
            public void run() {
                if (plugin.getServer().getPlayerExact(name) == null) {
                    plugin.getPlayerManager().removePlayer(name);
                }
            }
        }, 20L);
        plugin.getPlayerManager().savePlayer(event.getPlayer().getUniqueId(), false);
    }

    @EventHandler
    public void onDisable(PluginDisableEvent event) {
        if (event.getPlugin().equals(this.plugin) || "Stats".equals(event.getPlugin().getName())) {
            if (plugin.getPlayerManager() != null) {
                for (UUID player : plugin.getPlayerManager().getPlayers()) {
                    plugin.getPlayerManager().savePlayer(player, true);
                }
            }
        }
    }
    
    @EventHandler
    public void onLoadComplete(AchievementPlayerLoadedEvent event){
        if(this.playerLoadWait.containsKey(event.getPlayer().getPlayername())){
            for(StatUpdateEvent ev : this.playerLoadWait.get(event.getPlayer().getPlayername())){
                try {
                    this.onStatUpdate(ev);
                } catch (Exception ex) {
                    Logger.getLogger(EventListener.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            this.playerLoadWait.remove(event.getPlayer().getPlayername());
        }
    }

    public boolean setupEconomy() {
        if (economy != null) {
            return true;
        }
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> economyProvider = this.plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }
        return (economy != null);
    }

    public boolean achievementGet(StatUpdateEvent event, Achievement ach) {
        for (Goal g : ach.getGoals()) {
            switch (g.getType()) {
                case STATS:
                    if (!meetsStatsGoal(event, g)) {
                        return false;
                    }
            }
        }
        return true;
    }

    private boolean meetsStatsGoal(StatUpdateEvent event, Goal g) {
		
        if (!event.getStat().equals(g.getStat())) {
            return hasCompletedStatsGoal(event, g);
        }
        if (g.isGlobal()) {
            int totalValue = 0;
            for (Object[] vars : event.getStatData().getAllVariables()) {
                totalValue += event.getStatData().getValue(vars);
            }
            totalValue += event.getUpdateValue();
            if (g.getAmount() > totalValue) {
                return true;
            }
        } else {
            if (!Arrays.toString(event.getVars()).equalsIgnoreCase(Arrays.toString(g.getVariables()))) {
                return false;
            }
            if (event.getNewValue() >= g.getAmount()) {
                return true;
            }
        }
        return false;
    }

    public void handleAchievementGet(OfflinePlayer player, AchievementPlayer aPlayer, Achievement ach) {
        aPlayer.markAsCompleted(ach.getId());
        AchievementGetEvent ae = new AchievementGetEvent(ach, aPlayer);
        plugin.getServer().getPluginManager().callEvent(ae);
        boolean invFullMessage = false;
        for (Reward reward : ach.getRewards()) {
            switch (reward.getRewardType()) {
                case COMMAND:
                    if (player.isOnline()) {
                        ((Player)player).performCommand(reward.getStringValue().replace("%player%", aPlayer.getPlayername()).replace("%name%", ach.getName()));
                    }
                    break;
                case CONSOLE_COMMAND:
                    plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), reward.getStringValue().replace("%player%", aPlayer.getPlayername()).replace("%name%", ach.getName()));
                    break;
                case ITEM:
                    if (!player.isOnline()) {
                        break;
                    }
                    String itemString = reward.getStringValue();
                    String item = itemString.split(",")[0];
                    int amount = Integer.parseInt(itemString.split(",")[1]);
                    ItemStack stack;
                    if (item.contains(".")) {
                        stack = new ItemStack(Material.getMaterial(Integer.parseInt(item.split("\\.")[0])), amount, Short.parseShort(item.split("\\.")[1]));
                    } else {
                        stack = new ItemStack(Material.getMaterial(Integer.parseInt(item)), amount);
                    }
                    if (!((Player)player).getInventory().addItem(stack).isEmpty()) {
                        ((Player)player).getWorld().dropItem(((Player)player).getLocation(), stack);
                        if (!invFullMessage) {
                            ((Player)player).sendMessage(ChatColor.GREEN + "Inventory full, item dropped on the ground.");
                            invFullMessage = true;
                        }
                    }
                    break;
                case MONEY:
                    if (setupEconomy()) {
                        economy.depositPlayer(aPlayer.getPlayername(), reward.getIntValue());
                    }
            }
        }
        for (Completion com : ach.getCompletions()) {
            switch (com.getType()) {
                case MESSAGE:
                    if (!player.isOnline()) {
                        break;
                    }
                    Bukkit.broadcastMessage(ChatColor.GOLD + "[Achievements] " + ChatColor.translateAlternateColorCodes('&', com.getValue().replace("%name%", ach.getName()).replace("%playername%", player.getName())));
                    break;
            }
        }
    }

    private boolean hasCompletedStatsGoal(StatUpdateEvent event, Goal g) {
        if (!this.plugin.getServer().getOfflinePlayer(event.getPlayer().getPlayername()).isOnline() && !g.isGlobal()) {
            return false; //he's not even online, how can he get stats
        }
        StatData statData = g.isGlobal() ? event.getPlayer().getGlobalStatData(g.getStat()) : event.getPlayer().getStatData(g.getStat(), this.plugin.getServer().getPlayerExact(event.getPlayer().getPlayername()).getWorld().getName(), false);
        if (statData == null) {
            return false; //has no data to begin with, lol
        }
        return statData.getValue(g.getVariables()) >= g.getAmount();
    }
}
