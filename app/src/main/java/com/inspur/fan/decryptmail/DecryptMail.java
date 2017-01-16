package com.inspur.fan.decryptmail;

/**
 * Created by fan on 16-7-12.
 */
public class DecryptMail {
    private static final String TAG = "DecryptMail";

    public native void init();

    public native int decryptMail(String inFilePath, String password, String outFilePath, String certFilePath);

    public native int encryptMail(String inFilePath, String outFilePath, String publickKey);

    public native int signedMail(String inFilePath,String password,String outFilePath,String certFilePath);

    public native int verifySignedMail(String inFilePath,String outFilePath,String inspurCertFilePath);

    public native int decryptCmsMail(String inFilePath, String password, String outFilePath, String certFilePath);

    private native int clean();

    public native int test(String key);

    public void jclean() {
        clean();
    }

    static {
        System.loadLibrary("DecryptMail");
    }
}
