package net.eatsense.integration;

import static com.jayway.restassured.RestAssured.expect;

import org.junit.After;

import com.jayway.restassured.RestAssured;

public class RestIntegrationTest {

	public RestIntegrationTest() {
		super();
		RestAssured.baseURI = "http://localhost/";
		RestAssured.port = 80;
	}
	
	
	@After
	public void tearDown() throws Exception {
		// delete live data
		//expect().statusCode(204).when().delete("/c/businesses/livedata");
	}
}