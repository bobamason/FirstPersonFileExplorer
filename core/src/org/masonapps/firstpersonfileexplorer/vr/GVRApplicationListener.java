package org.masonapps.firstpersonfileexplorer.vr;

import com.badlogic.gdx.ApplicationListener;

/**
 * Created by Bob on 10/9/2016.
 */

public abstract class GVRApplicationListener implements ApplicationListener {

    @Override
    public void render() {
    }

    public abstract void onNewFrame(GdxHeadTransform gdxHeadTransform);

    public abstract void onDrawEye(GdxEye gdxEye);
}
