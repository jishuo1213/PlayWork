//
// Created by fan on 16-7-12.
//

#ifndef DECRYPTMAIL_SMIME_H
#define DECRYPTMAIL_SMIME_H

int decrypt_smime(const char *recipfile, const char *pass, const char *infile, const char *outfile);

int sign(const char *recipfile, const char *pass, const char *infile, const char *outfile);

int encrypt_mime(const char *recipfile, const char *infile, const char *outfile);

int verify_mime(const char *infile, char *outfile, const char *CAfile);

#endif //DECRYPTMAIL_SMIME_H
