package org.example.question;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public abstract class AbstractQuestion {
	
    private final String question;
    private String correctAnswer;

    public abstract boolean checkAnswer(String answer);
    
}
