package net.eatsense.dataimport.giata;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import com.google.appengine.api.datastore.GeoPt;
import com.google.common.io.Files;

public class GiataMulticodeDocumentParserTest {

	private String testFileName;
	private GiataMulticodeDocumentParser parser;

	@Before
	public void setUp() throws Exception {
		testFileName = "src/test/resources/giata-test-data.xml";
		parser = new GiataMulticodeDocumentParser();
	}

	@Test
	public void testReadFromStream() throws Exception {
		
		GiataPropertyData data = parser.readFromStream(Files.newInputStreamSupplier(new File(testFileName)).getInput());
		
		assertThat(data.getGiataId(), is(23051l));
		assertThat(data.getCity(), is("Berlin"));
		assertThat(data.getAddress(), is("Friedrichstraße 158-164"));
		assertThat(data.getName(), is("The Westin Grand Berlin"));
		assertThat(data.getPhone(), is("+493020270"));
		assertThat(data.getEmail(), is("info@westin-grand.com"));
		assertThat(data.getUrl(), is("http://aktuelles.westin.de/berlin/1/111"));
		GeoPt geoPt = new GeoPt(Float.parseFloat("52.515819335265"), Float.parseFloat("13.388605713844"));
		assertThat(data.getGeoCode(), is(geoPt));
	}

}
