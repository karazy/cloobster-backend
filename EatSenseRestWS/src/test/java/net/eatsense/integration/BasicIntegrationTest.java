package net.eatsense.integration;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class BasicIntegrationTest {
	String barcode;
	@Before
	public void setUp() throws Exception {
		barcode = "hup001";
		
	}
	
	@Test
	public void basicCheckInTest() {
		
		//expect().body("status", equalTo("INTENT")).when().get("/restaurant/spot/" + barcode);
	}
	
	@After
	public void tearDown() throws Exception {
		
	}
}
