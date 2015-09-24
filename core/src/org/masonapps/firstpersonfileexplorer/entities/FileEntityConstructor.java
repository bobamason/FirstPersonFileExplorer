package org.masonapps.firstpersonfileexplorer.entities;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;

import org.masonapps.firstpersonfileexplorer.bullet.BulletConstructor;

/**
 * Created by Bob on 8/29/2015.
 */
public class FileEntityConstructor extends BulletConstructor {
    public FileEntityConstructor(Model model, float mass, btCollisionShape shape) {
        super(model, mass, shape);
    }

    public FileEntityConstructor(Model model, float mass, float width, float height, float depth) {
        super(model, mass, width, height, depth);
    }

    public FileEntityConstructor(Model model, float mass) {
        super(model, mass);
    }

    public FileEntityConstructor(Model model, btCollisionShape shape) {
        super(model, shape);
    }

    public FileEntityConstructor(Model model, float width, float height, float depth) {
        super(model, width, height, depth);
    }

    public FileEntityConstructor(Model model) {
        super(model);
    }

    public FileEntity construct(FileHandle file, float x, float y, float z) {
        if (bodyInfo == null && shape != null) {
            btCollisionObject obj = new btCollisionObject();
            obj.setCollisionShape(shape);
            return new FileEntity(file, model, obj, x, y, z);
        } else
            return new FileEntity(file, model, bodyInfo, x, y, z);
    }

    public FileEntity construct(FileHandle file, Matrix4 transform) {
        if (bodyInfo == null && shape != null) {
            btCollisionObject obj = new btCollisionObject();
            obj.setCollisionShape(shape);
            return new FileEntity(file, model, obj, transform);
        } else
            return new FileEntity(file, model, bodyInfo, transform);
    }
}
