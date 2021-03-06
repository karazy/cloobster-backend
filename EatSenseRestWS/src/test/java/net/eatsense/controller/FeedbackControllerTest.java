/**
 * 
 */
package net.eatsense.controller;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Validator;

import net.eatsense.domain.Business;
import net.eatsense.domain.CheckIn;
import net.eatsense.domain.Feedback;
import net.eatsense.domain.FeedbackForm;
import net.eatsense.domain.Spot;
import net.eatsense.domain.embedded.FeedbackQuestion;
import net.eatsense.event.NewFeedbackEvent;
import net.eatsense.exceptions.NotFoundException;
import net.eatsense.exceptions.ValidationException;
import net.eatsense.persistence.CheckInRepository;
import net.eatsense.persistence.FeedbackFormRepository;
import net.eatsense.persistence.FeedbackRepository;
import net.eatsense.persistence.SpotRepository;
import net.eatsense.representation.FeedbackDTO;
import net.eatsense.validation.ValidationHelper;

import org.apache.bval.guice.ValidationModule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.eventbus.EventBus;
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
	
	@Mock
	private CheckInRepository checkInRepo;

	@Mock
	private SpotRepository spotRepo;

	@Mock
	private EventBus eventBus;

	@Before
	public void setUp() throws Exception {
		Injector injector = Guice.createInjector(new ValidationModule());
		
		ctrl = new FeedbackController(checkInRepo, feedbackFormRepo, feedbackRepo, injector.getInstance(ValidationHelper.class), spotRepo, eventBus);
		
		businessId = 1l;
		when(business.getKey()).thenReturn(businessKey);
		when(checkIn.getKey()).thenReturn(checkInKey);
	}
	
	/**
	 * Test method for {@link net.eatsense.controller.FeedbackController#createFeedback(net.eatsense.domain.Business, net.eatsense.domain.CheckIn, net.eatsense.representation.FeedbackDTO)}.
	 */
	@Test
	public void testAddFeedback() {
		
		FeedbackDTO testFeedbackData = getTestFeedbackData();
		when(feedbackFormRepo.getKey(testFeedbackData.getFormId())).thenReturn(formKey);
		Feedback feedback = new Feedback();
		Key<Spot> spotKey = mock(Key.class);
		when(checkIn.getSpot()).thenReturn(spotKey );
		Spot spot = mock(Spot.class);
		when(spot.isWelcome()).thenReturn(false);
		when(spotRepo.getByKey(spotKey)).thenReturn(spot );
		when(feedbackRepo.newEntity()).thenReturn(feedback );
		ctrl.createFeedback(business, checkIn, testFeedbackData);
		verify(feedbackRepo).saveOrUpdate(feedback);
		
		ArgumentCaptor<NewFeedbackEvent> eventCaptor = ArgumentCaptor.forClass(NewFeedbackEvent.class);
		
		verify(eventBus,times(2)).post(eventCaptor.capture());
		NewFeedbackEvent event = eventCaptor.getValue();
		assertThat(event.getCheckIn(), is(checkIn));
		assertThat(event.getFeedback(), is(feedback));
		
		assertThat(feedback.getAnswers(), is(testFeedbackData.getAnswers()));
		assertThat(feedback.getBusiness(), is(businessKey));
		assertThat(feedback.getCheckIn(), is(checkInKey));
		assertThat(feedback.getComment(), is(testFeedbackData.getComment()));
		assertThat(feedback.getEmail(), is(testFeedbackData.getEmail()));
		assertThat(feedback.getForm(), is(formKey));
	}
	
	@Test
	public void testUpdateFeedbackUnknownId() {
		FeedbackDTO testFeedbackData = getTestFeedbackData();
		Feedback feedback = new Feedback();
		Key<Feedback> feedbackKey = mock(Key.class);
		Long feedbackId = 1l;
		testFeedbackData.setId(feedbackId);
		when(feedbackKey.getId()).thenReturn(2l );
		when(checkIn.getFeedback()).thenReturn(feedbackKey );
		when(feedbackRepo.getByKey(feedbackKey)).thenReturn(feedback);
		
		thrown.expect(NotFoundException.class);
		
		ctrl.updateFeedback(checkIn, testFeedbackData);
	}
	
	@Test
	public void testUpdateFeedback() {
		FeedbackDTO testFeedbackData = getTestFeedbackData();
		Feedback feedback = new Feedback();
		Key<Feedback> feedbackKey = mock(Key.class);
		Long feedbackId = 1l;
		testFeedbackData.setId(feedbackId);
		when(feedbackKey.getId()).thenReturn(feedbackId );
		when(checkIn.getFeedback()).thenReturn(feedbackKey );
		when(feedbackRepo.getByKey(feedbackKey)).thenReturn(feedback);
		
		ctrl.updateFeedback(checkIn, testFeedbackData);
	
		verify(feedbackRepo).saveOrUpdate(feedback);
		
		assertThat(feedback.getAnswers(), is(testFeedbackData.getAnswers()));
		assertThat(feedback.getComment(), is(testFeedbackData.getComment()));
		assertThat(feedback.getEmail(), is(testFeedbackData.getEmail()));
	}
	
	@Test
	public void testAddFeedbackInvalidFormId() {
		Feedback feedback = new Feedback();
		when(feedbackRepo.newEntity()).thenReturn(feedback );

		FeedbackDTO testFeedbackData = getTestFeedbackData();
		testFeedbackData.setFormId(0);
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("formId");
				
		ctrl.createFeedback(business, checkIn, testFeedbackData);
	}
	
	@Test
	public void testAddFeedbackInvalidAnswer2() {
		Feedback feedback = new Feedback();
		when(feedbackRepo.newEntity()).thenReturn(feedback );
		Key<Spot> spotKey = mock(Key.class);
		when(checkIn.getSpot()).thenReturn(spotKey );
		Spot spot = mock(Spot.class);
		when(spot.isWelcome()).thenReturn(false);
		when(spotRepo.getByKey(spotKey)).thenReturn(spot );


		FeedbackDTO testFeedbackData = getTestFeedbackData();
		testFeedbackData.getAnswers().get(0).setRating(-1);
		thrown.expect(ValidationException.class);
		thrown.expectMessage("rating");
		
		
		ctrl.createFeedback(business, checkIn, testFeedbackData);
	}
	
	@Test
	public void testAddFeedbackInvalidAnswer() {
		Feedback feedback = new Feedback();
		when(feedbackRepo.newEntity()).thenReturn(feedback );
		Key<Spot> spotKey = mock(Key.class);
		when(checkIn.getSpot()).thenReturn(spotKey );
		Spot spot = mock(Spot.class);
		when(spot.isWelcome()).thenReturn(false);
		when(spotRepo.getByKey(spotKey)).thenReturn(spot );


		FeedbackDTO testFeedbackData = getTestFeedbackData();
		testFeedbackData.getAnswers().get(0).setRating(10);
		thrown.expect(ValidationException.class);
		thrown.expectMessage("rating");
				
		ctrl.createFeedback(business, checkIn, testFeedbackData);
	}

	private FeedbackDTO getTestFeedbackData() {
		FeedbackDTO data = new FeedbackDTO();
		List<FeedbackQuestion> answers = new ArrayList<FeedbackQuestion>();
		answers.add(new FeedbackQuestion("Frage1", 1, 1l));
		answers.add(new FeedbackQuestion("Frage2", 2, 2l));
		answers.add(new FeedbackQuestion("Frage3", 0, 3l));
		answers.add(new FeedbackQuestion("Frage4", 5, 4l));
		data.setAnswers(answers );
		data.setComment("comment");
		data.setEmail("email");
		data.setFormId(1l);
		return data;
	}
	
	
}
