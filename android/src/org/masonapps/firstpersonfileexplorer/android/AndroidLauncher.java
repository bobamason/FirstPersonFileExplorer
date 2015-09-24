package org.masonapps.firstpersonfileexplorer.android;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

import org.masonapps.firstpersonfileexplorer.FileExplorer;

public class AndroidLauncher extends AndroidApplication {
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		config.useGLSurfaceView20API18 = true;
		config.useAccelerometer = false;
		config.useCompass = false;
		config.useImmersiveMode = true;
		initialize(new FileExplorer(new AndroidActivityInterface(this)), config);
	}
}
