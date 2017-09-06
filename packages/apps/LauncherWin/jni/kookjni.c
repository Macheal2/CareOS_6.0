#include <string.h>
#include <jni.h>
#include <com_cappu_launcherwin_nativejni_KookNativeChannel.h>

jstring Java_com_cappu_launcherwin_nativejni_KookNativeChannel_CappuChannelNet(JNIEnv* env, jobject thiz) {
    return (*env)->NewStringUTF(env, "7871bcc507ae2ce314f242aa45ffa931");
}

jstring Java_com_cappu_launcherwin_nativejni_KookNativeChannel_CappuChannel(JNIEnv* env, jobject thiz) {
    return (*env)->NewStringUTF(env, "de9c9e0d0a3c4722b50543894bd19b26");
}

jstring Java_com_cappu_launcherwin_nativejni_KookNativeChannel_KLTChannel(JNIEnv* env, jobject thiz) {
    return (*env)->NewStringUTF(env, "2f07ba4f1c5e362f702b2dc53afae167");
}

