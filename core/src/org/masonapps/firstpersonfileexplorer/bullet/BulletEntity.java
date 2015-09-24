package org.masonapps.firstpersonfileexplorer.bullet;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.linearmath.btMotionState;

/**
 * Created by Bob on 8/10/2015.
 */
public class BulletEntity extends BaseEntity{
    private final static Matrix4 tmpM = new Matrix4();
    public BulletEntity.MotionState motionState;
    public btCollisionObject body;
    
    public BulletEntity (final Model model, final btRigidBody.btRigidBodyConstructionInfo bodyInfo, final float x, final float y, final float z){
        this(model, bodyInfo == null ? null : new btRigidBody(bodyInfo), x, y, z);
    }
    
    public BulletEntity (final Model model, final btRigidBody.btRigidBodyConstructionInfo bodyInfo, final Matrix4 transform){
        this(model, bodyInfo == null ? null : new btRigidBody(bodyInfo), transform);
    }
    
    public BulletEntity (final Model model, final btCollisionObject body, final float x, final float y, final float z){
        this(model, body, tmpM.setToTranslation(x, y, z));
    }

    public BulletEntity(final Model model, final btCollisionObject body, final Matrix4 transform) {
        this(new ModelInstance(model, transform.cpy()), body);
    }
    
    public BulletEntity (final ModelInstance modelInstance, final btCollisionObject body){
        super(modelInstance);
        this.body = body;
        if(body != null){
            body.userData = this;
            if(body instanceof btRigidBody){
                this.motionState = new MotionState(this.modelInstance.transform);
                ((btRigidBody) this.body).setMotionState(motionState);
            } else {
                body.setWorldTransform(transform);
            }
        }
    }

    @Override
    public void dispose() {
        if(motionState != null) motionState.dispose();
        if(body != null) body.dispose();
        motionState = null;
        body = null;
    }
    
    static class MotionState  extends btMotionState {
        private final Matrix4 transform;

        MotionState(final Matrix4 transform) {
            this.transform = transform;
        }

        @Override
        public void getWorldTransform(final Matrix4 worldTrans) {
            worldTrans.set(transform);
        }

        @Override
        public void setWorldTransform(final Matrix4 worldTrans) {
            transform.set(worldTrans);
        }
    }
}
