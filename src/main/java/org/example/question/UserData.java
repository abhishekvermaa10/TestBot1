package org.example.question;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserData {
	
    private int score;
    private int current;

    public void incrementScore() {
        score++;
    }
    
    public void incrementCurrent() {
        current++;
    }

    public void reset() {
        score = 0;
        current = 0;
    }
    
}
