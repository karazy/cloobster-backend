package net.eatsense.persistence;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ DummyDataCreator.class, RestaurantRepositoryTest.class })
public class AllPersistenceTests {

}
