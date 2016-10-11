package org.masonapps.firstpersonfileexplorer.android.vr;

import android.view.View;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplicationBase;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.AndroidGraphics;
import com.badlogic.gdx.backends.android.surfaceview.FillResolutionStrategy;
import com.badlogic.gdx.backends.android.surfaceview.ResolutionStrategy;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.GvrView;
import com.google.vr.sdk.base.HeadTransform;
import com.google.vr.sdk.base.Viewport;

import org.masonapps.firstpersonfileexplorer.vr.GVRApplicationListener;
import org.masonapps.firstpersonfileexplorer.vr.GdxEye;
import org.masonapps.firstpersonfileexplorer.vr.GdxHeadTransform;

import javax.microedition.khronos.egl.EGLConfig;


/**
 * Created by Bob on 10/9/2016.
 */

public class GVRGraphics extends AndroidGraphics implements GvrView.StereoRenderer {

    private static final int OFFSET_RIGHT = 0;
    private static final int OFFSET_UP = OFFSET_RIGHT + 3;
    private static final int OFFSET_FORWARD = OFFSET_UP + 3;
    private static final int OFFSET_TRANSLATION = OFFSET_FORWARD + 3;
    private static final int OFFSET_QUATERNION = OFFSET_TRANSLATION + 3;
    private static final int OFFSET_EULER = OFFSET_QUATERNION + 4;
    protected GdxEye gdxEye;
    protected GdxHeadTransform gdxHeadTransform;
    private float[] array;

    public GVRGraphics(AndroidApplicationBase application, AndroidApplicationConfiguration config) {
        super(application, config, new FillResolutionStrategy());
        init();
    }

    public GVRGraphics(AndroidApplicationBase application, AndroidApplicationConfiguration config, boolean focusableView) {
        super(application, config, new FillResolutionStrategy(), focusableView);
        init();
    }

    private void init() {
        gdxEye = new GdxEye();
        gdxHeadTransform = new GdxHeadTransform();
        array = new float[3 * 4 + 4 + 3];
    }

    @Override
    protected View createGLSurfaceView(AndroidApplicationBase application, final ResolutionStrategy resolutionStrategy) {
        if (!checkGL20()) throw new GdxRuntimeException("Libgdx requires OpenGL ES 2.0");

        GVRSurfaceView view = new GVRSurfaceView(application.getContext());
//          view.setEGLConfigChooser(config.r, config.g, config.b, config.a, config.depth, config.stencil);
        view.setRenderer(this);
        return view;
    }

    @Override
    public void onPauseGLSurfaceView() {
        final View view = getView();
        if (view != null) {
            if (view instanceof GVRSurfaceView) ((GVRSurfaceView) view).onPause();
        }
    }

    @Override
    public void onResumeGLSurfaceView() {
        final View view = getView();
        if (view != null) {
            if (view instanceof GVRSurfaceView) ((GVRSurfaceView) view).onResume();
        }
    }

    @Override
    public void setContinuousRendering(boolean isContinuous) {
    } // not supported in GvrView.StereoRenderer

    @Override
    public void requestRendering() {
    } // not supported in GvrView.StereoRenderer

    @Override
    public void onNewFrame(HeadTransform headTransform) {
        gdxHeadTransform.setHeadView(headTransform.getHeadView());

        headTransform.getRightVector(array, OFFSET_RIGHT);
        gdxHeadTransform.setRight(array, OFFSET_RIGHT);

        headTransform.getUpVector(array, OFFSET_UP);
        gdxHeadTransform.setUp(array, OFFSET_UP);

        headTransform.getForwardVector(array, OFFSET_FORWARD);
        gdxHeadTransform.setForward(array, OFFSET_FORWARD);

        headTransform.getTranslation(array, OFFSET_TRANSLATION);
        gdxHeadTransform.setTranslation(array, OFFSET_TRANSLATION);

        headTransform.getQuaternion(array, OFFSET_QUATERNION);
        gdxHeadTransform.setQuaternion(array, OFFSET_QUATERNION);

        headTransform.getEulerAngles(array, OFFSET_EULER);
        gdxHeadTransform.setEulerAngles(array, OFFSET_EULER);

        ((GVRApplicationListener) Gdx.app.getApplicationListener()).onNewFrame(gdxHeadTransform);
    }

    @Override
    public void onDrawEye(Eye eye) {
        gdxEye.setView(eye.getEyeView());
        gdxEye.setFov(eye.getFov().getLeft(), eye.getFov().getTop(), eye.getFov().getRight(), eye.getFov().getBottom());
        gdxEye.setPerspective(eye.getPerspective(gdxEye.getNear(), gdxEye.getFar()));
        gdxEye.setProjectionChanged(eye.getProjectionChanged());
        // TODO: 10/10/2016 add constants that match gvr eye types to GdxEye
        eye.getType();
        final Viewport viewport = eye.getViewport();
        gdxEye.setViewport(viewport.x, viewport.y, viewport.width, viewport.height);
        onDrawFrame(null);
        ((GVRApplicationListener) Gdx.app.getApplicationListener()).onDrawEye(gdxEye);
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
