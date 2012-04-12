package net.eatsense.integration;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ BasicIntegrationTest.class , BillIntegrationTest.class, RequestIntegrationTest.class})
public class AllIntegrationTests {

}
