package org.masonapps.firstpersonfileexplorer.android;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

import org.masonapps.firstpersonfileexplorer.FileExplorer;
import org.masonapps.firstpersonfileexplorer.android.vr.GVRApplication;

public class AndroidLauncher extends GVRApplication {
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		config.useAccelerometer = false;
		config.useCompass = false;
		config.useImmersiveMode = true;
		initialize(new FileExplorer(new AndroidActivityInterface(this)), config);
	}
}
