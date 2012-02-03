package net.eatsense.integration;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.get;
import static org.hamcrest.Matchers.equalTo;

public class BasicIntegrationTest {
	String barcode;
	@Before
	public void setUp() throws Exception {
		barcode = "hup001";
		
	}
	
	@Test
	public void basicCheckInTest() {
		
		expect().body("status", equalTo("INTENT")).when().get("/restaurant/spot/" + barcode);
	}
	
	@After
	public void tearDown() throws Exception {
		
	}
}
