package org.masonapps.firstpersonfileexplorer.firstperson;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btAxisSweep3;
import com.badlogic.gdx.physics.bullet.collision.btBroadphaseProxy;
import com.badlogic.gdx.physics.bullet.collision.btCapsuleShape;
import com.badlogic.gdx.physics.bullet.collision.btCollisionDispatcher;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btConvexShape;
import com.badlogic.gdx.physics.bullet.collision.btDefaultCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.collision.btGhostPairCallback;
import com.badlogic.gdx.physics.bullet.collision.btPairCachingGhostObject;
import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btKinematicCharacterController;
import com.badlogic.gdx.physics.bullet.dynamics.btSequentialImpulseConstraintSolver;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import org.masonapps.firstpersonfileexplorer.bullet.BaseBulletApp;
import org.masonapps.firstpersonfileexplorer.bullet.BulletWorld;

/**
 * Created by Bob on 8/15/2015.
 */
public class FirstPersonApp extends BaseBulletApp {

    public static final String THUMB_PATH = "joystick/joystick_thumb.png";
    public static final String TOUCH_AREA_PATH = "joystick/joystick_touch_area.png";
    private static final float TOUCHPAD_SIZE = 128f;
    public btGhostPairCallback ghostPairCallback;
    public btPairCachingGhostObject ghostObject;
    public btConvexShape ghostShape;
    public btKinematicCharacterController characterController;
    public Matrix4 characterTransform;
    public Vector3 walkDirection = new Vector3();
    public Stage stage;
    public Skin skin;
    public Touchpad touchpad;
    public FPCameraContoller cameraController;
    public float deltaTime;
    public float walkSpeed = 6f;
    private Vector3 tempV = new Vector3();
    private Vector3 cross = new Vector3();

    @Override
    public BulletWorld createWorld() {
        btDefaultCollisionConfiguration collisionConfig = new btDefaultCollisionConfiguration();
        btCollisionDispatcher dispatcher = new btCollisionDispatcher(collisionConfig);
        btAxisSweep3 sweep = new btAxisSweep3(new Vector3(-1000, -1000, -1000), new Vector3(1000, 1000, 1000));
        btSequentialImpulseConstraintSolver solver = new btSequentialImpulseConstraintSolver();
        btDiscreteDynamicsWorld collisionWorld = new btDiscreteDynamicsWorld(dispatcher, sweep, solver, collisionConfig);
        ghostPairCallback = new btGhostPairCallback();
        sweep.getOverlappingPairCache().setInternalGhostPairCallback(ghostPairCallback);
        return new BulletWorld(collisionConfig, dispatcher, sweep, solver, collisionWorld);
    }

    @Override
    public void create() {
        super.create();
        skin = new Skin();
        stage = new Stage(new ScreenViewport());
        disposables.add(stage);
        characterTransform = new Matrix4().setToTranslation(0f, 1f, 0f);
        ghostObject = new btPairCachingGhostObject();
        ghostObject.setWorldTransform(characterTransform);
        ghostShape = new btCapsuleShape(0.5f, 1f);
        ghostObject.setCollisionShape(ghostShape);
        ghostObject.setCollisionFlags(btCollisionObject.CollisionFlags.CF_CHARACTER_OBJECT);
        characterController = new btKinematicCharacterController(ghostObject, ghostShape, 0.2f);
        characterController.setJumpSpeed(20f);

        world.collisionWorld.addCollisionObject(ghostObject, (short) btBroadphaseProxy.CollisionFilterGroups.CharacterFilter, (short) (btBroadphaseProxy.CollisionFilterGroups.StaticFilter | btBroadphaseProxy.CollisionFilterGroups.DefaultFilter));
        ((btDiscreteDynamicsWorld) world.collisionWorld).addAction(characterController);

        characterTransform.getTranslation(camera.position);
        camera.up.set(0, 1, 0);
        camera.update();
        cameraController = new FPCameraContoller(camera);
        Gdx.input.setInputProcessor(new InputMultiplexer(stage, cameraController));
        
        assets.load(THUMB_PATH, Texture.class);
        assets.load(TOUCH_AREA_PATH, Texture.class);
    }

    @Override
    public void doneLoading() {
        skin.add(THUMB_PATH, assets.get(THUMB_PATH, Texture.class));
        skin.add(TOUCH_AREA_PATH, assets.get(TOUCH_AREA_PATH, Texture.class));
        touchpad = new Touchpad(40f, new Touchpad.TouchpadStyle(skin.newDrawable(TOUCH_AREA_PATH), skin.newDrawable(THUMB_PATH)));
        stage.addActor(touchpad);
        final float density = Gdx.graphics.getDensity();
        final int margin = Math.round(10f * density);
        touchpad.setSize(MathUtils.round(density * TOUCHPAD_SIZE), MathUtils.round(density * TOUCHPAD_SIZE));
        touchpad.setPosition(margin, margin);
        super.doneLoading();
    }

    @Override
    public void update() {
        deltaTime = Math.min(1f / 30f, Gdx.graphics.getDeltaTime());
        stage.act();
        cameraController.update();
        ghostObject.setWorldTransform(characterTransform);
        walkDirection.set(0, 0, 0);
        if(!loading) {
            cross.set(camera.direction).crs(camera.up);
            cross.y = 0;
            cross.nor();
            tempV.set(camera.direction);
            tempV.y = 0;
            tempV.nor();
            walkDirection.add(cross.scl(touchpad.getKnobPercentX() * walkSpeed * deltaTime)).add(tempV.scl(touchpad.getKnobPercentY() * walkSpeed * deltaTime));
        }
        characterController.setWalkDirection(walkDirection);
        super.update();
        ghostObject.getWorldTransform(characterTransform);
        characterTransform.getTranslation(camera.position);
        camera.position.add(0, 0.75f, 0);
        camera.update();
    }

    @Override
    public void render() {
        super.render();
        stage.draw();
    }

    @Override
    public void dispose() {
        ((btDiscreteDynamicsWorld) world.collisionWorld).removeAction(characterController);
        world.collisionWorld.removeCollisionObject(ghostObject);
        super.dispose();
        characterController.dispose();
        ghostObject.dispose();
        ghostShape.dispose();
        ghostPairCallback.dispose();
        skin.dispose();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }
}