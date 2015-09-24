package org.masonapps.firstpersonfileexplorer.bullet;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.DebugDrawer;
import com.badlogic.gdx.physics.bullet.collision.btBroadphaseInterface;
import com.badlogic.gdx.physics.bullet.collision.btCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.collision.btCollisionDispatcher;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionWorld;
import com.badlogic.gdx.physics.bullet.collision.btDbvtBroadphase;
import com.badlogic.gdx.physics.bullet.collision.btDefaultCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.dynamics.btConstraintSolver;
import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.dynamics.btSequentialImpulseConstraintSolver;
import com.badlogic.gdx.physics.bullet.linearmath.btIDebugDraw;
import com.badlogic.gdx.utils.PerformanceCounter;

/**
 * Created by Bob on 8/10/2015.
 */
public class BulletWorld extends BaseWorld<BulletEntity> {
    public DebugDrawer debugDrawer = null;
    public boolean renderMeshes = true;
    public final btCollisionConfiguration collisionConfiguration;
    public final btCollisionDispatcher dispatcher;
    public final btBroadphaseInterface broadphase;
    public final btConstraintSolver solver;
    public final btCollisionWorld collisionWorld;
    public PerformanceCounter performanceCounter;
    public final Vector3 gravity;
    
    public int maxSubSteps = 3;
    public float fixedTimeStep = 1f / 60f;

    public BulletWorld(final btCollisionConfiguration collisionConfiguration, final btCollisionDispatcher dispatcher, final btBroadphaseInterface broadphase, final btConstraintSolver solver, final btCollisionWorld collisionWorld, final Vector3 gravity) {
        this.collisionConfiguration = collisionConfiguration;
        this.dispatcher = dispatcher;
        this.broadphase = broadphase;
        this.solver = solver;
        this.collisionWorld = collisionWorld;
        if(collisionWorld instanceof btDynamicsWorld) ((btDynamicsWorld)collisionWorld).setGravity(gravity);
        this.gravity = gravity;
    }
    
    public BulletWorld (final btCollisionConfiguration collisionConfiguration, final btCollisionDispatcher dispatcher, final btBroadphaseInterface broadphase, final btConstraintSolver solver, final btCollisionWorld collisionWorld){
        this(collisionConfiguration, dispatcher, broadphase, solver, collisionWorld, new Vector3(0f, -10f, 0f));
    }
    
    public BulletWorld(Vector3 gravity){
        collisionConfiguration = new btDefaultCollisionConfiguration();
        dispatcher = new btCollisionDispatcher(collisionConfiguration);
        broadphase = new btDbvtBroadphase();
        solver = new btSequentialImpulseConstraintSolver();
        collisionWorld = new btDiscreteDynamicsWorld(dispatcher, broadphase, solver, collisionConfiguration);
        ((btDynamicsWorld) collisionWorld).setGravity(gravity);
        this.gravity = gravity;
    }
    
    public BulletWorld(){
        this(new Vector3(0f, -10f, 0f));
    }

    @Override
    public void add(BulletEntity entity) {
        super.add(entity);
        if(entity.body != null){
            if(entity.body instanceof btRigidBody){
                ((btDiscreteDynamicsWorld)collisionWorld).addRigidBody((btRigidBody) entity.body);
            } else {
                collisionWorld.addCollisionObject(entity.body);
            }
            entity.body.setUserValue(entities.size - 1);
        }
    }

    @Override
    public void update() {
        if(performanceCounter != null) {
            performanceCounter.tick();
            performanceCounter.start();
        }
        if(collisionWorld instanceof btDynamicsWorld){
            ((btDynamicsWorld) collisionWorld).stepSimulation(Gdx.graphics.getDeltaTime(), maxSubSteps, fixedTimeStep);
        }
        if (performanceCounter != null) performanceCounter.stop();
    }

    @Override
    public void render(ModelBatch batch, Environment lights, Iterable<BulletEntity> entities) {
        if(renderMeshes) super.render(batch, lights, entities);
        if(debugDrawer != null && debugDrawer.getDebugMode() > 0){
            batch.flush();
            debugDrawer.begin(batch.getCamera());
            collisionWorld.debugDrawWorld();
            debugDrawer.end();
        }
    }

    @Override
    public void dispose() {
        clearEntities();
        super.dispose();
        collisionWorld.dispose();
        if(solver != null) solver.dispose();
        if(broadphase != null) broadphase.dispose();
        if(dispatcher != null) dispatcher.dispose();
        if(collisionConfiguration != null) collisionConfiguration.dispose();
    }

    public void clearEntities() {
        for (int i = 0; i < entities.size; i++) {
            btCollisionObject body = entities.get(i).body;
            if(body != null){
                if (body instanceof btRigidBody)
                    ((btDynamicsWorld) collisionWorld).removeRigidBody((btRigidBody) body);
                else 
                    collisionWorld.removeCollisionObject(body);
            }
        }
        entities.clear();
    }

    public void setDebugMode (final int mode){
        if(mode == btIDebugDraw.DebugDrawModes.DBG_NoDebug && debugDrawer == null) return;
        if (debugDrawer == null) collisionWorld.setDebugDrawer(debugDrawer = new DebugDrawer());
        debugDrawer.setDebugMode(mode);
    }
    
    public int getDebugMode(){
        return debugDrawer == null ? 0 : debugDrawer.getDebugMode();
    }
}
