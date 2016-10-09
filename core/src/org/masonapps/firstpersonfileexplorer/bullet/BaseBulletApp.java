package org.masonapps.firstpersonfileexplorer.bullet;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.BaseLight;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.collision.btConvexHullShape;
import com.badlogic.gdx.physics.bullet.collision.btShapeHull;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

/**
 * Created by Bob on 8/11/2015.
 */
public class BaseBulletApp extends ApplicationAdapter {
    
    public Array<Disposable> disposables = new Array<Disposable>();
    public BulletWorld world;
    public Environment environment;
    public PerspectiveCamera camera;
    public ModelBatch modelBatch;
    public AssetManager assets;
    public boolean loading = true;

    public static btConvexHullShape createConvexHullShape(Model model, boolean optimize) {
        final Mesh mesh = model.meshes.get(0);
        final btConvexHullShape shape = new btConvexHullShape(mesh.getVerticesBuffer(), mesh.getNumVertices(), mesh.getVertexSize());
        if (!optimize) return shape;
        // now optimize the shape
        final btShapeHull hull = new btShapeHull(shape);
        hull.buildHull(shape.getMargin());
        final btConvexHullShape result = new btConvexHullShape(hull);
        // delete the temporary shape
        shape.dispose();
        hull.dispose();
        return result;
    }

    public static btConvexHullShape createConvexHullShape(Mesh mesh, boolean optimize) {
        final btConvexHullShape shape = new btConvexHullShape(mesh.getVerticesBuffer(), mesh.getNumVertices(), mesh.getVertexSize());
        if (!optimize) return shape;
        // now optimize the shape
        final btShapeHull hull = new btShapeHull(shape);
        hull.buildHull(shape.getMargin());
        final btConvexHullShape result = new btConvexHullShape(hull);
        // delete the temporary shape
        shape.dispose();
        hull.dispose();
        return result;
    }

    private void init(){
        Bullet.init();
    }

    public BulletWorld createWorld(){
        return new BulletWorld();
    }

    @Override
    public void create() {
        init();
        assets = new AssetManager();
        world = createWorld();
        modelBatch = new ModelBatch();
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, Color.LIGHT_GRAY));
        environment.add(createLight());
        final int width = Gdx.graphics.getWidth();
        final int height = Gdx.graphics.getHeight();
        if(width > height)
            camera = new PerspectiveCamera(90, 1f * width / height, 1f);
        else
            camera = new PerspectiveCamera(90, 1f, 1f * height / width);
    }

    public BaseLight createLight() {
        final DirectionalLight light = new DirectionalLight();
        light.set(Color.DARK_GRAY, 0.0f, -1.0f, 0.0f);
        return light;
    }

    @Override
    public void render() {
        update();
        beginRender();
        renderWorld();
    }

    public void update() {
        if(loading) {
            if (assets.update()) {
                doneLoading();
            }
        }
        world.update();
    }

    private void beginRender() {
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
    }

    public void renderWorld() {
        modelBatch.begin(camera);
        world.render(modelBatch, environment);
        modelBatch.end();
    }

    @Override
    public void dispose() {
        world.dispose();
        world = null;
        for (Disposable d : disposables) {
            d.dispose();
        }
        disposables.clear();
        modelBatch.dispose();
        assets.dispose();
        assets = null;
        modelBatch = null;
    }

    public void doneLoading() {
        loading = false;
    }
}
