/*
 *  Copyright 2013 Lolmewn <info@lolmewn.nl>.
 */
package nl.lolmewn.achievements;

import java.util.Arrays;
import net.milkbowl.vault.economy.Economy;
import nl.lolmewn.achievements.completion.Completion;
import nl.lolmewn.achievements.goal.Goal;
import nl.lolmewn.achievements.player.AchievementPlayer;
import nl.lolmewn.achievements.reward.Reward;
import nl.lolmewn.stats.StatType;
import nl.lolmewn.stats.api.StatUpdateEvent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.getspout.spoutapi.Spout;
import org.getspout.spoutapi.player.SpoutPlayer;

/**
 *
 * @author Lolmewn <info@lolmewn.nl>
 */
public class EventListener implements Listener {

    private Main plugin;
    private Economy economy;

    public EventListener(Main m) {
        plugin = m;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onStatUpdate(StatUpdateEvent event) throws Exception {
        Player player = plugin.getServer().getPlayerExact(event.getPlayer().getPlayername());
        AchievementPlayer aPlayer;
        try {
            aPlayer = plugin.getPlayerManager().getPlayer(player.getName());
        } catch (NullPointerException e) {
            plugin.getLogger().warning("NPE happened in StatUpdateEvent, please give these details to Lolmewn (creator of Achievements, this plugin):");
            plugin.getLogger().warning("Stat: " + event.getStatType().toString() + " ; " + Arrays.toString(event.getVars()));
            plugin.getLogger().warning("Player: " + event.getPlayer() + " with name + " + event.getPlayer() == null ? "null" : event.getPlayer().getPlayername());
            plugin.getLogger().warning("Values: " + event.getNewValue() + "=old+" + event.getUpdateValue());
            Exception ex = new Exception();
            Throwable t = ex.fillInStackTrace();
            t.printStackTrace();
            plugin.getLogger().warning("=======The above is the stacktrace of a new exception=======");
            plugin.getLogger().warning("=======Below is the original exception               =======");
            e.printStackTrace();
            return;
        }

        for (Achievement ach : plugin.getAchievementManager().getAchievements()) {
            if (aPlayer.hasCompletedAchievement(ach.getId())) {
                continue;
            }
            boolean cont = false;
            for (Goal g : ach.getGoals()) {
                boolean breaking = false;
                switch (g.getType()) {
                    case STATS:
                        if(handleStatsGoal(event, ach, g, aPlayer)){
                            cont = true;
                            breaking = true;
                        }
                        break;
                }
                if (!event.getStat().equals(plugin.getAPI().getStat(g.getStatType().makeMePretty()))) {
                    cont = true;
                    break;
                }
                if (g.isGlobal()) {
                    int totalValue = 0;
                    for (Object[] vars : event.getStatData().getAllVariables()) {
                        if (event.getStat().equals(plugin.getAPI().getStat(g.getStatType().makeMePretty()))) {
                            totalValue += event.getStatData().getValueDouble(vars);
                        } else {
                            totalValue += event.getStatData().getValue(vars);
                        }
                    }
                    totalValue += event.getUpdateValue();
                    if (g.getAmount() > totalValue) {
                        cont = true;
                        break;
                    }
                } else {
                    if (event.getNewValue() < g.getAmount()) {
                        cont = true;
                        break;
                    }
                    if (!Arrays.toString(event.getVars()).equalsIgnoreCase(Arrays.toString(g.getVariables()))) {
                        cont = true;
                        break;
                    }
                }
            }
            if (cont) {
                continue;
            }
            aPlayer.markAsCompleted(ach.getId());
            if (plugin.hasSpout()) {
                SpoutPlayer p = Spout.getServer().getPlayer(aPlayer.getPlayername());
                if (p != null) {
                    p.sendNotification("Achievement get!", ach.getName(), Material.BOOK);
                }
            }
            boolean invFullMessage = false;
            for (Reward reward : ach.getRewards()) {
                switch (reward.getRewardType()) {
                    case COMMAND:
                        player.performCommand(reward.getStringValue().replace("%player%", aPlayer.getPlayername()).replace("%name%", ach.getName()));
                        break;
                    case CONSOLE_COMMAND:
                        plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), reward.getStringValue().replace("%player%", aPlayer.getPlayername()).replace("%name%", ach.getName()));
                        break;
                    case ITEM:
                        String itemString = reward.getStringValue();
                        String item = itemString.split(",")[0];
                        int amount = Integer.parseInt(itemString.split(",")[1]);
                        ItemStack stack;
                        if (item.contains(".")) {
                            stack = new ItemStack(Material.getMaterial(Integer.parseInt(item.split("\\.")[0])), amount, Short.parseShort(item.split("\\.")[1]));
                        } else {
                            stack = new ItemStack(Material.getMaterial(Integer.parseInt(item)), amount);
                        }
                        if (!player.getInventory().addItem(stack).isEmpty()) {
                            player.getWorld().dropItem(player.getLocation(), stack);
                            if (!invFullMessage) {
                                player.sendMessage(ChatColor.GREEN + "Inventory full, item dropped on the ground.");
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
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', com.getValue().replace("%name%", ach.getName())));
                        break;
                }
            }
        }
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        plugin.getPlayerManager().loadPlayer(event.getPlayer().getName());
    }

    @EventHandler
    public void onLogout(PlayerQuitEvent event) {
        plugin.getPlayerManager().savePlayer(event.getPlayer().getName(), true);
    }

    public boolean setupEconomy() {
        if (economy != null) {
            return true;
        }
        if(plugin.getServer().getPluginManager().getPlugin("Vault") == null){
            return false;
        }
        RegisteredServiceProvider<Economy> economyProvider = this.plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }
        return (economy != null);
    }

    private boolean handleStatsGoal(StatUpdateEvent event, Achievement ach, Goal g, AchievementPlayer aPlayer) {
        if (!event.getStat().equals(plugin.getAPI().getStat(g.getStatType().makeMePretty()))) {
            return true;
        }
        if (g.isGlobal()) {
            int totalValue = 0;
            for (Object[] vars : event.getStatData().getAllVariables()) {
                if (event.getStat().equals(plugin.getAPI().getStat("Move"))) {
                    totalValue += event.getStatData().getValueDouble(vars);
                } else {
                    totalValue += event.getStatData().getValue(vars);
                }
            }
            totalValue += event.getUpdateValue();
            if (g.getAmount() > totalValue) {
                return true;
            }
        } else {
            if (event.getNewValue() < g.getAmount()) {
                return true;
            }
            if (!Arrays.toString(event.getVars()).equalsIgnoreCase(Arrays.toString(g.getVariables()))) {
                return true;
            }
        }
        return false;
    }
}
