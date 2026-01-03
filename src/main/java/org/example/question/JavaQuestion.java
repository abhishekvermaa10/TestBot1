package org.example.question;

public class JavaQuestion extends AbstractQuestion {

    public JavaQuestion() {
        super("What is the size of an `int` in Java in bytes?");
    }

    @Override
    public boolean checkAnswer(String answer) {
        return answer.equals("4");
    }

    @Override
    public String getCorrectAnswer() {
        return "4";
    }
    
}
