package com.inspur.playwork.weiyou.rsa;

import android.os.Handler;
import android.util.Log;

import com.inspur.playwork.config.AppConfig;
import com.inspur.playwork.utils.FileUtil;
import com.inspur.playwork.utils.PreferencesHelper;
import com.inspur.playwork.utils.json.GsonUtils;
import com.inspur.playwork.weiyou.utils.OkHttpClientManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;


/**
 * @类名: CAEncryptUtils
 * @描述: 供给Js调用的Java类
 * @作者: ★ 孙 源 ★
 * @创建时间: 2013-9-12 下午11:21:06
 */
public class CAEncryptUtils {
    private final static String TAG = "CAEncryptUtils";
    private final static String PBKEY_SUFFIX = "_pbKey";
    private final static String CURR_USING_CA = "_cuca";
    public final static String SAFTY_INFO = "_caData";
    public final static String DEFAULT_ENCRYPT = "_eway";
    public final static String DEFAULT_Sign = "_sway";

    /**
     * 取出SharePreference中的证书列表信息并转化成List返回
     * 读取已安装的证书数据
     *
     * @return
     */
    public static List<CAObject> getCAListData(String account) {
        List<CAObject> list = new ArrayList<>();
        Log.i(TAG, "getCAListData key:" + account + SAFTY_INFO);
        String json = PreferencesHelper.getInstance().readStringPreference(account + SAFTY_INFO);
        if (json.length() > 0) {
            String jsonStr[] = json.split("※");
            int i;
            int l = jsonStr.length;
            for (i = 0; i < l; i++) {
                String caoStr = jsonStr[i];
                Log.i(TAG, "getCAListData caoStr:" + caoStr);
                CAObject cao = GsonUtils.json2Bean(caoStr, CAObject.class);
                if (cao != null) {
                    list.add(cao);
                } else {
                    Log.i(TAG, "getCAListData " + i + " cao==null");
                }
            }
        } else {
            Log.i(TAG, "getCAListData json==\"\"");
        }
        return list;
    }

    public static void saveCaListData(String account, List<CAObject> caList) {
        String jsonStr = "";
        for (CAObject cao : caList) {
            String caoStr = GsonUtils.bean2Json(cao, CAObject.class);
            jsonStr += caoStr + ((caList.indexOf(cao) == caList.size() - 1) ? "" : "※");
        }
        PreferencesHelper.getInstance().writeToPreferencesFree(account + SAFTY_INFO, jsonStr);
    }

    public static void setDefaultEncrypt(String account, boolean flag) {
        PreferencesHelper.getInstance().writeToPreferences(account + DEFAULT_ENCRYPT, flag);
    }

    public static boolean getDefaultEncrypt(String account) {
        return PreferencesHelper.getInstance().readBooleanPreference(account + DEFAULT_ENCRYPT);
    }

    public static void setDefaultSign(String account, boolean flag) {
        PreferencesHelper.getInstance().writeToPreferences(account + DEFAULT_Sign, flag);
    }

    public static boolean getDefaultSign(String account) {
        return PreferencesHelper.getInstance().readBooleanPreference(account + DEFAULT_Sign);
    }

    /**
     * 定义证书信息的读取函数如下：
     *
     * @param email
     * @param caPath
     * @param caName
     * @param password
     * @return
     */
    public static CAObject readCAFile(String email,String caPath, String caName, String password) {
        PublicKey Userpubkey;
        PrivateKey priKey = null;
        String caAlias = null;
        X509Certificate x509certificate = null;
        CAObject cao = new CAObject();
        try {
            FileInputStream in1 = new FileInputStream(caPath);
            KeyStore ks = KeyStore.getInstance("PKCS12", "BC");
            ks.load(in1, password.toCharArray());

            Enumeration e = ks.aliases();
            while (e.hasMoreElements()) {
                String alias = (String) e.nextElement();

                x509certificate = (X509Certificate) ks.getCertificate(alias);
                Key key = ks.getKey(alias, password.toCharArray());
                if (key != null) {
//                    priKey = (PrivateKey) key;
                } else {
                    caAlias = alias;
                }
            }
            if (x509certificate != null) {
                x509certificate.checkValidity();//检查证书是否有效，若已过期则抛出异常
                String subjectDN = x509certificate.getSubjectDN().getName();
                String subjectArr[] = subjectDN.split(",");
                String username = "";//用户名
                for (String str : subjectArr) {
                    if (str.indexOf("CN=") != -1) {
                        username = str.split("=")[1];
                    }
                }
                String issuerInfo[] = x509certificate.getIssuerDN().getName().split(",");
                String issuer = "";//颁发者
                for (String str : issuerInfo) {
                    if (str.indexOf("CN=") != -1) {
                        issuer = str.split("=")[1];
                    }
                }
                if (caAlias == null) caAlias = caName;
//                Userpubkey = x509certificate.getPublicKey();//公钥
                String newName = createSHA1(caAlias)+".so";
                if(!caPath.contains(newName)){//如果不包含 说明是新安装的 需要把证书保存到程序目录
                    caPath = FileUtil.getCurrMailSafetyPath(email) + newName;
                    File outF = new File(caPath);
                    boolean saveRes = FileUtil.saveZSFile(in1,outF); //把证书文件保存到程序目录中
                    if(!saveRes)
                        cao.setErrorInfo("证书保存失败，请联系");
                }
                cao = new CAObject(caPath, password, caAlias, username, x509certificate.getSerialNumber().toString(),
                        issuer, x509certificate.getNotAfter(), true);
            }else{
                cao.setErrorInfo("证书文件已损坏，请重新选择文件");
            }
            in1.close();
        } catch (CertificateExpiredException e) {
            e.printStackTrace();
            cao.setErrorInfo("证书已过期，安装失败");
        } catch (CertificateNotYetValidException e) {
            e.printStackTrace();
            cao.setErrorInfo("证书已失效，安装失败");
        } catch (CertificateException e1) {
            e1.printStackTrace();
            cao.setErrorInfo("证书异常，安装失败");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            cao.setErrorInfo("证书文件未检测到，安装失败");
        } catch (IOException e) {
            e.printStackTrace();
            cao.setErrorInfo("证书密码错误，读取失败");
        } catch (KeyStoreException e) {
            e.printStackTrace();
            cao.setErrorInfo("keyStore异常，证书安装失败");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            cao.setErrorInfo("加密算法错误，证书安装失败");
        } catch (Exception e1) {
            e1.printStackTrace();
            cao.setErrorInfo("证书因未知异常导致安装失败");
        }
        return cao;
    }

    /**
     * 获取公钥
     *
     * @param userIds
     * @param loginName
     * @param password
     * @param callback
     */
    public static void getPublicKey(String userIds, String loginName, String password, OkHttpClientManager.HttpOperationCallback callback) {
        String url = AppConfig.GET_PUBLIC_KEY_ADDR + "?toSearchUid=" + userIds + "&loginName=" + loginName + "&password=" + password+"&pem=1";
        OkHttpClientManager.getInstance().initHandler(new Handler()).get(url, callback);
    }

    /**
     * 生成SHA1值
     *
     * @param strSrc
     * @return
     */
    public static String createSHA1(String strSrc) {

        byte[] hash;
        try {
            hash = MessageDigest.getInstance("SHA1").digest(strSrc.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Huh, MD5 should be supported?", e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Huh, UTF-8 should be supported?", e);
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10)
                hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }
        return hex.toString();

    }
}
