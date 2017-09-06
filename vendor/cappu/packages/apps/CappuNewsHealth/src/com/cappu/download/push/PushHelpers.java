/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cappu.download.push;

import java.io.File;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.cappu.download.database.PushSettings;
import com.cappu.download.utils.PushConstants;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import android.os.SystemClock;
import android.os.SystemFacade;
import android.util.Config;
import android.util.Log;
import android.webkit.MimeTypeMap;

/**
 * Some helper functions for the download manager
 * 
 * 在下载管理器的一些辅助功能,比如文件名，路径等等
 */
public class PushHelpers {
    
    static String TAG ="Helpers";

    public static Random sRandom = new Random(SystemClock.uptimeMillis());

    /** Regex used to parse content-disposition headers */
    private static final Pattern CONTENT_DISPOSITION_PATTERN = Pattern.compile("attachment;\\s*filename\\s*=\\s*\"([^\"]*)\"");

    private PushHelpers() {
    }

    /*
     * Parse the Content-Disposition HTTP Header. The format of the header is
     * defined here: http://www.w3.org/Protocols/rfc2616/rfc2616-sec19.html This
     * header provides a filename for content that is going to be downloaded to
     * the file system. We only support the attachment type.
     */
    private static String parseContentDisposition(String contentDisposition) {
        try {
            Matcher m = CONTENT_DISPOSITION_PATTERN.matcher(contentDisposition);
            if (m.find()) {
                return m.group(1);
            }
        } catch (IllegalStateException ex) {
            // This function is defined as returning null when it can't parse
            // the header
        }
        return null;
    }

    /**
     * Exception thrown from methods called by generateSaveFile() for any fatal
     * error.
     */
    public static class GenerateSaveFileError extends Exception {
        private static final long serialVersionUID = 4293675292408637112L;

        int mStatus;
        String mMessage;

        public GenerateSaveFileError(int status, String message) {
            mStatus = status;
            mMessage = message;
        }
    }

    /**
     * Creates a filename (where the file should be saved) from info about a
     * download.
     * 
     * url 请求的url
     * 
     * 
     */
    public static String generateSaveFile(Context context, String url, String hint, String contentDisposition, String contentLocation,
            String mimeType, int destination, long contentLength, boolean isPublicApi) throws GenerateSaveFileError {
        checkCanHandleDownload(context, mimeType, destination, isPublicApi);
        if (destination == PushSettings.DESTINATION_FILE_URI) {
            return getPathForFileUri(url, hint, contentDisposition, contentLocation, mimeType, destination, contentLength);
        } else {
            return chooseFullPath(context, url, hint, contentDisposition, contentLocation, mimeType, destination, contentLength);
        }
    }

    private static String getPathForFileUri(String url, String hint, String contentDisposition, String contentLocation, String mimeType,
            int destination, long contentLength) throws GenerateSaveFileError {
        if (!isExternalMediaMounted()) {
            throw new GenerateSaveFileError(PushSettings.STATUS_DEVICE_NOT_FOUND_ERROR, "external media not mounted");
        }
        String path = Uri.parse(hint).getPath();
        if (path.endsWith("/")) {
            String basePath = path.substring(0, path.length() - 1);
            path = generateFilePath(basePath, url,hint, contentDisposition, contentLocation, mimeType, destination, contentLength);
        } else if (new File(path).exists()) {
            Log.d(TAG, "File already exists: " + path);
            throw new GenerateSaveFileError(PushSettings.STATUS_FILE_ALREADY_EXISTS_ERROR, "requested destination file already exists");
        }
        if (getAvailableBytes(getFilesystemRoot(path)) < contentLength) {
            throw new GenerateSaveFileError(PushSettings.STATUS_INSUFFICIENT_SPACE_ERROR, "insufficient space on external storage");
        }

        return path;
    }

    /**
     * @return the root of the filesystem containing the given path
     */
    public static File getFilesystemRoot(String path) {
        File cache = Environment.getDownloadCacheDirectory();
        if (path.startsWith(cache.getPath())) {
            return cache;
        }
        File external = Environment.getExternalStorageDirectory();
        if (path.startsWith(external.getPath())) {
            return external;
        }
        throw new IllegalArgumentException("Cannot determine filesystem root for " + path);
    }

    private static String generateFilePath(String basePath, String url,String hint, String contentDisposition, String contentLocation, String mimeType,
            int destination, long contentLength) throws GenerateSaveFileError {
        String filename = chooseFilename(url, hint, contentDisposition, contentLocation, destination);

        // Split filename between base and extension
        // Add an extension if filename does not have one
        String extension = null;//后缀名
        int dotIndex = filename.indexOf('.');
        if (dotIndex < 0) {
            extension = chooseExtensionFromMimeType(mimeType, true);
        } else {
            extension = chooseExtensionFromFilename(mimeType, destination, filename, dotIndex);
            filename = filename.substring(0, dotIndex);
        }

        boolean recoveryDir = PushConstants.RECOVERY_DIRECTORY.equalsIgnoreCase(filename + extension);

        filename = basePath + File.separator + filename;

        //if (LOGVV) {
            Log.v(TAG, "文件名 : " + filename +"     后缀名       "+ extension);
        //}

        return chooseUniqueFilename(destination, filename, extension, recoveryDir);
    }

    private static String chooseFullPath(Context context, String url, String hint, String contentDisposition, String contentLocation,
            String mimeType, int destination, long contentLength) throws GenerateSaveFileError {
        File base = locateDestinationDirectory(context, mimeType, destination, contentLength);//拿到了存储路径
        return generateFilePath(base.getPath(), url,hint, contentDisposition, contentLocation, mimeType, destination, contentLength);
    }

    private static void checkCanHandleDownload(Context context, String mimeType, int destination, boolean isPublicApi) throws GenerateSaveFileError {
        if (isPublicApi) {
            return;
        }

        if (destination == PushSettings.DESTINATION_EXTERNAL) {
            if (mimeType == null) {
                throw new GenerateSaveFileError(PushSettings.STATUS_NOT_ACCEPTABLE, "external download with no mime type not allowed");
            }
            // Check to see if we are allowed to download this file. Only files
            // that can be handled by the platform can be downloaded.
            // special case DRM files, which we should always allow downloading.
            Intent intent = new Intent(Intent.ACTION_VIEW);

            // We can provide data as either content: or file: URIs,
            // so allow both. (I think it would be nice if we just did
            // everything as content: URIs)
            // Actually, right now the download manager's UId restrictions
            // prevent use from using content: so it's got to be file: or
            // nothing

            PackageManager pm = context.getPackageManager();
            intent.setDataAndType(Uri.fromParts("file", "", null), mimeType);
            ResolveInfo ri = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);

            Log.v(TAG, "-------------- ResolveInfo:" + (ri == null)+"    mimeType:"+mimeType);
            if (ri == null) {
                //if (Constants.LOGV) {
                    Log.v(TAG, "no handler found for type " + mimeType+"    PackageManager:"+(pm == null));
               // }
                throw new GenerateSaveFileError(PushSettings.STATUS_NOT_ACCEPTABLE, "no handler found for this download type");
            }
        }
    }

    /**获取本地的外部存储路径*/
    private static File locateDestinationDirectory(Context context, String mimeType, int destination, long contentLength)
            throws GenerateSaveFileError {
        return getExternalDestination(contentLength);
    }

    /**获取本地的外部存储路径*/
    private static File getExternalDestination(long contentLength) throws GenerateSaveFileError {
        if (!isExternalMediaMounted()) {
            throw new GenerateSaveFileError(PushSettings.STATUS_DEVICE_NOT_FOUND_ERROR, "external media not mounted");
        }

        File root = Environment.getExternalStorageDirectory();
        if (getAvailableBytes(root) < contentLength) {
            // Insufficient space.
            Log.d(TAG, "download aborted - not enough free space");
            throw new GenerateSaveFileError(PushSettings.STATUS_INSUFFICIENT_SPACE_ERROR, "insufficient space on external media");
        }

        File base = new File(root.getPath() + PushConstants.DEFAULT_DL_SUBDIR);
        boolean isDirectory = base.isDirectory();
        boolean mkdir = base.mkdirs();
        if (!isDirectory && !mkdir) {
            // Can't create download directory, e.g. because a file called
            // "download"
            // already exists at the root level, or the SD card filesystem is
            // read-only.
            throw new GenerateSaveFileError(PushSettings.STATUS_FILE_ERROR, "unable to create external downloads directory " +"  isDirectory:"+isDirectory+"    mkdir:"+mkdir+"       "+ base.getPath());
        }
        
        Log.i(TAG, "base:"+base);
        return base;
    }

    public static boolean isExternalMediaMounted() {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            // No SD card found.
            Log.d(TAG, "no external storage");
            return false;
        }
        return true;
    }

    /**
     * @return the number of bytes available on the filesystem rooted at the
     *         given File
     */
    public static long getAvailableBytes(File root) {
        StatFs stat = new StatFs(root.getPath());
        // put a bit of margin (in case creating the file grows the system by a
        // few blocks)
        long availableBlocks = (long) stat.getAvailableBlocks() - 4;
        return stat.getBlockSize() * availableBlocks;
    }

    private static String chooseFilename(String url, String hint, String contentDisposition, String contentLocation, int destination) {
        String filename = null;

        // First, try to use the hint from the application, if there's one
        
        Log.i(TAG, "构造名字 url："+url+"    hint:"+hint+"     contentDisposition:"+contentDisposition+"     contentLocation:"+contentLocation+"    destination:"+destination);
        if (filename == null && hint != null && !hint.endsWith("/")) {//如果应用程序给定里一个下载名字
            //if (Constants.LOGVV) {
                Log.v(TAG, "getting filename from hint");
            //}
            int index = hint.lastIndexOf('/') + 1;
            if (index > 0) {
                filename = hint.substring(index);
            } else {
                filename = hint;
            }
        }

        // If we couldn't do anything with the hint, move toward the content
        // disposition
        if (filename == null && contentDisposition != null) {
            filename = parseContentDisposition(contentDisposition);
            if (filename != null) {
               // if (Constants.LOGVV) {
                    Log.v(TAG, "getting filename from content-disposition");
               // }
                int index = filename.lastIndexOf('/') + 1;
                if (index > 0) {
                    filename = filename.substring(index);
                }
            }
        }

        // If we still have nothing at this point, try the content location
        if (filename == null && contentLocation != null) {
            String decodedContentLocation = Uri.decode(contentLocation);
            if (decodedContentLocation != null && !decodedContentLocation.endsWith("/") && decodedContentLocation.indexOf('?') < 0) {
                //if (Constants.LOGVV) {
                    Log.v(TAG, "getting filename from content-location");
                //}
                int index = decodedContentLocation.lastIndexOf('/') + 1;
                if (index > 0) {
                    filename = decodedContentLocation.substring(index);
                } else {
                    filename = decodedContentLocation;
                }
            }
        }

        // If all the other http-related approaches failed, use the plain uri
        if (filename == null) {
            String decodedUrl = Uri.decode(url);
            if (decodedUrl != null && !decodedUrl.endsWith("/") && decodedUrl.indexOf('?') < 0) {
                int index = decodedUrl.lastIndexOf('/') + 1;
                if (index > 0) {
                    //if (Constants.LOGVV) {
                        Log.v(TAG, "getting filename from uri");
                    //}
                    filename = decodedUrl.substring(index);
                }
            }
        }

        // Finally, if couldn't get filename from URI, get a generic filename
        if (filename == null) {
            //if (Constants.LOGVV) {
                Log.v(TAG, "using default filename");
            //}
            filename = PushConstants.DEFAULT_DL_FILENAME;
        }

        filename = filename.replaceAll("[^a-zA-Z0-9\\.\\-_]+", "_");

        return filename;
    }

    /**根据类型来获取一个后缀名*/
    private static String chooseExtensionFromMimeType(String mimeType, boolean useDefaults) {
        String extension = null;
        if (mimeType != null) {
            extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
            if (extension != null) {
                //if (Constants.LOGVV) {
                    Log.v(TAG, "adding extension from type");
                //}
                extension = "." + extension;
            } else {
                //if (Constants.LOGVV) {
                    Log.v(TAG, "couldn't find extension for " + mimeType);
                //}
            }
        }
        if (extension == null) {
            if (mimeType != null && mimeType.toLowerCase().startsWith("text/")) {
                if (mimeType.equalsIgnoreCase("text/html")) {
                    //if (Constants.LOGVV) {
                        Log.v(TAG, "adding default html extension");
                    //}
                    extension = PushConstants.DEFAULT_DL_HTML_EXTENSION;
                } else if (useDefaults) {
                    //if (Constants.LOGVV) {
                        Log.v(TAG, "adding default text extension");
                    //}
                    extension = PushConstants.DEFAULT_DL_TEXT_EXTENSION;
                }
            } else if (useDefaults) {
                //if (Constants.LOGVV) {
                    Log.v(TAG, "adding default binary extension");
                //}
                extension = PushConstants.DEFAULT_DL_BINARY_EXTENSION;
            }
        }
        
        Log.i(TAG, "扩展的后缀名是:"+extension+"    mimeType:"+mimeType);
        return extension;
    }

    private static String chooseExtensionFromFilename(String mimeType, int destination, String filename, int dotIndex) {
        String extension = null;
        if (mimeType != null) {
            // Compare the last segment of the extension against the mime type.
            // If there's a mismatch, discard the entire extension.
            int lastDotIndex = filename.lastIndexOf('.');
            String typeFromExt = MimeTypeMap.getSingleton().getMimeTypeFromExtension(filename.substring(lastDotIndex + 1));
            if (typeFromExt == null || !typeFromExt.equalsIgnoreCase(mimeType)) {
                extension = chooseExtensionFromMimeType(mimeType, false);
                if (extension != null) {
                    //if (Constants.LOGVV) {
                        Log.v(TAG, "substituting extension from type");
                    //}
                } else {
                    //if (Constants.LOGVV) {
                        Log.v(TAG, "couldn't find extension for " + mimeType);
                    //}
                }
            }
        }
        if (extension == null) {
            //if (Constants.LOGVV) {
                Log.v(TAG, "keeping extension");
            //}
            extension = filename.substring(dotIndex);
        }
        return extension;
    }

    /**当一个文件存在的时候，我们就将文件名设置为 “文件-数字”组合*/
    private static String chooseUniqueFilename(int destination, String filename, String extension, boolean recoveryDir) throws GenerateSaveFileError {
        String fullFilename = filename + extension;
        File fullFile = new File(fullFilename);
        
        /*kook add*/
        if(fullFile.exists()){
            fullFile.delete();
        }
        /*kook add end */
        Log.i(TAG, "filename:"+filename+"    extension:"+extension+"  recoveryDir:"+recoveryDir+"    fullFile:"+fullFile.exists());
        if (!fullFile.exists() && !recoveryDir) {
            return fullFilename;
        }
        filename = filename + PushConstants.FILENAME_SEQUENCE_SEPARATOR;
        /*
         * This number is used to generate partially randomized filenames to
         * avoid collisions. It starts at 1. The next 9 iterations increment it
         * by 1 at a time (up to 10). The next 9 iterations increment it by 1 to
         * 10 (random) at a time. The next 9 iterations increment it by 1 to 100
         * (random) at a time. ... Up to the point where it increases by
         * 100000000 at a time. (the maximum value that can be reached is
         * 1000000000) As soon as a number is reached that generates a filename
         * that doesn't exist, that filename is used. If the filename coming in
         * is [base].[ext], the generated filenames are [base]-[sequence].[ext].
         */
        int sequence = 1;
        for (int magnitude = 1; magnitude < 1000000000; magnitude *= 10) {
            for (int iteration = 0; iteration < 9; ++iteration) {
                fullFilename = filename + sequence + extension;
                if (!new File(fullFilename).exists()) {
                    return fullFilename;
                }
                //if (Constants.LOGVV) {
                    Log.v(TAG, "file with sequence number " + sequence + " exists");
                //}
                sequence += sRandom.nextInt(magnitude) + 1;
            }
        }
        throw new GenerateSaveFileError(PushSettings.STATUS_FILE_ERROR, "failed to generate an unused filename on internal download storage");
    }

    /**
     * Returns whether the network is available
     */
    public static boolean isNetworkAvailable(SystemFacade system) {
        return system.getActiveNetworkType() != null;
    }

    /**
     * Checks whether the filename looks legitimate
     */
    public static boolean isFilenameValid(String filename) {
        filename = filename.replaceFirst("/+", "/"); // normalize leading
        // slashes
        return filename.startsWith(Environment.getDownloadCacheDirectory().toString())
                || filename.startsWith(Environment.getExternalStorageDirectory().toString());
    }

    /**
     * Checks whether this looks like a legitimate selection parameter
     */
    public static void validateSelection(String selection, Set<String> allowedColumns) {
        try {
            if (selection == null || selection.length() == 0) {
                return;
            }
            Lexer lexer = new Lexer(selection, allowedColumns);
            parseExpression(lexer);
            if (lexer.currentToken() != Lexer.TOKEN_END) {
                throw new IllegalArgumentException("syntax error");
            }
        } catch (RuntimeException ex) {
            //if (Constants.LOGV) {
                Log.d(TAG, "invalid selection [" + selection + "] triggered " + ex);
            //} else if (Config.LOGD) {
                Log.d(TAG, "invalid selection triggered " + ex);
            //}
            throw ex;
        }

    }

    // expression <- ( expression ) | statement [AND_OR ( expression ) |
    // statement] *
    // | statement [AND_OR expression]*
    private static void parseExpression(Lexer lexer) {
        for (;;) {
            // ( expression )
            if (lexer.currentToken() == Lexer.TOKEN_OPEN_PAREN) {
                lexer.advance();
                parseExpression(lexer);
                if (lexer.currentToken() != Lexer.TOKEN_CLOSE_PAREN) {
                    throw new IllegalArgumentException("syntax error, unmatched parenthese");
                }
                lexer.advance();
            } else {
                // statement
                parseStatement(lexer);
            }
            if (lexer.currentToken() != Lexer.TOKEN_AND_OR) {
                break;
            }
            lexer.advance();
        }
    }

    // statement <- COLUMN COMPARE VALUE
    // | COLUMN IS NULL
    private static void parseStatement(Lexer lexer) {
        // both possibilities start with COLUMN
        if (lexer.currentToken() != Lexer.TOKEN_COLUMN) {
            throw new IllegalArgumentException("syntax error, expected column name");
        }
        lexer.advance();

        // statement <- COLUMN COMPARE VALUE
        if (lexer.currentToken() == Lexer.TOKEN_COMPARE) {
            lexer.advance();
            if (lexer.currentToken() != Lexer.TOKEN_VALUE) {
                throw new IllegalArgumentException("syntax error, expected quoted string");
            }
            lexer.advance();
            return;
        }

        // statement <- COLUMN IS NULL
        if (lexer.currentToken() == Lexer.TOKEN_IS) {
            lexer.advance();
            if (lexer.currentToken() != Lexer.TOKEN_NULL) {
                throw new IllegalArgumentException("syntax error, expected NULL");
            }
            lexer.advance();
            return;
        }

        // didn't get anything good after COLUMN
        throw new IllegalArgumentException("syntax error after column name");
    }

    /**
     * A simple lexer that recognizes the words of our restricted subset of SQL
     * where clauses
     */
    private static class Lexer {
        public static final int TOKEN_START = 0;
        public static final int TOKEN_OPEN_PAREN = 1;
        public static final int TOKEN_CLOSE_PAREN = 2;
        public static final int TOKEN_AND_OR = 3;
        public static final int TOKEN_COLUMN = 4;
        public static final int TOKEN_COMPARE = 5;
        public static final int TOKEN_VALUE = 6;
        public static final int TOKEN_IS = 7;
        public static final int TOKEN_NULL = 8;
        public static final int TOKEN_END = 9;

        private final String mSelection;
        private final Set<String> mAllowedColumns;
        private int mOffset = 0;
        private int mCurrentToken = TOKEN_START;
        private final char[] mChars;

        public Lexer(String selection, Set<String> allowedColumns) {
            mSelection = selection;
            mAllowedColumns = allowedColumns;
            mChars = new char[mSelection.length()];
            mSelection.getChars(0, mChars.length, mChars, 0);
            advance();
        }

        public int currentToken() {
            return mCurrentToken;
        }

        public void advance() {
            char[] chars = mChars;

            // consume whitespace
            while (mOffset < chars.length && chars[mOffset] == ' ') {
                ++mOffset;
            }

            // end of input
            if (mOffset == chars.length) {
                mCurrentToken = TOKEN_END;
                return;
            }

            // "("
            if (chars[mOffset] == '(') {
                ++mOffset;
                mCurrentToken = TOKEN_OPEN_PAREN;
                return;
            }

            // ")"
            if (chars[mOffset] == ')') {
                ++mOffset;
                mCurrentToken = TOKEN_CLOSE_PAREN;
                return;
            }

            // "?"
            if (chars[mOffset] == '?') {
                ++mOffset;
                mCurrentToken = TOKEN_VALUE;
                return;
            }

            // "=" and "=="
            if (chars[mOffset] == '=') {
                ++mOffset;
                mCurrentToken = TOKEN_COMPARE;
                if (mOffset < chars.length && chars[mOffset] == '=') {
                    ++mOffset;
                }
                return;
            }

            // ">" and ">="
            if (chars[mOffset] == '>') {
                ++mOffset;
                mCurrentToken = TOKEN_COMPARE;
                if (mOffset < chars.length && chars[mOffset] == '=') {
                    ++mOffset;
                }
                return;
            }

            // "<", "<=" and "<>"
            if (chars[mOffset] == '<') {
                ++mOffset;
                mCurrentToken = TOKEN_COMPARE;
                if (mOffset < chars.length && (chars[mOffset] == '=' || chars[mOffset] == '>')) {
                    ++mOffset;
                }
                return;
            }

            // "!="
            if (chars[mOffset] == '!') {
                ++mOffset;
                mCurrentToken = TOKEN_COMPARE;
                if (mOffset < chars.length && chars[mOffset] == '=') {
                    ++mOffset;
                    return;
                }
                throw new IllegalArgumentException("Unexpected character after !");
            }

            // columns and keywords
            // first look for anything that looks like an identifier or a
            // keyword
            // and then recognize the individual words.
            // no attempt is made at discarding sequences of underscores with no
            // alphanumeric
            // characters, even though it's not clear that they'd be legal
            // column names.
            if (isIdentifierStart(chars[mOffset])) {
                int startOffset = mOffset;
                ++mOffset;
                while (mOffset < chars.length && isIdentifierChar(chars[mOffset])) {
                    ++mOffset;
                }
                String word = mSelection.substring(startOffset, mOffset);
                if (mOffset - startOffset <= 4) {
                    if (word.equals("IS")) {
                        mCurrentToken = TOKEN_IS;
                        return;
                    }
                    if (word.equals("OR") || word.equals("AND")) {
                        mCurrentToken = TOKEN_AND_OR;
                        return;
                    }
                    if (word.equals("NULL")) {
                        mCurrentToken = TOKEN_NULL;
                        return;
                    }
                }
                if (mAllowedColumns.contains(word)) {
                    mCurrentToken = TOKEN_COLUMN;
                    return;
                }
                throw new IllegalArgumentException("unrecognized column or keyword");
            }

            // quoted strings
            if (chars[mOffset] == '\'') {
                ++mOffset;
                while (mOffset < chars.length) {
                    if (chars[mOffset] == '\'') {
                        if (mOffset + 1 < chars.length && chars[mOffset + 1] == '\'') {
                            ++mOffset;
                        } else {
                            break;
                        }
                    }
                    ++mOffset;
                }
                if (mOffset == chars.length) {
                    throw new IllegalArgumentException("unterminated string");
                }
                ++mOffset;
                mCurrentToken = TOKEN_VALUE;
                return;
            }

            // anything we don't recognize
            throw new IllegalArgumentException("illegal character: " + chars[mOffset]);
        }

        private static final boolean isIdentifierStart(char c) {
            return c == '_' || (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');
        }

        private static final boolean isIdentifierChar(char c) {
            return c == '_' || (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9');
        }
    }

    /**
     * Delete the given file from device and delete its row from the downloads
     * database.
     * 
     * 当这里删除数据库的时候同时也删除里文件,这里修改一下不让删除文件
     */
    /* package */static void deleteFile(ContentResolver resolver,Uri uri, long id, String path, String mimeType) {
        /*try {
            File file = new File(path);
            Log.i("DownloadProvider", "delete File:"+path+"    717");
            file.delete();
        } catch (Exception e) {
            Log.w(Constants.TAG, "file: '" + path + "' couldn't be deleted", e);
        }*/
        resolver.delete(uri, PushSettings.BasePushColumns._ID + " = ? ", new String[] { String.valueOf(id) });
    }
}
