package org.masonapps.firstpersonfileexplorer.bullet;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Disposable;

/**
 * Created by Bob on 8/10/2015.
 */
public abstract class BaseEntity implements Disposable{
    public final Vector3 dimensions = new Vector3();
    public final Vector3 center = new Vector3();
    public final float radius;
    public Matrix4 transform;
    public ModelInstance modelInstance;
    private static final BoundingBox bounds = new BoundingBox();
    private static final Vector3 position = new Vector3();

    public BaseEntity(ModelInstance modelInstance) {
        this.modelInstance = modelInstance;
        this.transform = modelInstance.transform;
        modelInstance.calculateBoundingBox(bounds);
        bounds.getDimensions(dimensions);
        bounds.getCenter(center);
        radius = dimensions.len() / 2f;
    }
    
    public boolean isVisible(Camera camera){
        transform.getTranslation(position);
        return camera.frustum.sphereInFrustum(position.add(center), radius * 2f);
    }
    
//    public boolean isLookingAt(Camera camera, float width, float height){
//        final Ray ray = camera.getPickRay(width / 2, height / 2);
//        ray.
//        transform.getTranslation(position);
//        return ;
//    }
}
