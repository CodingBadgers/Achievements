/*
 *  Copyright 2013 Lolmewn <info@lolmewn.nl>.
 */

package nl.lolmewn.achievements.goal;

import java.util.Arrays;
import nl.lolmewn.stats.StatType;

/**
 *
 * @author Lolmewn <info@lolmewn.nl>
 */
public class Goal {

    private StatType type;
    private int amount;
    private boolean global;
    private Object[] variables;
    
    public Goal(StatType type, int amount, boolean global, Object[] variables) {
        this.type = type;
        this.amount = amount;
        this.global = global;
        this.variables = variables;
    }

    public int getAmount() {
        return amount;
    }

    public boolean isGlobal() {
        return global;
    }

    public StatType getType() {
        return type;
    }

    public Object[] getVariables() {
        return variables;
    }
    
    @Override
    public String toString(){
        return type.toString() + ", " + amount + ", " + global + ", " + Arrays.toString(variables);
    }

}