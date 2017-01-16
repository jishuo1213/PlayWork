package com.inspur.playwork.utils.encryptUtil;

import android.util.Log;

import com.inspur.playwork.config.AppConfig;
import com.inspur.playwork.utils.PreferencesHelper;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class EncryptUtil {

    private static String TAG = "EncryptUtil";

    /**
     * AES加密
     *
     * @param content  需要加密的内容
     * @param password 加密密钥
     * @return
     */
    private static byte[] aesEncrypt(String content, String password) {
        try {
            /*KeyGenerator kgen = KeyGenerator.getInstance("AES");
            kgen.init(128, new SecureRandom(password.getBytes()));
			SecretKey secretKey = kgen.generateKey();
			byte[] enCodeFormat = secretKey.getEncoded();
			SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");*/
            SecretKeySpec key = new SecretKeySpec(password.getBytes("UTF-8"), "AES");
            //Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
//            Cipher cipher = Cipher.getInstance("AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS7Padding");
            byte[] byteContent = content.getBytes("UTF-8");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(byteContent);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | UnsupportedEncodingException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * AES解密
     *
     * @param content  待解密内容
     * @param password 解密密钥
     * @return
     */
    private static byte[] aesDecrypt(byte[] content, String password) {
        try {
            SecretKeySpec key = new SecretKeySpec(password.getBytes("UTF-8"), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS7Padding");
//            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            return cipher.doFinal(content);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | UnsupportedEncodingException | BadPaddingException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * AES解密
     *
     * @param base64Content 待解密内容
     * @return
     * @throws Exception
     */
    public static String aesDecrypt(String base64Content) {
        byte[] content;
        if (base64Content.startsWith("<"))
            return base64Content;
        try {
            content = Base64Utils.decode(base64Content);
            content = aesDecrypt(content, PreferencesHelper.getInstance().key);
            if (content == null)
                return base64Content;
            return new String(content, "UTF-8");
        } catch (Exception e) {
            Log.e(TAG, "aesDecrypt: bad Base64" + base64Content);
            e.printStackTrace();
            return base64Content;
        }
    }

    /**
     * AES解密
     *
     * @param base64Content 待解密内容
     * @return
     * @throws Exception
     */
    public static String aesDecryptAD(String base64Content) {
        try {
            byte[] content = Base64Utils.decode(base64Content);
            content = aesDecrypt(content, AppConfig.AD_KEY);
            if (content == null)
                return base64Content;
            return new String(content, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            return base64Content;
        }
    }

    /**
     * 将16进制字符串转换成二进制数据
     *
     * @param hexString 16进制字符串
     * @return 二进制数据
     */
    public static byte[] convertHexString(String hexString) {
        byte digest[] = new byte[hexString.length() / 2];
        for (int i = 0; i < digest.length; i++) {
            String byteString = hexString.substring(2 * i, 2 * i + 2);
            int byteValue = Integer.parseInt(byteString, 16);
            digest[i] = (byte) byteValue;
        }
        return digest;
    }

    /**
     * 将二进制数据转换成16进制字符串
     *
     * @param b 二进制数据
     * @return 16进制字符串
     */
    public static String toHexString(byte b[]) {
        StringBuilder hexString = new StringBuilder();
        for (byte aB : b) {
            String plainText = Integer.toHexString(0xff & aB);
            if (plainText.length() < 2)
                plainText = "0" + plainText;
            hexString.append(plainText);
        }
        return hexString.toString();
    }

    public static String encrypt2aes(String message, String key) {
        String miwen;
        try {
            miwen = Base64Utils.encode(aesEncrypt(message, key));
        } catch (Exception e) {
            e.printStackTrace();
            miwen = "";
        }
        return miwen;
    }

    public static String encrypt2aes(String message) {
        String miwen;
        try {
            miwen = Base64Utils.encode(aesEncrypt(message, PreferencesHelper.getInstance().key));
        } catch (Exception e) {
            e.printStackTrace();
            miwen = "";
        }
        return miwen;
    }

    public static String encrypt2aesAD(String message) {
        String miwen;
        try {
            miwen = Base64Utils.encode(aesEncrypt(message, AppConfig.AD_KEY));
        } catch (Exception e) {
            e.printStackTrace();
            miwen = "";
        }
        return miwen;
    }

    public static void main(String[] args) throws Exception {

        String message = "home\\sunyuan";
        String key = "!QAZ2wsx@WSX1qaz";

        String miwen = "ACiotJo53dSv0JsduBzhlw==";

        System.out.println("加密数据:" + message);
        String jiami = encrypt2aes(message, key);
        System.out.println(TAG + "加密后的数据为:" + jiami);

        String jiemi1 = aesDecrypt(jiami);
        System.out.println(TAG + "解密后的数据1:" + jiemi1);

        String jiemi = aesDecrypt(miwen);
        System.out.println(TAG + "解密后的数据2:" + jiemi);
    }
}
