//
// Created by fan on 16-7-12.
//
#include "smime.h"
#include "opensslutil.h"
#include "strutil.h"
#include <openssl/pkcs12.h>
#include <openssl/pem.h>

int dump_certs_keys_p12(BIO *out, PKCS12 *p12, char *pass, int passlen,
                        int options, char *pempass);

int dump_certs_pkeys_bags(BIO *out, STACK_OF(PKCS12_SAFEBAG) *bags,
                          char *pass, int passlen, int options,
                          char *pempass);

int dump_certs_pkeys_bag(BIO *out, PKCS12_SAFEBAG *bag, char *pass,
                         int passlen, int options, char *pempass);

int print_attribs(BIO *out, STACK_OF(X509_ATTRIBUTE) *attrlst,
                  const char *name);

void hex_prin(BIO *out, unsigned char *buf, int len);

int parse_cert_file(const char *infile, const char *password, const char *outfile);

static int smime_cb(int ok, X509_STORE_CTX *ctx);

static int save_certs(char *signerfile, STACK_OF(X509) *signers);

int decrypt_smime(const char *recipfile, const char *pass, const char *infile,
                  const char *outfile) {

    X509 *recip = NULL;
    const char *keyfile = NULL;
    EVP_PKEY *key = NULL;
    BIO *in = NULL, *out = NULL, *indata = NULL;
    PKCS7 *p7 = NULL;
    int flags = PKCS7_DETACHED;
    if (!recipfile)
        return 1;
    char *prefix = ".cer";
    char *cretout = OPENSSL_malloc(strlen(recipfile) + 5);
    strcpy(cretout, recipfile);
    strcat(cretout, prefix);
    if (parse_cert_file(recipfile, pass, cretout)) {
        LOGI(cretout);
        OPENSSL_free(cretout);
        return 8;
    }
    recip = load_cert(cretout);
    int ret = 0;
    if (recip) {
        keyfile = cretout;
        key = load_key(keyfile, pass);
        if (key) {
            if ((in = BIO_new_file(infile, "r"))) {
                p7 = SMIME_read_PKCS7(in, &indata);
                if (p7) {
                    out = BIO_new_file(outfile, "w");
                    if (out) {
                        flags &= ~PKCS7_DETACHED;
                        if (PKCS7_decrypt(p7, key, recip, out, flags))
                            ret = 0;
                        else
                            ret = 7;
                    } else {
                        ret = 6;
                    }
                } else {
                    ret = 5;
                }
            } else {
                ret = 4;
            }
        } else {
            ret = 3;
        }
    } else {
        ret = 2;
    }
    X509_free(recip);
    EVP_PKEY_free(key);
    BIO_free(in);
    BIO_free(indata);
    BIO_free_all(out);
    PKCS7_free(p7);
    OPENSSL_free(cretout);
    return ret;
}

int parse_cert_file(const char *infile, const char *password, const char *outfile) {
    BIO *in = NULL, *out = NULL;
    PKCS12 *p12 = NULL;
    char *pass = OPENSSL_malloc(strlen(password) + 1);
    strcpy(pass, password);
    int ret = 0;
    LOGI("infile:%s\npassword:%s\noutfile:%s", infile, password, outfile);
    if (infile) {
        in = BIO_new_file(infile, "rb");
        if (in) {
            if ((out = BIO_new_file(outfile, "wb"))) {
                if ((p12 = d2i_PKCS12_bio(in, NULL))) {
                    if (PKCS12_verify_mac(p12, password, -1)) {
                        if (dump_certs_keys_p12(out, p12, pass, -1, 0, NULL)) {
                            ret = 0;
                        } else {
                            ret = 6;
                        }
                    } else {
                        ret = 5;
                    }
                } else {
                    ret = 4;
                }
            } else {
                ret = 3;
            }
        } else {
            ret = 2;
        }
    } else {
        ret = 1;
    }
    LOGI("ret = %d", ret);
    if (p12)
        PKCS12_free(p12);
    BIO_free(in);
    BIO_free_all(out);
    OPENSSL_free(pass);
    return ret;
}

int dump_certs_keys_p12(BIO *out, PKCS12 *p12, char *pass,
                        int passlen, int options, char *pempass) {
    STACK_OF(PKCS7) *asafes = NULL;
    STACK_OF(PKCS12_SAFEBAG) *bags;
    int i, bagnid;
    int ret = 0;
    PKCS7 *p7;

    if (!(asafes = PKCS12_unpack_authsafes(p12)))
        return ret;
    for (i = 0; i < sk_PKCS7_num(asafes); i++) {
        p7 = sk_PKCS7_value(asafes, i);
        bagnid = OBJ_obj2nid(p7->type);
        if (bagnid == NID_pkcs7_data) {
            bags = PKCS12_unpack_p7data(p7);
        } else if (bagnid == NID_pkcs7_encrypted) {
            bags = PKCS12_unpack_p7encdata(p7, pass, passlen);
        } else
            continue;
        if (!bags)
            goto err;
        if (!dump_certs_pkeys_bags(out, bags, pass, passlen,
                                   options, pempass)) {
            sk_PKCS12_SAFEBAG_pop_free(bags, PKCS12_SAFEBAG_free);
            goto err;
        }
        sk_PKCS12_SAFEBAG_pop_free(bags, PKCS12_SAFEBAG_free);
    }
    ret = 1;

    err:
    sk_PKCS7_pop_free(asafes, PKCS7_free);
    return ret;
}

int dump_certs_pkeys_bags(BIO *out, STACK_OF(PKCS12_SAFEBAG) *bags,
                          char *pass, int passlen, int options, char *pempass) {
    int i;
    for (i = 0; i < sk_PKCS12_SAFEBAG_num(bags); i++) {
        if (!dump_certs_pkeys_bag(out,
                                  sk_PKCS12_SAFEBAG_value(bags, i),
                                  pass, passlen, options, pempass))
            return 0;
    }
    return 1;
}

int dump_certs_pkeys_bag(BIO *out, PKCS12_SAFEBAG *bag, char *pass,
                         int passlen, int options, char *pempass) {
    EVP_PKEY *pkey;
    PKCS8_PRIV_KEY_INFO *p8;
    X509 *x509;

    switch (M_PKCS12_bag_type(bag)) {
        case NID_keyBag:
            print_attribs(out, bag->attrib, "Bag Attributes");
            p8 = bag->value.keybag;
            if (!(pkey = EVP_PKCS82PKEY(p8)))
                return 0;
            PEM_write_bio_PrivateKey(out, pkey, NULL, NULL, 0, NULL, pempass);
            EVP_PKEY_free(pkey);
            break;

        case NID_pkcs8ShroudedKeyBag:
            print_attribs(out, bag->attrib, "Bag Attributes");
            if (!(p8 = PKCS12_decrypt_skey(bag, pass, passlen)))
                return 0;
            if (!(pkey = EVP_PKCS82PKEY(p8))) {
                PKCS8_PRIV_KEY_INFO_free(p8);
                return 0;
            }
            print_attribs(out, p8->attributes, "Key Attributes");
            PKCS8_PRIV_KEY_INFO_free(p8);
            PEM_write_bio_PrivateKey(out, pkey, NULL, NULL, 0, NULL, pempass);
            EVP_PKEY_free(pkey);
            break;

        case NID_certBag:
            print_attribs(out, bag->attrib, "Bag Attributes");
            if (M_PKCS12_cert_bag_type(bag) != NID_x509Certificate)
                return 1;
            if (!(x509 = PKCS12_certbag2x509(bag)))
                return 0;
            dump_cert_text(out, x509);
            PEM_write_bio_X509(out, x509);
            X509_free(x509);
            break;

        case NID_safeContentsBag:
            print_attribs(out, bag->attrib, "Bag Attributes");
            return dump_certs_pkeys_bags(out, bag->value.safes, pass,
                                         passlen, options, pempass);
        default:
            return 1;
    }
    return 1;
}

int sign(const char *signerfile, const char *pass, const char *infile, const char *outfile) {
    STACK_OF(OPENSSL_STRING) *sksigners = NULL, *skkeys = NULL;
    const char *keyfile = NULL;
    int flag = PKCS7_DETACHED;
    int ret = 0;
    BIO *in = NULL, *out = NULL;
    EVP_PKEY *key = NULL;
    PKCS7 *p7 = NULL;
    X509 *signer = NULL;
    char *prefix = ".cer";
    char *cretout = OPENSSL_malloc(strlen(signerfile) + 5);
    strcpy(cretout, signerfile);
    strcat(cretout, prefix);
    if (parse_cert_file(signerfile, pass, cretout)) {
        OPENSSL_free(cretout);
        return 8;
    }
    if (cretout) {
        sksigners = sk_OPENSSL_STRING_new_null();
        sk_OPENSSL_STRING_push(sksigners, cretout);
        skkeys = sk_OPENSSL_STRING_new_null();
        keyfile = cretout;
        sk_OPENSSL_STRING_push(skkeys, keyfile);
    }
    in = BIO_new_file(infile, "r");
    if (in) {
        if ((out = BIO_new_file(outfile, "w")) != NULL) {
            int i;
            if (flag & PKCS7_DETACHED) {
                flag |= PKCS7_STREAM;
                flag |= PKCS7_PARTIAL;
                p7 = PKCS7_sign(NULL, NULL, NULL, in, flag);
                if (p7) {
                    for (i = 0; i < sk_OPENSSL_STRING_num(sksigners); i++) {
                        cretout = sk_OPENSSL_STRING_value(sksigners, i);
                        keyfile = sk_OPENSSL_STRING_value(skkeys, i);
                        signer = load_cert(cretout);
                        if (signer) {
                            key = load_key(keyfile, pass);
                            if (key) {
                                if (!PKCS7_sign_add_signer(p7, signer, key, NULL, flag)) {
                                    X509_free(signer);
                                    EVP_PKEY_free(key);
                                    ret = 6;
                                } else {
                                    X509_free(signer);
                                    EVP_PKEY_free(key);
                                }
                            } else {
                                ret = 5;
                            }
                        } else {
                            ret = 4;
                        }
                    }
                    SMIME_write_PKCS7(out, p7, in, flag);
                } else {
                    ret = 3;
                }
            }
        } else {
            ret = 2;
        }
    } else {
        ret = 1;
    }
    OPENSSL_free(cretout);
    PKCS7_free(p7);
    BIO_free(in);
    BIO_free_all(out);
    return ret;
}

int encrypt_mime(const char *recipfile, const char *infile, const char *outfile) {
    const EVP_CIPHER *chiper = EVP_des_ede3_cbc();
    STACK_OF(X509) *encerts = NULL;
    X509 *cert = NULL;
    int ret = 0;
    BIO *in = NULL, *out = NULL;
    PKCS7 *p7 = NULL;
    int flags = PKCS7_DETACHED;
//    char *prefix = ".cer";
//    char *cretout = OPENSSL_malloc(strlen(recipfile) + 5);
//    strcpy(cretout, recipfile);
//    strcat(cretout, prefix);
//    if (parse_cert_file(recipfile, pass, cretout)) {
//        OPENSSL_free(cretout);
//        return 8;
//    }
    if (chiper) {
        encerts = sk_X509_new_null();
        cert = load_cert_by_string(recipfile);
        if (cert) {
            sk_X509_push(encerts, cert);
            if ((in = BIO_new_file(infile, "r"))) {
                if (((out = BIO_new_file(outfile, "w")))) {
                    flags &= ~PKCS7_DETACHED;
                    LOGI("%x", flags);
                    p7 = PKCS7_encrypt(encerts, in, chiper, flags);
                    if (p7) {
                        SMIME_write_PKCS7(out, p7, in, flags);
                    } else {
                        ret = 5;
                    }
                } else {
                    ret = 4;
                }
            } else {
                ret = 3;
            }
        } else {
            ret = 2;
        }
    } else {
        ret = 1;
    }
    sk_X509_pop_free(encerts, X509_free);
    PKCS7_free(p7);
    BIO_free(in);
    BIO_free_all(out);
//    OPENSSL_free(cretout);
//    X509_free(cert);
    return ret;
}


int verify_mime(const char *infile, char *outfile, const char *CAfile) {
    BIO *in = NULL, *out = NULL;
    int ret = 0;
    int flags = PKCS7_DETACHED;
    flags &= ~PKCS7_DETACHED;
    char *outmode = "w", *inmode = "r";
    PKCS7 *p7 = NULL;
    BIO *indata = NULL;
    X509_STORE *store = NULL;
    char *temp_cafile = OPENSSL_malloc(strlen(CAfile) + 1);
    strcpy(temp_cafile, CAfile);
    if (flags & PKCS7_BINARY)
        outmode = "wb";
    if ((in = BIO_new_file(infile, inmode))) {
        p7 = SMIME_read_PKCS7(in, &indata);
        if (p7) {
            if ((out = BIO_new_file(outfile, outmode))) {
                if ((store = setup_verify(temp_cafile, NULL))) {
                    X509_STORE_set_verify_cb(store, smime_cb);
                    STACK_OF(X509) *signers;
                    if (PKCS7_verify(p7, NULL, store, indata, out, flags)) {
                        signers = PKCS7_get0_signers(p7, NULL, flags);
                        if (!save_certs(NULL, signers))
                            ret = 5;
                        sk_X509_free(signers);
                    } else {
                        ret = 4;
                    }
                } else {
                    ret = 6;
                }
            } else {
                ret = 3;
            }
        } else {
            ret = 2;
        }
    } else {
        ret = 1;
    }

    BIO_free(in);
    BIO_free(out);
    BIO_free(indata);
    PKCS7_free(p7);
    X509_STORE_free(store);
    OPENSSL_free(temp_cafile);
    return ret;
}

int print_attribs(BIO *out, STACK_OF(X509_ATTRIBUTE) *attrlst,
                  const char *name) {
    X509_ATTRIBUTE *attr;
    ASN1_TYPE *av;
    char *value;
    int i, attr_nid;
    if (!attrlst) {
        BIO_printf(out, "%s: <No Attributes>\n", name);
        return 1;
    }
    if (!sk_X509_ATTRIBUTE_num(attrlst)) {
        BIO_printf(out, "%s: <Empty Attributes>\n", name);
        return 1;
    }
    BIO_printf(out, "%s\n", name);
    for (i = 0; i < sk_X509_ATTRIBUTE_num(attrlst); i++) {
        attr = sk_X509_ATTRIBUTE_value(attrlst, i);
        attr_nid = OBJ_obj2nid(attr->object);
        BIO_printf(out, "    ");
        if (attr_nid == NID_undef) {
            i2a_ASN1_OBJECT(out, attr->object);
            BIO_printf(out, ": ");
        } else
            BIO_printf(out, "%s: ", OBJ_nid2ln(attr_nid));

        if (sk_ASN1_TYPE_num(attr->value.set)) {
            av = sk_ASN1_TYPE_value(attr->value.set, 0);
            switch (av->type) {
                case V_ASN1_BMPSTRING:
                    value = OPENSSL_uni2asc(av->value.bmpstring->data,
                                            av->value.bmpstring->length);
                    BIO_printf(out, "%s\n", value);
                    OPENSSL_free(value);
                    break;

                case V_ASN1_OCTET_STRING:
                    hex_prin(out, av->value.octet_string->data,
                             av->value.octet_string->length);
                    BIO_printf(out, "\n");
                    break;

                case V_ASN1_BIT_STRING:
                    hex_prin(out, av->value.bit_string->data,
                             av->value.bit_string->length);
                    BIO_printf(out, "\n");
                    break;

                default:
                    BIO_printf(out, "<Unsupported tag %d>\n", av->type);
                    break;
            }
        } else
            BIO_printf(out, "<No Values>\n");
    }
    return 1;
}

void hex_prin(BIO *out, unsigned char *buf, int len) {
    int i;
    for (i = 0; i < len; i++)
        BIO_printf(out, "%02X ", buf[i]);
}

static int smime_cb(int ok, X509_STORE_CTX *ctx) {
    int error;

    error = X509_STORE_CTX_get_error(ctx);

    if ((error != X509_V_ERR_NO_EXPLICIT_POLICY)
        && ((error != X509_V_OK) || (ok != 2)))
        return ok;

    policies_print(NULL, ctx);

    return ok;

}


static int save_certs(char *signerfile, STACK_OF(X509) *signers) {
    int i;
    BIO *tmp;
    if (!signerfile)
        return 1;
    tmp = BIO_new_file(signerfile, "w");
    if (!tmp)
        return 0;
    for (i = 0; i < sk_X509_num(signers); i++)
        PEM_write_bio_X509(tmp, sk_X509_value(signers, i));
    BIO_free(tmp);
    return 1;
}