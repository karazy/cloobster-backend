/**
 * 
 */
package net.eatsense.controller;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Validator;

import net.eatsense.domain.Business;
import net.eatsense.domain.CheckIn;
import net.eatsense.domain.Feedback;
import net.eatsense.domain.FeedbackForm;
import net.eatsense.domain.embedded.FeedbackQuestion;
import net.eatsense.exceptions.ValidationException;
import net.eatsense.persistence.FeedbackFormRepository;
import net.eatsense.persistence.FeedbackRepository;
import net.eatsense.representation.FeedbackDTO;

import org.apache.bval.guice.ValidationModule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.googlecode.objectify.Key;

/**
 * @author Nils Weiher
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class FeedbackControllerTest {
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	@Mock
	private FeedbackRepository feedbackRepo;
	@Mock
	private FeedbackFormRepository feedbackFormRepo;
	private FeedbackController ctrl;
	@Mock
	private Business business;
	@Mock
	private CheckIn checkIn;
	private long businessId;
	@Mock
	private Key<Business> businessKey;
	@Mock
	private Key<FeedbackForm> formKey;
	@Mock
	private Key<CheckIn> checkInKey;

	@Before
	public void setUp() throws Exception {
		Injector injector = Guice.createInjector(new ValidationModule());
		
		ctrl = new FeedbackController(feedbackFormRepo, feedbackRepo, injector.getInstance(Validator.class));
		
		businessId = 1l;
		when(business.getKey()).thenReturn(businessKey);
		when(checkIn.getKey()).thenReturn(checkInKey);
	}
	
	/**
	 * Test method for {@link net.eatsense.controller.FeedbackController#addFeedback(net.eatsense.domain.Business, net.eatsense.domain.CheckIn, net.eatsense.representation.FeedbackDTO)}.
	 */
	@Test
	public void testAddFeedback() {
		
		FeedbackDTO testFeedbackData = getTestFeedbackData();
		when(feedbackFormRepo.getKey(testFeedbackData.getFormId())).thenReturn(formKey);
		ctrl.addFeedback(business, checkIn, testFeedbackData);
		ArgumentCaptor<Feedback> saveArg = ArgumentCaptor.forClass(Feedback.class);
		verify(feedbackRepo).saveOrUpdate(saveArg.capture());
		Feedback result = saveArg.getValue();
		
		assertThat(result.getAnswers(), is(testFeedbackData.getAnswers()));
		assertThat(result.getBusiness(), is(businessKey));
		assertThat(result.getCheckIn(), is(checkInKey));
		assertThat(result.getComment(), is(testFeedbackData.getComment()));
		assertThat(result.getEmail(), is(testFeedbackData.getEmail()));
		assertThat(result.getForm(), is(formKey));
	}
	
	@Test
	public void testAddFeedbackInvalidAnswer2() {
		FeedbackDTO testFeedbackData = getTestFeedbackData();
		testFeedbackData.getAnswers().get(0).setRating(-1);
		thrown.expect(ValidationException.class);
		thrown.expectMessage("rating");
				
		ctrl.addFeedback(business, checkIn, testFeedbackData);
	}
	
	@Test
	public void testAddFeedbackInvalidAnswer() {
		FeedbackDTO testFeedbackData = getTestFeedbackData();
		testFeedbackData.getAnswers().get(0).setRating(10);
		thrown.expect(ValidationException.class);
		thrown.expectMessage("rating");
				
		ctrl.addFeedback(business, checkIn, testFeedbackData);
	}

	private FeedbackDTO getTestFeedbackData() {
		FeedbackDTO data = new FeedbackDTO();
		List<FeedbackQuestion> answers = new ArrayList<FeedbackQuestion>();
		answers.add(new FeedbackQuestion("Frage1", 1, 1l));
		answers.add(new FeedbackQuestion("Frage2", 2, 2l));
		answers.add(new FeedbackQuestion("Frage3", 0, 2l));
		answers.add(new FeedbackQuestion("Frage4", 5, 2l));
		data.setAnswers(answers );
		data.setComment("comment");
		data.setEmail("email");
		data.setFormId(1l);
		return data;
	}
	
	
}
