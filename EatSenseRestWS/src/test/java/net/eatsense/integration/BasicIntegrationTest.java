package net.eatsense.integration;

import java.util.List;

import groovy.time.BaseDuration.From;
import net.eatsense.domain.CheckInStatus;
import net.eatsense.representation.CheckInDTO;
import net.eatsense.representation.MenuDTO;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;

import static com.jayway.restassured.RestAssured.*;
import static com.jayway.restassured.path.json.JsonPath.*;
import static com.jayway.restassured.matcher.RestAssuredMatchers.*;
import static org.hamcrest.Matchers.*;


public class BasicIntegrationTest {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	String barcode;
	String server;
	private String checkInId;
	private long restaurantId;
	@Before
	public void setUp() throws Exception {
		barcode = "hup001";
		RestAssured.baseURI = "http://eatsense-test.appspot.com";
		RestAssured.port = 80;
	}
	
	/**
	 * Testing a basic checkin, ordering and payment request.
	 */
	@Test
	public void basicWorkflowTest() {
		//#1 Get spot information
		Response response = expect().statusCode(200).
				body("name", equalTo("Tisch 1")).body("restaurant", equalTo("Heidi und Paul")).
				when().get("/spots/{barcode}",barcode);
		
		restaurantId = response.getBody().jsonPath().getLong("restaurantId");
		
		//#2.1 Create checkin data to send to server ...
		CheckInDTO checkInData = new CheckInDTO();
		checkInData.setSpotId(barcode);
		checkInData.setStatus(CheckInStatus.INTENT);
		checkInData.setNickname("TestNils");
		
		//#2.2 Send the data and check for correctness, update the checkin data from the response.
		response = given().contentType("application/json").body(checkInData).
	       expect().statusCode(200).body("userId", notNullValue()).body("status", equalTo(CheckInStatus.CHECKEDIN.toString()))
	       .when().post("/checkins");
		checkInData = response.as(CheckInDTO.class);
		checkInId = checkInData.getUserId();
		
		//#3 Check if we can retrieve the same data by user id
		expect().statusCode(200).body("userId", is(checkInId)).when().get("/checkins/{userId}", checkInId);
		
		//#4 Get the menu from the restaurant
		
		String jsonString = expect().statusCode(200).when().get("/restaurants/{id}/menus", restaurantId).path("$.products.name");
		//List<MenuDTO> menus = from(jsonString).getList("", MenuDTO.class);
		logger.info("getting product, {}",jsonString);
	}
	
	@After
	public void tearDown() throws Exception {
		
		//delete the checkin
		expect().statusCode(204).when().delete("/checkins/{userId}", checkInId);
	}
}
