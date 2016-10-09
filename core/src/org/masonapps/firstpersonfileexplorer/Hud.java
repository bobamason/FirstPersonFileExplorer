package org.masonapps.firstpersonfileexplorer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

/**
 * Created by Bob on 9/25/2015.
 */
public class Hud {

    private static final String ENTER_BUTTON_TEXTURE = "buttons/enter_btn.png";
    private static final String COPY_BUTTON_TEXTURE = "buttons/copy_btn.png";
    private static final String DELETE_BUTTON_TEXTURE = "buttons/delete_btn.png";
    private static final String RENAME_BUTTON_TEXTURE = "buttons/rename_btn.png";
    private final HudInteractionListener listener;
    private final Stage stage;
    private final Skin skin;
    private float density = 1f;
    private int buttonSize;
    private int margin;
    private Label label;

    public Hud(HudInteractionListener listener, Stage stage, Skin skin, BitmapFont labelFont) {
        this.listener = listener;
        this.stage = stage;
        this.skin = skin;
        label = new Label("", new Label.LabelStyle(labelFont, Color.LIGHT_GRAY));
        stage.addActor(label);
        initButtonDimens();
    }

    public void setDensity(float density) {
        this.density = density;
        initButtonDimens();
    }

    public void load(AssetManager assets) {
        assets.load(ENTER_BUTTON_TEXTURE, Texture.class);
        assets.load(COPY_BUTTON_TEXTURE, Texture.class);
        assets.load(DELETE_BUTTON_TEXTURE, Texture.class);
        assets.load(RENAME_BUTTON_TEXTURE, Texture.class);
    }

    public void initButtonDimens() {
        buttonSize = Math.round(density * 60f);
        margin = Math.round(density * 10f);
    }

    public void doneLoading(AssetManager assets) {
        setupEnterButton(assets);
//        setupCopyButton(assets);
//        setupDeleteButton(assets);
//        setupRenameButton(assets);
    }

    private void setupEnterButton(AssetManager assets) {
        skin.add(ENTER_BUTTON_TEXTURE, assets.get(ENTER_BUTTON_TEXTURE, Texture.class));
        ImageButton enterButton = new ImageButton(skin.newDrawable(ENTER_BUTTON_TEXTURE), skin.newDrawable(ENTER_BUTTON_TEXTURE, Color.GRAY));
        enterButton.setSize(buttonSize, buttonSize);
        enterButton.setPosition(Gdx.graphics.getWidth() - buttonSize * 2, margin);
        enterButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                listener.onGoClicked();
            }
        });
        stage.addActor(enterButton);
    }

    private void setupCopyButton(AssetManager assets) {
        skin.add(COPY_BUTTON_TEXTURE, assets.get(COPY_BUTTON_TEXTURE, Texture.class));
        ImageButton copyButton = new ImageButton(skin.newDrawable(COPY_BUTTON_TEXTURE), skin.newDrawable(COPY_BUTTON_TEXTURE, Color.GRAY));
        copyButton.setSize(buttonSize, buttonSize);
        copyButton.setPosition(Gdx.graphics.getWidth() - buttonSize - margin, buttonSize);
        copyButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
            }
        });
        stage.addActor(copyButton);
    }

    private void setupDeleteButton(AssetManager assets) {
        skin.add(DELETE_BUTTON_TEXTURE, assets.get(DELETE_BUTTON_TEXTURE, Texture.class));
        ImageButton deleteButton = new ImageButton(skin.newDrawable(DELETE_BUTTON_TEXTURE), skin.newDrawable(DELETE_BUTTON_TEXTURE, Color.GRAY));
        deleteButton.setSize(buttonSize, buttonSize);
        deleteButton.setPosition(Gdx.graphics.getWidth() - buttonSize * 2, buttonSize * 2);
        deleteButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
            }
        });
        stage.addActor(deleteButton);
    }


    private void setupRenameButton(AssetManager assets) {
        skin.add(RENAME_BUTTON_TEXTURE, assets.get(RENAME_BUTTON_TEXTURE, Texture.class));
        ImageButton renameButton = new ImageButton(skin.newDrawable(RENAME_BUTTON_TEXTURE), skin.newDrawable(RENAME_BUTTON_TEXTURE, Color.GRAY));
        renameButton.setSize(buttonSize, buttonSize);
        renameButton.setPosition(Gdx.graphics.getWidth() - buttonSize * 3, buttonSize);
        renameButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {

            }
        });
        stage.addActor(renameButton);
    }

    public void setLabelText(String s) {
        label.setText(s);
    }

    public interface HudInteractionListener {
        void onGoClicked();

        void onRenameClicked();

        void onCutClicked();

        void onCopyClicked();

        void onPasteClicked();

        void onDeleteClicked();

        void onSendClicked();

        void onOpenClicked();
    }
}
