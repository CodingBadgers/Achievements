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

/**
 *
 * @author Lolmewn <info@lolmewn.nl>
 */
public class EventListener implements Listener{
    
    private Main plugin;
    private Economy economy;
    
    public EventListener(Main m) {
        plugin = m;
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onStatUpdate(StatUpdateEvent event){
        Player player = plugin.getServer().getPlayerExact(event.getPlayer().getPlayername());
        AchievementPlayer aPlayer = plugin.getPlayerManager().getPlayer(player.getName());
        for(Achievement ach : plugin.getAchievementManager().getAchievements()){
            if(aPlayer.hasCompletedAchievement(ach.getId())){
                continue;
            }
            boolean cont = false;
            for(Goal g : ach.getGoals()){
                if(!event.getStatType().equals(g.getType())){
                    cont = true;
                    break;
                }
                if(g.isGlobal()){
                    int totalValue = 0;
                    for(Object[] vars : event.getStat().getAllVariables()){
                        if(event.getStatType().equals(StatType.MOVE)){
                            totalValue+= event.getStat().getValueDouble(vars);
                        }else{
                            totalValue+= event.getStat().getValue(vars);
                        }
                    }
                    totalValue+=event.getUpdateValue();
                    if(g.getAmount() > totalValue){
                        cont = true;
                        break;
                    }
                }else{
                    if(event.getNewValue() < g.getAmount()){
                        cont = true;
                        break;
                    }
                    if(!Arrays.toString(event.getVars()).equalsIgnoreCase(Arrays.toString(g.getVariables()))){
                        cont = true;
                        break;
                    }
                }
            }
            if(cont){
                continue;
            }
            aPlayer.markAsCompleted(ach.getId());
            boolean invFullMessage = false;
            for(Reward reward : ach.getRewards()){
                switch(reward.getRewardType()){
                    case COMMAND:
                        player.performCommand(reward.getStringValue().replace("%player%", aPlayer.getPlayername()).replace("%name%", ach.getName()));
                        break;
                    case ITEM:
                        String itemString = reward.getStringValue();
                        String item = itemString.split(",")[0];
                        int amount = Integer.parseInt(itemString.split(",")[1]);
                        ItemStack stack;
                        if(item.contains(".")){
                            stack = new ItemStack(Material.getMaterial(Integer.parseInt(item.split(".")[0])), amount, Short.parseShort(item.split(".")[1]));
                        }else{
                            stack = new ItemStack(Material.getMaterial(Integer.parseInt(item)), amount);
                        }
                        if(!player.getInventory().addItem(stack).isEmpty()){
                            player.getWorld().dropItem(player.getLocation(), stack);
                            if(!invFullMessage){
                                player.sendMessage(ChatColor.GREEN + "Inventory full, item dropped on the ground.");
                                invFullMessage = true;
                            }                            
                        }
                        break;
                    case MONEY:
                        if(setupEconomy()){
                            economy.depositPlayer(aPlayer.getPlayername(), reward.getIntValue());
                        }
                }
            }
            for(Completion com : ach.getCompletions()){
                switch(com.getType()){
                    case MESSAGE:
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', com.getValue()));
                        break;
                }
            }
        }
    }
    
    @EventHandler
    public void onLogin(PlayerLoginEvent event){
        plugin.getPlayerManager().loadPlayer(event.getPlayer().getName());
    }
    
    @EventHandler
    public void onLogout(PlayerQuitEvent event){
        plugin.getPlayerManager().savePlayer(event.getPlayer().getName(), true);
    }
    
    public boolean setupEconomy() {
        if (economy != null) {
            return true;
        }
        RegisteredServiceProvider<Economy> economyProvider = this.plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }
        return (economy != null);
    }

}
