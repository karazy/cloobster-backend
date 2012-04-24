package net.eatsense.controller.bill;

import net.eatsense.controller.bill.CreateBillTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ CreateBillTest.class, CalculateTotalPriceTest.class, UpdateBillTest.class })
public class BillControllerTests {

}
