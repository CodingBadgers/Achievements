/*
 *  Copyright 2013 Lolmewn <info@lolmewn.nl>.
 */

package nl.lolmewn.achievements.completion;

/**
 *
 * @author Lolmewn <info@lolmewn.nl>
 */
public class Completion {

    private final CompletionType type;
    private final String value;
    
    public Completion(CompletionType type, String value) {
        this.type = type;
        this.value = value;
    }
    
    public CompletionType getType(){
        return type;
    }
    
    public String getValue(){
        return value;
    }

}
