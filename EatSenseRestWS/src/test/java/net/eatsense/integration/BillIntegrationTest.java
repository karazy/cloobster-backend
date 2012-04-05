package net.eatsense.integration;

import static com.jayway.restassured.RestAssured.expect;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jayway.restassured.RestAssured;

public class BillIntegrationTest {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	String barcode;
	String server;
	private String checkInId;
	private long businessId;
	private ObjectMapper jsonMapper;

	@Before
	public void setUp() throws Exception {
		barcode = "hup001";
		RestAssured.baseURI = "http://localhost";
		RestAssured.port = 8080;
		
		this.jsonMapper = new ObjectMapper();
	}
	
	
	@After
	public void tearDown() throws Exception {
		// delete live data
		expect().statusCode(204).when().delete("/c/businesses/livedata");
	}
}
