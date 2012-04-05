package net.eatsense.integration;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import groovy.time.BaseDuration.From;
import net.eatsense.domain.embedded.CheckInStatus;
import net.eatsense.domain.embedded.OrderStatus;
import net.eatsense.domain.embedded.PaymentMethod;
import net.eatsense.representation.BillDTO;
import net.eatsense.representation.CheckInDTO;
import net.eatsense.representation.MenuDTO;
import net.eatsense.representation.OrderDTO;
import net.eatsense.representation.ProductDTO;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.parsing.Parser;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

import static com.jayway.restassured.RestAssured.*;
import static com.jayway.restassured.path.json.JsonPath.*;
import static com.jayway.restassured.matcher.RestAssuredMatchers.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class BasicIntegrationTest {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	String barcode;
	String server;
	private String checkInId;
	private long businessId;
	private ObjectMapper jsonMapper;

	@Before
	public void setUp() throws Exception {
		barcode = "hup001";
//		RestAssured.baseURI = "http://eatsense-test.appspot.com";
//		RestAssured.port = 80;
		
		RestAssured.baseURI = "http://localhost";
		RestAssured.port = 8888;
		
		
		this.jsonMapper = new ObjectMapper();
	}

	/**
	 * 1.1 Testing a basic checkin.
	 */
	@Test
	public void basicCheckInTest() {
		// #1 Get spot information
		Response response = expect().statusCode(200)
				.body("name", equalTo("Tisch 1"))
				.body("business", equalTo("Heidi und Paul")).when()
				.get("/spots/{barcode}", barcode);

		businessId = response.getBody().jsonPath().getLong("businessId");

		// #2.1 Create checkin data to send to server ...
		CheckInDTO checkInData = new CheckInDTO();
		checkInData.setSpotId(barcode);
		checkInData.setStatus(CheckInStatus.INTENT);
		checkInData.setNickname("TestNils");

		// #2.2 Send the data and check for correctness, update the checkin data
		// from the response.
		response = given().contentType("application/json").body(checkInData)
				.expect().statusCode(200).body("userId", notNullValue())
				.body("status", equalTo(CheckInStatus.CHECKEDIN.toString()))
				.when().post("/checkins");
		checkInData = response.as(CheckInDTO.class);
		checkInId = checkInData.getUserId();

		// #3 Check if we can retrieve the same data by user id
		expect().statusCode(200).body("userId", is(checkInId)).when()
				.get("/checkins/{userId}", checkInId);

	}

	/**
	 * #2 Test order placement, confirmation and cancellation.
	 */
	@Test
	public void testBasicOrderStatus() {
		// #1 Get spot information
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

		// #2.1 Do a checkin and check for correctness, update the checkin data
		// from the response.
		response = given().contentType("application/json")
				.body(checkInData).expect().statusCode(200)
				.body("userId", notNullValue())
				.body("status", equalTo(CheckInStatus.CHECKEDIN.toString()))
				.when().post("/checkins");
		checkInData = response.as(CheckInDTO.class);
		checkInId = checkInData.getUserId();
		

		// Check the checkincount at "Tisch 1"
		response = given().contentType("application/json")
				.headers("login","admin","password","test")
				.expect().statusCode(200)
				.body("find {p -> p.name == 'Tisch 1'}.checkInCount", is(1))
				.when().get("/b/businesses/{id}/spots", businessId);

		// Get the menu from the restaurant
		JsonPath menus = expect().statusCode(200).when()
				.get("/c/businesses/{id}/menus", businessId).jsonPath();
		JsonPath menu = with(menus.get("find {m -> m.title == 'Getränke'}").toString());
		menu.setRoot("products");
		
		// Find Coca Cola
		Map<String, String> productMap = menu.get("find {p -> p.name == 'Coca-Cola'}");
		productMap.put("choices", "[]");
		logger.info("Ordering product {}", productMap.toString());
		
		ProductDTO productData= jsonMapper.convertValue(productMap, ProductDTO.class);
		
		OrderDTO orderData = new OrderDTO();
		orderData.setAmount(1);
		orderData.setProduct(productData);
		orderData.setStatus(OrderStatus.CART);
		// Place Order in Cart
		response = given().contentType("application/json")
				.body(orderData)
				.queryParam("checkInId",checkInId)
				.expect().statusCode(200).when().post("/c/businesses/{id}/orders", businessId);
		String orderId = response.asString();
		assertThat(orderId , notNullValue());
		// Save the returned order id.
		orderData.setId(Long.valueOf(orderId));
		
		menu = with(menus.get("find {m -> m.title == 'Unser Fingerfood'}").toString());
		menu.setRoot("products");

		// Find "Die Knusprigen"
		productMap = menu.get("find {p -> p.name == 'Die Knusprigen'}");
		productMap.put("choices", "[]");
		logger.info("Ordering product {}", productMap.toString());
		
		ProductDTO productData2= jsonMapper.convertValue(productMap, ProductDTO.class);
		
		OrderDTO orderData2 = new OrderDTO();
		orderData2.setAmount(1);
		orderData2.setProduct(productData2);
		orderData2.setStatus(OrderStatus.CART);
		
		// Place Order in Cart
		response = given().contentType("application/json")
				.body(orderData2)
				.queryParam("checkInId",checkInId)
				.expect().statusCode(200).when().post("/c/businesses/{id}/orders", businessId);
		orderId = response.asString();
		assertThat(orderId , notNullValue());
		// Save the returned order id.
		orderData2.setId(Long.valueOf(orderId));
		
		orderData.setStatus(OrderStatus.PLACED);
		orderData2.setStatus(OrderStatus.PLACED);
		
		// Sent Order to restaurant( update status to PLACED)
		response = given().contentType("application/json")
					.body(orderData)
					.queryParam("checkInId",checkInId)
					.expect().statusCode(200).when().put("/c/businesses/{id}/orders/{order}", businessId, orderData.getId());
		
		// Sent Order #2 to restaurant( update status to PLACED)
		response = given().contentType("application/json")
					.body(orderData)
					.queryParam("checkInId",checkInId)
					.expect().statusCode(200).when().put("/c/businesses/{id}/orders/{order}", businessId, orderData2.getId());
		
		// Retrieve spot status and check that status is ORDER_PLACED for "Tisch 1"
		response = given().contentType("application/json")
				.headers("login","admin","password","test")
				.expect().statusCode(200)
				.body("find {p -> p.name == 'Tisch 1'}.status", is("ORDER_PLACED"))
				.when().get("/b/businesses/{id}/spots", businessId);
		
		// 2.3 Confirm one order
		orderData.setStatus(OrderStatus.RECEIVED);
		response = given().contentType("application/json")
				.headers("login","admin","password","test")
				.body(orderData)
				.expect().statusCode(200).when().put("/b/businesses/{id}/orders/{order}", businessId, orderData.getId());
	
		// Check status for spot
		response = given().contentType("application/json")
				.headers("login","admin","password","test")
				.expect().statusCode(200)
				.body("find {p -> p.name == 'Tisch 1'}.status", is("ORDER_PLACED"))
				.when().get("/b/businesses/{id}/spots", businessId);
		
		// 2.4 Cancel the other order
		orderData.setStatus(OrderStatus.CANCELED);
		response = given().contentType("application/json")
				.headers("login","admin","password","test")
				.body(orderData)
				.expect().statusCode(200).when().put("/b/businesses/{id}/orders/{order}", businessId, orderData2.getId());
		
		// Check status for spot
		response = given().contentType("application/json")
				.headers("login","admin","password","test")
				.expect().statusCode(200)
				.rootPath("find {p -> p.name == 'Tisch 1'}")
				.body("status", not( is("ORDER_PLACED") ))
				.body("checkInCount", is(1))
				.when().get("/b/businesses/{id}/spots", businessId);
		// #2.5 Issue payment request
		BillDTO billData = new BillDTO();
		billData.setPaymentMethod(new PaymentMethod("EC"));
		
		response = given().contentType("application/json")
				.body(billData)
				.queryParam("checkInId",checkInId)
				.expect().statusCode(200).when().post("/c/businesses/{id}/bills", businessId);
		billData = response.as(BillDTO.class);
		assertThat(billData.getId(), notNullValue());
		
		// Check status for spot
		response = given().contentType("application/json")
				.headers("login","admin","password","test")
				.expect().statusCode(200)
				.rootPath("find {p -> p.name == 'Tisch 1'}")
				.body("status", is("PAYMENT_REQUEST") )
				.when().get("/b/businesses/{id}/spots", businessId);
	}
	
	/**
	 * 4
	 */
	@Test
	public void testCheckInCount() {
		barcode = "hup002";
		// #1 Get spot information
		Response response = expect().statusCode(200)
				.body("name", equalTo("Tisch 2"))
				.body("business", equalTo("Heidi und Paul")).when()
				.get("/spots/{barcode}", barcode);

		businessId = response.getBody().jsonPath().getLong("businessId");
		
		// #3.1 Do 3 checkins and check for correctness, update the checkin data
		// from the response.
		CheckInDTO[] checkIns = new CheckInDTO[3];
		for (int i = 0; i < checkIns.length; i++) {
			checkIns[i] = new CheckInDTO();
			checkIns[i].setSpotId(barcode);
			checkIns[i].setStatus(CheckInStatus.INTENT);
			checkIns[i].setNickname("TestNils"+i);
			
			response = given().contentType("application/json")
					.body(checkIns[i]).expect().statusCode(200)
					.body("userId", notNullValue())
					.body("status", equalTo(CheckInStatus.CHECKEDIN.toString()))
					.when().post("/checkins");
			checkIns[i] = response.as(CheckInDTO.class);
		}

		response = given().contentType("application/json")
				.headers("login","admin","password","test")
				.expect().statusCode(200)
				.rootPath("find {p -> p.name == 'Tisch 2'}")
				.body("checkInCount", is(3))
				.when().get("/b/businesses/{id}/spots", businessId);
		
		// #3.2 Order a product for checkin 1
		
		// Get the menu from the restaurant
		JsonPath menus = expect().statusCode(200).when()
				.get("/c/businesses/{id}/menus", businessId).jsonPath();
		// Find Coca Cola
		ProductDTO productData = menus.getObject("find {m -> m.title == 'Getränke'}.products.find {p -> p.name == 'Coca-Cola'}", ProductDTO.class);
		logger.info("Ordering product {}", productData.getName());
	
		OrderDTO orderData = new OrderDTO();
		orderData.setAmount(1);
		orderData.setProduct(productData);
		orderData.setStatus(OrderStatus.CART);
		// Place Order in Cart
		response = given().contentType("application/json")
				.body(orderData)
				.queryParam("checkInId",checkIns[0].getUserId())
				.expect().statusCode(200).when().post("/c/businesses/{id}/orders", businessId);
		String orderId = response.asString();
		assertThat(orderId , notNullValue());
		// Save the returned order id.
		orderData.setId(Long.valueOf(orderId));

		// Sent Order to restaurant( update status to PLACED)
		orderData.setStatus(OrderStatus.PLACED);

		response = given().contentType("application/json")
					.body(orderData)
					.queryParam("checkInId",checkIns[0].getUserId())
					.expect().statusCode(200).when().put("/c/businesses/{id}/orders/{order}", businessId, orderData.getId());		
	
		// Check status for spot
		response = given().contentType("application/json")
				.headers("login","admin","password","test")
				.expect().statusCode(200)
				.body("find {p -> p.name == 'Tisch 2'}.status", is("ORDER_PLACED"))
				.when().get("/b/businesses/{id}/spots", businessId);
		
		// #3.3 Issue payment request
		BillDTO billData = new BillDTO();
		billData.setPaymentMethod(new PaymentMethod("EC"));
		
		response = given().contentType("application/json")
				.body(billData)
				.queryParam("checkInId",checkIns[0].getUserId())
				.expect().statusCode(200).when().post("/c/businesses/{id}/bills", businessId);
		billData = response.as(BillDTO.class);
		assertThat(billData.getId(), notNullValue());
		
		// Check status for spot
		response = given().contentType("application/json")
				.headers("login","admin","password","test")
				.expect().statusCode(200)
				.rootPath("find {p -> p.name == 'Tisch 2'}")
				.body("status", is("PAYMENT_REQUEST") )
				.when().get("/b/businesses/{id}/spots", businessId);
	}

	/**
	 * 5
	 */
	@Test
	public void testOrderCount() {
		barcode = "hup003";
		// #1 Get spot information
		Response response = expect().statusCode(200)
				.body("name", equalTo("Tisch 3"))
				.body("business", equalTo("Heidi und Paul")).when()
				.get("/spots/{barcode}", barcode);

		businessId = response.getBody().jsonPath().getLong("businessId");

		// #3.1 Do 3 checkins and check for correctness, update the checkin data
		// from the response.
		CheckInDTO[] checkIns = new CheckInDTO[3];
		for (int i = 0; i < checkIns.length; i++) {
			checkIns[i] = new CheckInDTO();
			checkIns[i].setSpotId(barcode);
			checkIns[i].setStatus(CheckInStatus.INTENT);
			checkIns[i].setNickname("TestNils"+i);
			
			response = given().contentType("application/json")
					.body(checkIns[i]).expect().statusCode(200)
					.body("userId", notNullValue())
					.body("status", equalTo(CheckInStatus.CHECKEDIN.toString()))
					.when().post("/checkins");
			checkIns[i] = response.as(CheckInDTO.class);
		}

		response = given().contentType("application/json")
				.headers("login","admin","password","test")
				.expect().statusCode(200)
				.rootPath("find {p -> p.name == 'Tisch 3'}")
				.body("checkInCount", is(3))
				.when().get("/b/businesses/{id}/spots", businessId);
		
		
		// #3.2 Do 2 orders for checkin 1 and 4 for checkin 2
		// Get the menu from the restaurant
		JsonPath menus = expect().statusCode(200).when()
				.get("/c/businesses/{id}/menus", businessId).jsonPath();
		// Find Coca Cola
		ProductDTO productData = menus.getObject("find {m -> m.title == 'Getränke'}.products.find {p -> p.name == 'Coca-Cola'}", ProductDTO.class);
		ProductDTO productData2 = menus.getObject("find {m -> m.title == 'Unser Fingerfood'}.products.find {p -> p.name == 'Der Heidi Mix'}", ProductDTO.class);
		logger.info("Ordering product {}", productData.getName());
		
		OrderDTO[] orders1 = new OrderDTO[6];
		for (int i = 0; i < 6; i++) {
			String checkInId = checkIns[0].getUserId();
			orders1[i] = new OrderDTO();
			orders1[i].setAmount(1);
			orders1[i].setProduct(productData);
			orders1[i].setStatus(OrderStatus.CART);
			
			if(i > 1) {
				checkInId = checkIns[1].getUserId();
				orders1[i].setProduct(productData2);
			}
				
				
			
			// Place Order in Cart
			response = given().contentType("application/json")
					.body(orders1[i])
					.queryParam("checkInId",checkInId)
					.expect().statusCode(200).when().post("/c/businesses/{id}/orders", businessId);
			String orderId = response.asString();
			assertThat(orderId , notNullValue());
			// Save the returned order id.
			orders1[i].setId(Long.valueOf(orderId));
			// Sent Order to restaurant( update status to PLACED)
			orders1[i].setStatus(OrderStatus.PLACED);

			response = given().contentType("application/json")
						.body(orders1[i])
						.queryParam("checkInId",checkIns[0].getUserId())
						.expect().statusCode(200).when().put("/c/businesses/{id}/orders/{order}", businessId, orders1[i].getId());		

		}

		// Check status for spot
		response = given().contentType("application/json")
				.headers("login","admin","password","test")
				.expect().statusCode(200)
				.body("find {p -> p.name == 'Tisch 3'}.status", is("ORDER_PLACED"))
				.when().get("/b/businesses/{id}/spots", businessId);
		int spotId = response.jsonPath().get("find {p -> p.name == 'Tisch 3'}.id");
		// Check order count
		
		response = given().contentType("application/json")
				.headers("login","admin","password","test")
				.queryParam("spotId", spotId)
				.expect().statusCode(200)
				.body("$.size()", is(6))
				.when().get("/b/businesses/{id}/orders", businessId);
	}

	@After
	public void tearDown() throws Exception {
		// delete live data
		expect().statusCode(204).when().delete("/c/businesses/livedata");
	}
}
