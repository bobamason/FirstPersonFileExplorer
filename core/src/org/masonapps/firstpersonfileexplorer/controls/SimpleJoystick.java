package org.masonapps.firstpersonfileexplorer.controls;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

/**
 * Created by ims_3 on 8/12/2015.
 */
public class SimpleJoystick implements Disposable{

    public  float limit = 88f;
    public static final String THUMB_PATH = "joystick/joystick_thumb.png";
    public static final String TOUCH_AREA_PATH = "joystick/joystick_touch_area.png";
    private Stage stage;
    private Array<Disposable> disposables;
    private Vector2 thumbPos = new Vector2();
    private Vector2 center = new Vector2();
    private Vector2 temp = new Vector2();
    private Image thumb;
    private Image touchArea;

    public SimpleJoystick(Stage stage) {
        this.stage = stage;
        create();
    }

    private void create(){
        disposables = new Array<Disposable>();
        final Texture thumbTexture = new Texture(THUMB_PATH);
        disposables.add(thumbTexture);
        final Texture areaTexture = new Texture(TOUCH_AREA_PATH);
        disposables.add(areaTexture);

        touchArea = new Image(areaTexture);
        touchArea.addListener(new InputListener() {

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                SimpleJoystick.this.touchDragged(x, y);
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                SimpleJoystick.this.touchUp();
            }
        });
        touchArea.setPosition(center.x, center.y, Align.center);
        stage.addActor(touchArea);
        
        thumb = new Image(thumbTexture);
        thumb.addListener(new InputListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                SimpleJoystick.this.touchDragged(thumb.getX() - touchArea.getX() + x, thumb.getY() - touchArea.getY() + y);
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                SimpleJoystick.this.touchUp();
            }
        });
        thumb.setPosition(center.x, center.y, Align.center);
        stage.addActor(thumb);
    }

    public void touchUp() {
        thumbPos.set(0f, 0f);
        thumb.setPosition(center.x + thumbPos.x, center.y + thumbPos.y, Align.center);
    }

    public void touchDragged(float x, float y) {
        thumbPos.set(x - touchArea.getWidth() * 0.5f, y - touchArea.getHeight() * 0.5f);
        thumbPos.limit(limit);
        thumb.setPosition(center.x + thumbPos.x, center.y + thumbPos.y, Align.center);
    }

    public Vector2 getThumbPos() {
        return temp.set(thumbPos).scl(-1f / limit);
    }

    @Override
    public void dispose() {
        for (Disposable d : disposables) {
            d.dispose();
        }
    }

    public void setPosition(float x, float y) {
        center.set(x, y);
        thumb.setPosition(center.x + thumbPos.x, center.y + thumbPos.y, Align.center);
        touchArea.setPosition(center.x, center.y, Align.center);
    }
    
    public void setSize(float width, float height){
        touchArea.setSize(width, height);
        thumb.setSize(width / 4f, height / 4f);
        limit = touchArea.getWidth() / 2f -  thumb.getWidth() / 2f ;
    }
}
