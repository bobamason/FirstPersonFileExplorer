package org.masonapps.firstpersonfileexplorer;

import java.io.File;

/**
 * Created by Bob on 8/25/2015.
 */
public interface IActivityInterface {
    void openFile(final File file);
    void shareFile(final File file);
    String getMimeType(final File file);
    void showErrorMessage(final String msg);
}
