package net.eatsense.representation;

import java.util.List;

import com.google.common.base.Function;

import net.eatsense.domain.FeedbackForm;
import net.eatsense.domain.Spot;
import net.eatsense.domain.embedded.FeedbackQuestion;

public class FeedbackFormDTO {
	private String title;
	private List<FeedbackQuestion> questions;
	private String description;
	private Long id;

	public FeedbackFormDTO() {
	}
	
	public FeedbackFormDTO(FeedbackForm feedbackForm) {
		this.id = feedbackForm.getId();
		this.description = feedbackForm.getDescription();
		this.questions = feedbackForm.getQuestions();
		this.title = feedbackForm.getTitle();
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public List<FeedbackQuestion> getQuestions() {
		return questions;
	}

	public void setQuestions(List<FeedbackQuestion> questions) {
		this.questions = questions;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public final static Function<FeedbackForm, FeedbackFormDTO> toDTO = 
		new Function<FeedbackForm, FeedbackFormDTO>() {
			@Override
			public FeedbackFormDTO apply(FeedbackForm input) {
				return new FeedbackFormDTO(input);
			}
	    };
}
