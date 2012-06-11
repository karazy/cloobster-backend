package net.eatsense.domain;

import java.util.ArrayList;
import java.util.List;

import net.eatsense.domain.embedded.FeedbackQuestion;

public class FeedbackForm extends GenericEntity {
	
	List<FeedbackQuestion> questions;
	
	String title;
	String description;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<FeedbackQuestion> getQuestions() {
		if(questions == null) {
			questions = new ArrayList<FeedbackQuestion>();
		}
		return questions;
	}

	public void setQuestions(List<FeedbackQuestion> questions) {
		this.questions = questions;
	}
}
