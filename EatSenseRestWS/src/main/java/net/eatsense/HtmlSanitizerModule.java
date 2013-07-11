package net.eatsense;

import org.owasp.html.AttributePolicy;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class HtmlSanitizerModule extends AbstractModule {

	@Override
	protected void configure() {

	}

	@Provides
	@Singleton
	public PolicyFactory providesHTMLPolicyFactory() {
		return new HtmlPolicyBuilder().allowCommonBlockElements().allowCommonInlineFormattingElements()
         .allowAttributes("style").matching(AttributePolicy.IDENTITY_ATTRIBUTE_POLICY).globally().toFactory();
	}
}
