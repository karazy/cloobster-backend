package net.eatsense.representation;

import java.util.Date;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import net.eatsense.domain.Feedback;
import net.eatsense.domain.embedded.FeedbackQuestion;

public class FeedbackDTO {

	@Valid
	@NotNull
	private List<FeedbackQuestion> answers;
	private long businessId;
	private Long checkInId;
	private String comment;
	private Date date;
	private String email;
	private long formId;
	private Long id;

	public FeedbackDTO() {
	}
	
	public FeedbackDTO(Feedback feedback) {
		answers = feedback.getAnswers();
		businessId = feedback.getBusiness().getId();
		if(feedback.getCheckIn() != null)
			checkInId = feedback.getCheckIn().getId();
		comment = feedback.getComment();
		date = feedback.getDate();
		email = feedback.getEmail();
		formId = feedback.getForm().getId();
		id = feedback.getId();
	}

	public List<FeedbackQuestion> getAnswers() {
		return answers;
	}

	public void setAnswers(List<FeedbackQuestion> answers) {
		this.answers = answers;
	}

	public long getBusinessId() {
		return businessId;
	}

	public void setBusinessId(long businessId) {
		this.businessId = businessId;
	}

	public Long getCheckInId() {
		return checkInId;
	}

	public void setCheckInId(Long checkInId) {
		this.checkInId = checkInId;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public long getFormId() {
		return formId;
	}

	public void setFormId(long formId) {
		this.formId = formId;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
}
