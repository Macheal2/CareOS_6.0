#include <string.h>
#include <jni.h>
#include <com_cappu_launcherwin_nativejni_KookNativeCalendar.h>
#include <android/log.h>


#include <locale.h>
#include <stdlib.h>
#include <stdio.h>
#include <errno.h>

#define TAG "HHJ" // 这个是自定义的LOG的标识
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,TAG ,__VA_ARGS__) // 定义LOGI类型

jintArray Java_com_cappu_launcherwin_nativejni_KookNativeCalendar_getJiYear(JNIEnv* env, jobject thiz, int year) {


}

jintArray Java_com_cappu_launcherwin_nativejni_KookNativeCalendar_getJiMonth(JNIEnv* env, jobject thiz, int year, int mounth, int day) {

}

jintArray Java_com_cappu_launcherwin_nativejni_KookNativeCalendar_getJiDay(JNIEnv* env, jobject thiz, int year, int mounth, int day) {

}
