package net.eatsense.integration;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import net.eatsense.domain.embedded.CheckInStatus;
import net.eatsense.domain.embedded.OrderStatus;
import net.eatsense.representation.CheckInDTO;
import net.eatsense.representation.OrderDTO;
import net.eatsense.representation.ProductDTO;
import net.eatsense.representation.cockpit.CheckInStatusDTO;
import net.eatsense.representation.cockpit.SpotStatusDTO;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

public class MoveTableIntegrationTest  extends RestIntegrationTest {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	String barcode;
	String server;
	private String checkInId;
	private long businessId;
	
	public MoveTableIntegrationTest() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Before
	public void setUp() throws Exception {
		barcode = "hup001";
	}
	
	@Test
	public void testBasicMoveTable() {
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
		
		// #2.1 Get the menu from the restaurant
		JsonPath menus = expect().statusCode(200).when()
				.get("/c/businesses/{id}/menus", businessId).jsonPath();
		// Find Coca Cola
		ProductDTO productData = menus.getObject("find {m -> m.title == 'Getränke'}.products.find {p -> p.name == 'Coca-Cola'}", ProductDTO.class);
		OrderDTO orderData = new OrderDTO();
		orderData.setAmount(1);
		orderData.setProductId(productData.getId());
		orderData.setStatus(OrderStatus.CART);
		// #2.2 Place Order in Cart
		response = given().contentType("application/json")
				.body(orderData)
				.queryParam("checkInId",checkInId)
				.expect().statusCode(200).when().post("/c/businesses/{id}/orders", businessId);
		String orderId = response.asString();
		assertThat(orderId , notNullValue());
		// Save the returned order id.
		orderData.setId(Long.valueOf(orderId));

		// #2.3 Sent Order to restaurant( update status to PLACED)
		orderData.setStatus(OrderStatus.PLACED);

		response = given().contentType("application/json")
					.body(orderData)
					.queryParam("checkInId",checkInId)
					.expect().statusCode(200).when().put("/c/businesses/{id}/orders/{order}", businessId, orderData.getId());
		
		// #3.1 get spot information
		// Check status for spot
		response = given().contentType("application/json")
				.headers("login","admin","password","test")
				.expect().statusCode(200)
				.body("find {p -> p.name == 'Tisch 1'}.status", is("ORDER_PLACED"))
				.body("find {p -> p.name == 'Tisch 1'}.checkInCount", is(1))
				.body("find {p -> p.name == 'Tisch 2'}.checkInCount", is(0))
				.when().get("/b/businesses/{id}/spots", businessId);
		SpotStatusDTO oldSpotStatus = response.jsonPath().getObject("find {p -> p.name == 'Tisch 1'}", SpotStatusDTO.class);
		SpotStatusDTO newSpotStatus = response.jsonPath().getObject("find {p -> p.name == 'Tisch 2'}", SpotStatusDTO.class);
		
		// #3.2 get checkin status information
		response = given().contentType("application/json")
				.headers("login","admin","password","test")
				.queryParam("spotId", oldSpotStatus.getId())
				.expect().statusCode(200)
				.body("$.size()", is(1))
				.when().get("/b/businesses/{id}/checkins", businessId);
		CheckInStatusDTO checkInStatus = response.jsonPath().getObject("find { c -> c.nickname = 'TestNils'}", CheckInStatusDTO.class);
		checkInStatus.setSpotId(newSpotStatus.getId());
		
		// #3.3 update checkin to initiate move
		response = given().contentType("application/json")
				.headers("login","admin","password","test")
				.body(checkInStatus)
				.expect().statusCode(200)
				.when().put("/b/businesses/{id}/checkins/{cId}", businessId, checkInStatus.getId());

		// #3.4 check new table status
		response = given().contentType("application/json")
				.headers("login","admin","password","test")
				.expect().statusCode(200)
				.body("find {p -> p.name == 'Tisch 1'}.status", not(equalTo(("ORDER_PLACED"))))
				.body("find {p -> p.name == 'Tisch 1'}.checkInCount", is(0))
				.body("find {p -> p.name == 'Tisch 2'}.checkInCount", is(1))
				.body("find {p -> p.name == 'Tisch 2'}.status", is("ORDER_PLACED"))
				.when().get("/b/businesses/{id}/spots", businessId);

	}
}
