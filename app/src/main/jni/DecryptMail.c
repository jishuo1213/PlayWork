#include <jni.h>
#include "smime.h"
#include "strutil.h"
#include "opensslutil.h"


JNIEXPORT jint JNICALL
Java_com_inspur_fan_decryptmail_DecryptMail_decryptMail(JNIEnv *env, jobject instance,
                                                        jstring inFilePath_, jstring password_,
                                                        jstring outFilePath_,
                                                        jstring certFilePath_) {
    const char *inFilePath = (*env)->GetStringUTFChars(env, inFilePath_, 0);
    const char *password = (*env)->GetStringUTFChars(env, password_, 0);
    const char *outFilePath = (*env)->GetStringUTFChars(env, outFilePath_, 0);
    const char *certFilePath = (*env)->GetStringUTFChars(env, certFilePath_, 0);

    LOGI(inFilePath);
    LOGI(password);
    LOGI(outFilePath);
    LOGI(certFilePath);
    int ret = decrypt_smime(certFilePath, password, inFilePath, outFilePath);


    (*env)->ReleaseStringUTFChars(env, inFilePath_, inFilePath);
    (*env)->ReleaseStringUTFChars(env, password_, password);
    (*env)->ReleaseStringUTFChars(env, outFilePath_, outFilePath);
    (*env)->ReleaseStringUTFChars(env, certFilePath_, certFilePath);
    return ret;
}

JNIEXPORT jint JNICALL
Java_com_inspur_fan_decryptmail_DecryptMail_clean__(JNIEnv *env, jobject instance) {
    apps_shutdown();
}

JNIEXPORT void JNICALL
Java_com_inspur_fan_decryptmail_DecryptMail_init(JNIEnv *env, jobject instance) {
    apps_startup();
}

JNIEXPORT jint JNICALL
Java_com_inspur_fan_decryptmail_DecryptMail_signedMail(JNIEnv *env, jobject instance,
                                                       jstring inFilePath_, jstring password_,
                                                       jstring outFilePath_,
                                                       jstring certFilePath_) {
    const char *inFilePath = (*env)->GetStringUTFChars(env, inFilePath_, 0);
    const char *password = (*env)->GetStringUTFChars(env, password_, 0);
    const char *outFilePath = (*env)->GetStringUTFChars(env, outFilePath_, 0);
    const char *certFilePath = (*env)->GetStringUTFChars(env, certFilePath_, 0);

    // TODO
    int ret = sign(certFilePath, password, inFilePath, outFilePath);

    (*env)->ReleaseStringUTFChars(env, inFilePath_, inFilePath);
    (*env)->ReleaseStringUTFChars(env, password_, password);
    (*env)->ReleaseStringUTFChars(env, outFilePath_, outFilePath);
    (*env)->ReleaseStringUTFChars(env, certFilePath_, certFilePath);
    return ret;
}

JNIEXPORT jint JNICALL
Java_com_inspur_fan_decryptmail_DecryptMail_encryptMail(JNIEnv *env, jobject instance,
                                                        jstring inFilePath_,
                                                        jstring outFilePath_,
                                                        jstring certFilePath_) {
    const char *inFilePath = (*env)->GetStringUTFChars(env, inFilePath_, 0);
//    const char *password = (*env)->GetStringUTFChars(env, password_, 0);
    const char *outFilePath = (*env)->GetStringUTFChars(env, outFilePath_, 0);
    const char *certFilePath = (*env)->GetStringUTFChars(env, certFilePath_, 0);

    // TODO
    int ret = encrypt_mime(certFilePath, inFilePath, outFilePath);

    (*env)->ReleaseStringUTFChars(env, inFilePath_, inFilePath);
    (*env)->ReleaseStringUTFChars(env, outFilePath_, outFilePath);
    (*env)->ReleaseStringUTFChars(env, certFilePath_, certFilePath);
    return ret;
}

JNIEXPORT jint JNICALL
Java_com_inspur_fan_decryptmail_DecryptMail_test(JNIEnv *env, jobject instance, jstring key_) {
    const char *key = (*env)->GetStringUTFChars(env, key_, 0);

    LOGI("%s", key);
    X509 *x509 = load_cert_by_string(key);
    if (x509 == NULL) {
        LOGI("%s","x509 == NULL");
    }

    (*env)->ReleaseStringUTFChars(env, key_, key);
}

JNIEXPORT jint JNICALL
Java_com_inspur_fan_decryptmail_DecryptMail_verifySignedMail(JNIEnv *env, jobject instance,
                                                         jstring inFilePath_, jstring outFilePath_,
                                                         jstring inspurCertFilePath_) {
    const char *inFilePath = (*env)->GetStringUTFChars(env, inFilePath_, 0);
    const char *outFilePath = (*env)->GetStringUTFChars(env, outFilePath_, 0);
    const char *inspurCertFilePath = (*env)->GetStringUTFChars(env, inspurCertFilePath_, 0);

    int res = verify_mime(inFilePath,outFilePath,inspurCertFilePath);
    // TODO

    (*env)->ReleaseStringUTFChars(env, inFilePath_, inFilePath);
    (*env)->ReleaseStringUTFChars(env, outFilePath_, outFilePath);
    (*env)->ReleaseStringUTFChars(env, inspurCertFilePath_, inspurCertFilePath);
    return res;
}

JNIEXPORT jint JNICALL
Java_com_inspur_fan_decryptmail_DecryptMail_decryptCmsMail(JNIEnv *env, jobject instance,
                                                           jstring inFilePath_, jstring password_,
                                                           jstring outFilePath_,
                                                           jstring certFilePath_) {
    const char *inFilePath = (*env)->GetStringUTFChars(env, inFilePath_, 0);
    const char *password = (*env)->GetStringUTFChars(env, password_, 0);
    const char *outFilePath = (*env)->GetStringUTFChars(env, outFilePath_, 0);
    const char *certFilePath = (*env)->GetStringUTFChars(env, certFilePath_, 0);


    // TODO
    int res = decrypt_cms(certFilePath,password,inFilePath,outFilePath);

    (*env)->ReleaseStringUTFChars(env, inFilePath_, inFilePath);
    (*env)->ReleaseStringUTFChars(env, password_, password);
    (*env)->ReleaseStringUTFChars(env, outFilePath_, outFilePath);
    (*env)->ReleaseStringUTFChars(env, certFilePath_, certFilePath);
    return res;
}