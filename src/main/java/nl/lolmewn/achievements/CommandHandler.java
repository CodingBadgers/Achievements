package nl.lolmewn.achievements;

import java.util.Arrays;
import java.util.Collection;
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
                TreeMap<Double, Achievement> map = orderByPercentageCompleted(ap, plugin.getAchievementManager().getAchievements(), true);
                int shown = 0;
                for (Map.Entry<Double, Achievement> entry : map.entrySet()) {
                    Double key = entry.getKey();
                    Achievement value = entry.getValue();
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

    private TreeMap<Double, Achievement> orderByPercentageCompleted(AchievementPlayer ap, Collection<Achievement> achievements, boolean checkRequirements) {
        TreeMap<Double, Achievement> map = new TreeMap<Double, Achievement>();
        StatsPlayer player = plugin.getAPI().getPlayer(ap.getPlayername());
        for (Achievement ach : achievements) {
            if (checkRequirements) {
                //Not implemented yet lol, confused with my Quests plugin
            }
            map.put(this.getPercentCompleted(player, ach), ach);
        }
        return map;
    }

    public double getPercentCompleted(StatsPlayer player, Achievement ach) {
        double percent = -1;
        int amount = 0;
        for (Goal goal : ach.getGoals()) {
            StatData globalData = player.getGlobalStatData(goal.getStat());
            if (percent == -1) {
                //first time
                percent = globalData.getValue(goal.getVariables()) / goal.getAmount() * 100;
                amount++;
            } else {
                percent = ((globalData.getValue(goal.getVariables()) / goal.getAmount() * 100) + percent * amount) / amount + 1;
                amount++;
            }
        }
        return percent;
    }

    public double getGoalCompletion(StatsPlayer player, Goal goal) {
        StatData globalData = player.getGlobalStatData(goal.getStat());
        return globalData.getValue(goal.getVariables()) / goal.getAmount() * 100;
    }

}