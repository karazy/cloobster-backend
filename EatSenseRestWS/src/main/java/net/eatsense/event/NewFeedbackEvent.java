package net.eatsense.event;

import net.eatsense.domain.Feedback;

import net.eatsense.domain.CheckIn;

public class NewFeedbackEvent {
	private final CheckIn checkIn;
	private final Feedback feedback;
	
	public NewFeedbackEvent(CheckIn checkIn, Feedback feedback) {
		super();
		this.checkIn = checkIn;
		this.feedback = feedback;
	}
	
	public CheckIn getCheckIn() {
		return checkIn;
	}
	
	public Feedback getFeedback() {
		return feedback;
	}
}
