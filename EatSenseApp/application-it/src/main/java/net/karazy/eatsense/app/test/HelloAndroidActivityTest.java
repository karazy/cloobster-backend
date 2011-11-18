package net.karazy.eatsense.app.test;

import android.test.ActivityInstrumentationTestCase2;
import net.karazy.eatsense.app.*;

public class HelloAndroidActivityTest extends ActivityInstrumentationTestCase2<EatSenseApp> {

    public HelloAndroidActivityTest() {
        super(EatSenseApp.class);
    }

    public void testActivity() {
        EatSenseApp activity = getActivity();
        assertNotNull(activity);
    }
}

