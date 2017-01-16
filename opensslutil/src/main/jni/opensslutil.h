//
// Created by fan on 16-7-12.
//

#ifndef DECRYPTMAIL_OPENSSLUTIL_H
#define DECRYPTMAIL_OPENSSLUTIL_H

//# include <openssl/bio.h>
//#include "e_os.h"

#include <openssl/x509.h>
#include <openssl/err.h>
#include <openssl/pkcs7.h>
#include <openssl/evp.h>
#include <openssl/ui.h>
#include <openssl/conf.h>
#include <openssl/rand.h>
//# define PW_MIN_LENGTH 4
typedef struct pw_cb_data {
    const void *password;
    const char *prompt_info;
} PW_CB_DATA;


# ifdef SIGPIPE
#  define do_pipe_sig()   signal(SIGPIPE,SIG_IGN)
# else
#  define do_pipe_sig()
# endif

# ifdef OPENSSL_NO_COMP
#  define zlib_cleanup()
# else
#  define zlib_cleanup() COMP_zlib_cleanup()
# endif

#define apps_startup() \
                        do { do_pipe_sig(); CRYPTO_malloc_init(); \
                        ERR_load_crypto_strings(); OpenSSL_add_all_algorithms(); \
                         setup_ui_method();} while(0)
#define apps_shutdown() \
                        do {CONF_modules_unload(1); destroy_ui_method();\
                        OBJ_cleanup(); EVP_cleanup(); \
                        CRYPTO_cleanup_all_ex_data(); ERR_remove_thread_state(NULL); \
                        RAND_cleanup(); \
                        ERR_free_strings();zlib_cleanup();} while(0)


X509 *load_cert(const char *file);

EVP_PKEY *load_key(const char *file, const char *pass);

int setup_ui_method(void);

void destroy_ui_method(void);

int dump_cert_text(BIO *out, X509 *x);

X509 *load_cert_by_string(const char *pubkey);

X509_STORE *setup_verify(char *CAfile, char *CApath);

void policies_print(BIO *out, X509_STORE_CTX *ctx);

#endif //DECRYPTMAIL_OPENSSLUTIL_H
