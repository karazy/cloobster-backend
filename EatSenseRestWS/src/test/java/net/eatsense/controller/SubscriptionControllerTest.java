package net.eatsense.controller;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.*;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import net.eatsense.domain.Subscription;
import net.eatsense.domain.embedded.SubscriptionStatus;
import net.eatsense.persistence.OfyService;
import net.eatsense.representation.SubscriptionDTO;
import net.eatsense.validation.ValidationHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.owasp.html.Sanitizers;

import com.google.inject.Provider;
import com.googlecode.objectify.Objectify;

@RunWith(MockitoJUnitRunner.class)

public class SubscriptionControllerTest {
	private SubscriptionController ctrl;
	@Mock
	private OfyService ofyService;
	@Mock
	private Objectify ofy;
	@Mock
	private ValidationHelper validator;
	@Mock
	private Provider<Subscription> subscriptionProvider;
	
	@Before
	public void setUp() throws Exception {
		when(ofyService.ofy()).thenReturn(ofy);
		
		ctrl = new SubscriptionController(ofyService, validator, subscriptionProvider);
	}
	
	@Test
	public void testCreateAndSaveTemplate() throws Exception {
		SubscriptionDTO testTemplateData = getTestTemplateData();
		Subscription newSub = new Subscription();
		when(subscriptionProvider.get()).thenReturn(newSub );
		ctrl.createAndSaveTemplate(testTemplateData );
		
		assertThat(newSub.isTemplate(), is(true));
		assertThat(newSub.getStatus(), nullValue(SubscriptionStatus.class));
		verify(ofy).put(newSub);
	}

	private SubscriptionDTO getTestTemplateData() {
		
		SubscriptionDTO sub = new SubscriptionDTO();
		sub.setName("Test Package");
		sub.setMaxSpotCount(100);
		
		return sub ;
	}
}
