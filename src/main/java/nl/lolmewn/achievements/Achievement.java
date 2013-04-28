/*
 *  Copyright 2013 Lolmewn <info@lolmewn.nl>.
 */
package nl.lolmewn.achievements;

import java.util.ArrayList;
import java.util.List;
import nl.lolmewn.achievements.completion.Completion;
import nl.lolmewn.achievements.completion.CompletionType;
import nl.lolmewn.achievements.goal.Goal;
import nl.lolmewn.achievements.reward.Reward;
import nl.lolmewn.achievements.reward.RewardType;
import nl.lolmewn.stats.StatType;
import org.bukkit.configuration.ConfigurationSection;

/**
 *
 * @author Lolmewn <info@lolmewn.nl>
 */
public class Achievement {

    private Main main;
    private String name;
    private List<Goal> goals = new ArrayList<Goal>();
    private List<Reward> rewards = new ArrayList<Reward>();
    private List<Completion> completions = new ArrayList<Completion>();

    public Achievement(Main main) {
        this.main = main;
    }

    public boolean load(ConfigurationSection loadFrom) {
        name = loadFrom.getString("name");
        for (String goal : loadFrom.getStringList("goals")) {
            String[] split = goal.split(" ");
            if (split.length < 3) {
                main.getLogger().warning("Unable to load achievement " + name + ", goal is set up wrong: " + goal);
                return false;
            }
            StatType type = StatType.valueOf(split[0]);
            if (type == null) {
                main.getLogger().warning("Unable to load achievement" + name + ", type was not found: " + split[0]);
                return false;
            }
            int amount = 0;
            try {
                amount = Integer.parseInt(split[1]);
            } catch (NumberFormatException e) {
                main.getLogger().warning("Unable to load achievement" + name + ", amount must be a number: " + split[1]);
                return false;
            }
            if (amount >= 0) {
                main.getLogger().warning("Unable to load achievement" + name + ", amount must be greater than 0: " + split[1]);
                return false;
            }
            Goal g;
            if (split[2].equalsIgnoreCase("TOTAL")) {
                g = new Goal(type, amount, true, null);
            } else {
                Object[] vars = new Object[split.length - 2];
                System.arraycopy(split, 2, vars, 0, vars.length);
                g = new Goal(type, amount, false, vars);
            }
            this.goals.add(g);
            main.debug("Goal created: " + g.toString());
        }
        if (this.goals.isEmpty()) {
            main.getLogger().warning("Unable to load achievement" + name + ", no goals specified");
            return false;
        }
        if (loadFrom.contains("rewards")) {
            loadRewards(loadFrom.getConfigurationSection("rewards"));
        }
        if(loadFrom.contains("onComplete")){
            loadOnComplete(loadFrom.getConfigurationSection("onComplete"));
        }
        return true;
    }

    public List<Goal> getGoals() {
        return goals;
    }

    public List<Reward> getRewards() {
        return rewards;
    }

    public List<Completion> getCompletions() {
        return this.completions;
    }
    
    public String getName(){
        return this.name;
    }

    private void loadRewards(ConfigurationSection loadFrom) {
        if (loadFrom.contains("money")) {
            this.rewards.add(new Reward(RewardType.MONEY, loadFrom.getInt("money")));
        }
        if (loadFrom.contains("items")) {
            String items = loadFrom.getString("items");
            if(items.contains(";")){
                for(String item : items.split(";")){
                    if(!item.contains(",")){
                        main.getLogger().warning("Unable to load item for achievement" + name + ", no amount set");
                    }else{
                        this.rewards.add(new Reward(RewardType.ITEM, item));
                    }
                }
            }
        }
        if(loadFrom.contains("commands")){
            for(String command : loadFrom.getStringList("commands")){
                this.rewards.add(new Reward(RewardType.COMMAND, command));
            }
        }
    }

    private void loadOnComplete(ConfigurationSection loadFrom) {
        if(loadFrom.contains("messages")){
            for(String message : loadFrom.getStringList("messages")){
                this.completions.add(new Completion(CompletionType.MESSAGE, message));
            }
        }
    }
}