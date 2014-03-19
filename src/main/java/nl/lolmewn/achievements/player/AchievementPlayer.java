/*
 *  Copyright 2013 Lolmewn <info@lolmewn.nl>.
 */

package nl.lolmewn.achievements.player;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Lolmewn <info@lolmewn.nl>
 */
public class AchievementPlayer {    
    
    private final String name;
    private final List<Integer> completedAchievements = new ArrayList<Integer>();
    
    public AchievementPlayer(String name) {
        this.name = name;
    }
    
    public String getPlayername(){
        return name;
    }
    
    public boolean hasCompletedAchievement(int id){
        return this.completedAchievements.contains(id);
    }
    
    public void markAsCompleted(int id){
        this.completedAchievements.add(id);
    }

    public List<Integer> getCompletedAchievements() {
        return this.completedAchievements;
    }

}
