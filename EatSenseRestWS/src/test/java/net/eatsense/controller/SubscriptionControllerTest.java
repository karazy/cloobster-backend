package net.eatsense.controller;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;

import net.eatsense.domain.Business;
import net.eatsense.domain.Subscription;
import net.eatsense.domain.embedded.SubscriptionStatus;
import net.eatsense.persistence.OfyService;
import net.eatsense.representation.SubscriptionDTO;
import net.eatsense.validation.ValidationHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.inject.Provider;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.Query;

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

	private SubscriptionDTO getTestTemplateData() {
		
		SubscriptionDTO sub = new SubscriptionDTO();
		sub.setName("Test Package");
		sub.setMaxSpotCount(100);
		
		return sub ;
	}
	
	private SubscriptionDTO getTestSubscriptionData() {
		
		SubscriptionDTO sub = new SubscriptionDTO();
		sub.setName("Test Package");
		sub.setStatus(SubscriptionStatus.APPROVED);
		sub.setBusinessId(1l);
		sub.setMaxSpotCount(100);
		
		return sub ;
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
	
	@Test
	public void testUpdateSubscriptionPendingToApproved() throws Exception {
		SubscriptionDTO subscriptionData = getTestSubscriptionData();
		Subscription subscription = mock(Subscription.class);
		Business location = mock(Business.class);
		
		Key<Business> businessKey = mock(Key.class);
		when(subscription.getBusiness()).thenReturn(businessKey );
		Key<Subscription> subscriptionKey = mock(Key.class);
		when(subscription.getKey()).thenReturn(subscriptionKey );
		when(subscription.getStatus()).thenReturn(SubscriptionStatus.PENDING);
		
		
		when(ofy.get(businessKey)).thenReturn(location );
				
		ctrl.updateSubscription(subscription, subscriptionData );
		verify(subscription).setStartDate(any(Date.class));
		verify(ofy).put(subscription);
		verify(ofy).put(location);
		verify(location).setActiveSubscription(subscriptionKey);
		
	}
	
	@Test
	public void testUpdateSubscriptionPendingToCanceled() throws Exception {
		SubscriptionDTO subscriptionData = getTestSubscriptionData();
		subscriptionData.setStatus(SubscriptionStatus.CANCELED);
		
		Subscription subscription = mock(Subscription.class);
		Business location = mock(Business.class);
		
		
		Key<Business> businessKey = mock(Key.class);
		when(subscription.getBusiness()).thenReturn(businessKey );
		Key<Subscription> subscriptionKey = mock(Key.class);
		when(subscription.getKey()).thenReturn(subscriptionKey );
		when(subscription.getStatus()).thenReturn(SubscriptionStatus.PENDING);
		when(location.getPendingSubscription()).thenReturn(subscriptionKey);
		
		when(ofy.get(businessKey)).thenReturn(location );
				
		ctrl.updateSubscription(subscription, subscriptionData );
		verify(subscription, atLeastOnce()).setStatus(SubscriptionStatus.CANCELED);
		verify(ofy).put(subscription);
		verify(ofy).put(location);
		verify(location).setPendingSubscription(null);
		
	}
	
	@Test
	public void testUpdateSubscriptionApprovedToArchived() throws Exception {
		SubscriptionDTO subscriptionData = getTestSubscriptionData();
		subscriptionData.setStatus(SubscriptionStatus.ARCHIVED);
		
		Subscription subscription = mock(Subscription.class);
		Business location = mock(Business.class);
		
		
		Key<Business> businessKey = mock(Key.class);
		when(location.getKey()).thenReturn(businessKey);
		when(subscription.getBusiness()).thenReturn(businessKey );
		Key<Subscription> subscriptionKey = mock(Key.class);
		when(subscription.getKey()).thenReturn(subscriptionKey );
		when(subscription.getStatus()).thenReturn(SubscriptionStatus.APPROVED);
		when(location.getActiveSubscription()).thenReturn(subscriptionKey);
		Query<Subscription> basicSubQuery = mock(Query.class);
		when(ofy.query(Subscription.class)).thenReturn(basicSubQuery );
		when(basicSubQuery.filter("basic", true)).thenReturn(basicSubQuery);
		when(basicSubQuery.filter("template", true)).thenReturn(basicSubQuery);
		Subscription basicSubscription = mock(Subscription.class);
		when(basicSubQuery.get()).thenReturn(basicSubscription );
		Subscription newBasicSubscription = mock(Subscription.class);
		when(subscriptionProvider.get()).thenReturn(newBasicSubscription );
		Key<Subscription> newBasicKey = mock(Key.class);
		when(newBasicSubscription.getKey()).thenReturn(newBasicKey );
		
		when(ofy.get(businessKey)).thenReturn(location );
				
		ctrl.updateSubscription(subscription, subscriptionData );
		verify(subscription, atLeastOnce()).setStatus(SubscriptionStatus.ARCHIVED);
		verify(ofy).put(subscription);
		verify(ofy).put(location);
		verify(location).setActiveSubscription(newBasicKey);
		
	}
}
