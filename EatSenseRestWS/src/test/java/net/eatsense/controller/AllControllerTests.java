package net.eatsense.controller;

import net.eatsense.controller.bill.BillControllerTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ AccountControllerTest.class, BusinessControllerTest.class,
		ChannelControllerTest.class, CheckInControllerTest.class,
		MenuControllerTest.class, OrderControllerTest.class,
		BillControllerTests.class })
public class AllControllerTests {

}
