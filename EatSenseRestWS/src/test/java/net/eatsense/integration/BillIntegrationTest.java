package net.eatsense.integration;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import net.eatsense.domain.embedded.CheckInStatus;
import net.eatsense.domain.embedded.OrderStatus;
import net.eatsense.domain.embedded.PaymentMethod;
import net.eatsense.representation.BillDTO;
import net.eatsense.representation.CheckInDTO;
import net.eatsense.representation.OrderDTO;
import net.eatsense.representation.ProductDTO;
import net.eatsense.representation.cockpit.SpotStatusDTO;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

public class BillIntegrationTest  extends RestIntegrationTest{
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	String barcode;
	String server;
	private long businessId;
	
	public BillIntegrationTest() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Before
	public void setUp() throws Exception {
		barcode = "hup001";
	}
	
	@Test
	public void testBillCreationAndConfirmation () {
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
				.when().post("/c/checkins");
		checkInData = response.as(CheckInDTO.class);
		checkInData.getUserId();
		
		// Get the menu from the restaurant
		JsonPath menus = expect().statusCode(200).when()
				.get("/c/businesses/{id}/menus", businessId).jsonPath();
		// Find Coca Cola
		ProductDTO productData = menus.getObject("find {m -> m.title == 'Getränke'}.products.find {p -> p.name == 'Coca-Cola'}", ProductDTO.class);
		logger.info("Ordering product {}", productData.getName());
	
		OrderDTO orderData = new OrderDTO();
		orderData.setAmount(1);
		orderData.setProductId(productData.getId());
		orderData.setStatus(OrderStatus.CART);
		// Place Order in Cart
		response = given().contentType("application/json")
				.body(orderData)
				.queryParam("checkInId",checkInData.getUserId())
				.expect().statusCode(200).when().post("/c/businesses/{id}/orders", businessId);
		String orderId = response.asString();
		assertThat(orderId , notNullValue());
		// Save the returned order id.
		orderData.setId(Long.valueOf(orderId));

		// Sent Order to restaurant( update status to PLACED)
		orderData.setStatus(OrderStatus.PLACED);

		response = given().contentType("application/json")
					.body(orderData)
					.queryParam("checkInId",checkInData.getUserId())
					.expect().statusCode(200).when().put("/c/businesses/{id}/orders/{order}", businessId, orderData.getId());		
		
		// #3.3 Issue payment request
		BillDTO billData = new BillDTO();
		billData.setPaymentMethod(new PaymentMethod("EC"));
		
		response = given().contentType("application/json")
				.body(billData)
				.queryParam("checkInId",checkInData.getUserId())
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
		
	
		// Check that the bill has been created on the server ...
		response = given().contentType("application/json")
				.headers("login","admin","password","test")
				.queryParam("checkInId", billData.getCheckInId())
				.expect().statusCode(200)
				.body("id", equalTo(billData.getId().intValue()))
				.when().get("/b/businesses/{id}/bills/", businessId);
		
		BillDTO returnedBillData = response.as(BillDTO.class);
		assertThat(returnedBillData.getTotal(), nullValue());
		
		returnedBillData.setCleared(true);
		// Confirm the payment request from the business side ...
		response = given().contentType("application/json")
				.body(returnedBillData)
				.headers("login","admin","password","test")
				.expect().statusCode(500)
				.when().put("/b/businesses/{id}/bills/{bill}", businessId, returnedBillData.getId());
		// ... but we have unconfirmed orders available.
		
		// Get and confirm the waiting order ...
		response = given().contentType("application/json")
				.queryParam("checkInId", billData.getCheckInId())
				.headers("login","admin","password","test")
				.expect().statusCode(200)
				.when().get("/b/businesses/{id}/orders", businessId);
		//logger.info(response.asString());
		
		OrderDTO newOrderData = response.jsonPath().getObject("find { o -> o.status = 'PLACED'}", OrderDTO.class);
		
		newOrderData.setStatus(OrderStatus.RECEIVED);
		response = given().contentType("application/json")
				.body(newOrderData)
				.headers("login","admin","password","test")
				.expect().statusCode(200)
				.when().put("/b/businesses/{id}/orders/{order}", businessId, newOrderData.getId());
		
		// Again confirm the payment request from the business side. Expected to work.
		response = given().contentType("application/json")
				.body(returnedBillData)
				.headers("login","admin","password","test")
				.expect().statusCode(200)
				.when().put("/b/businesses/{id}/bills/{bill}", businessId, returnedBillData.getId());
		
		returnedBillData = response.as(BillDTO.class);
		
		assertThat(returnedBillData.getTotal(), is(newOrderData.getProductPrice()));
		
		// Check status for spot
		response = given().contentType("application/json")
				.headers("login","admin","password","test")
				.expect().statusCode(200)
				.rootPath("find {p -> p.name == 'Tisch 1'}")
				.body("checkInCount", is(0))
				.when().get("/b/businesses/{id}/spots", businessId);
		SpotStatusDTO spotStatus = response.jsonPath().getObject("find {p -> p.name == 'Tisch 1'}", SpotStatusDTO.class);
		assertThat(spotStatus.getStatus(), nullValue());
	}
}
