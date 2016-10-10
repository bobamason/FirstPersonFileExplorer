package org.masonapps.firstpersonfileexplorer.android.vr;

import com.badlogic.gdx.backends.android.AndroidApplicationBase;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.AndroidGraphics;
import com.badlogic.gdx.backends.android.surfaceview.FillResolutionStrategy;
import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.GvrView;
import com.google.vr.sdk.base.HeadTransform;
import com.google.vr.sdk.base.Viewport;

import javax.microedition.khronos.egl.EGLConfig;

/**
 * Created by Bob on 10/9/2016.
 */

public class GVRGraphics extends AndroidGraphics implements GvrView.StereoRenderer {

    public GVRGraphics(AndroidApplicationBase application, AndroidApplicationConfiguration config) {
        super(application, config, new FillResolutionStrategy());
    }

    public GVRGraphics(AndroidApplicationBase application, AndroidApplicationConfiguration config, boolean focusableView) {
        super(application, config, new FillResolutionStrategy(), focusableView);
    }

    @Override
    public void onNewFrame(HeadTransform headTransform) {

    }

    @Override
    public void onDrawEye(Eye eye) {

    }

    @Override
    public void onFinishFrame(Viewport viewport) {

    }

    @Override
    public void onSurfaceChanged(int i, int i1) {

    }

    @Override
    public void onSurfaceCreated(EGLConfig eglConfig) {

    }

    @Override
    public void onRendererShutdown() {

    }
}
