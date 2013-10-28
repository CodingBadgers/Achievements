/*
 *  Copyright 2013 Lolmewn <info@lolmewn.nl>.
 */

package nl.lolmewn.achievements.reward;

/**
 *
 * @author Lolmewn <info@lolmewn.nl>
 */
public class Reward {

    private final RewardType type;
    private String stringValue;
    private int intValue;
    
    public Reward(RewardType type, String value){
        this.type = type;
        this.stringValue = value;
    }
    
    public Reward(RewardType type, int value) {
        this.type = type;
        this.intValue = value;
    }
    
    public int getIntValue(){
        return intValue;
    }
    
    public String getStringValue(){
        return stringValue;
    }
    
    public RewardType getRewardType(){
        return type;
    }

}
