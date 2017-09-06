package com.cappu.launcherwin.zipUtil.crypto;

import com.cappu.launcherwin.zipUtil.exception.ZipException;

public  interface IDecrypter {
    
    public int decryptData(byte[] buff, int start, int len) throws ZipException;
      
    public int decryptData(byte[] buff) throws ZipException;
      
} 