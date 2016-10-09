package org.masonapps.firstpersonfileexplorer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.collision.btStaticPlaneShape;
import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;

import org.masonapps.firstpersonfileexplorer.bullet.BulletConstructor;
import org.masonapps.firstpersonfileexplorer.bullet.BulletEntity;
import org.masonapps.firstpersonfileexplorer.entities.FileEntity;
import org.masonapps.firstpersonfileexplorer.entities.FileEntityConstructor;
import org.masonapps.firstpersonfileexplorer.firstperson.FirstPersonApp;
import org.masonapps.firstpersonfileexplorer.text.Text3D;

import java.io.File;
import java.util.Comparator;

public class FileExplorer extends FirstPersonApp implements Hud.HudInteractionListener {

    public static final Vector3 START_POS = new Vector3(0f, 1f, 0f);
    private static final String TURN_BODY = "models/turn_blank.g3db";
    private static final String TURN_MODEL = "models/turn.g3db";
    private static final String END_BODY = "models/end_blank.g3db";
    private static final String END_MODEL = "models/end.g3db";
    private static final String T_SECTION_BODY = "models/t_section_blank.g3db";
    private static final String T_SECTION_MODEL = "models/t_section.g3db";
    private static final String TRANSPORTER_MODEL = "models/transporter.g3db";
    private static final String TRANSPORTER_BODY = "models/transporter_blank.g3db";
    private static final String ROOM_MODEL = "models/file_room.g3db";
    private static final String IMAGE_MODEL = "models/image.g3db";
    private static final String VIDEO_MODEL = "models/video.g3db";
    private static final String MUSIC_MODEL = "models/music.g3db";
    private static final String FILE_MODEL = "models/file_model.g3db";
    private static final String FONT = "fonts/roboto_bold.ttf";
    private static final float MIN_DIST2 = 8 * 8;
    private static final Vector3 tempV = new Vector3();
    private static final Matrix4 tempM = new Matrix4();
    private static final Comparator<? super FileHandle> comparator = new Comparator<FileHandle>() {
        @Override
        public int compare(FileHandle o1, FileHandle o2) {
            return o1.name().compareTo(o2.name());
        }
    };
    private final IActivityInterface activityInterface;
    private final ObjectMap<String, FileEntityConstructor> constructors = new ObjectMap<String, FileEntityConstructor>();
    private int closestIndex = -1;
    private SpriteBatch spriteBatch;
    private Text3D text3D;
    private FileHandle currentDir;
    private BitmapFont labelFont;
    private Array<FileHandle> dirs;
    private Array<FileHandle> files;
    private int lastClosestIndex = closestIndex;
    private Hud hud;

    public FileExplorer(IActivityInterface activityInterface) {
        this.activityInterface = activityInterface;
    }

    @Override
    public void create() {
        super.create();
        ((btDiscreteDynamicsWorld) world.collisionWorld).addRigidBody(new btRigidBody(0, null, new btStaticPlaneShape(Vector3.Y, 0)));
        final float density = Gdx.graphics.getDensity();
        dirs = new Array<FileHandle>();
        files = new Array<FileHandle>();

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(FONT));
        final FreeTypeFontGenerator.FreeTypeFontParameter fontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        fontParameter.size = Math.round(16f * density);
        labelFont = generator.generateFont(fontParameter);
        disposables.add(labelFont);
        hud = new Hud(this, stage, skin, labelFont);
        hud.setDensity(density);

        final FreeTypeFontGenerator.FreeTypeFontParameter fontParameter3D = new FreeTypeFontGenerator.FreeTypeFontParameter();
        fontParameter3D.size = 20;
        fontParameter3D.genMipMaps = true;
        fontParameter3D.minFilter = Texture.TextureFilter.MipMapLinearLinear;
        fontParameter3D.magFilter = Texture.TextureFilter.Linear;
        fontParameter3D.borderColor = Color.BLACK;
        fontParameter3D.borderWidth = 1f;
        final BitmapFont font3D = generator.generateFont(fontParameter3D);
        generator.dispose();
        text3D = new Text3D(font3D);
        text3D.setScale(0.016f);

        spriteBatch = new SpriteBatch();
        
        assets.load(T_SECTION_MODEL, Model.class);
        assets.load(T_SECTION_BODY, Model.class);
        assets.load(TRANSPORTER_MODEL, Model.class);
        assets.load(TRANSPORTER_BODY, Model.class);
        assets.load(TURN_MODEL, Model.class);
        assets.load(TURN_BODY, Model.class);
        assets.load(END_MODEL, Model.class);
        assets.load(END_BODY, Model.class);
        assets.load(ROOM_MODEL, Model.class);
        
        assets.load(IMAGE_MODEL, Model.class);
        assets.load(VIDEO_MODEL, Model.class);
        assets.load(MUSIC_MODEL, Model.class);
        assets.load(FILE_MODEL, Model.class);

        hud.load(assets);
    }

    @Override
    public void doneLoading() {
        hud.doneLoading(assets);

        characterTransform.setToTranslation(START_POS);
        characterTransform.getTranslation(camera.position);
        camera.lookAt(10f, 0.75f, 0f);
        camera.up.set(0f, 1f, 0f);
        camera.near = 0.1f;
        camera.far = 100f;
        camera.update();

        initFileModels(Gdx.files.absolute(Gdx.files.getExternalStoragePath()));
        super.doneLoading();
    }

    private void initFileModels(FileHandle file) {
        final Model turnModel = assets.get(TURN_MODEL, Model.class);
        fixTextures(turnModel);
        world.addConstructor(TURN_MODEL, new BulletConstructor(turnModel, 0f, Bullet.obtainStaticNodeShape(assets.get(TURN_BODY, Model.class).nodes)));
        
        final Model endModel = assets.get(END_MODEL, Model.class);
        fixTextures(endModel);
        world.addConstructor(END_MODEL, new BulletConstructor(endModel, 0f, Bullet.obtainStaticNodeShape(assets.get(END_BODY, Model.class).nodes)));
        
        final Model T_SectionModel = assets.get(T_SECTION_MODEL, Model.class);
        fixTextures(T_SectionModel);
        world.addConstructor(T_SECTION_MODEL, new BulletConstructor(T_SectionModel, 0f, Bullet.obtainStaticNodeShape(assets.get(T_SECTION_BODY, Model.class).nodes)));
        
        final Model transporterModel = assets.get(TRANSPORTER_MODEL, Model.class);
        fixTextures(transporterModel);
        constructors.put(TRANSPORTER_MODEL, new FileEntityConstructor(transporterModel, 0f, Bullet.obtainStaticNodeShape(assets.get(TRANSPORTER_BODY, Model.class).nodes)));
        
        final Model roomModel = assets.get(ROOM_MODEL, Model.class);
        fixTextures(roomModel);
        world.addConstructor(ROOM_MODEL, new BulletConstructor(roomModel, 0f, Bullet.obtainStaticNodeShape(roomModel.nodes)));
        
        constructors.put(IMAGE_MODEL, new FileEntityConstructor(assets.get(IMAGE_MODEL, Model.class), 0f));
        constructors.put(VIDEO_MODEL, new FileEntityConstructor(assets.get(VIDEO_MODEL, Model.class), 0f));
        constructors.put(MUSIC_MODEL, new FileEntityConstructor(assets.get(MUSIC_MODEL, Model.class), 0f));
        constructors.put(FILE_MODEL, new FileEntityConstructor(assets.get(FILE_MODEL, Model.class), 0f));
//        Model fileModel = modelBuilder.createSphere(1f, 1f, 1f, 32, 24, new Material(ColorAttribute.createDiffuse(Color.GREEN)),VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
//        constructors.put("fileModel", new FileEntityConstructor(fileModel, 0f, new btSphereShape(0.5f)));
        setupFileWorld(file, null);
    }

    private void fixTextures(Model transporterModel) {
        for (Material m : transporterModel.materials) {
            if(m.has(TextureAttribute.Diffuse)){
                TextureAttribute attrib = (TextureAttribute) m.get(TextureAttribute.Diffuse);
                final Texture texture = attrib.textureDescription.texture;
                texture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
                texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            }
        }
    }

    private void clearFiles() {
        closestIndex = -1;
        dirs.clear();
        files.clear();
        world.clearEntities();
    }

    private void setupFileWorld(FileHandle file, String fromFilePath) {
        if (!file.isDirectory()) return;
        final Vector3 characterPos = new Vector3(START_POS);
        currentDir = file;
        hud.setLabelText(file.pathWithoutExtension());
        FileHandle[] fileList = file.list();
        dirs.clear();
        files.clear();
        for (FileHandle fileHandle : fileList) {
            if (fileHandle.isDirectory())
                dirs.add(fileHandle);
            else
                files.add(fileHandle);
        }
        tempM.idt().rotate(Vector3.Y, 90);
        world.add(T_SECTION_MODEL, tempM);

        final int numFiles = files.size;
        if(numFiles > 0) {
            int n = MathUtils.ceil(numFiles / 10f);
            int x = 0;
            for (int i = 0; i < n; i++) {
                x = -6 - i * 19;
                tempM.idt().translate(x, 0, 0);
                world.add(ROOM_MODEL, tempM);
            }
            tempM.idt().translate(x - 19, 0, 0).rotate(Vector3.Y, 90);
            world.add(END_MODEL, tempM);
            
            final Vector3 fileVec = new Vector3();
            for (int i = 0; i < numFiles; i++) {
                boolean even = i % 2 == 0;
                String key;
                String type = activityInterface.getMimeType(files.get(i).file());
                if (type.startsWith("audio")) {
                    key = MUSIC_MODEL;
                } else if (type.startsWith("video")) {
                    key = VIDEO_MODEL;
                } else if (type.startsWith("image")) {
                    key = IMAGE_MODEL;
                } else {
                    key = FILE_MODEL;
                }
                fileVec.set(((i / 2) * -3.68f) - 7.64f - (MathUtils.floor(i / 10f) * 0.6f), 0, even ? -4 : 4);
                tempM.idt().translate(fileVec).rotate(Vector3.Y, even ? 0 : 180);
                world.add(constructors.get(key).construct(files.get(i), tempM));
            }
        } else {
            tempM.idt().translate(-6, 0, 0).rotate(Vector3.Y, 90);
            world.add(END_MODEL, tempM);
        }
        if(file.parent() != null) {
            final FileEntity parentEntity = constructors.get(TRANSPORTER_MODEL).construct(file.parent(), tempM.idt().translate(0, 0, 8).rotate(Vector3.Y, 180));
            parentEntity.setIsParentDirectory(true);
            world.add(parentEntity);
        }
        boolean dirIsLeft = false;
        boolean dirIsLeft2 = false;
        final Vector3 mainHallVec = new Vector3();
        final Vector3 folderHallVec = new Vector3();
        
        dirs.sort(comparator);
        files.sort(comparator);
        final int numPerRow = 5;
        int numDirs = dirs.size;
        if(numDirs > 0) {
            for (int i = 0; i < numDirs; i++) {
                if(i % numPerRow == 0) {
                    dirIsLeft = !dirIsLeft;
                    final boolean lastRow = numDirs - i < numPerRow;
                    if (lastRow) {
                        tempM.idt().translate(mainHallVec.add(12, 0, 0)).rotate(Vector3.Y, dirIsLeft ? 270 : 0);
                    world.add(TURN_MODEL, tempM);
                } else {
                        tempM.idt().translate(mainHallVec.add(12, 0, 0)).rotate(Vector3.Y, dirIsLeft ? 270 : 90);
                        world.add(T_SECTION_MODEL, tempM);
                    }
                    folderHallVec.set(mainHallVec);
                    dirIsLeft2 = dirIsLeft;
                }
                final boolean end = i % numPerRow == numPerRow - 1 || i == numDirs - 1;
                if(end){
                    tempM.idt().translate(folderHallVec.add(0, 0, dirIsLeft ? -12 : 12)).rotate(Vector3.Y, dirIsLeft ? (dirIsLeft2 ? 0 : 90) : (dirIsLeft2 ? 180 : 270));
                    world.add(TURN_MODEL, tempM);
                }else {
                    tempM.idt().translate(folderHallVec.add(0, 0, dirIsLeft ? -12 : 12)).rotate(Vector3.Y, dirIsLeft ? (dirIsLeft2 ? 0 : 180) : (dirIsLeft2 ? 180 : 0));
                    world.add(T_SECTION_MODEL, tempM);
                }
                tempV.set(folderHallVec).add(dirIsLeft ? (dirIsLeft2 ? -8 : 8) : (dirIsLeft2 ? 8 : -8), 0, 0);
                final int degrees = dirIsLeft ? (dirIsLeft2 ? 90 : 270) : (dirIsLeft2 ? 270 : 90);
                world.add(constructors.get(TRANSPORTER_MODEL).construct(dirs.get(i), tempM.idt().translate(tempV).rotate(Vector3.Y, degrees)));
                if(fromFilePath != null){
                    if(dirs.get(i).path().equals(fromFilePath)) {
                        characterPos.set(tempV);
                        camera.position.set(tempV);
                        camera.lookAt(tempV.set(0f, 1.5f, -10f).rotate(Vector3.Y, degrees + 180).add(characterPos));
                        camera.up.set(Vector3.Y);
                        camera.update();
                    }
                }
                dirIsLeft2 = !dirIsLeft2;
            }
        } else {
            tempM.idt().translate(mainHallVec.add(5, 0, 0)).rotate(Vector3.Y, 270);
            world.add(END_MODEL, tempM);
        }
        if(fromFilePath == null){
            camera.lookAt(10f, 1.5f, 0f);
            camera.up.set(Vector3.Y);
            camera.update();
        }
        characterTransform.setToTranslation(characterPos);
    }

    @Override
    public void update() {
        super.update();
        if (loading) {
            return;
        }
        lastClosestIndex = closestIndex;
        closestIndex = getClosestIndex();
        if (closestIndex != lastClosestIndex) {
            if (closestIndex != -1 ) {
                final FileEntity fileEntity = (FileEntity) world.entities.get(closestIndex);
                if(!fileEntity.isDirectory())
                    fileEntity.activate();
            }
            if (lastClosestIndex != -1) {
                final FileEntity fileEntity = (FileEntity) world.entities.get(lastClosestIndex);
                if(!fileEntity.isDirectory())
                    fileEntity.deactivate();
            }
        }
    }

    private int getClosestIndex() {
        int index = -1;
        float dst2 = MIN_DIST2;
        for (int i = 0; i < world.entities.size; i++) {
            final BulletEntity e = world.entities.get(i);
            if (e instanceof FileEntity) {
                if (!e.isVisible(camera)) continue;
                ((FileEntity) e).transform.getTranslation(tempV);
                final float d = tempV.dst2(camera.position);
                if (d < dst2) {
                    dst2 = d;
                    index = i;
                }
            }
        }
        return index;
    }

    @Override
    public void pause() {
        super.pause();
        closestIndex = -1;
    }

    @Override
    public void renderWorld() {
        super.renderWorld();
        renderText();
    }

    private void renderText() {
        spriteBatch.begin();
        if(closestIndex != -1) {
            final FileEntity fileEntity = (FileEntity) world.entities.get(closestIndex);
            if(fileEntity != null) {
                fileEntity.transform.getTranslation(text3D.position);
                text3D.position.add(0f, 1.5f, 0f);
                text3D.draw(camera, spriteBatch, fileEntity.getDescription(), true);
            }
        }
        for (int i = 0; i < world.entities.size; i++) {
            final BulletEntity entity = world.entities.get(i);
            if (entity instanceof FileEntity) {
                ((FileEntity) entity).update(deltaTime);
//                ((FileEntity) entity).transform.getTranslation(text3D.position);
//                text3D.position.add(0f, 6f, 0f);
//                ((FileEntity) entity).transform.getRotation(text3D.rotation);
//                text3D.draw(camera, spriteBatch, ((FileEntity) entity).getDescription(), true);
            }
        }
        
        spriteBatch.end();
    }

    @Override
    public void dispose() {
        super.dispose();
        for (FileEntityConstructor constructor : constructors.values()) {
            constructor.dispose();
        }
        constructors.clear();
        text3D.dispose();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
    }

    @Override
    public void onGoClicked() {
        if (closestIndex != -1) {
            FileEntity entity = (FileEntity) world.entities.get(closestIndex);
            if (entity != null) {
                if (entity.file.isDirectory()) {
                    clearFiles();
                    setupFileWorld(entity.file, currentDir.path());
                } else {
                    activityInterface.openFile(entity.file.file());
                }
            }
        }
    }

    @Override
    public void onRenameClicked() {
        if (closestIndex != -1) {
            FileEntity entity = (FileEntity) world.entities.get(closestIndex);
            if (entity != null) {
                final FileHandle file = entity.file;
                final Input.TextInputListener listener = new Input.TextInputListener() {
                    @Override
                    public void input(String text) {
                        try {
                            final boolean ok = file.file().renameTo(new File(file.parent().file(), text + "." + file.extension()));
                            if (!ok) activityInterface.showErrorMessage("unable to rename file");
                            clearFiles();
                            setupFileWorld(currentDir, null);
                        } catch (GdxRuntimeException e) {
                            activityInterface.showErrorMessage("unable to rename file: " + e.getMessage());
                        }
                    }

                    @Override
                    public void canceled() {

                    }
                };
                Gdx.input.getTextInput(listener, "Rename", file.nameWithoutExtension(), "");
            }
        }
    }

    @Override
    public void onCutClicked() {

    }

    @Override
    public void onCopyClicked() {
        if (closestIndex != -1) {
            FileEntity entity = (FileEntity) world.entities.get(closestIndex);
            if (entity != null) {
                try {
                    final FileHandle file = entity.file;
                    String extension = file.extension();
                    file.copyTo(new FileHandle(file.pathWithoutExtension() + "-Copy" + (extension.isEmpty() ? "" : "." + extension)));
                    clearFiles();
//                            setupFileWorld(currentDir);
                } catch (GdxRuntimeException e) {
                    activityInterface.showErrorMessage("unable to copy file: " + e.getMessage());
                }
            }
        }
    }

    @Override
    public void onPasteClicked() {

    }

    @Override
    public void onDeleteClicked() {
        if (closestIndex != -1) {
            FileEntity entity = (FileEntity) world.entities.get(closestIndex);
            if (entity != null) {
                final FileHandle file = entity.file;
                try {
                    file.delete();
                    clearFiles();
                    setupFileWorld(currentDir, null);
                } catch (GdxRuntimeException e) {
                    activityInterface.showErrorMessage("unable to delete file: " + e.getMessage());
                }
            }
        }
    }

    @Override
    public void onSendClicked() {

    }

    @Override
    public void onOpenClicked() {
        if (closestIndex != -1) {
            FileEntity entity = (FileEntity) world.entities.get(closestIndex);
            if (entity != null) {
                if (entity.file.isDirectory()) {
                    clearFiles();
                    setupFileWorld(entity.file, currentDir.path());
                } else {
                    activityInterface.openFile(entity.file.file());
                }
            }
        }
    }
}