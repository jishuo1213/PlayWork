package com.inspur.playwork.utils.encryptUtil;


import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * <p>
 * BASE64编码解码工具包
 * </p>
 * <p>
 * </p>
 * 
 * @author zhaohaixing
 * @version 1.0
 */
public class Base64Utils {

    /**
     * 文件读取缓冲区大小
     */
    private static final int CACHE_SIZE = 1024;
    
    /**
     * <p>
     * BASE64字符串解码为二进制数据
     * </p>
     * 
     * @param base64
     * @return
     * @throws Exception
     */
    public static byte[] decode(String base64) throws IllegalArgumentException{
        return Base64.decode(base64,Base64.DEFAULT);
    }
    
    /**
     * <p>
     * 二进制数据编码为BASE64字符串
     * </p>
     * 
     * @param bytes
     * @return
     * @throws Exception
     */
    public static String encode(byte[] bytes) throws Exception {
        return new String(Base64.encode(bytes,Base64.DEFAULT));
    }

    

    
	/**
	 * <p>
	 * 将输入流编码为BASE64字符串
	 * </p>
	 * <p>
	 * 大文件慎用，可能会导致内存溢出
	 * </p>
	 * 
	 * @param in 输入流
	 * @return BASE64字符串
	 * @throws Exception
	 */
	public static String encodeStream(InputStream in) throws Exception {
		byte[] bytes = streamToByte(in);
		return encode(bytes);
	}

    /**
     * <p>
     * 输入流转换为二进制数组
     * </p>
     * 
     * @param in 输入流
     * @return
     * @throws Exception
     */
	public static byte[] streamToByte(InputStream in) throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream(2048);
		byte[] cache = new byte[CACHE_SIZE];
		int nRead = 0;
		while ((nRead = in.read(cache)) != -1) {
			out.write(cache, 0, nRead);
			out.flush();
		}
		out.close();
		in.close();
		return out.toByteArray();
	}

    public static void main(String[] args) throws Exception {
        String x1 = new String(decode("MjA6MTY6RDg6MTA6MzQ6NDUxNDU4RDU1MUJGRUJGQkZGMDAwMzA2QTk="));
        System.out.println("解密后：" + x1);
	}
}
