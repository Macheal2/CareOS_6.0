
package com.cappu.music.uitl;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.util.Log;

public final class DocumentsContract {
    
    private static final String PATH_DOCUMENT = "document";
    
    public static final String PROVIDER_INTERFACE = "android.content.action.DOCUMENTS_PROVIDER";
    
    
    /**
     * Test if the given URI represents a {@link Document} backed by a
     * {@link DocumentsProvider}.
     */
    @SuppressLint("NewApi")
    public static boolean isDocumentUri(Context context, Uri uri) {
        final List<String> paths = uri.getPathSegments();
        if (paths.size() < 2) {
            //Log.i("hhjun", "28  paths");
            return false;
        }
        if (!PATH_DOCUMENT.equals(paths.get(0))) {
            //Log.i("hhjun", "32  paths");
            return false;
        }

        //Log.i("hhjun", "36  paths");
        /*final Intent intent = new Intent(PROVIDER_INTERFACE);
        final List<ResolveInfo> infos = context.getPackageManager().queryIntentContentProviders(intent, 0);
        for (ResolveInfo info : infos) {
            if (uri.getAuthority().equals(info.providerInfo.authority)) {
                //Log.i("hhjun", "41  paths");
                return true;
            }
        }*/
        return false;
    }
    
    
    /**
     * Extract the {@link Document#COLUMN_DOCUMENT_ID} from the given URI.
     */
    public static String getDocumentId(Uri documentUri) {
        final List<String> paths = documentUri.getPathSegments();
        if (paths.size() < 2) {
            throw new IllegalArgumentException("Not a document: " + documentUri);
        }
        if (!PATH_DOCUMENT.equals(paths.get(0))) {
            throw new IllegalArgumentException("Not a document: " + documentUri);
        }
        return paths.get(1);
    }
}
