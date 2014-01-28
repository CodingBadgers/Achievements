package nl.lolmewn.achievements;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import nl.lolmewn.achievements.goal.Goal;
import nl.lolmewn.achievements.player.AchievementPlayer;
import nl.lolmewn.stats.player.StatData;
import nl.lolmewn.stats.player.StatsPlayer;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Sybren
 */
public class CommandHandler implements CommandExecutor {

    private Main plugin;

    public CommandHandler(Main main) {
        this.plugin = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("You're not a player, can't view your achievements!");
                return true;
            }
            if (sender.hasPermission("achievements.view.self")) {
                AchievementPlayer ap = plugin.getPlayerManager().getPlayer(sender.getName());
                sender.sendMessage("====Achievements====");
                sender.sendMessage("Completed: " + ap.getCompletedAchievements().size() + "/" + plugin.getAchievementManager().getAchievements().size());
                System.out.println("Building treemap");
                TreeMap<Achievement, Double> map = orderByPercentageCompleted(ap, plugin.getAchievementManager().getAchievements(), true);
                Map<Achievement, Double> sortedMap = this.sortByValue(map);
                for (Map.Entry<Achievement, Double> entry : map.entrySet()) {
                    System.out.println("Key: " + entry.getKey().getName() + ". Value: " + entry.getValue());
                }
                int shown = 0;
                for (Map.Entry<Achievement, Double> entry : sortedMap.entrySet()) {
                    Double key = entry.getValue();
                    Achievement value = entry.getKey();
                    if (key.doubleValue() == 100) {
                        continue;
                    }
                    StringBuilder sb = new StringBuilder();
                    sb.append(value.getName()).append(ChatColor.RESET).append(" [");
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
            return true;
        }
        if (args[0].equalsIgnoreCase("help")) {

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

    private TreeMap<Achievement, Double> orderByPercentageCompleted(AchievementPlayer ap, Collection<Achievement> achievements, boolean checkRequirements) {
        TreeMap<Achievement, Double> map = new TreeMap<Achievement, Double>();
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
        System.out.println("  " + ach.getName() + " " + percent + "%");
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
        System.out.println("    " + goal.getStat().getName() + " " + value + "%");
        return value;
    }

    public <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
            @Override
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });

        Map<K, V> result = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

}
