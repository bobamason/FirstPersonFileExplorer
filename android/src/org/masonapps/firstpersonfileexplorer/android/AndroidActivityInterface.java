package org.masonapps.firstpersonfileexplorer.android;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import org.masonapps.firstpersonfileexplorer.IActivityInterface;

import java.io.File;

/**
 * Created by Bob on 8/25/2015.
 */
public class AndroidActivityInterface implements IActivityInterface {

    private static final String TAG = "ActivityInterface";
    private final Handler handler;
    private Context context;
    private static final MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();

    public AndroidActivityInterface(Context context) {
        this.context = context;
        this.handler = new Handler();
    }

    @Override
    public void openFile(final File file) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                String mimeType = getMimeType(file);
                Uri uri = Uri.fromFile(file);
                if(file.exists() && uri != null){
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setDataAndType(uri, mimeType);
                    try {
                        context.startActivity(intent);
                    } catch (Exception e){
                        makeToast("unable to open file" + e.getMessage());
                    }
                } else {
                    makeToast("unable to open file");
                }
            }
        });
    }

    @Override
    public void shareFile(final File file) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                String mimeType = getMimeType(file);
                Uri uri = Uri.fromFile(file);
                if(file.exists() && uri != null){
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_SEND);
                    intent.setDataAndType(uri, mimeType);
                    try {
                        context.startActivity(intent);
                    } catch (Exception e){
                        makeToast("unable to share file" + e.getMessage());
                    }
                } else {
                    makeToast("unable to share file");
                }
            }
        });
    }

    @Override
    public String getMimeType(final File file) {
        final String fileName = file.getName();
        int index = fileName.lastIndexOf('.') + 1;
        String extension = index == -1 ? "" : fileName.substring(index);
        String mimeType;
        if (mimeTypeMap.hasExtension(extension)) 
            mimeType = mimeTypeMap.getMimeTypeFromExtension(extension);
        else 
            mimeType = "text/plain";
        return mimeType;
    }

    @Override
    public void showErrorMessage(final String msg) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                makeToast(msg);
            }
        });
    }

    private void makeToast(String msg) {
        Toast.makeText(context.getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }
}
