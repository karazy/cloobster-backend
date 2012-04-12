package net.eatsense.integration;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;


import net.eatsense.domain.embedded.CheckInStatus;
import net.eatsense.representation.CheckInDTO;
import net.eatsense.representation.CustomerRequestDTO;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;

public class RequestIntegrationTest {
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
	
	@Test
	public void testCheckInAndRequest() {
		// #1.1 Get spot information
		Response response = expect().statusCode(200)
				.body("name", equalTo("Tisch 1"))
				.body("business", equalTo("Heidi und Paul")).when()
				.get("/spots/{barcode}", barcode);

		businessId = response.getBody().jsonPath().getLong("businessId");
		
		// Create checkin data to send to server ...
		CheckInDTO checkInData = new CheckInDTO();
		checkInData.setSpotId(barcode);
		checkInData.setStatus(CheckInStatus.INTENT);
		checkInData.setNickname("TestNils");

		// #1.2 Do a checkin and check for correctness, update the checkin data
		// from the response.
		response = given().contentType("application/json")
				.body(checkInData).expect().statusCode(200)
				.body("userId", notNullValue())
				.body("status", equalTo(CheckInStatus.CHECKEDIN.toString()))
				.when().post("c/checkins");
		checkInData = response.as(CheckInDTO.class);
		checkInId = checkInData.getUserId();
		
		// #2 Post a waiter request
		CustomerRequestDTO requestData = new CustomerRequestDTO();
		
		requestData.setType("CALL_WAITER");
		response = given().contentType("application/json")
				.body(requestData).expect().statusCode(200)
				.body("type", equalTo("CALL_WAITER"))
				.body("id", notNullValue())
				.when().post("c/checkins/{id}/requests", checkInId);
		requestData = response.as(CustomerRequestDTO.class);
		
		// #3.1 get a waiter request
		response = given().contentType("application/json")
				.queryParam("checkInId", requestData.getCheckInId())
				.headers("login","admin","password","test")
				.expect().statusCode(200)
				.when().get("/b/businesses/{id}/requests", businessId);

		requestData = response.jsonPath().getObject("find {r-> r.checkInId == "+requestData.getCheckInId()+" }", CustomerRequestDTO.class);
		assertThat(requestData.getType(), equalTo("CALL_WAITER"));
		assertThat(requestData.getId(), notNullValue());
		
		// #3.2 delete the waiter request
		response = given().contentType("application/json")
				.headers("login","admin","password","test")
				.expect().statusCode(204)
				.when().delete("/b/businesses/{id}/requests/{requestid}", businessId, requestData.getId());
		
	}
	
	
	@After
	public void tearDown() throws Exception {
		// delete live data
		expect().statusCode(204).when().delete("/c/businesses/livedata");
	}
}
