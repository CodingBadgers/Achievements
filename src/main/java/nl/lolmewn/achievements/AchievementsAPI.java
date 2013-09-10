package nl.lolmewn.achievements;

/**
 * @author Lolmewn
 */
public class AchievementsAPI {
    
    private Main m;
    
    protected AchievementsAPI(Main m){
        this.m = m;
    }
    
    public Main getPlugin(){
        return m;
    }
    
    public int findNextFreeId(){
        return m.getAchievementManager().getNextFreeId();
    }
    
    public void addAchievement(Achievement ach){
        m.getAchievementManager().addAchievement(ach.getId(), ach);
    }

}
