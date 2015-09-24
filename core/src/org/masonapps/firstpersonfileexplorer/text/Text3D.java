package org.masonapps.firstpersonfileexplorer.text;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;

/**
 * Created by Bob on 8/19/2015.
 */
public class Text3D implements Disposable{

    private final BitmapFont font;
    public final Vector3 position = new Vector3();
    public final Quaternion rotation = new Quaternion();
    private final Matrix4 transform = new Matrix4();
    private static final Vector3 dir = new Vector3();
    private static final Vector3 temp = new Vector3();
    private static final Vector3 temp2 = new Vector3();
    private static final Matrix4 tempMat = new Matrix4();
    private float scale = 1.0f;
    
    public Text3D(BitmapFont font) {
        this.font = font;
    }
    
    public void lookAt(Vector3 position, Vector3 up){
        dir.set(position).sub(this.position).nor();
        temp.set(up).crs(dir).nor();
        temp2.set(dir).crs(temp).nor();
        rotation.setFromAxes(temp.x, temp2.x, dir.x, temp.y, temp2.y, dir.y, temp.z, temp2.z, dir.z);
    }
    
    public void draw(Camera camera, SpriteBatch batch, String text){
        transform.idt().translate(position).rotate(rotation).scale(scale, scale, scale);
        batch.setProjectionMatrix(tempMat.set(camera.combined).mul(transform));
        font.draw(batch, text, 0, 0, 0f, Align.center, false);
    }

    public void draw(Camera camera, SpriteBatch spriteBatch, String text, boolean lookAtCam) {
        if(lookAtCam)
            lookAt(camera.position, camera.up);
        draw(camera, spriteBatch, text);
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public float getScale() {
        return scale;
    }

    @Override
    public void dispose() {
        font.dispose();
    }
}
