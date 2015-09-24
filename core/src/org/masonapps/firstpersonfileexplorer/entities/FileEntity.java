package org.masonapps.firstpersonfileexplorer.entities;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;

import org.masonapps.firstpersonfileexplorer.bullet.BulletEntity;

import java.text.DecimalFormat;

/**
 * Created by Bob on 8/23/2015.
 */
public class FileEntity extends BulletEntity {

    private static final Vector3 tmpV = new Vector3();
    private static final int ACTIVATING = 1;
    private static final int ACTIVATED = 2;
    private static final int DEACTIVATING = 3;
    private static final int DEACTIVATED = 4;
    private static DecimalFormat df = new DecimalFormat("#.##");
    public FileHandle file;
    private boolean isParentDirectory;
    private String description;
    private int state;
    public Quaternion rotation;
    private Vector3 startPos;
    private Vector3 endPos;
    private float alpha;
    private float duration;

    public FileEntity(FileHandle file, Model model, btRigidBody.btRigidBodyConstructionInfo bodyInfo, float x, float y, float z) {
        super(model, bodyInfo, x, y, z);
        init(file);
    }

    public FileEntity(FileHandle file, Model model, btRigidBody.btRigidBodyConstructionInfo bodyInfo, Matrix4 transform) {
        super(model, bodyInfo, transform);
        init(file);
    }

    public FileEntity(FileHandle file, Model model, btCollisionObject body, float x, float y, float z) {
        super(model, body, x, y, z);
        init(file);
    }

    public FileEntity(FileHandle file, Model model, btCollisionObject body, Matrix4 transform) {
        super(model, body, transform);
        init(file);
    }

    private void init(FileHandle file) {
        this.file = file;
        this.isParentDirectory = false;
        this.description = file.name() + (file.isDirectory() ? "" :  "\n" + fileSizeString(file.length()));
        this.state = DEACTIVATED;
        this.alpha = 0f;
        this.duration = 0.5f;
        this.startPos = new Vector3();
        this.endPos = new Vector3();
        this.rotation = new Quaternion();
        transform.getTranslation(startPos);
        transform.getRotation(rotation);
        endPos.set(startPos).add(0f, 0.75f, 0f);
    }

    public String getDescription() {
        return description;
    }
    
    public void activate(){
        state = ACTIVATING;
    }
    
    public void deactivate(){
        state = DEACTIVATING;
    }
    
    public void update(float dT){
        switch (state){
            case DEACTIVATED:
                break;
            case DEACTIVATING:
                alpha -= dT / duration;
                if(alpha <= 0f){
                    state = DEACTIVATED;
                    alpha = 0f;
                }
                invalidateTransform();
                break;
            case ACTIVATING:
                alpha += dT / duration;
                if(alpha >= 1f){
                    state = ACTIVATED;
                    alpha = 1f;
                }
                invalidateTransform();
                break;
            case ACTIVATED:
                break;
        }
    }
    
    private void invalidateTransform(){
        if(!isDirectory())
            transform.idt().translate(tmpV.set(startPos).lerp(endPos, alpha)).rotate(rotation).rotate(Vector3.Y, MathUtils.lerp(0, 360, alpha));
    }

    private static String fileSizeString(long length){
        if(length > 1000000000L) return df.format(length / 1000000000d) + " GB";
        if(length > 1000000L) return df.format(length / 1000000d) + " MB";
        if(length > 1000L) return df.format(length / 1000d) + " kB";
        return length + " B";
    }

    public Vector3 getStartPos() {
        return startPos.cpy();
    }

    public void setStartPos(Vector3 startPos) {
        this.startPos.set(startPos);
    }

    public Vector3 getEndPos() {
        return endPos.cpy();
    }

    public void setEndPos(Vector3 endPos) {
        this.endPos.set(endPos);
    }

    public void setDuration(float duration) {
        this.duration = duration;
    }

    public float getDuration() {
        return duration;
    }

    public boolean isParentDirectory() {
        return isParentDirectory;
    }

    public void setIsParentDirectory(boolean isParentDirectory) {
        this.isParentDirectory = isParentDirectory;
    }

    public boolean isDirectory() {
        return file.isDirectory();
    }
}
