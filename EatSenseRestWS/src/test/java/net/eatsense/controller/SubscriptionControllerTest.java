package net.eatsense.controller;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
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
import net.eatsense.domain.Spot;
import net.eatsense.domain.Subscription;
import net.eatsense.domain.embedded.SubscriptionStatus;
import net.eatsense.event.NewPendingSubscription;
import net.eatsense.exceptions.ValidationException;
import net.eatsense.persistence.ObjectifyKeyFactory;
import net.eatsense.persistence.OfyService;
import net.eatsense.representation.SubscriptionDTO;
import net.eatsense.restws.customer.LocationsResource;
import net.eatsense.validation.ValidationHelper;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.base.Optional;
import com.google.common.eventbus.EventBus;
import com.google.inject.Provider;
import com.googlecode.objectify.AsyncObjectify;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.Query;

@RunWith(MockitoJUnitRunner.class)

public class SubscriptionControllerTest {
	private SubscriptionController ctrl;
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	@Mock
	private OfyService ofyService;
	@Mock
	private Objectify ofy;
	@Mock
	private ValidationHelper validator;
	@Mock
	private Provider<Subscription> subscriptionProvider;
	
	@Mock
	private EventBus eventBus;
	@Mock
	private Query<Subscription> query;
	@Mock
	private ObjectifyKeyFactory keyFactory;
	@Mock
	private AsyncObjectify ofyAsync;
	
	@Before
	public void setUp() throws Exception {
		when(ofyService.ofy()).thenReturn(ofy);
		when(ofyService.keys()).thenReturn(keyFactory);
		when(ofy.async()).thenReturn(ofyAsync);
		
		ctrl = new SubscriptionController(ofyService, validator, subscriptionProvider, eventBus);
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
	
	@Test
	public void testUpdateTemplateNoTemplate() throws Exception {
		Subscription subscription = mock(Subscription.class);
		SubscriptionDTO subscriptionData = getTestTemplateData();
		
		thrown.expect(ValidationException.class);
		
		ctrl.updateTemplate(subscription, subscriptionData);
	}
	
	@Test
	public void testUpdateTemplateBasic() throws Exception {
		Subscription subscription = mock(Subscription.class);
		SubscriptionDTO subscriptionData = getTestTemplateData();
		subscriptionData.setBasic(true);
		
		when(subscription.isBasic()).thenReturn(false, true);
		when(subscription.isTemplate()).thenReturn(true);
		when(ofy.query(Subscription.class)).thenReturn(query );
		when(query.filter("template", true)).thenReturn(query);
		when(query.filter("basic", true)).thenReturn(query);
		
		ctrl.updateTemplate(subscription, subscriptionData);
		
		verify(subscription).setBasic(true);
		verify(ofy).put(subscription);
	}
	
	@Test
	public void testUpdateTemplateExistingBasic() throws Exception {
		Subscription subscription = mock(Subscription.class);
		Subscription basicSub = mock(Subscription.class);
		SubscriptionDTO subscriptionData = getTestTemplateData();
		subscriptionData.setBasic(true);
		
		when(subscription.isBasic()).thenReturn(false, true);
		when(subscription.isTemplate()).thenReturn(true);
		when(ofy.query(Subscription.class)).thenReturn(query );
		when(query.filter("template", true)).thenReturn(query);
		when(query.filter("basic", true)).thenReturn(query);
		
		when(query.get()).thenReturn(basicSub);
		
		ctrl.updateTemplate(subscription, subscriptionData);
		verify(basicSub).setBasic(false);
		
		verify(subscription).setBasic(true);
		
		verify(ofy).put(basicSub, subscription);
	}
	
	@Test
	public void testCreateAndSetSubscriptionWithPendingStatus() throws Exception {
		Long businessId = 1l;
		Long templateId = 11l;
		
		Subscription template = mock(Subscription.class);
		when(ofy.get(Subscription.class, templateId)).thenReturn(template );
		@SuppressWarnings("unchecked")
		Key<Business> businessKey = mock(Key.class);
		when(keyFactory.create(Business.class, businessId)).thenReturn(businessKey );
		Subscription newSubscription = mock(Subscription.class);
		when(subscriptionProvider.get()).thenReturn(newSubscription );
		Key<Subscription> newSubKey = mock(Key.class);
		when(newSubscription.getKey()).thenReturn(newSubKey );
		Business business = mock(Business.class);
		when(ofy.get(businessKey)).thenReturn(business );
		
		
		ctrl.createAndSetSubscription(templateId , SubscriptionStatus.PENDING, businessId);
		
		verify(business).setPendingSubscription(newSubKey);
		verify(ofy).put(business);
		verify(ofy).put(newSubscription);
		verify(eventBus).post(any(NewPendingSubscription.class));
	}
	
	@Test
	public void testCreateAndSetSubscriptionWithApprovedStatus() throws Exception {
		Long businessId = 1l;
		Long templateId = 11l;
		
		Subscription template = mock(Subscription.class);
		when(ofy.get(Subscription.class, templateId)).thenReturn(template );
		@SuppressWarnings("unchecked")
		Key<Business> businessKey = mock(Key.class);
		when(keyFactory.create(Business.class, businessId)).thenReturn(businessKey );
		Subscription newSubscription = mock(Subscription.class);
		when(subscriptionProvider.get()).thenReturn(newSubscription );
		Key<Subscription> newSubKey = mock(Key.class);
		when(newSubscription.getKey()).thenReturn(newSubKey );
		Business business = mock(Business.class);
		when(ofy.get(businessKey)).thenReturn(business );
		Key<Subscription> oldPendingKey = mock(Key.class);
		when(business.getPendingSubscription()).thenReturn(oldPendingKey );
		Subscription oldPendingSub = mock(Subscription.class);
		when(ofy.get(oldPendingKey)).thenReturn(oldPendingSub);
		
		
		ctrl.createAndSetSubscription(templateId , SubscriptionStatus.APPROVED, businessId);
		
		verify(business).setActiveSubscription(newSubKey);
		verify(business).setPendingSubscription(null);
		verify(ofy).put(business);
		verify(ofy).put(newSubscription);
		verify(oldPendingSub).setStatus(SubscriptionStatus.CANCELED);
		verify(ofyAsync).put(oldPendingSub);
	}
	
	@Test
	public void testCreateSubscriptionFromTemplateApproved() throws Exception {
		Subscription newSubscription = mock(Subscription.class);
		when(subscriptionProvider.get()).thenReturn(newSubscription );
		Subscription template = mock(Subscription.class);
		@SuppressWarnings("unchecked")
		Key<Business> businessKey = mock(Key.class);
		
		ctrl.createSubscriptionFromTemplate(template, SubscriptionStatus.APPROVED, businessKey);
		ArgumentCaptor<Date> captor = ArgumentCaptor.forClass(Date.class);
		verify(newSubscription).setStartDate(captor.capture());
		assertThat(captor.getValue(), lessThanOrEqualTo(new Date()));
		verify(ofy).put(newSubscription);
	}
	
	@Test
	public void testSetActiveSubscription() throws Exception {
		Subscription newActiveSub = mock(Subscription.class);
		Business location = mock(Business.class);
		Integer spotCount = 100;
		Key<Subscription> oldActiveSubKey = mock(Key.class);
		when(location.getActiveSubscription()).thenReturn(oldActiveSubKey );
		when(location.getSpotCount()).thenReturn(null, spotCount);
		Subscription oldActiveSub = mock(Subscription.class);
		when(ofy.find(oldActiveSubKey)).thenReturn(oldActiveSub );
		Key<Subscription> newSubKey = mock(Key.class);
		when(newActiveSub.getKey()).thenReturn(newSubKey );
		Query<Spot> spotQuery = mock(Query.class);
		when(ofy.query(Spot.class)).thenReturn(spotQuery );
		Key<Business> locationKey = mock(Key.class);
		when(location.getKey()).thenReturn(locationKey );
		when(spotQuery.ancestor(locationKey)).thenReturn(spotQuery);
		
		when(spotQuery.filter("trash", false)).thenReturn(spotQuery);
		
		when(spotQuery.count()).thenReturn(spotCount );
		
		ctrl.setActiveSubscription(location , Optional.of(newActiveSub ), true);
		
		// Verify archiving of old active subscription
		verify(oldActiveSub).setStatus(SubscriptionStatus.ARCHIVED);
		verify(oldActiveSub).setEndData(any(Date.class));
		verify(ofyAsync).put(oldActiveSub);
		
		verify(location).setBasic(false);
		verify(location).setActiveSubscription(newSubKey);
		verify(location).setSpotCount(spotCount);
		
		verify(ofy).put(location);
	}	

	@Test
	public void testSetPendingSubscription() throws Exception {
		Key<Subscription> newPendingSubKey = mock(Key.class);
		Subscription newPendingSub = when(mock(Subscription.class).getKey()).thenReturn(newPendingSubKey ).getMock();
		Business location = mock(Business.class);
		Key<Subscription> oldSubKey = mock(Key.class);
		when(location.getPendingSubscription()).thenReturn(oldSubKey );

		ctrl.setPendingSubscription(location, newPendingSub, true);
		
		verify(location).setPendingSubscription(newPendingSubKey);
		verify(ofy).put(location);
		verify(ofyAsync).delete(oldSubKey);
	}
	
	@Test
	public void testSetBasicSubscriptions() throws Exception {
		Business location = mock(Business.class);
		Key<Business> locationKey = mock(Key.class);
		Subscription newSub = mock(Subscription.class);
		Key<Subscription> newSubKey = mock(Key.class);
		when(newSub.getKey()).thenReturn(newSubKey );
		when(subscriptionProvider.get()).thenReturn(newSub );
		when(location.getKey()).thenReturn(locationKey );
		when(ofy.query(Subscription.class)).thenReturn(query);
		when(query.filter("template", true)).thenReturn(query);
		when(query.filter("basic", true)).thenReturn(query);
		Subscription basicSub = mock(Subscription.class);
		when(query.get()).thenReturn(basicSub );
		
		ctrl.setBasicSubscription(location);
		
		verify(ofy).put(newSub);
		verify(location).setActiveSubscription(newSubKey);
	}
}
