//
// Created by fan on 16-7-12.
//
#include "opensslutil.h"
#include "strutil.h"
#include <openssl/pem.h>
#include <openssl/x509v3.h>

static UI_METHOD *ui_method = NULL;

/*
int password_callback(char *buf, int bufsiz, int verify, PW_CB_DATA *cb_tmp) {
    UI *ui = NULL;
    int res = 0;
    const char *prompt_info = NULL;
    const char *password = NULL;
    PW_CB_DATA *cb_data = cb_tmp;

    if (cb_data) {
        if (cb_data->password) {
            password = (char *) cb_data->password;
        }
        if (cb_data->prompt_info)
            prompt_info = cb_data->prompt_info;
    }

    if (password) {
        res = (int) strlen(password);
        if (res > bufsiz)
            res = bufsiz;
        memcpy(buf, password, (size_t) res);
        return res;
    }

    ui = UI_new_method(ui_method);
    if (ui) {
        int ok = 0;
        char *buff = NULL;
        int ui_flags = 0;
        char *prompt = NULL;

        prompt = UI_construct_prompt(ui, "pass phrase", prompt_info);
        if (!prompt) {
            UI_free(ui);
            return 0;
        }

        ui_flags |= UI_INPUT_FLAG_DEFAULT_PWD;
        UI_ctrl(ui, UI_CTRL_PRINT_ERRORS, 1, 0, 0);

        if (ok >= 0)
            ok = UI_add_input_string(ui, prompt, ui_flags, buf,
                                     PW_MIN_LENGTH, bufsiz - 1);
        if (ok >= 0 && verify) {
            buff = (char *) OPENSSL_malloc(bufsiz);
            if (!buff) {
                UI_free(ui);
                OPENSSL_free(prompt);
                return 0;
            }
            ok = UI_add_verify_string(ui, prompt, ui_flags, buff,
                                      PW_MIN_LENGTH, bufsiz - 1, buf);
        }
        if (ok >= 0)
            do {
                ok = UI_process(ui);
            }
            while (ok < 0 && UI_ctrl(ui, UI_CTRL_IS_REDOABLE, 0, 0, 0));

        if (buff) {
            OPENSSL_cleanse(buff, (unsigned int) bufsiz);
            OPENSSL_free(buff);
        }

        if (ok >= 0)
            res = (int) strlen(buf);
        if (ok == -1) {
            OPENSSL_cleanse(buf, (unsigned int) bufsiz);
            res = 0;
        }
        if (ok == -2) {
            OPENSSL_cleanse(buf, (unsigned int) bufsiz);
            res = 0;
        }
        UI_free(ui);
        OPENSSL_free(prompt);
    }
    return res;
}*/


X509 *load_cert(const char *file) {
    X509 *x = NULL;
    BIO *cert;

    if ((cert = BIO_new(BIO_s_file())) == NULL) {
        return NULL;
    }

    if (BIO_read_filename(cert, file) <= 0) {
        return NULL;
    }

//    x = PEM_read_bio_X509_AUX(cert, NULL, (pem_password_cb *) password_callback, NULL);
    x = PEM_read_bio_X509_AUX(cert, NULL, NULL, NULL);

    printf("%d \n", (x == NULL));
    if (cert != NULL)
        BIO_free(cert);
    return (x);
}

X509 *load_cert_by_string(const char *pubkey) {
    int len = strlen(pubkey);
    BIO *bufio = BIO_new_mem_buf(pubkey, len);
    if (bufio == NULL)
        LOGI("%s", "bufio == NULL");
    X509 *x = NULL;
    x = PEM_read_bio_X509(bufio, NULL, 0, NULL);

    if (bufio != NULL)
        BIO_free(bufio);
    return x;
}

EVP_PKEY *load_key(const char *file, const char *pass) {
    BIO *key = NULL;
    EVP_PKEY *pkey = NULL;
    PW_CB_DATA cb_data;

    cb_data.password = pass;
    cb_data.prompt_info = file;

    printf("%s \n%s \n", file, pass);
    key = BIO_new(BIO_s_file());
    if (key == NULL) {
        return NULL;
    }
    if (BIO_read_filename(key, file) <= 0) {
        BIO_free(key);
        return NULL;
    }
//    pkey = PEM_read_bio_PrivateKey(key, NULL, (pem_password_cb *) password_callback, &cb_data);
    pkey = PEM_read_bio_PrivateKey(key, NULL, NULL, &cb_data);

    BIO_free(key);
    return (pkey);
}

int dump_cert_text(BIO *out, X509 *x) {
    char *p;

    p = X509_NAME_oneline(X509_get_subject_name(x), NULL, 0);
    BIO_puts(out, "subject=");
    BIO_puts(out, p);
    OPENSSL_free(p);

    p = X509_NAME_oneline(X509_get_issuer_name(x), NULL, 0);
    BIO_puts(out, "\nissuer=");
    BIO_puts(out, p);
    BIO_puts(out, "\n");
    OPENSSL_free(p);

    return 0;
}

/*
static char *app_get_pass(BIO *err, char *arg, int keepbio);

int app_passwd(BIO *err, char *arg1, char *arg2, char **pass1, char **pass2) {
    int same;
    if (!arg2 || !arg1 || strcmp(arg1, arg2))
        same = 0;
    else
        same = 1;
    if (arg1) {
        *pass1 = app_get_pass(err, arg1, same);
        if (!*pass1)
            return 0;
    } else if (pass1)
        *pass1 = NULL;
    if (arg2) {
        *pass2 = app_get_pass(err, arg2, same ? 2 : 0);
        if (!*pass2)
            return 0;
    } else if (pass2)
        *pass2 = NULL;
    return 1;
}

static char *app_get_pass(BIO *err, char *arg, int keepbio) {
    char *tmp, tpass[1024];
    static BIO *pwdbio = NULL;
    int i;
    if (!strncmp(arg, "pass:", 5))
        return BUF_strdup(arg + 5);
    if (!strncmp(arg, "env:", 4)) {
        tmp = getenv(arg + 4);
        if (!tmp) {
            BIO_printf(err, "Can't read environment variable %s\n", arg + 4);
            return NULL;
        }
        return BUF_strdup(tmp);
    }
    if (!keepbio || !pwdbio) {
        if (!strncmp(arg, "file:", 5)) {
            pwdbio = BIO_new_file(arg + 5, "r");
            if (!pwdbio) {
                BIO_printf(err, "Can't open file %s\n", arg + 5);
                return NULL;
            }
#if !defined(_WIN32)
            *//*
             * Under _WIN32, which covers even Win64 and CE, file
             * descriptors referenced by BIO_s_fd are not inherited
             * by child process and therefore below is not an option.
             * It could have been an option if bss_fd.c was operating
             * on real Windows descriptors, such as those obtained
             * with CreateFile.
             *//*
        } else if (!strncmp(arg, "fd:", 3)) {
            BIO *btmp;
            i = atoi(arg + 3);
            if (i >= 0)
                pwdbio = BIO_new_fd(i, BIO_NOCLOSE);
            if ((i < 0) || !pwdbio) {
                BIO_printf(err, "Can't access file descriptor %s\n", arg + 3);
                return NULL;
            }
            *//*
             * Can't do BIO_gets on an fd BIO so add a buffering BIO
             *//*
            btmp = BIO_new(BIO_f_buffer());
            pwdbio = BIO_push(btmp, pwdbio);
#endif
        } else if (!strcmp(arg, "stdin")) {
            pwdbio = BIO_new_fp(stdin, BIO_NOCLOSE);
            if (!pwdbio) {
                BIO_printf(err, "Can't open BIO for stdin\n");
                return NULL;
            }
        } else {
            BIO_printf(err, "Invalid password argument \"%s\"\n", arg);
            return NULL;
        }
    }
    i = BIO_gets(pwdbio, tpass, 1024);
    if (keepbio != 1) {
        BIO_free_all(pwdbio);
        pwdbio = NULL;
    }
    if (i <= 0) {
        BIO_printf(err, "Error reading password from BIO\n");
        return NULL;
    }
    tmp = strchr(tpass, '\n');
    if (tmp)
        *tmp = 0;
    return BUF_strdup(tpass);
}
*/
static int ui_open(UI *ui) {
    return UI_method_get_opener(UI_OpenSSL())(ui);
}

static int ui_read(UI *ui, UI_STRING *uis) {
    if (UI_get_input_flags(uis) & UI_INPUT_FLAG_DEFAULT_PWD
        && UI_get0_user_data(ui)) {
        switch (UI_get_string_type(uis)) {
            case UIT_PROMPT:
            case UIT_VERIFY: {
                const char *password =
                        (char *) ((PW_CB_DATA *) UI_get0_user_data(ui))->password;
                if (password && password[0] != '\0') {
                    UI_set_result(ui, uis, password);
                    return 1;
                }
            }
            default:
                break;
        }
    }
    return UI_method_get_reader(UI_OpenSSL())(ui, uis);
}

static int ui_write(UI *ui, UI_STRING *uis) {
    if (UI_get_input_flags(uis) & UI_INPUT_FLAG_DEFAULT_PWD
        && UI_get0_user_data(ui)) {
        switch (UI_get_string_type(uis)) {
            case UIT_PROMPT:
            case UIT_VERIFY: {
                const char *password =
                        (char *) ((PW_CB_DATA *) UI_get0_user_data(ui))->password;
                if (password && password[0] != '\0')
                    return 1;
            }
            default:
                break;
        }
    }
    return UI_method_get_writer(UI_OpenSSL())(ui, uis);
}

X509_STORE *setup_verify(char *CAfile, char *CApath) {
    X509_STORE *store;
    X509_LOOKUP *lookup;
    if (!(store = X509_STORE_new()))
        goto end;
    lookup = X509_STORE_add_lookup(store, X509_LOOKUP_file());
    if (lookup == NULL)
        goto end;
    if (CAfile) {
        if (!X509_LOOKUP_load_file(lookup, CAfile, X509_FILETYPE_PEM)) {
            goto end;
        }
    } else
        X509_LOOKUP_load_file(lookup, NULL, X509_FILETYPE_DEFAULT);

    lookup = X509_STORE_add_lookup(store, X509_LOOKUP_hash_dir());
    if (lookup == NULL)
        goto end;
    if (CApath) {
        if (!X509_LOOKUP_add_dir(lookup, CApath, X509_FILETYPE_PEM)) {
            goto end;
        }
    } else
        X509_LOOKUP_add_dir(lookup, NULL, X509_FILETYPE_DEFAULT);

    ERR_clear_error();
    return store;
    end:
    X509_STORE_free(store);
    return NULL;
}

static void nodes_print(BIO *out, const char *name,
                        STACK_OF(X509_POLICY_NODE) *nodes)
{
    X509_POLICY_NODE *node;
    int i;
    BIO_printf(out, "%s Policies:", name);
    if (nodes) {
        BIO_puts(out, "\n");
        for (i = 0; i < sk_X509_POLICY_NODE_num(nodes); i++) {
            node = sk_X509_POLICY_NODE_value(nodes, i);
            X509_POLICY_NODE_print(out, node, 2);
        }
    } else
        BIO_puts(out, " <empty>\n");
}

void policies_print(BIO *out, X509_STORE_CTX *ctx)
{
    X509_POLICY_TREE *tree;
    int explicit_policy;
    int free_out = 0;
    if (out == NULL) {
        out = BIO_new_fp(stderr, BIO_NOCLOSE);
        free_out = 1;
    }
    tree = X509_STORE_CTX_get0_policy_tree(ctx);
    explicit_policy = X509_STORE_CTX_get_explicit_policy(ctx);

    BIO_printf(out, "Require explicit Policy: %s\n",
               explicit_policy ? "True" : "False");

    nodes_print(out, "Authority", X509_policy_tree_get0_policies(tree));
    nodes_print(out, "User", X509_policy_tree_get0_user_policies(tree));
    if (free_out)
        BIO_free(out);
}

static int ui_close(UI *ui) {
    return UI_method_get_closer(UI_OpenSSL())(ui);
}

int setup_ui_method(void) {
    ui_method = UI_create_method("OpenSSL application user interface");
    UI_method_set_opener(ui_method, ui_open);
    UI_method_set_reader(ui_method, ui_read);
    UI_method_set_writer(ui_method, ui_write);
    UI_method_set_closer(ui_method, ui_close);
    return 0;
}

void destroy_ui_method(void) {
    if (ui_method) {
        UI_destroy_method(ui_method);
        ui_method = NULL;
    }
}

