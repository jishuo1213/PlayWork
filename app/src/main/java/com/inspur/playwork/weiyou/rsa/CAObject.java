package com.inspur.playwork.weiyou.rsa;

import java.util.Date;

/**
 * Created by sunyuan on 2016/4/12 0012 10:47.
 * Email: sunyuan@inspur.com
 */
public class CAObject {
    private String filepath;
    private String password;
    private String caname;
    private String cn;
    private String sn;
    private String issuer;
    private Date cert_date;
//    private PublicKey publicKey;
//    private PrivateKey privateKey;
    private boolean isDefaultCA;

    private String errorInfo;

    public CAObject() {}

    public CAObject(String filepath, String password, String caname, String cn, String sn, String issuer,
                    Date cert_date, boolean isDefaultCA) {
        this.filepath = filepath;
        this.password = password;
        this.caname = caname;
        this.cn = cn;
        this.sn = sn;
        this.issuer = issuer;
        this.cert_date = cert_date;
//        this.publicKey = publicKey;
//        this.privateKey = privateKey;
        this.isDefaultCA = isDefaultCA;
    }

    public String getCaname() {
        return caname;
    }

    public void setCaname(String caname) {
        this.caname = caname;
    }

    public String getCn() {
        return cn;
    }

    public void setCn(String cn) {
        this.cn = cn;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isDefaultCA() {
        return isDefaultCA;
    }

    public void setDefaultCA(boolean defaultCA) {
        isDefaultCA = defaultCA;
    }

    public Date getCert_date() {
        return cert_date;
    }

    public void setCert_date(Date cert_date) {
        this.cert_date = cert_date;
    }

//    public PublicKey getPublicKey() {
//        return publicKey;
//    }
//
//    public void setPublicKey(PublicKey publicKey) {
//        this.publicKey = publicKey;
//    }
//
//    public PrivateKey getPrivateKey() {
//        return privateKey;
//    }
//
//    public void setPrivateKey(PrivateKey privateKey) {
//        this.privateKey = privateKey;
//    }

    public String getErrorInfo() {
        return errorInfo;
    }

    public void setErrorInfo(String errorInfo) {
        this.errorInfo = errorInfo;
    }

    @Override
    public String toString() {
        return "CAObject{" +
                "filepath='" + filepath + '\'' +
                ", password='" + password + '\'' +
                ", isDefaultCA=" + isDefaultCA +
                ", caname='" + caname + '\'' +
                ", cn='" + cn + '\'' +
                ", issuer='" + issuer + '\'' +
                ", cert_date=" + cert_date +
//                ", publicKey=" + publicKey +
//                ", privateKey=" + privateKey +
                '}';
    }
}
