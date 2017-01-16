# Sets the minimum version of CMake required to build your native library.
# This ensures that a certain set of CMake features is available to
# your build.

cmake_minimum_required(VERSION 3.4.1)

set(lib_openssl_dir ${CMAKE_SOURCE_DIR}/../distribution/openssl)

# Specifies a library name, specifies whether the library is STATIC or
# SHARED, and provides relative paths to the source code. You can
# define multiple libraries by adding multiple add.library() commands,
# and CMake builds them for you. When you build your app, Gradle
# automatically packages shared libraries with your APK.

add_library(DecryptMail SHARED src/main/jni/DecryptMail.c
                               src/main/jni/opensslutil.c
                               src/main/jni/smime.c
                               src/main/jni/strutil.c
)



# Specifies a path to native header files.
include_directories(${lib_openssl_dir}/include/)

find_library(log-lib log)

add_library(crypto STATIC IMPORTED)
add_library(ssl STATIC IMPORTED)

set_target_properties(crypto PROPERTIES IMPORTED_LOCATION ${lib_openssl_dir}/lib/${ANDROID_ABI}/libcrypto.a)
set_target_properties(ssl PROPERTIES IMPORTED_LOCATION ${lib_openssl_dir}/lib/${ANDROID_ABI}/libssl.a)

target_link_libraries(DecryptMail ${log-lib} crypto ssl)


