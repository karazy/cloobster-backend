package net.karazy.eatsense.app;

import android.os.Bundle;
import android.util.Log;

import com.phonegap.DroidGap;

public class EatSenseApp extends DroidGap {

	private static String TAG = "EatSenseApp";

	/**
	 * Called when the activity is first created.
	 * 
	 * @param savedInstanceState
	 *            If the activity is being re-initialized after previously being
	 *            shut down then this Bundle contains the data it most recently
	 *            supplied in onSaveInstanceState(Bundle). <b>Note: Otherwise it
	 *            is null.</b>
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "onCreate");
		super.loadUrl("file:///android_asset/www/index.html");
	}

}
