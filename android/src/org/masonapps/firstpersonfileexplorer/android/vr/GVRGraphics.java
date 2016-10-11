package org.masonapps.firstpersonfileexplorer.android.vr;

import android.opengl.GLSurfaceView;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.LifecycleListener;
import com.badlogic.gdx.backends.android.AndroidApplicationBase;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.AndroidGL20;
import com.badlogic.gdx.backends.android.surfaceview.GLSurfaceView20;
import com.badlogic.gdx.backends.android.surfaceview.GLSurfaceView20API18;
import com.badlogic.gdx.backends.android.surfaceview.GdxEglConfigChooser;
import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.WindowedMean;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.GvrView;
import com.google.vr.sdk.base.HeadTransform;
import com.google.vr.sdk.base.Viewport;

import org.masonapps.firstpersonfileexplorer.vr.GVRApplicationListener;
import org.masonapps.firstpersonfileexplorer.vr.GdxEye;
import org.masonapps.firstpersonfileexplorer.vr.GdxHeadTransform;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.opengles.GL10;


/**
 * Created by Bob on 10/9/2016.
 */

public class GVRGraphics implements Graphics, GvrView.StereoRenderer {

    private static final int OFFSET_RIGHT = 0;
    private static final int OFFSET_UP = OFFSET_RIGHT + 3;
    private static final int OFFSET_FORWARD = OFFSET_UP + 3;
    private static final int OFFSET_TRANSLATION = OFFSET_FORWARD + 3;
    private static final int OFFSET_QUATERNION = OFFSET_TRANSLATION + 3;
    private static final int OFFSET_EULER = OFFSET_QUATERNION + 4;
    private static final String LOG_TAG = GVRGraphics.class.getSimpleName();
    static volatile boolean enforceContinuousRendering = false;
    protected final AndroidApplicationConfiguration config;
    final View view;
    protected GdxEye gdxEye;
    protected GdxHeadTransform gdxHeadTransform;
    protected long lastFrameTime = System.nanoTime();
    protected float deltaTime = 0;
    protected long frameStart = System.nanoTime();
    protected long frameId = -1;
    protected int frames = 0;
    protected int fps;
    protected WindowedMean mean = new WindowedMean(5);
    int width;
    int height;
    AndroidApplicationBase app;
    GL20 gl20;
    GL30 gl30;
    EGLContext eglContext;
    String extensions;
    volatile boolean created = false;
    volatile boolean running = false;
    volatile boolean pause = false;
    volatile boolean resume = false;
    volatile boolean destroy = false;
    int[] value = new int[1];
    Object synch = new Object();
    private float[] array;
    private float ppiX = 0;
    private float ppiY = 0;
    private float ppcX = 0;
    private float ppcY = 0;
    private float density = 1;
    private BufferFormat bufferFormat = new BufferFormat(5, 6, 5, 0, 16, 0, 0, false);
    private boolean isContinuous = true;

    public GVRGraphics(AndroidApplicationBase application, AndroidApplicationConfiguration config) {
        this(application, config, true);
    }

    public GVRGraphics(AndroidApplicationBase application, AndroidApplicationConfiguration config, boolean focusableView) {
        this.config = config;
        this.app = application;
        view = createGLSurfaceView(application);
        preserveEGLContextOnPause();
        if (focusableView) {
            view.setFocusable(true);
            view.setFocusableInTouchMode(true);
        }
    }

    private void init(AndroidApplicationBase application) {
        this.app = application;
        gdxEye = new GdxEye();
        gdxHeadTransform = new GdxHeadTransform();
        array = new float[3 * 4 + 4 + 3];
    }

    protected void preserveEGLContextOnPause() {
        int sdkVersion = android.os.Build.VERSION.SDK_INT;
        if ((sdkVersion >= 11 && view instanceof GLSurfaceView20) || view instanceof GLSurfaceView20API18) {
            try {
                view.getClass().getMethod("setPreserveEGLContextOnPause", boolean.class).invoke(view, true);
            } catch (Exception e) {
                Gdx.app.log(LOG_TAG, "Method GLSurfaceView.setPreserveEGLContextOnPause not found");
            }
        }
    }

    protected View createGLSurfaceView(AndroidApplicationBase application) {
        if (!checkGL20()) throw new GdxRuntimeException("Libgdx requires OpenGL ES 2.0");

        GVRSurfaceView view = new GVRSurfaceView(application.getContext());
//          view.setEGLConfigChooser(config.r, config.g, config.b, config.a, config.depth, config.stencil);
        view.setRenderer(this);
        return view;
    }

    public void onPauseGLSurfaceView() {
        final View view = getView();
        if (view != null) {
            if (view instanceof GVRSurfaceView) ((GVRSurfaceView) view).onPause();
        }
    }

    public void onResumeGLSurfaceView() {
        final View view = getView();
        if (view != null) {
            if (view instanceof GVRSurfaceView) ((GVRSurfaceView) view).onResume();
        }
    }

    protected GLSurfaceView.EGLConfigChooser getEglConfigChooser() {
        return new GdxEglConfigChooser(config.r, config.g, config.b, config.a, config.depth, config.stencil, config.numSamples);
    }

    private void updatePpi() {
        DisplayMetrics metrics = new DisplayMetrics();
        app.getWindowManager().getDefaultDisplay().getMetrics(metrics);

        ppiX = metrics.xdpi;
        ppiY = metrics.ydpi;
        ppcX = metrics.xdpi / 2.54f;
        ppcY = metrics.ydpi / 2.54f;
        density = metrics.density;
    }

    protected boolean checkGL20() {
        EGL10 egl = (EGL10) EGLContext.getEGL();
        EGLDisplay display = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);

        int[] version = new int[2];
        egl.eglInitialize(display, version);

        int EGL_OPENGL_ES2_BIT = 4;
        int[] configAttribs = {EGL10.EGL_RED_SIZE, 4, EGL10.EGL_GREEN_SIZE, 4, EGL10.EGL_BLUE_SIZE, 4, EGL10.EGL_RENDERABLE_TYPE,
                EGL_OPENGL_ES2_BIT, EGL10.EGL_NONE};

        EGLConfig[] configs = new EGLConfig[10];
        int[] num_config = new int[1];
        egl.eglChooseConfig(display, configAttribs, configs, 10, num_config);
        egl.eglTerminate(display);
        return num_config[0] > 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GL20 getGL20() {
        return gl20;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getHeight() {
        return height;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getWidth() {
        return width;
    }

    /**
     * This instantiates the GL10, GL11 and GL20 instances. Includes the check for certain devices that pretend to support GL11 but
     * fuck up vertex buffer objects. This includes the pixelflinger which segfaults when buffers are deleted as well as the
     * Motorola CLIQ and the Samsung Behold II.
     */
    private void setupGL() {
        if (gl20 != null) return;

        gl20 = new AndroidGL20();

        Gdx.gl = gl20;
        Gdx.gl20 = gl20;
    }

    private void logConfig(EGLConfig config) {
        EGL10 egl = (EGL10) EGLContext.getEGL();
        EGLDisplay display = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
        int r = getAttrib(egl, display, config, EGL10.EGL_RED_SIZE, 0);
        int g = getAttrib(egl, display, config, EGL10.EGL_GREEN_SIZE, 0);
        int b = getAttrib(egl, display, config, EGL10.EGL_BLUE_SIZE, 0);
        int a = getAttrib(egl, display, config, EGL10.EGL_ALPHA_SIZE, 0);
        int d = getAttrib(egl, display, config, EGL10.EGL_DEPTH_SIZE, 0);
        int s = getAttrib(egl, display, config, EGL10.EGL_STENCIL_SIZE, 0);
        int samples = Math.max(getAttrib(egl, display, config, EGL10.EGL_SAMPLES, 0),
                getAttrib(egl, display, config, GdxEglConfigChooser.EGL_COVERAGE_SAMPLES_NV, 0));
        boolean coverageSample = getAttrib(egl, display, config, GdxEglConfigChooser.EGL_COVERAGE_SAMPLES_NV, 0) != 0;

        Gdx.app.log(LOG_TAG, "framebuffer: (" + r + ", " + g + ", " + b + ", " + a + ")");
        Gdx.app.log(LOG_TAG, "depthbuffer: (" + d + ")");
        Gdx.app.log(LOG_TAG, "stencilbuffer: (" + s + ")");
        Gdx.app.log(LOG_TAG, "samples: (" + samples + ")");
        Gdx.app.log(LOG_TAG, "coverage sampling: (" + coverageSample + ")");

        bufferFormat = new BufferFormat(r, g, b, a, d, s, samples, coverageSample);
    }

    private int getAttrib(EGL10 egl, EGLDisplay display, EGLConfig config, int attrib, int defValue) {
        if (egl.eglGetConfigAttrib(display, config, attrib, value)) {
            return value[0];
        }
        return defValue;
    }

    void resume() {
        synchronized (synch) {
            running = true;
            resume = true;
        }
    }

    void pause() {
        synchronized (synch) {
            if (!running) return;
            running = false;
            pause = true;
            while (pause) {
                try {
                    // TODO: fix deadlock race condition with quick resume/pause.
                    // Temporary workaround:
                    // Android ANR time is 5 seconds, so wait up to 4 seconds before assuming
                    // deadlock and killing process. This can easily be triggered by opening the
                    // Recent Apps list and then double-tapping the Recent Apps button with
                    // ~500ms between taps.
                    synch.wait(4000);
                    if (pause) {
                        // pause will never go false if onDrawFrame is never called by the GLThread
                        // when entering this method, we MUST enforce continuous rendering
                        Gdx.app.error(LOG_TAG, "waiting for pause synchronization took too long; assuming deadlock and killing");
                        android.os.Process.killProcess(android.os.Process.myPid());
                    }
                } catch (InterruptedException ignored) {
                    Gdx.app.log(LOG_TAG, "waiting for pause synchronization failed!");
                }
            }
        }
    }

    void destroy() {
        synchronized (synch) {
            running = false;
            destroy = true;

            while (destroy) {
                try {
                    synch.wait();
                } catch (InterruptedException ex) {
                    Gdx.app.log(LOG_TAG, "waiting for destroy synchronization failed!");
                }
            }
        }
    }

    @Override
    public long getFrameId() {
        return frameId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getDeltaTime() {
        return mean.getMean() == 0 ? deltaTime : mean.getMean();
    }

    @Override
    public float getRawDeltaTime() {
        return deltaTime;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphicsType getType() {
        return GraphicsType.AndroidGL;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getFramesPerSecond() {
        return fps;
    }

    public void clearManagedCaches() {
        Mesh.clearAllMeshes(app);
        Texture.clearAllTextures(app);
        Cubemap.clearAllCubemaps(app);
        ShaderProgram.clearAllShaderPrograms(app);
        FrameBuffer.clearAllFrameBuffers(app);

        logManagedCachesStatus();
    }

    protected void logManagedCachesStatus() {
        Gdx.app.log(LOG_TAG, Mesh.getManagedStatus());
        Gdx.app.log(LOG_TAG, Texture.getManagedStatus());
        Gdx.app.log(LOG_TAG, Cubemap.getManagedStatus());
        Gdx.app.log(LOG_TAG, ShaderProgram.getManagedStatus());
        Gdx.app.log(LOG_TAG, FrameBuffer.getManagedStatus());
    }

    public View getView() {
        return view;
    }

    @Override
    public float getPpiX() {
        return ppiX;
    }

    @Override
    public float getPpiY() {
        return ppiY;
    }

    @Override
    public float getPpcX() {
        return ppcX;
    }

    @Override
    public float getPpcY() {
        return ppcY;
    }

    @Override
    public float getDensity() {
        return density;
    }

    @Override
    public boolean supportsDisplayModeChange() {
        return false;
    }

    @Override
    public boolean setDisplayMode(DisplayMode displayMode) {
        return false;
    }

    @Override
    public DisplayMode[] getDisplayModes() {
        return new DisplayMode[]{getDesktopDisplayMode()};
    }

    @Override
    public boolean setDisplayMode(int width, int height, boolean fullscreen) {
        return false;
    }

    @Override
    public void setTitle(String title) {

    }

    @Override
    public DisplayMode getDesktopDisplayMode() {
        DisplayMetrics metrics = new DisplayMetrics();
        app.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return new AndroidDisplayMode(metrics.widthPixels, metrics.heightPixels, 0, 0);
    }

    @Override
    public BufferFormat getBufferFormat() {
        return bufferFormat;
    }

    @Override
    public void setVSync(boolean vsync) {
    }

    @Override
    public boolean supportsExtension(String extension) {
        if (extensions == null) extensions = Gdx.gl.glGetString(GL10.GL_EXTENSIONS);
        return extensions.contains(extension);
    }

    @Override
    public boolean isContinuousRendering() {
        return isContinuous;
    }

    @Override
    public void setContinuousRendering(boolean isContinuous) {
    } // not supported in GvrView.StereoRenderer

    @Override
    public boolean isFullscreen() {
        return true;
    }

    @Override
    public boolean isGL30Available() {
        return gl30 != null;
    }

    @Override
    public GL30 getGL30 () {
        return gl30;
    }

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
        long time = System.nanoTime();
        deltaTime = (time - lastFrameTime) / 1000000000.0f;
        lastFrameTime = time;

        // After pause deltaTime can have somewhat huge value that destabilizes the mean, so let's cut it off
        if (!resume) {
            mean.addValue(deltaTime);
        } else {
            deltaTime = 0;
        }

        boolean lrunning = false;
        boolean lpause = false;
        boolean ldestroy = false;
        boolean lresume = false;

        synchronized (synch) {
            lrunning = running;
            lpause = pause;
            ldestroy = destroy;
            lresume = resume;

            if (resume) {
                resume = false;
            }

            if (pause) {
                pause = false;
                synch.notifyAll();
            }

            if (destroy) {
                destroy = false;
                synch.notifyAll();
            }
        }

        if (lresume) {
            Array<LifecycleListener> listeners = app.getLifecycleListeners();
            synchronized (listeners) {
                for (LifecycleListener listener : listeners) {
                    listener.resume();
                }
            }
            app.getApplicationListener().resume();
            Gdx.app.log(LOG_TAG, "resumed");
        }

        if (lrunning) {
            synchronized (app.getRunnables()) {
                app.getExecutedRunnables().clear();
                app.getExecutedRunnables().addAll(app.getRunnables());
                app.getRunnables().clear();
            }

            for (int i = 0; i < app.getExecutedRunnables().size; i++) {
                try {
                    app.getExecutedRunnables().get(i).run();
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
            // TODO: 10/11/2016 fix input 
//            app.getInput().processEvents();
            frameId++;
            ((GVRApplicationListener) Gdx.app.getApplicationListener()).onDrawEye(gdxEye);
        }

        if (lpause) {
            Array<LifecycleListener> listeners = app.getLifecycleListeners();
            synchronized (listeners) {
                for (LifecycleListener listener : listeners) {
                    listener.pause();
                }
            }
            app.getApplicationListener().pause();
            Gdx.app.log(LOG_TAG, "paused");
        }

        if (ldestroy) {
            Array<LifecycleListener> listeners = app.getLifecycleListeners();
            synchronized (listeners) {
                for (LifecycleListener listener : listeners) {
                    listener.dispose();
                }
            }
            app.getApplicationListener().dispose();
            Gdx.app.log(LOG_TAG, "destroyed");
        }

        if (time - frameStart > 1000000000) {
            fps = frames;
            frames = 0;
            frameStart = time;
        }
        frames++;
    }

    @Override
    public void onFinishFrame(Viewport viewport) {

    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        this.width = width;
        this.height = height;
        updatePpi();
        Gdx.gl.glViewport(0, 0, this.width, this.height);
        if (created == false) {
            app.getApplicationListener().create();
            created = true;
            synchronized (this) {
                running = true;
            }
        }
        app.getApplicationListener().resize(width, height);
    }

    @Override
    public void onSurfaceCreated(EGLConfig eglConfig) {
        eglContext = ((EGL10) EGLContext.getEGL()).eglGetCurrentContext();
        setupGL();
        logConfig(eglConfig);
        updatePpi();

        Mesh.invalidateAllMeshes(app);
        Texture.invalidateAllTextures(app);
        Cubemap.invalidateAllCubemaps(app);
        ShaderProgram.invalidateAllShaderPrograms(app);
        FrameBuffer.invalidateAllFrameBuffers(app);

        logManagedCachesStatus();

        Display display = app.getWindowManager().getDefaultDisplay();
        this.width = display.getWidth();
        this.height = display.getHeight();
        this.mean = new WindowedMean(5);
        this.lastFrameTime = System.nanoTime();

        Gdx.gl.glViewport(0, 0, this.width, this.height);
    }

    @Override
    public void onRendererShutdown() {
    }

    private class AndroidDisplayMode extends DisplayMode {
        protected AndroidDisplayMode(int width, int height, int refreshRate, int bitsPerPixel) {
            super(width, height, refreshRate, bitsPerPixel);
        }
    }
}
