package org.example.question;

public class SQLQuestion extends AbstractQuestion{
	
    public SQLQuestion() {
        super("How many main types of relationships exist between tables in SQL?");
    }

    @Override
    public boolean checkAnswer(String answer) {
        return answer.equals("3");
    }

    @Override
    public String getCorrectAnswer() {
        return "3";
    }
    
}
