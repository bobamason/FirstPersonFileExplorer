package org.masonapps.firstpersonfileexplorer.android.vr;

import android.os.Build;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.LifecycleListener;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationBase;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.AndroidAudio;
import com.badlogic.gdx.backends.android.AndroidFiles;
import com.badlogic.gdx.backends.android.AndroidInputFactory;
import com.badlogic.gdx.backends.android.AndroidNet;
import com.badlogic.gdx.utils.GdxRuntimeException;

import java.lang.reflect.Method;

/**
 * Created by Bob on 10/9/2016.
 */

public class GVRApplication extends AndroidApplication {


    @Override
    public void initialize(ApplicationListener listener, AndroidApplicationConfiguration config) {
        setup(listener, config, false);
    }

    @Override
    public View initializeForView(ApplicationListener listener, AndroidApplicationConfiguration config) {
        setup(listener, config, true);
        return graphics.getView();
    }

    private void setup(ApplicationListener listener, AndroidApplicationConfiguration config, boolean isForView) {
        if (this.getVersion() < MINIMUM_SDK) {
            throw new GdxRuntimeException("LibGDX requires Android API Level " + MINIMUM_SDK + " or later.");
        }
        graphics = new GVRGraphics(this, config);
        input = AndroidInputFactory.newAndroidInput(this, this, graphics.getView(), config);
        audio = new AndroidAudio(this, config);
        this.getFilesDir(); // workaround for Android bug #10515463
        files = new AndroidFiles(this.getAssets(), this.getFilesDir().getAbsolutePath());
        net = new AndroidNet(this);
        this.listener = listener;
        this.handler = new Handler();
        this.useImmersiveMode = true;
        this.hideStatusBar = config.hideStatusBar;

        // Add a specialized audio lifecycle listener
        addLifecycleListener(new LifecycleListener() {

            @Override
            public void resume() {
                // No need to resume audio here
            }

            @Override
            public void pause() {
                // TODO: 10/9/2016 fix audio pause
//                audio.pause();
            }

            @Override
            public void dispose() {
                audio.dispose();
            }
        });

        Gdx.app = this;
        Gdx.input = this.getInput();
        Gdx.audio = this.getAudio();
        Gdx.files = this.getFiles();
        Gdx.graphics = this.getGraphics();
        Gdx.net = this.getNet();

        if (!isForView) {
            try {
                requestWindowFeature(Window.FEATURE_NO_TITLE);
            } catch (Exception ex) {
                log("AndroidApplication", "Content already displayed, cannot request FEATURE_NO_TITLE", ex);
            }
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            setContentView(graphics.getView(), createLayoutParams());
        }

        createWakeLock(config.useWakelock);
        hideStatusBar(this.hideStatusBar);
        useImmersiveMode(this.useImmersiveMode);
        if (this.useImmersiveMode && getVersion() >= Build.VERSION_CODES.KITKAT) {
            try {
                Class<?> vlistener = Class.forName("com.badlogic.gdx.backends.android.AndroidVisibilityListener");
                Object o = vlistener.newInstance();
                Method method = vlistener.getDeclaredMethod("createListener", AndroidApplicationBase.class);
                method.invoke(o, this);
            } catch (Exception e) {
                log("AndroidApplication", "Failed to create AndroidVisibilityListener", e);
            }
        }
    }
}
