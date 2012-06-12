package net.eatsense.domain.embedded;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.apache.bval.constraints.NotEmpty;

public class FeedbackQuestion {
	
	public FeedbackQuestion(String question, Integer rating, Long id) {
		super();
		this.question = question;
		this.rating = rating;
		this.id = id;
	}
	
	public FeedbackQuestion() {
		super();
	}

	@NotNull
	@NotEmpty
	private String question;
	@Min(0)
	@Max(5)
	private Integer rating;
	
	@NotNull
	private Long id;
	
	public String getQuestion() {
		return question;
	}
	public void setQuestion(String question) {
		this.question = question;
	}
	public Integer getRating() {
		return rating;
	}
	public void setRating(Integer rating) {
		this.rating = rating;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
}
