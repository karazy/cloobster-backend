package net.eatsense;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

public class EatSenseGuiceServletContextListener extends GuiceServletContextListener{

//	@Override
//	protected Injector getInjector() {
//		return Guice.createInjector(new EatSenseDomainModule());
//	}
//	
	 @Override
	   protected Injector getInjector() {
	      return Guice.createInjector(new JerseyServletModule() {
	         @Override
	         protected void configureServlets() {
	            
	            // Route all requests through GuiceContainer
	            serve("/*").with(GuiceContainer.class);
	         }
	      }, new EatSenseDomainModule());
	   }

}
