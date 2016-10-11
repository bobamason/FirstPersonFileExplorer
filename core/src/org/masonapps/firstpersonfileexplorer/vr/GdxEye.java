package org.masonapps.firstpersonfileexplorer.vr;

import com.badlogic.gdx.math.Matrix4;

/**
 * Created by Bob on 10/10/2016.
 */
public class GdxEye {

    protected Fov fov;
    protected Viewport viewport;
    protected Matrix4 perspective;
    protected Matrix4 view;
    protected float near;
    protected float far;
    protected boolean projectionChanged;

    public GdxEye() {
        fov = new Fov();
        viewport = new Viewport();
        perspective = new Matrix4();
        view = new Matrix4();
        near = 0.1f;
        far = 100f;
    }

    public Fov getFov() {
        return fov;
    }

    public Matrix4 getView() {
        return view;
    }

    public void setView(float[] view) {
        this.view.set(view);
    }

    public void setFov(float left, float top, float right, float bottom) {
        fov.set(left, top, right, bottom);
    }

    public float getNear() {
        return near;
    }

    public float getFar() {
        return far;
    }

    public boolean getProjectionChanged() {
        return projectionChanged;
    }

    public void setProjectionChanged(boolean projectionChanged) {
        this.projectionChanged = projectionChanged;
    }

    public Matrix4 getPerspective() {
        return perspective;
    }

    public void setPerspective(float[] perspective) {
        this.perspective.set(perspective);
    }

    public void setViewport(int x, int y, int width, int height) {
        this.viewport.x = x;
        this.viewport.y = y;
        this.viewport.width = width;
        this.viewport.height = height;
    }

    public Viewport getViewport() {
        return viewport;
    }

    public static class Fov {
        private float left, top, right, bottom;

        public Fov() {
        }

        public void set(float left, float top, float right, float bottom) {
            this.left = left;
            this.top = top;
            this.right = right;
            this.bottom = bottom;
        }

        public float getLeft() {
            return left;
        }

        public float getTop() {
            return top;
        }

        public float getRight() {
            return right;
        }

        public float getBottom() {
            return bottom;
        }
    }

    public static class Viewport {
        public int x, y, width, height;
    }
}
