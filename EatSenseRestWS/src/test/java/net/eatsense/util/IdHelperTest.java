package net.eatsense.util;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class IdHelperTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGenerateId() {
		String id = IdHelper.generateId();
		assertNotNull(id);
		System.out.println("testGenerateId " + id);
	}

}
