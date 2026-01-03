package org.example.question;

public class GitQuestion extends AbstractQuestion {

	public GitQuestion() {
		super("Which Git command shows who edited each line of a file?");
	}

	@Override
	public boolean checkAnswer(String answer) {
		return answer.contains("blame");
	}

	@Override
	public String getCorrectAnswer() {
		return "git blame";
	}

}
