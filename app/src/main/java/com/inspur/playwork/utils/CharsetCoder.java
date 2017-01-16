package com.inspur.playwork.utils;

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

/**
 * Created by Fan on 15-9-16.
 */
public class CharsetCoder {

    private static final String TAG = "CharsetCoderFan";


    public static CharsetCoder getInstance() {
//        return SingleRefreshManager.getInstance().getCharsetCoder();
        return null;
    }

    private CharsetEncoder charinEncoder, charinEncoder2;
    private CharsetDecoder charOutDecoder, charOutDecoder2;

    public CharsetCoder() {
    }

    public void init() {
        Charset inSet = Charset.forName("iso8859-1");
        Charset outSet = Charset.forName("UTF-8");

        charinEncoder = inSet.newEncoder();
        charOutDecoder = outSet.newDecoder();

        charinEncoder2 = outSet.newEncoder();
        charOutDecoder2 = inSet.newDecoder();
    }


    public String convertStringCharsetCode(String in) {
        try {
            ByteBuffer byteBuffer = charinEncoder.encode(CharBuffer.wrap(in.toCharArray()));
            CharBuffer charBuffer = charOutDecoder.decode(byteBuffer);
            return charBuffer.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return in;
        }
    }


    public String conventRequestCharSet(String str) {
        Log.i(TAG, "conventRequestCharSet: ");
        try {
            ByteBuffer byteBuffer = charinEncoder2.encode(CharBuffer.wrap(str.toCharArray()));
            CharBuffer charBuffer = charOutDecoder2.decode(byteBuffer);
            return charBuffer.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return str;
        }
    }
}
