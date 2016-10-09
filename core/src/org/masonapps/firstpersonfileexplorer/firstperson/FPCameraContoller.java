package org.masonapps.firstpersonfileexplorer.firstperson;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;

/**
 * Created by Bob on 10/8/2015.
 */
public class FPCameraContoller extends InputAdapter {

    private static final Vector3 tempV = new Vector3();
    private Camera camera;
    //    private int pointer = -1;
    private float degreesPerPixel = 0.5f;

    public FPCameraContoller(Camera camera) {
        this.camera = camera;
    }

//    @Override
//    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
//        this.pointer = pointer;
//        return true;
//    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
//        if(this.pointer != pointer) return false;
        final float dx = -Gdx.input.getDeltaX(pointer) * degreesPerPixel;
        final float dy = -Gdx.input.getDeltaY(pointer) * degreesPerPixel;
        camera.direction.rotate(camera.up, dx);
        tempV.set(camera.direction).crs(camera.up).nor();
        camera.direction.rotate(tempV, dy);
        return true;
    }

    public float getDegreesPerPixel() {
        return degreesPerPixel;
    }

    public void setDegreesPerPixel(float degreesPerPixel) {
        this.degreesPerPixel = degreesPerPixel;
    }

    public void update() {
        camera.update();
    }
}
