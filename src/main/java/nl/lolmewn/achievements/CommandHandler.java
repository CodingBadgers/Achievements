package nl.lolmewn.achievements;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.lolmewn.achievements.goal.Goal;
import nl.lolmewn.achievements.player.AchievementPlayer;
import nl.lolmewn.stats.player.StatData;
import nl.lolmewn.stats.player.StatsPlayer;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;

/**
 * @author Sybren
 */
public class CommandHandler implements CommandExecutor {

    private final Main plugin;

    public CommandHandler(Main main) {
        this.plugin = main;
    }

	private void outputAchievmentsToSender(CommandSender sender, int page) {
		
		final int noofToShow = 8;
						
		AchievementPlayer ap = plugin.getPlayerManager().getPlayer(sender.getName());
		int noofPages = (int)Math.ceil(plugin.getAchievementManager().getAchievements().size() / noofToShow) + 1;
		
		if (page < 1) page = 1;
		if (page > noofPages) page = noofPages;
		
		sender.sendMessage(ChatColor.GREEN + "==== Achievements [Page " + page + "/" + noofPages + "] ====");
		sender.sendMessage(ChatColor.LIGHT_PURPLE + "Completed: "
				+ ChatColor.GREEN + ap.getCompletedAchievements().size()
				+ ChatColor.LIGHT_PURPLE + "/"
				+ ChatColor.RED + plugin.getAchievementManager().getAchievements().size());
		
		HashMap<Achievement, Double> map = orderByPercentageCompleted(ap, plugin.getAchievementManager().getAchievements(), true);
		SortedSet<Map.Entry<Achievement, Double>> sortedSet = this.entriesSortedByValues(map);
		int shown = 0;
		
		int skipAmount = (page - 1) * noofToShow;
		
		for (Map.Entry<Achievement, Double> entry : sortedSet) {
			
			skipAmount--;
			if (skipAmount >= 0) {
				continue;
			}
			
			Double key = entry.getValue();
			Achievement value = entry.getKey();

			if (key > 100) {
				key = 100.0;
			}

			String achievment = ChatColor.LIGHT_PURPLE + value.getName() + ChatColor.RESET + " [" + ChatColor.GREEN;

			for (int i = 0; i < key / 10.0; i++) {
				achievment += "|";
			}

			achievment += ChatColor.RED;
			for (int i = 10 - (int)(key / 10.0); i > 0; i--) {
				achievment += "|";
			}

			achievment += ChatColor.RESET + "]";

			sender.sendMessage(achievment);

			if (++shown > (noofToShow - 1)) {
				break;
			}
		}
		
	}
	
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("You're not a player, can't view your achievements!");
                return true;
            }
            if (sender.hasPermission("achievements.view.self")) {
                outputAchievmentsToSender(sender, 1);
            }
            return true;
        }
		
		try {
			int page = Integer.parseInt(args[0]);
			outputAchievmentsToSender(sender, page);
			return true;
		} catch(Exception ex) {}
		
        if (args[0].equalsIgnoreCase("help")) {

        }
        if (args[0].equalsIgnoreCase("player")) {
            if (args.length == 1) {
                sender.sendMessage(ChatColor.RED + "Correct usage: /" + label + " player <playerName>");
                return true;
            }
            String playerName = args[1];
            Player player = plugin.getServer().getPlayer(playerName);
            if (player == null) {
                sender.sendMessage(ChatColor.RED + "Player could not be found! Is he offline?");
                return true;
            }
            if (sender.hasPermission("achievements.view.others")) {
                AchievementPlayer ap = plugin.getPlayerManager().getPlayer(player.getName());
                sender.sendMessage(ChatColor.GREEN + "====Achievements====");
                sender.sendMessage(ChatColor.LIGHT_PURPLE + "Completed: "
                        + ChatColor.GREEN + ap.getCompletedAchievements().size()
                        + ChatColor.LIGHT_PURPLE + "/"
                        + ChatColor.RED + plugin.getAchievementManager().getAchievements().size());
                HashMap<Achievement, Double> map = orderByPercentageCompleted(ap, plugin.getAchievementManager().getAchievements(), true);
                SortedSet<Map.Entry<Achievement, Double>> sortedSet = this.entriesSortedByValues(map);
                int shown = 0;
                for (Map.Entry<Achievement, Double> entry : sortedSet) {
                    Double key = entry.getValue();
                    Achievement value = entry.getKey();
                    if (key.doubleValue() == 100) {
                        continue;
                    }
                    StringBuilder sb = new StringBuilder();
                    sb.append(ChatColor.LIGHT_PURPLE).append(value.getName()).append(ChatColor.RESET).append(" [");
                    for (int i = 0; i < key.doubleValue() / 10; i++) {
                        sb.append(ChatColor.GREEN).append("|");
                    }
                    for (int i = 10 - (int) key.doubleValue() / 10; i > 0; i--) {
                        sb.append(ChatColor.RED).append("|");
                    }
                    sb.append(ChatColor.RESET).append("]");
                    sender.sendMessage(sb.toString());
                    if (++shown > 8) {
                        break;
                    }
                }
            }
        }
        if (args[0].equalsIgnoreCase("reload")) {
            try {
                sender.sendMessage("Reloading achievements...");
                plugin.getAchievementManager().loadAchievements();
                sender.sendMessage(ChatColor.GREEN + "All achievements have been succesfully reloaded");
                sender.sendMessage("Saving and loading all players...");
                for (UUID player : plugin.getPlayerManager().getPlayers()) {
                    plugin.getPlayerManager().savePlayer(player, true);
                    plugin.getPlayerManager().loadPlayer(player);
                }
                sender.sendMessage(ChatColor.GREEN + "All set and ready to roll!");
            } catch (InvalidConfigurationException ex) {
                Logger.getLogger(CommandHandler.class.getName()).log(Level.SEVERE, null, ex);
                sender.sendMessage(ChatColor.RED + "There seems to be a problem with your config file, please check the logs");
            }
        }
        Achievement ach = plugin.getAchievementManager().findAchievement(args[0]);
        if (ach == null) {
            sender.sendMessage("Achievement or subcommand not found! Try /" + label + " help");
        } else {
            sender.sendMessage("===" + ach.getName() + "===");
            if (ach.getDescription() != null && !ach.getDescription().equals("")) {
                sender.sendMessage(ach.getDescription());
            }
            AchievementPlayer ap = this.plugin.getPlayerManager().getPlayer(sender.getName());
            if (ap != null) {
                StatsPlayer player = plugin.getAPI().getPlayer(ap.getPlayername());
                double completed = this.getPercentCompleted(player, ach);
                StringBuilder sb = new StringBuilder();
                sb.append(ChatColor.RESET).append("[");
                for (int i = 0; i < completed / 10; i++) {
                    sb.append(ChatColor.GREEN).append("|");
                }
                for (int i = 10 - (int) completed / 10; i > 0; i--) {
                    sb.append(ChatColor.RED).append("|");
                }
                sb.append(ChatColor.RESET).append("]");
                sender.sendMessage(sb.toString());
                sender.sendMessage("Completion: " + sb.toString());
            }
            StringBuilder sb = new StringBuilder();
            sb.append("Goals: ");
            for (Goal goal : ach.getGoals()) {
                sb.append(goal.getStat().getName()).append(" ").append(goal.getAmount());
                sb.append(Arrays.toString(goal.getVariables())).append(", ");
            }
        }
        return true;
    }

    private HashMap<Achievement, Double> orderByPercentageCompleted(AchievementPlayer ap, Collection<Achievement> achievements, boolean checkRequirements) {
        HashMap<Achievement, Double> map = new HashMap<Achievement, Double>();
        StatsPlayer player = plugin.getAPI().getPlayer(ap.getPlayername());
        for (Achievement ach : achievements) {
            if (checkRequirements) {
                //Not implemented yet lol, confused with my Quests plugin
            }
            map.put(ach, this.getPercentCompleted(player, ach));
        }
        return map;
    }

    public double getPercentCompleted(StatsPlayer player, Achievement ach) {
        double percent = -1;
        int amount = 0;
        for (Goal goal : ach.getGoals()) {
            if (percent == -1) {
                //first time
                percent = this.getGoalCompletion(player, goal);
                amount++;
            } else {
                percent = ((this.getGoalCompletion(player, goal)) + percent * amount) / (amount + 1);
                amount++;
            }
        }
        return percent;
    }

    public double getGoalCompletion(StatsPlayer player, Goal goal) {
        StatData globalData = player.getGlobalStatData(goal.getStat());
        double count = 0;
        if (goal.isGlobal()) {
            for (Object[] vars : globalData.getAllVariables()) {
                count += globalData.getValue(vars);
            }
        } else {
            count = globalData.getValue(goal.getVariables());
        }
        double value = (count / goal.getAmount()) * 100;
        return value;
    }

    public <Achievement, Double extends Comparable<? super Double>> SortedSet<Map.Entry<Achievement, Double>> entriesSortedByValues(HashMap<Achievement, Double> originalMap) {
        Map<Achievement, Double> map = (Map<Achievement, Double>) originalMap;
        SortedSet<Map.Entry<Achievement, Double>> sortedEntries = new TreeSet<Map.Entry<Achievement, Double>>(
                new Comparator<Map.Entry<Achievement, Double>>() {

                    @Override
                    public int compare(Map.Entry<Achievement, Double> e1, Map.Entry<Achievement, Double> e2) {
                        int res = e2.getValue().compareTo(e1.getValue());
                        if (e1.getKey().equals(e2.getKey())) {
                            return res; // Code will now handle equality properly
                        } else {
                            return res != 0 ? res : 1; // While still adding all entries
                        }
                    }
                });
        sortedEntries.addAll(map.entrySet());
        return sortedEntries;
    }

}
