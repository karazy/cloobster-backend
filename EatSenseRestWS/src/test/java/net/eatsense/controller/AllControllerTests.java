package net.eatsense.controller;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ AccountControllerTest.class, BillControllerTest.class, CheckInControllerTest.class,
		MenuControllerTest.class, OrderControllerTest.class })
public class AllControllerTests {

}
