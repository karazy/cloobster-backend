package net.eatsense.domain;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Embedded;
import javax.persistence.Transient;

import org.codehaus.jackson.annotate.JsonIgnore;

import com.googlecode.objectify.Key;

import net.eatsense.domain.embedded.FeedbackQuestion;

public class FeedbackForm extends GenericEntity<FeedbackForm> {
	@Embedded
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
	
	@Transient
	@JsonIgnore
	public Key<FeedbackForm> getKey() {
		
		return getKey(super.getId());
	}
	
	@Transient
	@JsonIgnore
	public static Key<FeedbackForm> getKey(long id) {
		return new Key<FeedbackForm>(FeedbackForm.class, id);
	}
}
