package net.eatsense.service;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

import java.net.URI;
import java.net.URLEncoder;

import net.eatsense.controller.SpotController;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class QRCodeGeneratorServiceTest {
	
	
	private QRCodeGeneratorService service;

	@Before
	public void setUp() throws Exception {
		service = new QRCodeGeneratorService();
	}
	
	@Test
	public void testBuildUri() throws Exception {
		 URI uri = service.buildUri("test1", 150, 150);
		 String qrImageUrl = "https://chart.googleapis.com/chart?chl=" + URLEncoder.encode("test1","UTF-8") + "&chs=150x150&cht=qr";
		 URI testUri = new URI(qrImageUrl);
		 assertThat(uri, is(testUri));
	}
}
