package net.eatsense.domain;

import java.util.Date;
import java.util.List;

import javax.persistence.Embedded;

import net.eatsense.domain.embedded.FeedbackQuestion;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Parent;

public class Feedback extends GenericEntity<Feedback> {
	@Parent
	private Key<Location> business;
	private Key<CheckIn> checkIn;
	private Key<FeedbackForm> form;
	@Embedded
	private List<FeedbackQuestion> answers;
	
	/**
	 * (optional) customer comment
	 */
	private String comment;
	
	/**
	 * (optional) e-mail for response to the feedback.
	 */
	private String email;
	/**
	 * Date and time of the creation of the feedback entry.
	 */
	private Date date;
	
	public Key<FeedbackForm> getForm() {
		return form;
	}
	public void setForm(Key<FeedbackForm> form) {
		this.form = form;
	}
	public List<FeedbackQuestion> getAnswers() {
		return answers;
	}
	public void setAnswers(List<FeedbackQuestion> answers) {
		this.answers = answers;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public Key<Location> getBusiness() {
		return business;
	}
	public void setBusiness(Key<Location> business) {
		this.business = business;
	}
	public Key<CheckIn> getCheckIn() {
		return checkIn;
	}
	public void setCheckIn(Key<CheckIn> checkIn) {
		this.checkIn = checkIn;
	}
	public Key<Feedback> getKey() {
		return new Key<Feedback>(business, Feedback.class, getId());
	}
}
